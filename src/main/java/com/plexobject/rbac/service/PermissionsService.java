package com.plexobject.rbac.service;

import javax.ws.rs.core.Response;

import com.plexobject.rbac.domain.Permission;

public interface PermissionsService {
    Response get(String domain, String id);

    Response index(String domain, String lastKey, int limit);

    Response post(String domain, Permission permission);

    Response delete(String domain, String id);

    Response delete(String domain);
}
