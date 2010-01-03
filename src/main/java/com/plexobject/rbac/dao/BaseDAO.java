package com.plexobject.rbac.dao;

import com.plexobject.rbac.domain.Identifiable;

public interface BaseDAO<T extends Identifiable<ID>, ID> {
    T findByID(ID id) throws PersistenceException;

    T save(T object) throws PersistenceException;

    boolean remove(ID id) throws PersistenceException;

    PagedList<T, ID> findAll(ID lastKey, int limit);
}
