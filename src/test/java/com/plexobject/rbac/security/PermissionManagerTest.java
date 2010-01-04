package com.plexobject.rbac.security;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.plexobject.rbac.dao.PermissionDAO;
import com.plexobject.rbac.dao.RoleDAO;
import com.plexobject.rbac.dao.SecurityErrorDAO;
import com.plexobject.rbac.dao.UserDAO;
import com.plexobject.rbac.dao.bdb.DatabaseRegistry;
import com.plexobject.rbac.domain.Permission;
import com.plexobject.rbac.domain.Role;
import com.plexobject.rbac.domain.User;
import com.plexobject.rbac.eval.js.JavascriptEvaluator;
import com.plexobject.rbac.utils.CurrentUserRequest;

public class PermissionManagerTest {
    private static final String USER_NAME = "shahbhat";
    private static final String APP_NAME = "appname";
    private static final String TEST_DB_DIR = "test_db_dir_perms";
    private DatabaseRegistry databaseRegistry;
    private UserDAO userDAO;
    private RoleDAO roleDAO;
    private PermissionDAO permissionDAO;
    private PermissionManager permissionManager;

    @Before
    public void setUp() throws Exception {
        FileUtils.deleteDirectory(new File(TEST_DB_DIR));

        CurrentUserRequest.startRequest(USER_NAME, "127.0.0.1");
        databaseRegistry = new DatabaseRegistry(TEST_DB_DIR);

        final SecurityErrorDAO securityErrorDAO = databaseRegistry
                .getSecurityErrorDAO(APP_NAME);
        userDAO = databaseRegistry.getUserDAO(APP_NAME);
        roleDAO = databaseRegistry.getRoleDAO(APP_NAME);
        permissionDAO = databaseRegistry.getPermissionDAO(APP_NAME);
        permissionManager = new PermissionManager(roleDAO, permissionDAO,
                securityErrorDAO, new JavascriptEvaluator());
        addPermissions();

    }

    @After
    public void tearDown() throws Exception {
        databaseRegistry.close(APP_NAME);
        FileUtils.deleteDirectory(new File(TEST_DB_DIR));
        CurrentUserRequest.endRequest();
    }

    @Test
    public void testCheck() {
        permissionManager.check("read", "database", toMap("amount", "100",
                "dept", "sales"));
    }

    @Test(expected = SecurityException.class)
    public void testCheckBadOperation() {
        permissionManager.check("xread", "database", toMap("amount", "100",
                "dept", "sales"));
    }

    @Test(expected = SecurityException.class)
    public void testCheckBadDept() {
        permissionManager.check("read", "database", toMap("amount", "100",
                "dept", "xsales"));
    }

    @Test(expected = SecurityException.class)
    public void testCheckBadAmount() {
        permissionManager.check("read", "database", toMap("amount", "1000",
                "dept", "sales"));
    }

    private static Map<String, String> toMap(final String... keyValues) {
        Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < keyValues.length - 1; i += 2) {
            map.put(keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    private void addPermissions() {
        //
        User shahbhat = new User(USER_NAME);
        userDAO.save(shahbhat);

        User bhatsha = new User("bhatsha");
        userDAO.save(bhatsha);

        //

        Role normal = new Role("normal");
        normal.addUser(shahbhat);
        roleDAO.save(normal);

        //
        Role admin = new Role("admin");
        admin.addUser(bhatsha);
        roleDAO.save(admin);

        Permission print = new Permission("print", "database",
                "company == 'plexobjects'");
        print.addRole(normal);
        permissionDAO.save(print);
        Permission crud = new Permission("(read|write|update|delete)",
                "database", "amount <= 500 && dept == 'sales'");
        crud.addRole(normal);

        permissionDAO.save(crud);
        Permission wild = new Permission("*", "*", "");
        wild.addRole(admin);
        permissionDAO.save(crud);

    }
}
