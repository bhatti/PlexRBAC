package com.plexobject.rbac.dao.bdb;

import com.plexobject.rbac.dao.PermissionDAO;
import com.plexobject.rbac.domain.Permission;
import com.sleepycat.persist.EntityStore;

public class PermissionDAOBDB extends BaseDAOBDB<Permission, Integer> implements
        PermissionDAO {
    public PermissionDAOBDB(final EntityStore store) {
        super(store);
    }
}
