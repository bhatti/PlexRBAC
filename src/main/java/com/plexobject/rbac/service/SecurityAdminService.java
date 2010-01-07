package com.plexobject.rbac.service;

import javax.ws.rs.core.Response;

public interface SecurityAdminService {

    /**
     * Add given roles to the user and save them
     * 
     * @param domain
     * @param user
     * @param roles
     */
    Response addRolesToUser(String domain, String user, String rolenamesJSON);

    /**
     * Remove given roles to the user and save them
     * 
     * @param domain
     * @param user
     * @param roles
     */
    Response removeRolesToUser(String domain, String user, String rolenamesJSON);

    /**
     * Adds permissions to given role
     * 
     * @param domain
     * @param role
     * @param permissionss
     */
    Response addPermissionsToRole(String domain, String role,
            String permissionIDsJSON);

    /**
     * Removes permissions from given role
     * 
     * @param domain
     * @param role
     * @param permissionss
     */
    Response removePermissionsToRole(String domain, String role,
            String permissionIDsJSON);

}
