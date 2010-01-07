package com.plexobject.rbac.repository;

import java.util.Collection;

/**
 * This class allows management of domains and mapping of users, roles and
 * permissions.
 * 
 * 
 */
public interface SecurityRepository {

    /**
     * Add given roles to the user and save them
     * 
     * @param domain
     * @param user
     * @param roles
     */
    void addRolesToUser(String domain, String user, Collection<String> roles);

    /**
     * Remove given roles to the user and save them
     * 
     * @param domain
     * @param user
     * @param roles
     */
    void removeRolesToUser(String domain, String user, Collection<String> roles);

    /**
     * Adds permissions to given role
     * 
     * @param domain
     * @param role
     * @param permissionss
     */
    void addPermissionsToRole(String domain, String role,
            Collection<Integer> permissionsIDs);

    /**
     * Removes permissions from given role
     * 
     * @param domain
     * @param role
     * @param permissionss
     */
    void removePermissionsToRole(String domain, String role,
            Collection<Integer> permissionsIDs);
}
