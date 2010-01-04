package com.plexobject.rbac.repository;

import com.plexobject.rbac.domain.Permission;
import com.plexobject.rbac.domain.Role;
import com.plexobject.rbac.domain.User;

public interface SecurityRepository {
    void addRolesToUser(User user, Role... roles);

    void removeRolesToUser(User user, Role... roles);

    void addPermissionsToRole(Role role, Permission... permissionss);

    void removePermissionsToRole(Role role, Permission... permissionss);
}
