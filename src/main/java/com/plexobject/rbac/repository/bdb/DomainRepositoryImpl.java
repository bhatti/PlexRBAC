package com.plexobject.rbac.repository.bdb;

import org.apache.commons.validator.GenericValidator;

import com.plexobject.rbac.domain.Domain;
import com.plexobject.rbac.domain.Subject;
import com.plexobject.rbac.repository.DomainRepository;
import com.plexobject.rbac.repository.PersistenceException;
import com.plexobject.rbac.repository.RepositoryFactory;
import com.plexobject.rbac.utils.CurrentRequest;
import com.sleepycat.persist.EntityStore;

public class DomainRepositoryImpl extends BaseRepositoryImpl<Domain, String>
        implements DomainRepository {
    private final DatabaseStore databaseStore;
    private final RepositoryFactory repositoryFactory;

    public DomainRepositoryImpl(final EntityStore entityStore,
            final DatabaseStore databaseStore,
            final RepositoryFactory repositoryFactory) {
        super(entityStore);
        this.databaseStore = databaseStore;
        this.repositoryFactory = repositoryFactory;
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
            Domain domain = super.findById(id);
            if (domain == null) {
                throw new IllegalStateException("domain with name " + id
                        + " does not exist");
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
        String subjectName = CurrentRequest.getSubjectName();
        String originalSubjectName = subjectName;
        try {
            databaseStore.beginTransaction();

            if (Domain.DEFAULT_DOMAIN_NAME.equals(domain.getId())) {
                CurrentRequest.setSubjectName(Subject.SUPER_ADMIN.getId());
                subjectName = repositoryFactory.getSuperAdmin().getId();
                repositoryFactory.getSubjectRepository(domain.getId())
                        .getOrCreateSubject(Subject.SUPER_ADMIN);
            }

            if (GenericValidator.isBlankOrNull(subjectName)) {
                throw new IllegalArgumentException(
                        "current subjectName is not specified");
            }

            // now assigning current subject to the domain as owner
            domain.addOwner(subjectName);

            Domain saved = super.save(domain);

            // Each domain will be stored as a different database, it
            // encapsulates subject/roles/permissions for each domain in a
            // different database physically.
            if (!databaseStore.getAllDatabases().contains(domain.getId())) {
                databaseStore.createDatabase(domain.getId());
            }
            databaseStore.commitTransaction();
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("****** Saved new domain " + saved);
            }
            return saved;
        } catch (RuntimeException e) {
            databaseStore.abortTransaction();
            throw new PersistenceException("Failed to save " + domain, e);
        } finally {
            CurrentRequest.setSubjectName(originalSubjectName);
        }
    }

    @Override
    public Domain getOrCreateDomain(final String name) {
        if (GenericValidator.isBlankOrNull(name)) {
            throw new IllegalArgumentException("domain name is not specified");
        }
        Domain domain = super.findById(name);
        if (domain == null) {
            domain = save(new Domain(name, name));
        }
        return domain;
    }

    @Override
    public boolean isSubjectOwner(String domainName, String subjectName) {
        if (GenericValidator.isBlankOrNull(domainName)) {
            throw new IllegalArgumentException("domain name is not specified");
        }
        if (GenericValidator.isBlankOrNull(subjectName)) {
            throw new IllegalArgumentException("subjectName is not specified");
        }
        Domain domain = super.findById(domainName);
        return domain != null
                && domain.getOwnerSubjectNames().contains(subjectName);
    }
}
