package com.plexobject.rbac.service;

import javax.ws.rs.core.Response;

public interface UsersService {
    Response get(String domain, String username);

    Response index(String domain);

    Response post(String domain, String userJson);

    Response delete(String domain, String username);

    Response delete(String domain);
}
