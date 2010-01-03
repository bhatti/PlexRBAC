package com.plexobject.rbac.dao.bdb;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.plexobject.rbac.dao.BaseDAO;
import com.plexobject.rbac.dao.PagedList;
import com.plexobject.rbac.dao.PersistenceException;
import com.plexobject.rbac.domain.Auditable;
import com.plexobject.rbac.domain.Identifiable;
import com.plexobject.rbac.domain.Validatable;
import com.plexobject.rbac.metric.Metric;
import com.plexobject.rbac.metric.Timing;
import com.plexobject.rbac.utils.CurrentUserRequest;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

public class BaseDAOBDB<T extends Identifiable<ID>, ID> implements
        BaseDAO<T, ID> {
    static final int MAX_LIMIT = 512;
    static final int DEFAULT_LIMIT = 20;
    final Logger LOGGER = Logger.getLogger(getClass());
    private final Class<T> entityBeanType;
    private final Class<ID> pkType;
    private final EntityStore store;

    protected PrimaryIndex<ID, T> primaryIndex;

    @SuppressWarnings("unchecked")
    public BaseDAOBDB(final EntityStore store) {
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
                lastKey = next.getID();
            }
            return new PagedList<T, ID>(all, firstKey, lastKey, limit, all
                    .size() == limit);
        } catch (DatabaseException e) {
            throw new PersistenceException(e);
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
            throw new PersistenceException(e);
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
            throw new PersistenceException(e);
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
            if (object instanceof Auditable) {
                Auditable auditable = (Auditable) object;
                if (auditable.getCreatedBy() == null) {
                    auditable.setCreatedAt(new Date());
                    auditable.setCreatedBy(CurrentUserRequest.getUsername());
                    auditable.setCreatedIPAddress(CurrentUserRequest
                            .getIPAddress());
                }
                auditable.setUpdatedAt(new Date());
                auditable.setUpdatedBy(CurrentUserRequest.getUsername());
                auditable
                        .setUpdatedIPAddress(CurrentUserRequest.getIPAddress());
            }
            return primaryIndex.put(object); // returns old object
        } catch (DatabaseException e) {
            throw new PersistenceException(e);
        } finally {
            timer.stop();
        }
    }

    public Class<T> getEntityBeanType() {
        return entityBeanType;
    }

    public void close(final String storeName) {
        try {
            store.close();
        } catch (DatabaseException e) {
            throw new PersistenceException(e);
        }
    }
}
