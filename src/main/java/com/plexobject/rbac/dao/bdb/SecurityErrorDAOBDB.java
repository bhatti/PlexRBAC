package com.plexobject.rbac.dao.bdb;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.plexobject.rbac.dao.PagedList;
import com.plexobject.rbac.dao.PersistenceException;
import com.plexobject.rbac.dao.SecurityErrorDAO;
import com.plexobject.rbac.domain.SecurityError;
import com.plexobject.rbac.metric.Metric;
import com.plexobject.rbac.metric.Timing;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.evolve.IncompatibleClassException;

public class SecurityErrorDAOBDB extends BaseDAOBDB<SecurityError, Integer>
        implements SecurityErrorDAO {
    private SecondaryIndex<Date, Integer, SecurityError> createdAtIndex;

    public SecurityErrorDAOBDB(String databaseDir, String storeName)
            throws IncompatibleClassException, DatabaseException {
        super(databaseDir, storeName);
        createdAtIndex = store.getSecondaryIndex(primaryIndex, Date.class,
                "createdAt");

    }

    @Override
    public PagedList<SecurityError, Integer> findAll(String appName,
            Date since, Integer firstKey, int limit) {
        if (appName == null) {
            throw new IllegalArgumentException("appName not specified");
        }

        if (since == null) {
            throw new IllegalArgumentException("date not specified");
        }
        final Date now = new Date();
        if (limit <= 0) {
            limit = DEFAULT_LIMIT;
        }
        limit = Math.max(limit, MAX_LIMIT);
        final Timing timer = Metric
                .newTiming(getClass().getName() + ".findAll");
        EntityCursor<SecurityError> cursor = null;
        List<SecurityError> all = new ArrayList<SecurityError>();
        try {
            cursor = createdAtIndex.entities(since, true, now, true);
            Integer lastKey = null;
            Iterator<SecurityError> it = cursor.iterator();
            for (int i = 0; it.hasNext() && i < limit;) {
                SecurityError next = it.next();
                if (firstKey != null
                        && firstKey.intValue() < next.getID().intValue()) {
                    continue;
                }
                if (!next.getApplicationName().equals(appName)) {
                    continue;
                }
                all.add(next);
                lastKey = next.getID();
                i++;
            }
            return new PagedList<SecurityError, Integer>(all, firstKey,
                    lastKey, limit, all.size() == limit);
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
