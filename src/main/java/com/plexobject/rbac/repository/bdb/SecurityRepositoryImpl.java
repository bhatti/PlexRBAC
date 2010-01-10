package com.plexobject.rbac.repository.bdb;

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
import com.plexobject.rbac.repository.SecurityRepository;

public class SecurityRepositoryImpl implements SecurityRepository {
    private static final Logger LOGGER = Logger
            .getLogger(SecurityRepositoryImpl.class);
    private final RepositoryFactory repositoryFactory;

    public SecurityRepositoryImpl(final RepositoryFactory repositoryFactory) {
        this.repositoryFactory = repositoryFactory;
    }

    @Override
    public void addPermissionsToRole(final String domain,
            final String rolename, final Collection<Integer> permissionIDs) {
        if (GenericValidator.isBlankOrNull(domain)) {
            throw new IllegalArgumentException("domain is not specified");
        }
        if (rolename == null) {
            throw new IllegalArgumentException("rolename is not specified");
        }
        if (permissionIDs == null || permissionIDs.size() == 0) {
            throw new IllegalArgumentException("permissions not specified");
        }
        verifyDomain(domain);
        Role role = verifyRole(domain, rolename);
        PermissionRepository repository = repositoryFactory
                .getPermissionRepository(domain);
        for (Integer permissionID : permissionIDs) {
            Permission permission = verifyPermission(domain, permissionID);
            permission.addRole(role);
            repository.save(permission);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Added permissions " + permissionIDs + " to "
                    + rolename + " in domain " + domain);
        }

    }

    @Override
    public void addRolesToSubject(final String domain, final String subjectname,
            final Collection<String> rolenames) {
        if (GenericValidator.isBlankOrNull(domain)) {
            throw new IllegalArgumentException("domain is not specified");
        }
        if (subjectname == null) {
            throw new IllegalArgumentException("subject is not specified");
        }
        if (rolenames == null || rolenames.size() == 0) {
            throw new IllegalArgumentException("roles not specified");
        }
        if (rolenames.size() == 1
                && rolenames.contains(Role.DOMAIN_OWNER.getId())) {
            // no need to check domain as
        } else {
            verifyDomain(domain);
            verifySubject(domain, subjectname);
        }

        RoleRepository repository = repositoryFactory.getRoleRepository(domain);
        for (String rolename : rolenames) {
            Role role = verifyRole(domain, rolename);
            role.addSubject(subjectname);
            repository.save(role);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Added roles " + rolenames + " to " + subjectname
                    + " in domain " + domain);
        }
    }

    @Override
    public void removePermissionsToRole(final String domain,
            final String rolename, final Collection<Integer> permissionIDs) {
        if (GenericValidator.isBlankOrNull(domain)) {
            throw new IllegalArgumentException("domain is not specified");
        }
        if (rolename == null) {
            throw new IllegalArgumentException("rolename is not specified");
        }
        if (permissionIDs == null || permissionIDs.size() == 0) {
            throw new IllegalArgumentException("permissions not specified");
        }
        verifyDomain(domain);
        Role role = verifyRole(domain, rolename);
        PermissionRepository repository = repositoryFactory
                .getPermissionRepository(domain);
        for (Integer permissionID : permissionIDs) {
            Permission permission = verifyPermission(domain, permissionID);
            permission.removeRole(role);
            repository.save(permission);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Removed permissions " + permissionIDs + " to "
                    + rolename + " in domain " + domain);
        }
    }

    @Override
    public void removeRolesToSubject(final String domain, final String subjectname,
            final Collection<String> rolenames) {
        if (GenericValidator.isBlankOrNull(domain)) {
            throw new IllegalArgumentException("domain is not specified");
        }
        if (subjectname == null) {
            throw new IllegalArgumentException("subject is not specified");
        }
        if (rolenames == null || rolenames.size() == 0) {
            throw new IllegalArgumentException("roles not specified");
        }
        verifyDomain(domain);
        Subject subject = verifySubject(domain, subjectname);
        RoleRepository repository = repositoryFactory.getRoleRepository(domain);
        for (String rolename : rolenames) {
            Role role = verifyRole(domain, rolename);
            role.removeSubject(subject);
            repository.save(role);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Removed roles " + rolenames + " from " + subjectname
                    + " in domain " + domain);
        }
    }

    @Override
    public boolean isSubjectInRole(final String domain, final String subjectname,
            final String rolename) {
        if (GenericValidator.isBlankOrNull(domain)) {
            throw new IllegalArgumentException("domain is not specified");
        }
        if (GenericValidator.isBlankOrNull(subjectname)) {
            throw new IllegalArgumentException("subjectname is not specified");
        }
        if (GenericValidator.isBlankOrNull(rolename)) {
            throw new IllegalArgumentException("rolename is not specified");
        }
        Role role = verifyRole(domain, rolename);
        return role.getSubjectIDs().contains(subjectname);
    }

    private Domain verifyDomain(String domainName) {
        Domain domain = repositoryFactory.getDomainRepository().findByID(
                domainName);
        if (domain == null) {
            throw new IllegalStateException("domain name " + domainName
                    + " does not exist");
        }
        return domain;
    }

    private Subject verifySubject(String domain, String subjectname) {
        Subject subject = repositoryFactory.getSubjectRepository(domain).findByID(
                subjectname);
        if (subject == null) {
            throw new IllegalStateException("subject name " + subjectname
                    + " does not exist");
        }
        return subject;
    }

    private Role verifyRole(String domain, String rolename) {
        Role role = repositoryFactory.getRoleRepository(domain).findByID(
                rolename);
        if (role == null) {
            throw new IllegalStateException("role name " + rolename
                    + " does not exist in " + domain);
        }
        return role;
    }

    private Permission verifyPermission(String domain, Integer id) {
        Permission permission = repositoryFactory.getPermissionRepository(
                domain).findByID(id);
        if (permission == null) {
            throw new IllegalStateException("permission with id " + id
                    + " does not exist");
        }
        return permission;
    }

}
