package com.plexobject.rbac.dao.bdb;

import java.util.Iterator;

import com.plexobject.rbac.dao.ApplicationDAO;
import com.plexobject.rbac.dao.PersistenceException;
import com.plexobject.rbac.domain.Application;
import com.plexobject.rbac.metric.Metric;
import com.plexobject.rbac.metric.Timing;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.evolve.IncompatibleClassException;

public class ApplicationDAOBDB extends BaseDAOBDB<Application, String>
        implements ApplicationDAO {
    private SecondaryIndex<String, String, Application> ownerIndex;

    public ApplicationDAOBDB(String databaseDir, String tableName)
            throws IncompatibleClassException, DatabaseException {
        super(databaseDir, tableName);
        ownerIndex = store.getSecondaryIndex(primaryIndex, String.class,
                "ownerUsername");
    }

    @Override
    public Iterator<Application> findByUser(final String username) {
        final Timing timer = Metric.newTiming(getClass().getName()
                + ".findByUser");
        try {
            return new CursorIterator<Application>(ownerIndex.entities(
                    username, true, username, true));
        } catch (DatabaseException e) {
            throw new PersistenceException(e);
        } finally {
            timer.stop();
        }
    }

    @Override
    public Application findByName(String name) {
        return super.findByID(name);
    }
}
