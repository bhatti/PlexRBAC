package com.plexobject.rbac.repository.bdb;

import com.plexobject.rbac.domain.Domain;
import com.plexobject.rbac.repository.DomainRepository;
import com.plexobject.rbac.repository.PersistenceException;
import com.sleepycat.persist.EntityStore;

public class DomainRepositoryImpl extends BaseRepositoryImpl<Domain, String>
        implements DomainRepository {
    private final SecurityRepositoryImpl securityRepository;

    public DomainRepositoryImpl(final EntityStore store,
            final SecurityRepositoryImpl securityRepository) {
        super(store);
        this.securityRepository = securityRepository;
    }

    @Override
    public boolean remove(final String id) throws PersistenceException {
        try {
            securityRepository.beginTransaction();
            boolean success = super.remove(id);
            if (success) {
                securityRepository.removeDatabase(id);
            }
            securityRepository.commitTransaction();
            return success;
        } catch (RuntimeException e) {
            securityRepository.abortTransaction();
            throw e;
        }
    }

    @Override
    public Domain save(final Domain domain) throws PersistenceException {

        try {
            securityRepository.beginTransaction();
            Domain saved = super.save(domain);
            if (!securityRepository.getAllDatabases().contains(domain.getID())) {
                securityRepository.createDatabase(domain.getID());
            }
            securityRepository.commitTransaction();
            return saved;
        } catch (RuntimeException e) {
            securityRepository.abortTransaction();
            throw e;
        }
    }

    @Override
    public Domain getOrCreateDomain(final String name) {
        Domain domain = super.findByID(name);
        if (domain == null) {
            domain = new Domain(name, name);
            save(domain);
        }
        return domain;
    }
}
