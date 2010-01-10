package com.plexobject.rbac.repository;

import java.util.Collection;


/**
 * This class allows management of domains and mapping of subjects, roles and
 * permissions.
 * 
 * 
 */
public interface SecurityRepository {

    /**
     * Add given roles to the subject and save them
     * 
     * @param domain
     * @param subject
     * @param roles
     */
    void addRolesToSubject(String domain, String subject, Collection<String> roles);

    /**
     * Remove given roles to the subject and save them
     * 
     * @param domain
     * @param subject
     * @param roles
     */
    void removeRolesToSubject(String domain, String subject, Collection<String> roles);

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

    /**
     * Checks if subject has given role
     * 
     * @param subjectname
     * @param rolename
     * @return
     */
    boolean isSubjectInRole(String domain, String subjectname, String rolename);
}
