package com.plexobject.rbac.service;

import javax.ws.rs.core.Response;

public interface RolePermissionsService {
    /**
     * Adds permissions to given role
     * 
     * @param domain
     * @param role
     * @param permissionss
     */
    Response addPermissionsToRole(String domain, String role,
            String permissionIdsJSON);

    /**
     * Removes permissions from given role
     * 
     * @param domain
     * @param role
     * @param permissionss
     */
    Response removePermissionsToRole(String domain, String role,
            String permissionIdsJSON);

}
