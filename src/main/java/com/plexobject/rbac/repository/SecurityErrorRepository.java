package com.plexobject.rbac.repository;

import java.util.Date;

import com.plexobject.rbac.domain.SecurityError;

public interface SecurityErrorRepository extends BaseRepository<SecurityError, Integer> {
    PagedList<SecurityError, Integer> findAll(String appName, Date since,
            Integer lastKey, int limit);
}
