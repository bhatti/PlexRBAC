package com.plexobject.rbac.dao;

import java.util.Date;

import com.plexobject.rbac.domain.SecurityError;

public interface SecurityErrorDAO extends BaseDAO<SecurityError, Integer> {
    PagedList<SecurityError, Integer> findAll(String appName, Date since,
            Integer lastKey, int limit);
}
