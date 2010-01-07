package com.plexobject.rbac.security;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.plexobject.rbac.domain.Domain;
import com.plexobject.rbac.domain.Permission;
import com.plexobject.rbac.domain.Role;
import com.plexobject.rbac.domain.User;
import com.plexobject.rbac.eval.js.JavascriptEvaluator;
import com.plexobject.rbac.repository.RepositoryFactory;
import com.plexobject.rbac.repository.bdb.RepositoryFactoryImpl;
import com.plexobject.rbac.utils.CurrentUserRequest;

public class PermissionManagerTest {
    private static final String USER_NAME = "shahbhat";
    private static final String APP_NAME = "appname";
    private static final String TEST_DB_DIR = "test_db_dir_perms";
    private RepositoryFactory repositoryFactory;
    private PermissionManager permissionManager;

    @Before
    public void setUp() throws Exception {
        FileUtils.deleteDirectory(new File(TEST_DB_DIR));

        CurrentUserRequest.startRequest(USER_NAME, "127.0.0.1");
        repositoryFactory = new RepositoryFactoryImpl(TEST_DB_DIR);

        permissionManager = new PermissionManager(repositoryFactory,
                new JavascriptEvaluator());
        addPermissions();

    }

    @After
    public void tearDown() throws Exception {
        ((RepositoryFactoryImpl) repositoryFactory).close("appname");
        FileUtils.deleteDirectory(new File(TEST_DB_DIR));
        CurrentUserRequest.endRequest();
    }

    @Test
    public void testCheck() {
        permissionManager.check(new PermissionRequest(APP_NAME, USER_NAME,
                "read", "database", toMap("amount", "100", "dept", "sales")));
    }

    @Test(expected = SecurityException.class)
    public void testCheckBadOperation() {
        permissionManager.check(new PermissionRequest(APP_NAME, USER_NAME,
                "xread", "database", toMap("amount", "100", "dept", "sales")));
    }

    @Test(expected = SecurityException.class)
    public void testCheckBadDept() {
        permissionManager.check(new PermissionRequest(APP_NAME, USER_NAME,
                "write", "database", toMap("amount", "100", "dept", "xsales")));
    }

    @Test(expected = SecurityException.class)
    public void testCheckBadAmount() {
        permissionManager.check(new PermissionRequest(APP_NAME, USER_NAME,
                "read", "database", toMap("amount", "1000", "dept", "sales")));
    }

    private static Map<String, String> toMap(final String... keyValues) {
        Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < keyValues.length - 1; i += 2) {
            map.put(keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    private void addPermissions() {
        repositoryFactory.getDomainRepository().save(new Domain(APP_NAME, ""));

        //
        User shahbhat = new User(USER_NAME);
        repositoryFactory.getUserRepository(APP_NAME).save(shahbhat);

        User bhatsha = new User("bhatsha");
        repositoryFactory.getUserRepository(APP_NAME).save(bhatsha);
        //
        Role anonymous = new Role("anonymous");
        repositoryFactory.getRoleRepository(APP_NAME).save(anonymous);

        Role normal = new Role("normal", anonymous);
        repositoryFactory.getRoleRepository(APP_NAME).save(normal);

        Role admin = new Role("admin", normal);
        repositoryFactory.getRoleRepository(APP_NAME).save(admin);

        Permission read = new Permission("read", "database", null);
        repositoryFactory.getPermissionRepository(APP_NAME).save(read);

        Permission wild = new Permission("*", "*", "");
        repositoryFactory.getPermissionRepository(APP_NAME).save(wild);

        Permission crud = new Permission("(read|write|update|delete)",
                "database", "amount <= 500 && dept == 'sales'");
        repositoryFactory.getPermissionRepository(APP_NAME).save(crud);

        Permission print = new Permission("print", "database",
                "company == 'plexobjects'");
        repositoryFactory.getPermissionRepository(APP_NAME).save(print);

        //
        repositoryFactory.getSecurityRepository().addRolesToUser(APP_NAME,
                shahbhat.getID(), Arrays.asList("normal"));

        repositoryFactory.getSecurityRepository().addRolesToUser(APP_NAME,
                bhatsha.getID(), Arrays.asList("admin"));

        repositoryFactory.getSecurityRepository().addPermissionsToRole(
                APP_NAME, anonymous.getID(), Arrays.asList(read.getID()));

        repositoryFactory.getSecurityRepository().addPermissionsToRole(
                APP_NAME, normal.getID(),
                Arrays.asList(print.getID(), crud.getID()));

        repositoryFactory.getSecurityRepository().addPermissionsToRole(
                APP_NAME, admin.getID(), Arrays.asList(wild.getID()));
    }
}
