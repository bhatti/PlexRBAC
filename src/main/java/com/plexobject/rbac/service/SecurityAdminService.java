package com.plexobject.rbac.service;

import javax.ws.rs.core.Response;

public interface SecurityAdminService {

    /**
     * Add given roles to the subject and save them
     * 
     * @param domain
     * @param subject
     * @param roles
     */
    Response addRolesToSubject(String domain, String subject, String rolenamesJSON);

    /**
     * Remove given roles to the subject and save them
     * 
     * @param domain
     * @param subject
     * @param roles
     */
    Response removeRolesToSubject(String domain, String subject, String rolenamesJSON);

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
