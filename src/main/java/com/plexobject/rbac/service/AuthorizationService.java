package com.plexobject.rbac.service;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public interface AuthorizationService {
    Response authorize(final UriInfo ui, final String domain,
            final String subjectname, final String operation, final String target);
}
