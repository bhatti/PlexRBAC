package com.plexobject.rbac.repository.bdb;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;

import com.plexobject.rbac.domain.Domain;
import com.plexobject.rbac.domain.Permission;
import com.plexobject.rbac.domain.Role;
import com.plexobject.rbac.domain.Subject;
import com.plexobject.rbac.repository.PermissionRepository;
import com.plexobject.rbac.repository.RepositoryFactory;
import com.plexobject.rbac.repository.RoleRepository;
import com.plexobject.rbac.repository.SecurityMappingRepository;
import com.plexobject.rbac.utils.IDUtils;

public class SecurityMappingRepositoryImpl implements SecurityMappingRepository {
    private static final Logger LOGGER = Logger
            .getLogger(SecurityMappingRepositoryImpl.class);
    private final RepositoryFactory repositoryFactory;
    private final String domain;

    public SecurityMappingRepositoryImpl(final String domain,
            final RepositoryFactory repositoryFactory) {
        if (GenericValidator.isBlankOrNull(domain)) {
            throw new IllegalArgumentException("domain is not specified");
        }
        if (repositoryFactory == null) {
            throw new IllegalArgumentException(
                    "repositoryFactory is not specified");
        }
        this.domain = domain;
        this.repositoryFactory = repositoryFactory;
    }

    @Override
    public Collection<Permission> addPermissionsToRole(Role role,
            Collection<Permission> permissions) {
        return addPermissionsToRole(role.getId(), IDUtils
                .getIdsAsIntegers(permissions));
    }

    @Override
    public Collection<Permission> addPermissionsToRole(Role role,
            Permission... permissions) {
        return addPermissionsToRole(role.getId(), IDUtils
                .getIdsAsIntegers(Arrays.asList(permissions)));
    }

    @Override
    public Collection<Permission> addPermissionsToRole(final String rolename,
            final Collection<Integer> permissionIds) {
        if (GenericValidator.isBlankOrNull(domain)) {
            throw new IllegalArgumentException("domain is not specified");
        }
        if (rolename == null) {
            throw new IllegalArgumentException("rolename is not specified");
        }
        if (permissionIds == null || permissionIds.size() == 0) {
            throw new IllegalArgumentException("permissions not specified");
        }
        verifyDomain(domain);
        Role role = verifyRole(domain, rolename);
        PermissionRepository repository = repositoryFactory
                .getPermissionRepository(domain);
        for (Integer permissionID : permissionIds) {
            Permission permission = verifyPermission(domain, permissionID);
            permission.addRole(role);
            repository.save(permission);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Added permissions " + permissionIds + " to "
                    + rolename + " in domain " + domain);
        }
        return repository.getPermissionsForRoles(Arrays.asList(role));
    }

    @Override
    public Collection<Role> addRolesToSubject(Subject subject,
            Collection<Role> roles) {
        return addRolesToSubject(subject.getId(), IDUtils.getIdsAsString(roles));
    }

    @Override
    public Collection<Role> addRolesToSubject(Subject subject, Role... roles) {
        return addRolesToSubject(subject.getId(), IDUtils.getIdsAsString(Arrays
                .asList(roles)));
    }

    @Override
    public Collection<Role> addRolesToSubject(final String subjectName,
            final Collection<String> rolenames) {
        if (GenericValidator.isBlankOrNull(domain)) {
            throw new IllegalArgumentException("domain is not specified");
        }
        if (subjectName == null) {
            throw new IllegalArgumentException("subject is not specified");
        }
        if (rolenames == null || rolenames.size() == 0) {
            throw new IllegalArgumentException("roles not specified");
        }
        verifyDomain(domain);
        verifySubject(domain, subjectName);

        RoleRepository repository = repositoryFactory.getRoleRepository(domain);
        for (String rolename : rolenames) {
            Role role = verifyRole(domain, rolename);
            role.addSubject(subjectName);
            repository.save(role);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Added roles " + rolenames + " to " + subjectName
                    + " in domain " + domain);
        }
        return repository.getRolesForSubject(subjectName);
    }

    @Override
    public Collection<Permission> removePermissionsToRole(Role role,
            Collection<Permission> permissions) {
        return removePermissionsToRole(role.getId(), IDUtils
                .getIdsAsIntegers(permissions));
    }

    @Override
    public Collection<Permission> removePermissionsToRole(Role role,
            Permission... permissions) {
        return removePermissionsToRole(role.getId(), IDUtils
                .getIdsAsIntegers(Arrays.asList(permissions)));
    }

    @Override
    public Collection<Permission> removePermissionsToRole(
            final String rolename, final Collection<Integer> permissionIds) {
        if (GenericValidator.isBlankOrNull(domain)) {
            throw new IllegalArgumentException("domain is not specified");
        }
        if (rolename == null) {
            throw new IllegalArgumentException("rolename is not specified");
        }
        if (permissionIds == null || permissionIds.size() == 0) {
            throw new IllegalArgumentException("permissions not specified");
        }
        verifyDomain(domain);
        Role role = verifyRole(domain, rolename);
        PermissionRepository repository = repositoryFactory
                .getPermissionRepository(domain);
        for (Integer permissionID : permissionIds) {
            Permission permission = verifyPermission(domain, permissionID);
            permission.removeRole(role);
            repository.save(permission);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Removed permissions " + permissionIds + " to "
                    + rolename + " in domain " + domain);
        }
        return repository.getPermissionsForRoles(Arrays.asList(role));

    }

    @Override
    public Collection<Role> removeRolesToSubject(Subject subject,
            Collection<Role> roles) {
        return removeRolesToSubject(subject.getId(), IDUtils
                .getIdsAsString(roles));
    }

    @Override
    public Collection<Role> removeRolesToSubject(Subject subject, Role... roles) {
        return removeRolesToSubject(subject.getId(), IDUtils
                .getIdsAsString(Arrays.asList(roles)));
    }

    @Override
    public Collection<Role> removeRolesToSubject(final String subjectName,
            final Collection<String> rolenames) {
        if (GenericValidator.isBlankOrNull(domain)) {
            throw new IllegalArgumentException("domain is not specified");
        }
        if (subjectName == null) {
            throw new IllegalArgumentException("subject is not specified");
        }
        if (rolenames == null || rolenames.size() == 0) {
            throw new IllegalArgumentException("roles not specified");
        }
        verifyDomain(domain);
        Subject subject = verifySubject(domain, subjectName);
        RoleRepository repository = repositoryFactory.getRoleRepository(domain);
        for (String rolename : rolenames) {
            Role role = verifyRole(domain, rolename);
            role.removeSubject(subject);
            repository.save(role);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Removed roles " + rolenames + " from " + subjectName
                    + " in domain " + domain);
        }
        return repository.getRolesForSubject(subjectName);
    }

    @Override
    public boolean isSubjectInRole(final String subjectName,
            final String rolename) {
        if (GenericValidator.isBlankOrNull(domain)) {
            throw new IllegalArgumentException("domain is not specified");
        }
        if (GenericValidator.isBlankOrNull(subjectName)) {
            throw new IllegalArgumentException("subjectName is not specified");
        }
        if (GenericValidator.isBlankOrNull(rolename)) {
            throw new IllegalArgumentException("rolename is not specified");
        }
        Role role = verifyRole(domain, rolename);
        return role.getSubjectIds().contains(subjectName);
    }

    private Domain verifyDomain(String domainName) {
        Domain domain = repositoryFactory.getDomainRepository().findById(
                domainName);
        if (domain == null) {
            throw new IllegalStateException("domain name " + domainName
                    + " does not exist");
        }
        return domain;
    }

    private Subject verifySubject(String domain, String subjectName) {
        Subject subject = repositoryFactory.getSubjectRepository(domain)
                .findById(subjectName);
        if (subject == null) {
            throw new IllegalStateException("subject name " + subjectName
                    + " does not exist");
        }
        return subject;
    }

    private Role verifyRole(String domain, String rolename) {
        Role role = repositoryFactory.getRoleRepository(domain).findById(
                rolename);
        if (role == null) {
            throw new IllegalStateException("role name " + rolename
                    + " does not exist in " + domain);
        }
        return role;
    }

    private Permission verifyPermission(String domain, Integer id) {
        Permission permission = repositoryFactory.getPermissionRepository(
                domain).findById(id);
        if (permission == null) {
            throw new IllegalStateException("permission with id " + id
                    + " does not exist");
        }
        return permission;
    }

}
