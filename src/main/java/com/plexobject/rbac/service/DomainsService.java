package com.plexobject.rbac.service;

import javax.ws.rs.core.Response;

public interface DomainsService {
    Response get(String domain);

    Response index();

    Response put(String domainJson);

    Response delete(String domain);

    Response delete();
}
