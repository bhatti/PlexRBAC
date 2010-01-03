package com.plexobject.rbac.dao.bdb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.plexobject.rbac.dao.PagedList;
import com.plexobject.rbac.dao.PermissionDAO;
import com.plexobject.rbac.dao.PersistenceException;
import com.plexobject.rbac.domain.Permission;
import com.plexobject.rbac.metric.Metric;
import com.plexobject.rbac.metric.Timing;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.evolve.IncompatibleClassException;

public class PermissionDAOBDB extends BaseDAOBDB<Permission, Integer> implements
        PermissionDAO {
    private SecondaryIndex<String, Integer, Permission> permissionIndex;

    public PermissionDAOBDB(String databaseDir, String storeName)
            throws IncompatibleClassException, DatabaseException {
        super(databaseDir, storeName);
        permissionIndex = store.getSecondaryIndex(primaryIndex, String.class,
                "applicationName");
    }

    @Override
    public PagedList<Permission, Integer> getPermissionsForApplication(
            String appName, Integer firstKey, int limit) {
        if (appName == null) {
            throw new IllegalArgumentException("appName not specified");
        }
        final Timing timer = Metric.newTiming(getClass().getName()
                + ".getPermissionsForApplication");
        if (limit <= 0) {
            limit = DEFAULT_LIMIT;
        }
        limit = Math.max(limit, MAX_LIMIT);
        EntityCursor<Permission> cursor = null;
        List<Permission> all = new ArrayList<Permission>();
        try {
            cursor = permissionIndex.entities(appName, true, appName, true);
            Integer lastKey = null;
            Iterator<Permission> it = cursor.iterator();
            for (int i = 0; it.hasNext() && i < limit;) {
                Permission next = it.next();
                if (firstKey != null
                        && firstKey.intValue() < next.getID().intValue()) {
                    continue;
                }
                all.add(next);
                lastKey = next.getID();
                i++;
            }
            return new PagedList<Permission, Integer>(all, firstKey, lastKey,
                    limit, all.size() == limit);
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
