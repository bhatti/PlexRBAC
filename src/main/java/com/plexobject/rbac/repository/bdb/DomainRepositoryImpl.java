package com.plexobject.rbac.repository.bdb;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.validator.GenericValidator;

import com.plexobject.rbac.domain.Domain;
import com.plexobject.rbac.domain.Role;
import com.plexobject.rbac.domain.User;
import com.plexobject.rbac.repository.DomainRepository;
import com.plexobject.rbac.repository.PersistenceException;
import com.plexobject.rbac.repository.RepositoryFactory;
import com.plexobject.rbac.repository.SecurityRepository;
import com.plexobject.rbac.utils.CurrentUserRequest;
import com.sleepycat.persist.EntityStore;

public class DomainRepositoryImpl extends BaseRepositoryImpl<Domain, String>
        implements DomainRepository {
    private final DatabaseStore databaseStore;
    private final RepositoryFactory repositoryFactory;
    private final SecurityRepository securityRepository;

    public DomainRepositoryImpl(final EntityStore entityStore,
            final DatabaseStore databaseStore,
            final RepositoryFactory repositoryFactory) {
        super(entityStore);
        this.databaseStore = databaseStore;
        this.repositoryFactory = repositoryFactory;
        this.securityRepository = repositoryFactory.getSecurityRepository();
    }

    @Override
    public boolean remove(final String id) throws PersistenceException {
        if (GenericValidator.isBlankOrNull(id)) {
            throw new IllegalArgumentException("domain name is not specified");
        }
        if (Domain.DEFAULT_DOMAIN_NAME.equals(id)) {
            throw new IllegalStateException(Domain.DEFAULT_DOMAIN_NAME
                    + " cannot be removed");
        }
        try {
            databaseStore.beginTransaction();
            Domain domain = super.findByID(id);
            if (domain == null) {
                throw new IllegalStateException("domain with name " + id
                        + " does not exist");
            }
            boolean success = super.remove(id);
            Collection<String> owners = domain.getOwnerUsernames();
            for (String owner : owners) {
                securityRepository.removeRolesToUser(domain.getID(), owner,
                        Arrays.asList(Role.DOMAIN_OWNER.getID()));
            }
            if (success) {
                databaseStore.removeDatabase(id);
            }
            databaseStore.commitTransaction();
            return success;
        } catch (RuntimeException e) {
            databaseStore.abortTransaction();
            throw e;
        }
    }

    @Override
    public Domain save(final Domain domain) throws PersistenceException {
        if (domain == null) {
            throw new IllegalArgumentException("domain is not specified");
        }
        try {
            databaseStore.beginTransaction();
            // each domain must have an owner, which will automatically be
            // assigned role of DOMAIN_OWNER
            // however for default domain, we control the owner.
            String username = Domain.DEFAULT_DOMAIN_NAME.equals(domain.getID()) ? repositoryFactory
                    .getSuperAdmin().getID()
                    : CurrentUserRequest.getUsername();
            if (GenericValidator.isBlankOrNull(username)) {
                throw new IllegalArgumentException(
                        "current username is not specified");
            }
            repositoryFactory.getUserRepository(domain.getID())
                    .getOrCreateUser(username);
            // now assigning current user to the domain as owner
            domain.addOwner(username);

            Domain saved = super.save(domain);

            // assigning DOMAIN_ROLE to the user
            securityRepository.addRolesToUser(domain.getID(), username, Arrays
                    .asList(Role.DOMAIN_OWNER.getID()));

            // Each domain will be stored as a different database, it
            // encapsulates user/roles/permissions for each domain in a
            // different database physically.
            if (!databaseStore.getAllDatabases().contains(domain.getID())) {
                databaseStore.createDatabase(domain.getID());
            }
            databaseStore.commitTransaction();
            return saved;
        } catch (RuntimeException e) {
            databaseStore.abortTransaction();
            throw e;
        }
    }

    @Override
    public Domain getOrCreateDomain(final String name) {
        if (GenericValidator.isBlankOrNull(name)) {
            throw new IllegalArgumentException("domain name is not specified");
        }
        Domain domain = super.findByID(name);
        if (domain == null) {
            save(domain);
        }
        return domain;
    }
}
