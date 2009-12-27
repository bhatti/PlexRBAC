package com.plexobject.rbac.dao;

import java.util.Date;
import java.util.Iterator;

import com.plexobject.rbac.domain.Application;
import com.plexobject.rbac.domain.SecurityError;

public interface SecurityErrorDAO extends BaseDAO<SecurityError, Integer> {
    Iterator<SecurityError> findAll(Application app, Date since);
}
