package com.plexobject.rbac.service;

import javax.ws.rs.core.Response;

import com.plexobject.rbac.domain.Permission;

public interface PermissionsService {
    /**
     * 
     * @param domain
     * @param id
     * @return
     */
    Response get(String domain, Integer id);

    /**
     * 
     * @param domain
     * @param lastKey
     * @param limit
     * @return
     */
    Response index(String domain, Integer lastKey, int limit);

    /**
     * 
     * @param domain
     * @param permission
     * @return
     */
    Response post(String domain, Permission permission);

    /**
     * 
     * @param domain
     * @param id
     * @return
     */
    Response delete(String domain, Integer id);

    /**
     * 
     * @param domain
     * @return
     */
    Response delete(String domain);
}
