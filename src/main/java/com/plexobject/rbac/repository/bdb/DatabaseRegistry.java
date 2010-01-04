package com.plexobject.rbac.repository.bdb;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.plexobject.rbac.Configuration;
import com.plexobject.rbac.repository.ApplicationRepository;
import com.plexobject.rbac.repository.PermissionRepository;
import com.plexobject.rbac.repository.PersistenceException;
import com.plexobject.rbac.repository.RoleRepository;
import com.plexobject.rbac.repository.SecurityErrorRepository;
import com.plexobject.rbac.repository.UserRepository;
import com.plexobject.rbac.metric.Metric;
import com.plexobject.rbac.metric.Timing;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentLockedException;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;
import com.sleepycat.persist.evolve.IncompatibleClassException;

public class DatabaseRegistry {
    private static final Logger LOGGER = Logger
            .getLogger(DatabaseRegistry.class);

    private static final String DATABASE_DIR = Configuration.getInstance()
            .getProperty("database.dir", "plexrbac");
    private Environment dbEnvironment;
    private StoreConfig storeConfig;
    private Map<String, EntityStore> stores = new HashMap<String, EntityStore>();
    private Map<String, ApplicationRepository> applicationRepositories = new HashMap<String, ApplicationRepository>();
    private Map<String, PermissionRepository> permissionRepositories = new HashMap<String, PermissionRepository>();
    private Map<String, SecurityErrorRepository> securityErrorRepositories = new HashMap<String, SecurityErrorRepository>();
    private Map<String, UserRepository> userRepositories = new HashMap<String, UserRepository>();
    private Map<String, RoleRepository> roleRepositories = new HashMap<String, RoleRepository>();

    public DatabaseRegistry() {
        this(DATABASE_DIR);
    }

    public DatabaseRegistry(final String databaseDir) {
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
        try {
            dbEnvironment = new Environment(dir, envConfig);
        } catch (EnvironmentLockedException e) {
            throw new PersistenceException(e);
        } catch (DatabaseException e) {
            throw new PersistenceException(e);
        }
        storeConfig = new StoreConfig();
        storeConfig.setAllowCreate(true);
        storeConfig.setDeferredWrite(true);
    }

    public synchronized ApplicationRepository getApplicationRepository(
            final String storeName) {
        ApplicationRepository repository = applicationRepositories
                .get(storeName);
        if (repository == null) {
            repository = new ApplicationRepositoryImpl(getStore(storeName));
            applicationRepositories.put(storeName, repository);
        }
        return repository;
    }

    public synchronized RoleRepository getRoleRepository(String storeName) {
        RoleRepository repository = roleRepositories.get(storeName);
        if (repository == null) {
            repository = new RoleRepositoryImpl(getStore(storeName));
            roleRepositories.put(storeName, repository);
        }
        return repository;
    }

    public synchronized PermissionRepository getPermissionRepository(
            final String storeName) {
        PermissionRepository repository = permissionRepositories.get(storeName);
        if (repository == null) {
            repository = new PermissionRepositoryImpl(getStore(storeName));
            permissionRepositories.put(storeName, repository);
        }
        return repository;
    }

    public synchronized SecurityErrorRepository getSecurityErrorRepository(
            final String storeName) {
        SecurityErrorRepository repository = securityErrorRepositories
                .get(storeName);
        if (repository == null) {
            repository = new SecurityErrorRepositoryImpl(getStore(storeName));
            securityErrorRepositories.put(storeName, repository);
        }
        return repository;
    }

    public synchronized UserRepository getUserRepository(final String storeName) {
        UserRepository repository = userRepositories.get(storeName);
        if (repository == null) {
            repository = new UserRepositoryImpl(getStore(storeName));
            userRepositories.put(storeName, repository);
        }
        return repository;

    }

    public synchronized EntityStore getStore(final String storeName) {
        EntityStore store = stores.get(storeName);
        if (store == null) {
            try {
                store = new EntityStore(dbEnvironment, storeName, storeConfig);
            } catch (IncompatibleClassException e) {
                throw new PersistenceException(e);
            } catch (DatabaseException e) {
                throw new PersistenceException(e);
            }
            stores.put(storeName, store);
        }
        return store;
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

    public void removeDatabase(final String storeName) {
        final Timing timer = Metric.newTiming(getClass().getName()
                + ".removeDatabase");
        try {
            dbEnvironment.removeDatabase(null, storeName);
        } catch (EnvironmentLockedException e) {
            throw new PersistenceException(e);
        } catch (DatabaseException e) {
            throw new PersistenceException(e);
        } finally {
            timer.stop();
        }
    }

    public void createDatabase(final String storeName) {
        final Timing timer = Metric.newTiming(getClass().getName()
                + ".createDatabase");
        try {
            DatabaseConfig dbconfig = new DatabaseConfig();
            dbconfig.setAllowCreate(true);
            dbconfig.setSortedDuplicates(false);
            dbconfig.setExclusiveCreate(false);
            dbconfig.setReadOnly(false);
            dbconfig.setTransactional(true);
            dbEnvironment.openDatabase(null, storeName, dbconfig);
        } catch (EnvironmentLockedException e) {
            throw new PersistenceException(e);
        } catch (DatabaseException e) {
            throw new PersistenceException(e);
        } finally {
            timer.stop();
        }
    }

    public synchronized void close(final String storeName) {
        EntityStore store = stores.get(storeName);
        if (store != null) {
            try {
                store.close();
            } catch (DatabaseException e) {
                throw new PersistenceException(e);
            }
        }
    }

    public void close() throws DatabaseException {
        if (dbEnvironment == null) {
            LOGGER.warn("already closed");
            return;
        }
        dbEnvironment.sync();
        dbEnvironment.close();
        dbEnvironment = null;
    }

}
