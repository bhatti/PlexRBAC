package com.plexobject.rbac.dao.bdb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.plexobject.rbac.dao.ApplicationDAO;
import com.plexobject.rbac.dao.PersistenceException;
import com.plexobject.rbac.domain.Application;
import com.plexobject.rbac.metric.Metric;
import com.plexobject.rbac.metric.Timing;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.evolve.IncompatibleClassException;

public class ApplicationDAOBDB extends BaseDAOBDB<Application, String>
        implements ApplicationDAO {
    private SecondaryIndex<String, String, Application> ownerIndex;

    public ApplicationDAOBDB(String databaseDir, String storeName)
            throws IncompatibleClassException, DatabaseException {
        super(databaseDir, storeName);
        ownerIndex = store.getSecondaryIndex(primaryIndex, String.class,
                "ownerUsername");
    }

    @Override
    public Collection<Application> findByUser(String username) {
        final Timing timer = Metric.newTiming(getClass().getName()
                + ".findByUser");
        EntityCursor<Application> cursor = null;
        List<Application> all = new ArrayList<Application>();
        try {
            cursor = ownerIndex.entities(username, true, username, true);
            Iterator<Application> it = cursor.iterator();
            while (it.hasNext()) {
                Application next = it.next();
                all.add(next);
            }
            return all;
        } catch (DatabaseException e) {
            throw new PersistenceException(e);
        } finally {
            timer.stop();
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (DatabaseException e) {
                    LOGGER.error("failed to close cursor", e);
                }
            }
        }
    }
}
