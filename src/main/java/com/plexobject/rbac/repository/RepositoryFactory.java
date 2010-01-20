package com.plexobject.rbac.repository;

import com.plexobject.rbac.domain.Domain;
import com.plexobject.rbac.domain.Subject;

/**
 * This class interface as a factory for repositories to add subjects, roles,
 * permissions and domains.
 * 
 */
public interface RepositoryFactory {
    /**
     * 
     * @return instance of security repository
     */
    SecurityMappingRepository getSecurityMappingRepository(String domain);

    /**
     * 
     * @return instance of domain repository to manage domains
     */
    DomainRepository getDomainRepository();

    /**
     * 
     * @return high level domain for this application
     */
    Subject getSuperAdmin();

    /**
     * 
     * @return high level domain for this application
     */
    Domain getDefaultDomain();

    /**
     * 
     * @param domain
     * @return repository of roles for specific domain
     */
    RoleRepository getRoleRepository(String domain);

    /**
     * 
     * @param domain
     * @return repository of permissions for given domain
     */
    PermissionRepository getPermissionRepository(String domain);

    /**
     * 
     * @param domain
     * @return repository of security errors for given domain
     */
    SecurityErrorRepository getSecurityErrorRepository(String domain);

    /**
     * 
     * @param domain
     * @return repository of subjects for given domain
     */
    SubjectRepository getSubjectRepository(String domain);

    /**
     * 
     * @return
     */
    SubjectRepository getDefaultSubjectRepository();
}
