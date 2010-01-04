package com.plexobject.rbac.dao.bdb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.plexobject.rbac.dao.PersistenceException;
import com.plexobject.rbac.dao.RoleDAO;
import com.plexobject.rbac.domain.Role;
import com.plexobject.rbac.metric.Metric;
import com.plexobject.rbac.metric.Timing;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.SecondaryIndex;

public class RoleDAOBDB extends BaseDAOBDB<Role, String> implements RoleDAO {
    private SecondaryIndex<String, String, Role> usernameIndex;

    public RoleDAOBDB(final EntityStore store) {
        super(store);
        try {
            usernameIndex = store.getSecondaryIndex(primaryIndex, String.class,
                    "userIDs");
        } catch (DatabaseException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public Collection<Role> getRolesForUser(String username) {
        final Timing timer = Metric.newTiming(getClass().getName()
                + ".getRolesForUser");
        List<Role> roles = new ArrayList<Role>();
        EntityCursor<Role> cursor = null;
        try {
            cursor = usernameIndex.subIndex(username).entities();
            Iterator<Role> it = cursor.iterator();
            while (it.hasNext()) {
                Role next = it.next();
                roles.add(next);
            }
            return roles;
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
