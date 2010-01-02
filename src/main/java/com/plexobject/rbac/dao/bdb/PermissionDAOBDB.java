package com.plexobject.rbac.dao.bdb;

import java.util.Iterator;

import com.plexobject.rbac.dao.PermissionDAO;
import com.plexobject.rbac.dao.PersistenceException;
import com.plexobject.rbac.domain.Permission;
import com.plexobject.rbac.metric.Metric;
import com.plexobject.rbac.metric.Timing;
import com.sleepycat.je.DatabaseException;
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
    public Iterator<Permission> getPermissionsForApplication(String appName) {
        final Timing timer = Metric.newTiming(getClass().getName()
                + ".getPermissionsForApplication");
        try {
            return new CursorIterator<Permission>(permissionIndex.entities(
                    appName, true, appName, true));
        } catch (DatabaseException e) {
            throw new PersistenceException(e);
        } finally {
            timer.stop();
        }
    }
}
