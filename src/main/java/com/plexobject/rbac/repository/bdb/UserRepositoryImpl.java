package com.plexobject.rbac.repository.bdb;

import com.plexobject.rbac.repository.UserRepository;
import com.plexobject.rbac.domain.User;
import com.sleepycat.persist.EntityStore;

public class UserRepositoryImpl extends BaseRepositoryImpl<User, String> implements UserRepository {
    public UserRepositoryImpl(final EntityStore store) {
        super(store);
    }
}
