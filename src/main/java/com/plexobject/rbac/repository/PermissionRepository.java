package com.plexobject.rbac.repository;

import java.util.Collection;
import java.util.Set;

import com.plexobject.rbac.domain.Permission;
import com.plexobject.rbac.domain.Role;

public interface PermissionRepository extends BaseRepository<Permission, Integer> {
    Set<Permission> getPermissionsForRoles(Collection<Role> roles);

}
