package com.plexobject.rbac.repository;

import java.util.Collection;

import com.plexobject.rbac.domain.Permission;
import com.plexobject.rbac.domain.Role;
import com.plexobject.rbac.domain.Subject;

/**
 * This class allows management of domains and mapping of subjects, roles and
 * permissions.
 * 
 * 
 */
public interface SecurityMappingRepository {
    /**
     * Add given roles to the subject and save them
     * 
     * @param subject
     * @param roles
     * @return role-names for given subject
     */
    Collection<Role> addRolesToSubject(String subject, Collection<String> roles);

    /**
     * Add given roles to the subject and save them
     * 
     * @param subject
     * @param roles
     * @return role-names for given subject
     */
    Collection<Role> addRolesToSubject(Subject subject, Collection<Role> roles);

    /**
     * Add given roles to the subject and save them
     * 
     * @param subject
     * @param roles
     * @return role-names for given subject
     */
    Collection<Role> addRolesToSubject(Subject subject, Role... roles);

    /**
     * Remove given roles to the subject and save them
     * 
     * @param subject
     * @param roles
     * @return role-names for given subject
     */
    Collection<Role> removeRolesToSubject(String subject,
            Collection<String> roles);

    /**
     * Remove given roles to the subject and save them
     * 
     * @param subject
     * @param roles
     * @return role-names for given subject
     */
    Collection<Role> removeRolesToSubject(Subject subject,
            Collection<Role> roles);

    /**
     * Remove given roles to the subject and save them
     * 
     * @param subject
     * @param roles
     * @return role-names for given subject
     */
    Collection<Role> removeRolesToSubject(Subject subject, Role... roles);

    /**
     * Adds permissions to given role
     * 
     * @param role
     * @param permissions
     * @return permission-ids for given role
     */
    Collection<Permission> addPermissionsToRole(String role,
            Collection<Integer> permissionsIds);

    /**
     * Adds permissions to given role
     * 
     * @param role
     * @param permissions
     * @return permission-ids for given role
     */
    Collection<Permission> addPermissionsToRole(Role role,
            Collection<Permission> permissions);

    /**
     * Adds permissions to given role
     * 
     * @param role
     * @param permissions
     * @return permission-ids for given role
     */
    Collection<Permission> addPermissionsToRole(Role role,
            Permission... permissions);

    /**
     * Removes permissions from given role
     * 
     * @param role
     * @param permissions
     * @return permission-ids for given role
     */
    Collection<Permission> removePermissionsToRole(String role,
            Collection<Integer> permissionsIds);

    /**
     * Removes permissions from given role
     * 
     * @param role
     * @param permissions
     * @return permission-ids for given role
     */
    Collection<Permission> removePermissionsToRole(Role role,
            Collection<Permission> permissions);

    /**
     * Removes permissions from given role
     * 
     * @param role
     * @param permissions
     * @return permission-ids for given role
     */
    Collection<Permission> removePermissionsToRole(Role role,
            Permission... permissions);

    /**
     * Checks if subject has given role
     * 
     * @param subjectName
     * @param rolename
     * @return
     */
    boolean isSubjectInRole(String subjectName, String rolename);
}
