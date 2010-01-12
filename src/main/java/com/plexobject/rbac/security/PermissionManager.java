package com.plexobject.rbac.security;

public interface PermissionManager {

    void check(PermissionRequest request) throws SecurityException;

}