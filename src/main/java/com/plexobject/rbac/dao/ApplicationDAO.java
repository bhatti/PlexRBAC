package com.plexobject.rbac.dao;

import java.util.Iterator;

import com.plexobject.rbac.domain.Application;

public interface ApplicationDAO extends BaseDAO<Application, String> {
    Iterator<Application> findByUser(String username);

    Application findByName(String name);

}
