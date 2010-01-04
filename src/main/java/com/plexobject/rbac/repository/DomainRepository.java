package com.plexobject.rbac.repository;

import com.plexobject.rbac.domain.Domain;

public interface DomainRepository extends BaseRepository<Domain, String> {

    Domain getOrCreateDomain(String domain);
}
