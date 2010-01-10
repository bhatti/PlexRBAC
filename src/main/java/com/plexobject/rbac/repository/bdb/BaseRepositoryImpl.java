package com.plexobject.rbac.repository.bdb;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.plexobject.rbac.repository.BaseRepository;
import com.plexobject.rbac.repository.PagedList;
import com.plexobject.rbac.repository.PersistenceException;
import com.plexobject.rbac.domain.PersistentObject;
import com.plexobject.rbac.domain.Identifiable;
import com.plexobject.rbac.domain.Validatable;
import com.plexobject.rbac.metric.Metric;
import com.plexobject.rbac.metric.Timing;
import com.plexobject.rbac.utils.CurrentRequest;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

public class BaseRepositoryImpl<T extends Identifiable<ID>, ID> implements
        BaseRepository<T, ID> {
    static final int MAX_LIMIT = 512;
    static final int DEFAULT_LIMIT = 20;
    final Logger LOGGER = Logger.getLogger(getClass());
    private final Class<T> entityBeanType;
    private final Class<ID> pkType;
    protected final EntityStore store;

    protected PrimaryIndex<ID, T> primaryIndex;

    @SuppressWarnings("unchecked")
    public BaseRepositoryImpl(final EntityStore store) {
        this.store = store;
        this.entityBeanType = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
        this.pkType = (Class<ID>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[1];
        try {
            primaryIndex = store.getPrimaryIndex(pkType, entityBeanType);
        } catch (DatabaseException e) {
            throw new PersistenceException(e);
        }

    }

    @Override
    public PagedList<T, ID> findAll(ID firstKey, int limit) {
        if (limit <= 0) {
            limit = DEFAULT_LIMIT;
        }
        limit = Math.max(limit, MAX_LIMIT);
        final Timing timer = Metric
                .newTiming(getClass().getName() + ".findAll");
        EntityCursor<T> cursor = null;
        List<T> all = new ArrayList<T>();
        try {
            cursor = primaryIndex.entities(firstKey, false, null, true);
            Iterator<T> it = cursor.iterator();
            ID lastKey = null;
            for (int i = 0; it.hasNext() && i < limit; i++) {
                T next = it.next();
                all.add(next);
                lastKey = next.getId();
            }
            return new PagedList<T, ID>(all, firstKey, lastKey, limit, all
                    .size() == limit);
        } catch (DatabaseException e) {
            throw new PersistenceException("Failed to find all in "
                    + store.getStoreName(), e);
        } finally {
            timer.stop();
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (DatabaseException e) {
                    LOGGER.error("failed to close cursor", e);
                }
            }
        }
    }

    @Override
    public T findByID(ID id) throws PersistenceException {
        final Timing timer = Metric.newTiming(getClass().getName()
                + ".findByID");
        try {
            return primaryIndex.get(id);
        } catch (DatabaseException e) {
            throw new PersistenceException("Failed to find " + id + " in "
                    + store.getStoreName(), e);
        } finally {
            timer.stop();
        }
    }

    @Override
    public boolean remove(ID id) throws PersistenceException {
        final Timing timer = Metric.newTiming(getClass().getName() + ".remove");
        try {
            return primaryIndex.delete(id);
        } catch (DatabaseException e) {
            throw new PersistenceException("Failed to remove " + id + " in "
                    + store.getStoreName(), e);
        } finally {
            timer.stop();
        }
    }

    @Override
    public T save(T object) throws PersistenceException {
        final Timing timer = Metric.newTiming(getClass().getName() + ".remove");

        try {
            // call validation
            if (object instanceof Validatable) {
                ((Validatable) object).validate();
            }
            if (object instanceof PersistentObject) {
                PersistentObject auditable = (PersistentObject) object;
                if (auditable.getCreatedBy() == null) {
                    auditable.setCreatedAt(new Date());
                    auditable.setCreatedBy(CurrentRequest.getSubjectname());
                    auditable.setCreatedIPAddress(CurrentRequest
                            .getIPAddress());
                }
                auditable.setUpdatedAt(new Date());
                auditable.setUpdatedBy(CurrentRequest.getSubjectname());
                auditable
                        .setUpdatedIPAddress(CurrentRequest.getIPAddress());
            }
            T old = primaryIndex.put(object);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("saving old " + old + ", new " + object);
            }
            return object;
        } catch (DatabaseException e) {
            throw new PersistenceException("Failed to save " + object + " in "
                    + store.getStoreName(), e);
        } finally {
            timer.stop();
        }
    }

    public Class<T> getEntityBeanType() {
        return entityBeanType;
    }

    public void clear() throws PersistenceException {
        try {
            store.truncateClass(entityBeanType);
        } catch (DatabaseException e) {
            throw new PersistenceException("Failed to remove all objects in "
                    + store.getStoreName(), e);
        }
    }

    public void close() {
        try {
            store.close();
        } catch (DatabaseException e) {
            throw new PersistenceException(e);
        }
    }
}
