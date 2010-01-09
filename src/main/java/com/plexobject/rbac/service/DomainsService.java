package com.plexobject.rbac.service;

import javax.ws.rs.core.Response;

import com.plexobject.rbac.domain.Domain;

public interface DomainsService {
    Response get(String domain);

    Response index(String lastKey, int limit);

    Response put(Domain domain);

    Response delete(String domain);

    Response delete();
}
