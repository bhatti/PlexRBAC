package com.plexobject.rbac.dao;

import com.plexobject.rbac.domain.User;

public interface UserDAO extends BaseDAO<User, Integer> {
    User findByName(String username);
}
