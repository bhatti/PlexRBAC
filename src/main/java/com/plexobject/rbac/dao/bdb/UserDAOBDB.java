package com.plexobject.rbac.dao.bdb;

import com.plexobject.rbac.dao.UserDAO;
import com.plexobject.rbac.domain.User;
import com.sleepycat.persist.EntityStore;

public class UserDAOBDB extends BaseDAOBDB<User, String> implements UserDAO {
    public UserDAOBDB(final EntityStore store) {
        super(store);
    }
}
