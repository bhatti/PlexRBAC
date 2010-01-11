package com.plexobject.rbac.service;

import javax.ws.rs.core.Response;

import com.plexobject.rbac.domain.Subject;

public interface SubjectsService {
    Response get(String domain, String subjectName);

    Response index(String domain, String lastKey, int limit);

    Response put(String domain, Subject subject);

    Response delete(String domain, String subjectName);

    Response delete(String domain);
}
