package com.plexobject.rbac.repository.bdb;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.validator.GenericValidator;

import com.plexobject.rbac.domain.Domain;
import com.plexobject.rbac.domain.Role;
import com.plexobject.rbac.domain.Subject;
import com.plexobject.rbac.repository.DomainRepository;
import com.plexobject.rbac.repository.PersistenceException;
import com.plexobject.rbac.repository.RepositoryFactory;
import com.plexobject.rbac.repository.SecurityRepository;
import com.plexobject.rbac.utils.CurrentRequest;
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
            Collection<String> owners = domain.getOwnerSubjectnames();
            for (String owner : owners) {
                securityRepository.removeRolesToSubject(domain.getId(), owner,
                        Arrays.asList(Role.DOMAIN_OWNER.getId()));
            }
            boolean success = super.remove(id);

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
        String subjectname = CurrentRequest.getSubjectname();
        String originalSubjectname = subjectname;
        try {
            databaseStore.beginTransaction();
            // each domain must have an owner, which will automatically be
            // assigned role of DOMAIN_OWNER
            // however for default domain, we control the owner.

            if (Domain.DEFAULT_DOMAIN_NAME.equals(domain.getId())) {
                CurrentRequest.setSubjectname(Subject.SUPER_ADMIN.getId());
                subjectname = repositoryFactory.getSuperAdmin().getId();
                repositoryFactory.getSubjectRepository(domain.getId())
                        .getOrCreateSubject(Subject.SUPER_ADMIN);
            }

            if (GenericValidator.isBlankOrNull(subjectname)) {
                throw new IllegalArgumentException(
                        "current subjectname is not specified");
            }

            repositoryFactory.getRoleRepository(domain.getId())
                    .getOrCreateRole(Role.DOMAIN_OWNER.getId());
            // now assigning current subject to the domain as owner
            domain.addOwner(subjectname);

            Domain saved = super.save(domain);

            // assigning DOMAIN_ROLE to the subject
            LOGGER.info("Adding domain owner to " + subjectname + " in domain "
                    + store.getStoreName());
            securityRepository.addRolesToSubject(domain.getId(), subjectname, Arrays
                    .asList(Role.DOMAIN_OWNER.getId()));

            // Each domain will be stored as a different database, it
            // encapsulates subject/roles/permissions for each domain in a
            // different database physically.
            if (!databaseStore.getAllDatabases().contains(domain.getId())) {
                databaseStore.createDatabase(domain.getId());
            }
            databaseStore.commitTransaction();
            return saved;
        } catch (RuntimeException e) {
            databaseStore.abortTransaction();
            throw new PersistenceException("Failed to save " + domain, e);
        } finally {
            CurrentRequest.setSubjectname(originalSubjectname);
        }
    }

    @Override
    public Domain getOrCreateDomain(final String name) {
        if (GenericValidator.isBlankOrNull(name)) {
            throw new IllegalArgumentException("domain name is not specified");
        }
        Domain domain = super.findByID(name);
        if (domain == null) {
            save(new Domain(name, name));
        }
        return domain;
    }

    @Override
    public boolean isSubjectOwner(String domainName, String subjectname) {
        if (GenericValidator.isBlankOrNull(domainName)) {
            throw new IllegalArgumentException("domain name is not specified");
        }
        if (GenericValidator.isBlankOrNull(subjectname)) {
            throw new IllegalArgumentException("subjectname is not specified");
        }
        Domain domain = super.findByID(domainName);
        return domain != null && domain.getOwnerSubjectnames().contains(subjectname);
    }
}
