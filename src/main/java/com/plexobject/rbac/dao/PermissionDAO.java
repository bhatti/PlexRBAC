package com.plexobject.rbac.dao;

import com.plexobject.rbac.domain.Permission;

public interface PermissionDAO extends BaseDAO<Permission, Integer> {
    PagedList<Permission, Integer> getPermissionsForApplication(String appName,
            Integer lastKey, int limit);
}
