package com.plexobject.rbac.service;

import javax.ws.rs.core.Response;

public interface ServerStatusService {
    Response flushCaches();

    Response cacheSizes();
}
