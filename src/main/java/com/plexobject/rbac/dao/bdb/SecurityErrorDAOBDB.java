package com.plexobject.rbac.dao.bdb;

import java.util.Date;
import java.util.Iterator;

import com.plexobject.rbac.dao.PersistenceException;
import com.plexobject.rbac.dao.SecurityErrorDAO;
import com.plexobject.rbac.domain.Application;
import com.plexobject.rbac.domain.SecurityError;
import com.plexobject.rbac.metric.Metric;
import com.plexobject.rbac.metric.Timing;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.evolve.IncompatibleClassException;

public class SecurityErrorDAOBDB extends BaseDAOBDB<SecurityError, Integer> implements
        SecurityErrorDAO {
    private SecondaryIndex<Date, Integer, SecurityError> createdAtIndex;

    public SecurityErrorDAOBDB(String databaseDir, String tableName)
            throws IncompatibleClassException, DatabaseException {
        super(databaseDir, tableName);
        createdAtIndex = store.getSecondaryIndex(primaryIndex, Date.class,
                "createdAt");

    }

    @Override
    public Iterator<SecurityError> findAll(Application app, Date since) {
        final Date now = new Date();
        final Timing timer = Metric.newTiming(getClass().getName()
                + ".findByID");
        try {
            return new CursorIterator<SecurityError>(createdAtIndex.entities(
                    since, true, now, true));
        } catch (DatabaseException e) {
            throw new PersistenceException(e);
        } finally {
            timer.stop();
        }
    }

}
