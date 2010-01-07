package com.plexobject.rbac.service;

import javax.ws.rs.core.Response;

public interface AuthenticationService {
    Response authenticate(String domain, String username, String password);
}
