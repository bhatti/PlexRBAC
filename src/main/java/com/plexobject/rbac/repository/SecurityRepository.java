package com.plexobject.rbac.repository;

import java.util.Collection;

import com.plexobject.rbac.domain.Permission;
import com.plexobject.rbac.domain.Role;


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
     * @return role-names for given subject
     */
    Collection<Role> addRolesToSubject(String domain, String subject, Collection<String> roles);

    /**
     * Remove given roles to the subject and save them
     * 
     * @param domain
     * @param subject
     * @param roles
     * @return role-names for given subject
     */
    Collection<Role>  removeRolesToSubject(String domain, String subject, Collection<String> roles);

    /**
     * Adds permissions to given role
     * 
     * @param domain
     * @param role
     * @param permissions
     * @return permission-ids for given role
     */
    Collection<Permission> addPermissionsToRole(String domain, String role,
            Collection<Integer> permissionsIds);

    /**
     * Removes permissions from given role
     * 
     * @param domain
     * @param role
     * @param permissions
     * @return permission-ids for given role
     */
    Collection<Permission> removePermissionsToRole(String domain, String role,
            Collection<Integer> permissionsIds);

    /**
     * Checks if subject has given role
     * 
     * @param subjectName
     * @param rolename
     * @return
     */
    boolean isSubjectInRole(String domain, String subjectName, String rolename);
}
