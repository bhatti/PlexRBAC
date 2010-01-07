package com.plexobject.rbac.service;

import javax.ws.rs.core.Response;

public interface PermissionsService {
    Response get(String domain, String id);

    Response index(String domain);

    Response post(String domain, String permissionJson);

    Response delete(String domain, String id);

    Response delete(String domain);
}
