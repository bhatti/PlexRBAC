package com.plexobject.rbac.dao.bdb;

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;

import com.plexobject.rbac.Configuration;
import com.plexobject.rbac.dao.BaseDAO;
import com.plexobject.rbac.dao.PersistenceException;
import com.plexobject.rbac.domain.Auditable;
import com.plexobject.rbac.domain.Validatable;
import com.plexobject.rbac.metric.Metric;
import com.plexobject.rbac.metric.Timing;
import com.plexobject.rbac.utils.CurrentUserRequest;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentLockedException;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;
import com.sleepycat.persist.evolve.IncompatibleClassException;

public class BaseDAOBDB<T, ID> implements BaseDAO<T, ID> {
    final Logger LOGGER = Logger.getLogger(getClass());
    private static final String DATABASE_DIR = Configuration.getInstance()
            .getProperty("database.dir", "plexrbac");
    private static final String DATABASE_NAME = Configuration.getInstance()
            .getProperty("database.name", "rbac_db");
    private final Class<T> entityBeanType;
    private final Class<ID> pkType;

    private Environment dbEnvironment;
    private final String tableName;
    EntityStore store;
    PrimaryIndex<ID, T> primaryIndex;

    public BaseDAOBDB() throws IncompatibleClassException, DatabaseException {
        this(DATABASE_DIR, DATABASE_NAME);
    }

    @SuppressWarnings("unchecked")
    public BaseDAOBDB(final String databaseDir, final String storeName)
            throws IncompatibleClassException, DatabaseException {
        if (GenericValidator.isBlankOrNull(databaseDir)) {
            throw new IllegalArgumentException("databaseDir is not specified");
        }
        if (GenericValidator.isBlankOrNull(storeName)) {
            throw new IllegalArgumentException("tableName is not specified");
        }
        this.entityBeanType = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
        this.pkType = (Class<ID>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[1];
        this.tableName = storeName;
        // Open the DB environment. Create if they do not already exist.
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(true);
        envConfig.setTransactional(false);
        envConfig.setSharedCache(true);
        // envConfig.setReadOnly(true);
        final File dir = new File(databaseDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        dbEnvironment = new Environment(dir, envConfig);
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setAllowCreate(true);
        storeConfig.setDeferredWrite(true);
        store = new EntityStore(dbEnvironment, storeName, storeConfig);
        primaryIndex = store.getPrimaryIndex(pkType, entityBeanType);
    }

    public String[] getAllDatabases() throws PersistenceException {
        final Timing timer = Metric.newTiming(getClass().getName()
                + ".getAllDatabases");
        try {
            List<String> dbList = dbEnvironment.getDatabaseNames();
            String[] dbNames = new String[dbList.size()];
            for (int i = 0; i < dbList.size(); i++) {
                dbNames[i] = dbList.get(i);
            }
            return dbNames;
        } catch (EnvironmentLockedException e) {
            throw new PersistenceException(e);
        } catch (DatabaseException e) {
            throw new PersistenceException(e);
        } finally {
            timer.stop();
        }
    }

    public void removeDatabase() {
        final Timing timer = Metric.newTiming(getClass().getName()
                + ".removeDatabase");
        try {
            dbEnvironment.removeDatabase(null, tableName);
        } catch (EnvironmentLockedException e) {
            throw new PersistenceException(e);
        } catch (DatabaseException e) {
            throw new PersistenceException(e);
        } finally {
            timer.stop();
        }
    }

    public void createDatabase() {
        final Timing timer = Metric.newTiming(getClass().getName()
                + ".createDatabase");
        try {
            DatabaseConfig dbconfig = new DatabaseConfig();
            dbconfig.setAllowCreate(true);
            dbconfig.setSortedDuplicates(false);
            dbconfig.setExclusiveCreate(false);
            dbconfig.setReadOnly(false);
            dbconfig.setTransactional(true);
            dbEnvironment.openDatabase(null, tableName, dbconfig);
        } catch (EnvironmentLockedException e) {
            throw new PersistenceException(e);
        } catch (DatabaseException e) {
            throw new PersistenceException(e);
        } finally {
            timer.stop();
        }
    }

    public void close() throws DatabaseException {
        if (dbEnvironment == null) {
            LOGGER.warn("already closed");
            return;
        }
        store.close();
        dbEnvironment.sync();
        dbEnvironment.close();
        dbEnvironment = null;
    }

    @Override
    public Iterator<T> findAll() {
        final Timing timer = Metric
                .newTiming(getClass().getName() + ".findAll");
        try {
            return new CursorIterator<T>(primaryIndex.entities());
        } catch (DatabaseException e) {
            throw new PersistenceException(e);
        } finally {
            timer.stop();
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

}
