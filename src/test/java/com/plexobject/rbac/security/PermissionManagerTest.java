package com.plexobject.rbac.security;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.plexobject.rbac.dao.PermissionDAO;
import com.plexobject.rbac.dao.SecurityErrorDAO;
import com.plexobject.rbac.dao.bdb.DatabaseRegistry;
import com.plexobject.rbac.domain.Permission;
import com.plexobject.rbac.eval.js.JavascriptEvaluator;
import com.plexobject.rbac.utils.CurrentUserRequest;

public class PermissionManagerTest {
    private static final String TEST_DB_DIR = "test_db_dir_perms";
    private DatabaseRegistry databaseRegistry;
    private PermissionDAO permissionDAO;
    private PermissionManager permissionManager;

    @Before
    public void setUp() throws Exception {
        FileUtils.deleteDirectory(new File(TEST_DB_DIR));

        CurrentUserRequest.startRequest("shahbhat", "127.0.0.1");
        databaseRegistry = new DatabaseRegistry(TEST_DB_DIR);

        final SecurityErrorDAO securityErrorDAO = databaseRegistry
                .getSecurityErrorDAO("appname");
        permissionDAO = databaseRegistry.getPermissionDAO("appname");
        permissionManager = new PermissionManager(permissionDAO,
                securityErrorDAO, new JavascriptEvaluator());
    }

    @After
    public void tearDown() throws Exception {
        databaseRegistry.close("appname");
        FileUtils.deleteDirectory(new File(TEST_DB_DIR));
        CurrentUserRequest.endRequest();
    }

    @Test(expected = SecurityException.class)
    public void testWithNoPermission() {
        permissionManager
                .check("read", toMap("amount", "100", "dept", "sales"));
    }

    @Test
    public void testCheck() {
        addPermission();

        permissionManager
                .check("read", toMap("amount", "100", "dept", "sales"));
    }

    @Test(expected = SecurityException.class)
    public void testCheckBadOperation() {
        addPermission();
        permissionManager.check("xread",
                toMap("amount", "100", "dept", "sales"));
    }

    @Test(expected = SecurityException.class)
    public void testCheckBadDept() {
        addPermission();
        permissionManager.check("read",
                toMap("amount", "100", "dept", "xsales"));
    }

    @Test(expected = SecurityException.class)
    public void testCheckBadAmount() {
        addPermission();

        permissionManager.check("read",
                toMap("amount", "1000", "dept", "sales"));
    }

    private static Map<String, String> toMap(final String... keyValues) {
        Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < keyValues.length - 1; i += 2) {
            map.put(keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    private void addPermission() {
        Permission permission = new Permission("print", "database",
                "company == 'plexobjects'");
        permissionDAO.save(permission);
        permission = new Permission("(read|write|update|delete)", "database",
                "amount <= 500 && dept == 'sales'");
        permissionDAO.save(permission);
    }

}
