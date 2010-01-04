package com.plexobject.rbac.dao.bdb;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.plexobject.rbac.Configuration;
import com.plexobject.rbac.dao.ApplicationDAO;
import com.plexobject.rbac.dao.PermissionDAO;
import com.plexobject.rbac.dao.PersistenceException;
import com.plexobject.rbac.dao.RoleDAO;
import com.plexobject.rbac.dao.SecurityErrorDAO;
import com.plexobject.rbac.dao.UserDAO;
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
    private Map<String, ApplicationDAO> applicationDAOs = new HashMap<String, ApplicationDAO>();
    private Map<String, PermissionDAO> permissionDAOs = new HashMap<String, PermissionDAO>();
    private Map<String, SecurityErrorDAO> securityErrorDAOs = new HashMap<String, SecurityErrorDAO>();
    private Map<String, UserDAO> userDAOs = new HashMap<String, UserDAO>();
    private Map<String, RoleDAO> roleDAOs = new HashMap<String, RoleDAO>();

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

    public synchronized ApplicationDAO getApplicationDAO(final String storeName) {
        ApplicationDAO dao = applicationDAOs.get(storeName);
        if (dao == null) {
            dao = new ApplicationDAOBDB(getStore(storeName));
            applicationDAOs.put(storeName, dao);
        }
        return dao;
    }

    public synchronized RoleDAO getRoleDAO(String storeName) {
        RoleDAO dao = roleDAOs.get(storeName);
        if (dao == null) {
            dao = new RoleDAOBDB(getStore(storeName));
            roleDAOs.put(storeName, dao);
        }
        return dao;
    }

    public synchronized PermissionDAO getPermissionDAO(final String storeName) {
        PermissionDAO dao = permissionDAOs.get(storeName);
        if (dao == null) {
            dao = new PermissionDAOBDB(getStore(storeName));
            permissionDAOs.put(storeName, dao);
        }
        return dao;
    }

    public synchronized SecurityErrorDAO getSecurityErrorDAO(
            final String storeName) {
        SecurityErrorDAO dao = securityErrorDAOs.get(storeName);
        if (dao == null) {
            dao = new SecurityErrorDAOBDB(getStore(storeName));
            securityErrorDAOs.put(storeName, dao);
        }
        return dao;
    }

    public synchronized UserDAO getUserDAO(final String storeName) {
        UserDAO dao = userDAOs.get(storeName);
        if (dao == null) {
            dao = new UserDAOBDB(getStore(storeName));
            userDAOs.put(storeName, dao);
        }
        return dao;

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
