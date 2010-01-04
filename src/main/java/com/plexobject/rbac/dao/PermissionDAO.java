package com.plexobject.rbac.dao;

import java.util.Collection;
import java.util.Set;

import com.plexobject.rbac.domain.Permission;
import com.plexobject.rbac.domain.Role;

public interface PermissionDAO extends BaseDAO<Permission, Integer> {
    Set<Permission> getPermissionsForRoles(Collection<Role> roles);

}
