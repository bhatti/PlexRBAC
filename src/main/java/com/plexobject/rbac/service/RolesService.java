package com.plexobject.rbac.service;

import javax.ws.rs.core.Response;

public interface RolesService {
    Response get(String domain, String rolename);

    Response index(String domain);

    Response post(String domain, String roleJson);

    Response delete(String domain, String rolename);

    Response delete(String domain);
}
