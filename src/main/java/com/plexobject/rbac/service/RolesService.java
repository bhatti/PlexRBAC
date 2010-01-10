package com.plexobject.rbac.service;

import javax.ws.rs.core.Response;

import com.plexobject.rbac.domain.Role;

public interface RolesService {
    Response get(String domain, String rolename);

    Response index(String domain, String lastKey, int limit);

    Response put(String domain, Role role);

    Response delete(String domain, String rolename);

    Response delete(String domain);
}
