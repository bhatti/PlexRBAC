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
     * @param subjectName
     * @param credentials
     * @return
     * @throws SecurityException
     */
    Subject authenticate(String subjectName, String credentials)
            throws SecurityException;
}
