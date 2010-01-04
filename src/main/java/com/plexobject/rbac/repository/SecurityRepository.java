package com.plexobject.rbac.repository;

import java.util.Set;

import com.plexobject.rbac.domain.Domain;
import com.plexobject.rbac.domain.Permission;
import com.plexobject.rbac.domain.Role;
import com.plexobject.rbac.domain.User;

/**
 * This class serves dual purpose, first it serves as a factory for repositories
 * to add users, roles, permissions and domains. Second, it allows management of
 * domains and mapping of users, roles and permissions.
 * 
 * 
 */
public interface SecurityRepository {
    /**
     * 
     * @return instance of domain repository to manage domains
     */
    DomainRepository getDomainRepository();

    /**
     * 
     * @return high level domain for this application
     */
    Domain getDefaultDomain();

    /**
     * 
     * @param domain
     * @return repository of roles for specific domain
     */
    RoleRepository getRoleRepository(String domain);

    /**
     * 
     * @param domain
     * @return repository of permissions for given domain
     */
    PermissionRepository getPermissionRepository(String domain);

    /**
     * 
     * @param domain
     * @return repository of security errors for given domain
     */
    SecurityErrorRepository getSecurityErrorRepository(String domain);

    /**
     * 
     * @param domain
     * @return repository of users for given domain
     */
    UserRepository getUserRepository(String domain);

    /**
     * Add given roles to the user and save them
     * 
     * @param domain
     * @param user
     * @param roles
     */
    void addRolesToUser(String domain, User user, Set<Role> roles);

    /**
     * Remove given roles to the user and save them
     * 
     * @param domain
     * @param user
     * @param roles
     */
    void removeRolesToUser(String domain, User user, Set<Role> roles);

    /**
     * Adds permissions to given role
     * 
     * @param domain
     * @param role
     * @param permissionss
     */
    void addPermissionsToRole(String domain, Role role,
            Set<Permission> permissionss);

    /**
     * Removes permissions from given role
     * 
     * @param domain
     * @param role
     * @param permissionss
     */
    void removePermissionsToRole(String domain, Role role,
            Set<Permission> permissionss);
}
