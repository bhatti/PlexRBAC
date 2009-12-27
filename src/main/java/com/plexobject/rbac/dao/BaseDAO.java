package com.plexobject.rbac.dao;

import java.util.Iterator;

public interface BaseDAO<T, ID> {
    T findByID(ID id) throws PersistenceException;

    T save(T object) throws PersistenceException;

    boolean remove(ID id) throws PersistenceException;

    Iterator<T> findAll();
}
