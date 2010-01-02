package com.plexobject.rbac.dao;

import java.util.Iterator;

import com.plexobject.rbac.domain.Permission;

public interface PermissionDAO extends BaseDAO<Permission, Integer> {
    Iterator<Permission> getPermissionsForApplication(String appName);
}
