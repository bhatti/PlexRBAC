package com.plexobject.rbac.repository;

import java.util.Collection;

import com.plexobject.rbac.domain.Role;

public interface RoleRepository extends BaseRepository<Role, String> {
    Collection<Role> getRolesForUser(String username);
}
