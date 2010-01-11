package com.plexobject.rbac.repository.bdb;

import org.apache.commons.validator.GenericValidator;

import com.plexobject.rbac.domain.Subject;
import com.plexobject.rbac.repository.PersistenceException;
import com.plexobject.rbac.repository.SubjectRepository;
import com.plexobject.rbac.utils.PasswordUtils;
import com.sleepycat.persist.EntityStore;

public class SubjectRepositoryImpl extends BaseRepositoryImpl<Subject, String>
        implements SubjectRepository {
    public SubjectRepositoryImpl(final EntityStore store) {
        super(store);
    }

    @Override
    public Subject getOrCreateSubject(Subject subject) {
        if (subject == null) {
            throw new IllegalArgumentException("subject is not specified");
        }
        if (super.findById(subject.getId()) == null) {
            save(subject);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Created subject " + subject);
            }
        }
        return subject;
    }

    @Override
    public boolean remove(final String subjectName) throws PersistenceException {
        if (GenericValidator.isBlankOrNull(subjectName)) {
            throw new IllegalArgumentException("subjectName is not specified");
        }
        if (Subject.SUPER_ADMIN.getId().equals(subjectName)) {
            throw new IllegalStateException(subjectName + " cannot be removed");
        }
        return super.remove(subjectName);
    }

    @Override
    public Subject authenticate(String subjectName, String credentials)
            throws SecurityException {
        Subject subject = findById(subjectName);
        if (subject == null) {
            throw new SecurityException("Failed to find subject " + subjectName
                    + " in " + store.getStoreName() + " domain");
        }
        if (PasswordUtils.getHash(credentials).equals(subject.getCredentials())) {
            return subject;
        }
        
        throw new SecurityException("Credentials mismatch for subject " + subjectName);

    }
}
