package com.plexobject.rbac.dao;

import java.util.Collection;

import com.plexobject.rbac.domain.Role;

public interface RoleDAO extends BaseDAO<Role, String> {
    Collection<Role> getRolesForUser(String username);
}
