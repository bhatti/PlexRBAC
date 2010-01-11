package com.plexobject.rbac.service;

import javax.ws.rs.core.Response;

public interface SubjectRolesService {

    /**
     * Add given roles to the subject and save them
     * 
     * @param domain
     * @param subject
     * @param roles
     */
    Response addRolesToSubject(String domain, String subject,
            String rolenamesJSON);

    /**
     * Remove given roles to the subject and save them
     * 
     * @param domain
     * @param subject
     * @param roles
     */
    Response removeRolesToSubject(String domain, String subject,
            String rolenamesJSON);

}
