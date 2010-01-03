package com.plexobject.rbac.dao;

import java.util.Collection;

import com.plexobject.rbac.domain.Application;

public interface ApplicationDAO extends BaseDAO<Application, String> {
    Collection<Application> findByUser(String username);
}
