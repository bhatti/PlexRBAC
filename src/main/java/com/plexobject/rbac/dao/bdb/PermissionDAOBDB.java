package com.plexobject.rbac.dao.bdb;

import com.plexobject.rbac.dao.PermissionDAO;
import com.plexobject.rbac.domain.Permission;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.evolve.IncompatibleClassException;

public class PermissionDAOBDB extends BaseDAOBDB<Permission, Integer> implements
        PermissionDAO {

    public PermissionDAOBDB(String databaseDir, String storeName)
            throws IncompatibleClassException, DatabaseException {
        super(databaseDir, storeName);
    }
}
