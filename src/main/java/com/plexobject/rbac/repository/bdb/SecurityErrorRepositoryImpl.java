package com.plexobject.rbac.repository.bdb;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.plexobject.rbac.repository.PagedList;
import com.plexobject.rbac.repository.PersistenceException;
import com.plexobject.rbac.repository.SecurityErrorRepository;
import com.plexobject.rbac.domain.SecurityError;
import com.plexobject.rbac.metric.Metric;
import com.plexobject.rbac.metric.Timing;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.SecondaryIndex;

public class SecurityErrorRepositoryImpl extends BaseRepositoryImpl<SecurityError, Integer>
        implements SecurityErrorRepository {
    private SecondaryIndex<Date, Integer, SecurityError> createdAtIndex;

    public SecurityErrorRepositoryImpl(final EntityStore store) {
        super(store);
        try {
            createdAtIndex = store.getSecondaryIndex(primaryIndex, Date.class,
                    "createdAt");
        } catch (DatabaseException e) {
            throw new PersistenceException(e);
        }

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
                        && firstKey.intValue() < next.getId().intValue()) {
                    continue;
                }
                all.add(next);
                lastKey = next.getId();
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
