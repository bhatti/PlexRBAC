package com.plexobject.rbac.repository;

import com.plexobject.rbac.domain.Subject;

public interface SubjectRepository extends BaseRepository<Subject, String> {
    /**
     * 
     * @param subject
     * @return
     */
    Subject getOrCreateSubject(Subject subject);

    /**
     * Verifies login
     * 
     * @param subjectname
     * @param credentials
     * @return
     * @throws SecurityException
     */
    Subject authenticate(String subjectname, String credentials)
            throws SecurityException;
}
