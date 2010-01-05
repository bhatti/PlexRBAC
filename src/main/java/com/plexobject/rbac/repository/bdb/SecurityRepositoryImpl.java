package com.plexobject.rbac.repository.bdb;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;

import com.plexobject.rbac.Configuration;
import com.plexobject.rbac.domain.Domain;
import com.plexobject.rbac.domain.Permission;
import com.plexobject.rbac.domain.Role;
import com.plexobject.rbac.domain.User;
import com.plexobject.rbac.metric.Metric;
import com.plexobject.rbac.metric.Timing;
import com.plexobject.rbac.repository.DomainRepository;
import com.plexobject.rbac.repository.PermissionRepository;
import com.plexobject.rbac.repository.PersistenceException;
import com.plexobject.rbac.repository.RoleRepository;
import com.plexobject.rbac.repository.SecurityErrorRepository;
import com.plexobject.rbac.repository.SecurityRepository;
import com.plexobject.rbac.repository.UserRepository;
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

public class SecurityRepositoryImpl implements SecurityRepository {
    private static final Logger LOGGER = Logger
            .getLogger(SecurityRepositoryImpl.class);

    private static final String DATABASE_DIR = Configuration.getInstance()
            .getProperty("database.dir", "plexrbac");
    private static final String DEFAULT_DOMAIN = Configuration.getInstance()
            .getProperty("default.domain", "plexrbac");
    private Environment dbEnvironment;
    private StoreConfig storeConfig;
    private Map<String, EntityStore> stores = new HashMap<String, EntityStore>();
    private Map<String, DomainRepository> applicationRepositories = new HashMap<String, DomainRepository>();
    private Map<String, PermissionRepository> permissionRepositories = new HashMap<String, PermissionRepository>();
    private Map<String, SecurityErrorRepository> securityErrorRepositories = new HashMap<String, SecurityErrorRepository>();
    private Map<String, UserRepository> userRepositories = new HashMap<String, UserRepository>();
    private Map<String, RoleRepository> roleRepositories = new HashMap<String, RoleRepository>();
    private Map<String, Database> databases = new HashMap<String, Database>();
    private static ThreadLocal<Transaction> CURRENT_TXN = new ThreadLocal<Transaction>();

    //
    public SecurityRepositoryImpl() {
        this(DATABASE_DIR);
    }

    public SecurityRepositoryImpl(final String databaseDir) {
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

    @Override
    public synchronized DomainRepository getDomainRepository() {
        DomainRepository repository = applicationRepositories
                .get(DEFAULT_DOMAIN);
        if (repository == null) {
            repository = new DomainRepositoryImpl(getStore(DEFAULT_DOMAIN),
                    this);
            applicationRepositories.put(DEFAULT_DOMAIN, repository);
        }
        return repository;
    }

    @Override
    public Domain getDefaultDomain() {
        return getDomainRepository().getOrCreateDomain(DEFAULT_DOMAIN);
    }

    @Override
    public synchronized RoleRepository getRoleRepository(String domain) {
        if (GenericValidator.isBlankOrNull(domain)) {
            throw new IllegalArgumentException("domain is not specified");
        }
        RoleRepository repository = roleRepositories.get(domain);
        if (repository == null) {
            repository = new RoleRepositoryImpl(getStore(domain));
            roleRepositories.put(domain, repository);
        }
        return repository;
    }

    @Override
    public synchronized PermissionRepository getPermissionRepository(
            final String domain) {
        if (GenericValidator.isBlankOrNull(domain)) {
            throw new IllegalArgumentException("domain is not specified");
        }
        PermissionRepository repository = permissionRepositories.get(domain);
        if (repository == null) {
            repository = new PermissionRepositoryImpl(getStore(domain));
            permissionRepositories.put(domain, repository);
        }
        return repository;
    }

    @Override
    public synchronized SecurityErrorRepository getSecurityErrorRepository(
            final String domain) {
        if (GenericValidator.isBlankOrNull(domain)) {
            throw new IllegalArgumentException("domain is not specified");
        }
        SecurityErrorRepository repository = securityErrorRepositories
                .get(domain);
        if (repository == null) {
            repository = new SecurityErrorRepositoryImpl(getStore(domain));
            securityErrorRepositories.put(domain, repository);
        }
        return repository;
    }

    @Override
    public synchronized UserRepository getUserRepository(final String domain) {
        if (GenericValidator.isBlankOrNull(domain)) {
            throw new IllegalArgumentException("domain is not specified");
        }
        UserRepository repository = userRepositories.get(domain);
        if (repository == null) {
            repository = new UserRepositoryImpl(getStore(domain));
            userRepositories.put(domain, repository);
        }
        return repository;

    }

    @Override
    public void addPermissionsToRole(final String domain, final Role role,
            final Set<Permission> permissions) {
        if (GenericValidator.isBlankOrNull(domain)) {
            throw new IllegalArgumentException("domain is not specified");
        }
        if (role == null) {
            throw new IllegalArgumentException("role is not specified");
        }
        if (permissions == null || permissions.size() == 0) {
            throw new IllegalArgumentException("permissions not specified");
        }
        PermissionRepository repository = getPermissionRepository(domain);
        for (Permission permission : permissions) {
            permission.addRole(role);
            repository.save(permission);
        }
    }

    @Override
    public void addRolesToUser(String domain, User user, Set<Role> roles) {
        if (GenericValidator.isBlankOrNull(domain)) {
            throw new IllegalArgumentException("domain is not specified");
        }
        if (user == null) {
            throw new IllegalArgumentException("user is not specified");
        }
        if (roles == null || roles.size() == 0) {
            throw new IllegalArgumentException("roles not specified");
        }
        RoleRepository repository = getRoleRepository(domain);
        for (Role role : roles) {
            role.addUser(user);
            repository.save(role);
        }
    }

    @Override
    public void removePermissionsToRole(String domain, Role role,
            Set<Permission> permissions) {
        if (GenericValidator.isBlankOrNull(domain)) {
            throw new IllegalArgumentException("domain is not specified");
        }
        if (role == null) {
            throw new IllegalArgumentException("role is not specified");
        }
        if (permissions == null || permissions.size() == 0) {
            throw new IllegalArgumentException("permissions not specified");
        }
        PermissionRepository repository = getPermissionRepository(domain);
        for (Permission permission : permissions) {
            permission.removeRole(role);
            repository.save(permission);
        }
    }

    @Override
    public void removeRolesToUser(String domain, User user, Set<Role> roles) {
        if (GenericValidator.isBlankOrNull(domain)) {
            throw new IllegalArgumentException("domain is not specified");
        }
        if (user == null) {
            throw new IllegalArgumentException("user is not specified");
        }
        if (roles == null || roles.size() == 0) {
            throw new IllegalArgumentException("roles not specified");
        }
        RoleRepository repository = getRoleRepository(domain);
        for (Role role : roles) {
            role.removeUser(user);
            repository.save(role);
        }
    }

    //
    private synchronized EntityStore getStore(final String domain) {
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

    public synchronized void closeDefault() {
        close(DEFAULT_DOMAIN);
    }

    public synchronized void close(final String domain) {
        applicationRepositories.remove(domain);
        permissionRepositories.remove(domain);
        securityErrorRepositories.remove(domain);
        userRepositories.remove(domain);
        roleRepositories.remove(domain);

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
