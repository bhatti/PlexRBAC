package com.plexobject.rbac.dao.bdb;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.plexobject.rbac.dao.PermissionDAO;
import com.plexobject.rbac.dao.PersistenceException;
import com.plexobject.rbac.dao.RoleDAO;
import com.plexobject.rbac.domain.Permission;
import com.plexobject.rbac.domain.Role;
import com.plexobject.rbac.metric.Metric;
import com.plexobject.rbac.metric.Timing;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.SecondaryIndex;

public class PermissionDAOBDB extends BaseDAOBDB<Permission, Integer> implements
        PermissionDAO {
    private RoleDAO roleDAO;
    private SecondaryIndex<String, Integer, Permission> rolenameIndex;

    public PermissionDAOBDB(final EntityStore store) {
        super(store);
        this.roleDAO = new RoleDAOBDB(store);
        try {
            rolenameIndex = store.getSecondaryIndex(primaryIndex, String.class,
                    "roleIDs");
        } catch (DatabaseException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public Set<Permission> getPermissionsForRoles(Collection<Role> roles) {
        final Timing timer = Metric.newTiming(getClass().getName()
                + ".getPermissionsForRole");
        Set<Permission> permissions = new HashSet<Permission>();
        try {
            for (Role role : roles) {
                loadPermissionsForRole(role, permissions);
            }
            return permissions;
        } catch (DatabaseException e) {
            throw new PersistenceException(e);
        } finally {
            timer.stop();
        }
    }

    private void loadPermissionsForRole(Role role,
            Collection<Permission> permissions) throws DatabaseException {
        EntityCursor<Permission> cursor = null;

        try {
            cursor = rolenameIndex.subIndex(role.getID()).entities();
            Iterator<Permission> it = cursor.iterator();
            while (it.hasNext()) {
                Permission next = it.next();
                permissions.add(next);
            }
            if (role.hasParentRoleID()) {
                Role parent = roleDAO.findByID(role.getParentRoleID());
                loadPermissionsForRole(parent, permissions);
            }
        } finally {
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
