package com.plexobject.rbac.repository.bdb;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;

import com.plexobject.rbac.domain.Domain;
import com.plexobject.rbac.domain.User;
import com.plexobject.rbac.repository.DomainRepository;
import com.plexobject.rbac.repository.PermissionRepository;
import com.plexobject.rbac.repository.RepositoryFactory;
import com.plexobject.rbac.repository.RoleRepository;
import com.plexobject.rbac.repository.SecurityErrorRepository;
import com.plexobject.rbac.repository.SecurityRepository;
import com.plexobject.rbac.repository.UserRepository;
import com.sleepycat.je.DatabaseException;

public class RepositoryFactoryImpl implements RepositoryFactory {
    private static final Logger LOGGER = Logger
            .getLogger(RepositoryFactoryImpl.class);

    private final DatabaseStore databaseStore;
    private Map<String, DomainRepository> applicationRepositories = new HashMap<String, DomainRepository>();
    private Map<String, PermissionRepository> permissionRepositories = new HashMap<String, PermissionRepository>();
    private Map<String, SecurityErrorRepository> securityErrorRepositories = new HashMap<String, SecurityErrorRepository>();
    private Map<String, UserRepository> userRepositories = new HashMap<String, UserRepository>();
    private Map<String, RoleRepository> roleRepositories = new HashMap<String, RoleRepository>();
    private Map<String, SecurityRepository> securityRepositories = new HashMap<String, SecurityRepository>();

    //
    public RepositoryFactoryImpl(final DatabaseStore databaseStore) {
        this.databaseStore = databaseStore;
    }

    public RepositoryFactoryImpl(final String dbName) {
        this(new DatabaseStore(dbName));
    }

    public RepositoryFactoryImpl() {
        this(new DatabaseStore());
    }

    @Override
    public synchronized DomainRepository getDomainRepository() {
        DomainRepository repository = applicationRepositories
                .get(Domain.DEFAULT_DOMAIN_NAME);
        if (repository == null) {
            repository = new DomainRepositoryImpl(databaseStore
                    .getStore(Domain.DEFAULT_DOMAIN_NAME), databaseStore, this);
            applicationRepositories.put(Domain.DEFAULT_DOMAIN_NAME, repository);
        }
        return repository;
    }

    @Override
    public Domain getDefaultDomain() {
        return getDomainRepository().getOrCreateDomain(
                Domain.DEFAULT_DOMAIN_NAME);
    }

    @Override
    public User getSuperAdmin() {
        return getUserRepository(Domain.DEFAULT_DOMAIN_NAME).getOrCreateUser(
                User.SUPER_ADMIN_USERNAME);
    }

    @Override
    public synchronized RoleRepository getRoleRepository(String domain) {
        if (GenericValidator.isBlankOrNull(domain)) {
            throw new IllegalArgumentException("domain is not specified");
        }
        RoleRepository repository = roleRepositories.get(domain);
        if (repository == null) {
            repository = new RoleRepositoryImpl(databaseStore.getStore(domain));
            roleRepositories.put(domain, repository);
        }
        return repository;
    }

    @Override
    public synchronized PermissionRepository getPermissionRepository(
            final String domain) {
        if (GenericValidator.isBlankOrNull(domain)) {
            throw new IllegalArgumentException("domain is not specified");
        }
        PermissionRepository repository = permissionRepositories.get(domain);
        if (repository == null) {
            repository = new PermissionRepositoryImpl(databaseStore
                    .getStore(domain));
            permissionRepositories.put(domain, repository);
        }
        return repository;
    }

    @Override
    public synchronized SecurityErrorRepository getSecurityErrorRepository(
            final String domain) {
        if (GenericValidator.isBlankOrNull(domain)) {
            throw new IllegalArgumentException("domain is not specified");
        }
        SecurityErrorRepository repository = securityErrorRepositories
                .get(domain);
        if (repository == null) {
            repository = new SecurityErrorRepositoryImpl(databaseStore
                    .getStore(domain));
            securityErrorRepositories.put(domain, repository);
        }
        return repository;
    }

    @Override
    public synchronized UserRepository getUserRepository(final String domain) {
        if (GenericValidator.isBlankOrNull(domain)) {
            throw new IllegalArgumentException("domain is not specified");
        }
        UserRepository repository = userRepositories.get(domain);
        if (repository == null) {
            repository = new UserRepositoryImpl(databaseStore.getStore(domain));
            userRepositories.put(domain, repository);
        }
        return repository;

    }

    @Override
    public SecurityRepository getSecurityRepository() {
        SecurityRepository repository = securityRepositories
                .get(Domain.DEFAULT_DOMAIN_NAME);
        if (repository == null) {
            repository = new SecurityRepositoryImpl(this);
            securityRepositories.put(Domain.DEFAULT_DOMAIN_NAME, repository);
        }
        return repository;
    }

    public synchronized void closeDefault() {
        close(Domain.DEFAULT_DOMAIN_NAME);
    }

    public synchronized void close(final String domain) {
        applicationRepositories.remove(domain);
        permissionRepositories.remove(domain);
        securityErrorRepositories.remove(domain);
        userRepositories.remove(domain);
        roleRepositories.remove(domain);
        try {
            databaseStore.close();
        } catch (DatabaseException e) {
            LOGGER.error("Failed to close " + domain, e);
        }
    }

}
