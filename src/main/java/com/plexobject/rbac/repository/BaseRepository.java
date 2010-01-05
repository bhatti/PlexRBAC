package com.plexobject.rbac.repository;

import com.plexobject.rbac.domain.Identifiable;

public interface BaseRepository<T extends Identifiable<ID>, ID> {
    T findByID(ID id) throws PersistenceException;

    T save(T object) throws PersistenceException;

    boolean remove(ID id) throws PersistenceException;

    PagedList<T, ID> findAll(ID lastKey, int limit);

    void clear() throws PersistenceException;

}
