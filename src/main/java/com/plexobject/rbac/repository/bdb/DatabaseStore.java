package com.plexobject.rbac.repository.bdb;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;

import com.plexobject.rbac.Configuration;
import com.plexobject.rbac.metric.Metric;
import com.plexobject.rbac.metric.Timing;
import com.plexobject.rbac.repository.PersistenceException;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.DeadlockException;
import com.sleepycat.je.Durability;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentLockedException;
import com.sleepycat.je.Transaction;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;
import com.sleepycat.persist.evolve.IncompatibleClassException;

public class DatabaseStore {
    private static final Logger LOGGER = Logger.getLogger(DatabaseStore.class);
    private static final String DATABASE_DIR = Configuration.getInstance()
            .getProperty("database.dir", "plexrbac");

    private Environment dbEnvironment;
    private StoreConfig storeConfig;
    private Map<String, EntityStore> stores = new HashMap<String, EntityStore>();
    private Map<String, Database> databases = new HashMap<String, Database>();

    private static ThreadLocal<Transaction> CURRENT_TXN = new ThreadLocal<Transaction>();

    public DatabaseStore() {
        this(DATABASE_DIR);
    }

    public DatabaseStore(final String databaseDir) {
        if (GenericValidator.isBlankOrNull(databaseDir)) {
            throw new IllegalArgumentException("databaseDir is not specified");
        }
        Durability defaultDurability = new Durability(
                Durability.SyncPolicy.SYNC, null, // unused by non-HA
                // applications.
                null); // unused by non-HA applications.

        // Open the DB environment. Create if they do not already exist.
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(true);
        envConfig.setTransactional(true);
        envConfig.setSharedCache(true);
        envConfig.setDurability(defaultDurability);
        // envConfig.setReadOnly(true);
        // envConfig.setTxnTimeout(1000000);
        final File dir = new File(databaseDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        //
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

    //
    synchronized EntityStore getStore(final String domain) {
        if (GenericValidator.isBlankOrNull(domain)) {
            throw new IllegalArgumentException("domain is not specified");
        }
        EntityStore store = stores.get(domain);
        if (store == null) {
            try {
                store = new EntityStore(dbEnvironment, domain, storeConfig);
            } catch (IncompatibleClassException e) {
                throw new PersistenceException(e);
            } catch (DatabaseException e) {
                throw new PersistenceException(e);
            }
            stores.put(domain, store);
        }
        return store;
    }

    Collection<String> getAllDatabases() throws PersistenceException {
        final Timing timer = Metric.newTiming(getClass().getName()
                + ".getAllDatabases");
        try {
            return dbEnvironment.getDatabaseNames();
        } catch (EnvironmentLockedException e) {
            throw new PersistenceException(e);
        } catch (DatabaseException e) {
            throw new PersistenceException(e);
        } finally {
            timer.stop();
        }
    }

    void removeDatabase(final String domain) {
        final Timing timer = Metric.newTiming(getClass().getName()
                + ".removeDatabase");
        close(domain);
        try {
            Database db = databases.get(domain);
            if (db != null) {
                db.close();
            }
            dbEnvironment.removeDatabase(null, domain);
        } catch (DeadlockException e) {
            LOGGER.error("Failed to remove database " + domain + " - " + e);
        } catch (EnvironmentLockedException e) {
            throw new PersistenceException("Failed to remove database "
                    + domain, e);
        } catch (DatabaseException e) {
            throw new PersistenceException("Failed to remove database "
                    + domain, e);
        } finally {
            timer.stop();
        }
    }

    void createDatabase(final String domain) {
        final Timing timer = Metric.newTiming(getClass().getName()
                + ".createDatabase");
        try {
            DatabaseConfig dbconfig = new DatabaseConfig();
            dbconfig.setAllowCreate(true);
            dbconfig.setSortedDuplicates(false);
            dbconfig.setExclusiveCreate(false);
            dbconfig.setReadOnly(false);
            dbconfig.setTransactional(true);
            Database db = dbEnvironment.openDatabase(null, domain, dbconfig);
            databases.put(domain, db);
            getStore(domain);
        } catch (EnvironmentLockedException e) {
            throw new PersistenceException(e);
        } catch (DatabaseException e) {
            throw new PersistenceException(e);
        } finally {
            timer.stop();
        }
    }

    synchronized void close(final String domain) {
        EntityStore store = stores.remove(domain);
        if (store != null) {
            try {
                store.close();
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Closed database " + domain);
                }
            } catch (IllegalStateException e) {
                // already closed
                LOGGER.warn("Aready closed database " + domain + ": " + e);
            } catch (DatabaseException e) {
                throw new PersistenceException(e);
            }
        }
    }

    void close() throws DatabaseException {
        if (dbEnvironment == null) {
            LOGGER.warn("already closed");
            return;
        }
        dbEnvironment.sync();
        dbEnvironment.close();
        dbEnvironment = null;
    }

    void beginTransaction() {
        if (CURRENT_TXN.get() != null) {
            throw new IllegalStateException("Already in transaction");
        }
        try {
            CURRENT_TXN.set(dbEnvironment.beginTransaction(null, null));
        } catch (DatabaseException e) {
            throw new PersistenceException(e);
        }
    }

    void commitTransaction() {
        Transaction txn = CURRENT_TXN.get();
        try {
            if (txn != null) {
                txn.commit();
            }
        } catch (DatabaseException e) {
            throw new PersistenceException(e);
        } finally {
            CURRENT_TXN.set(null);
        }
    }

    void abortTransaction() {
        Transaction txn = CURRENT_TXN.get();
        try {
            if (txn != null) {
                txn.abort();
            }
        } catch (DatabaseException e) {
            throw new PersistenceException(e);
        } finally {
            CURRENT_TXN.set(null);
        }
    }
}
