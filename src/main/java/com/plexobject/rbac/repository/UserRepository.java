package com.plexobject.rbac.repository;

import com.plexobject.rbac.domain.User;

public interface UserRepository extends BaseRepository<User, String> {
    User getOrCreateUser(String user);

    User authenticate(String username, String password)
            throws SecurityException;

}
