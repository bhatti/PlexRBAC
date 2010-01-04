package com.plexobject.rbac.security;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.plexobject.rbac.repository.PermissionRepository;
import com.plexobject.rbac.repository.RoleRepository;
import com.plexobject.rbac.repository.SecurityErrorRepository;
import com.plexobject.rbac.repository.SecurityRepository;
import com.plexobject.rbac.repository.UserRepository;
import com.plexobject.rbac.repository.bdb.SecurityRepositoryImpl;
import com.plexobject.rbac.domain.Permission;
import com.plexobject.rbac.domain.Role;
import com.plexobject.rbac.domain.User;
import com.plexobject.rbac.eval.js.JavascriptEvaluator;
import com.plexobject.rbac.utils.CurrentUserRequest;

public class PermissionManagerTest {
    private static final String USER_NAME = "shahbhat";
    private static final String APP_NAME = "appname";
    private static final String TEST_DB_DIR = "test_db_dir_perms";
    private SecurityRepository securityRegistry;
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PermissionRepository permissionRepository;
    private PermissionManager permissionManager;

    @Before
    public void setUp() throws Exception {
        FileUtils.deleteDirectory(new File(TEST_DB_DIR));

        CurrentUserRequest.startRequest(USER_NAME, "127.0.0.1");
        securityRegistry = new SecurityRepositoryImpl(TEST_DB_DIR);

        final SecurityErrorRepository securityErrorRepository = securityRegistry
                .getSecurityErrorRepository(APP_NAME);
        userRepository = securityRegistry.getUserRepository(APP_NAME);
        roleRepository = securityRegistry.getRoleRepository(APP_NAME);
        permissionRepository = securityRegistry
                .getPermissionRepository(APP_NAME);
        permissionManager = new PermissionManager(roleRepository,
                permissionRepository, securityErrorRepository,
                new JavascriptEvaluator());
        addPermissions();

    }

    @After
    public void tearDown() throws Exception {
        ((SecurityRepositoryImpl) securityRegistry).close("appname");
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
        userRepository.save(shahbhat);

        User bhatsha = new User("bhatsha");
        userRepository.save(bhatsha);
        //

        Role normal = new Role("normal");
        normal.addUser(shahbhat);
        roleRepository.save(normal);

        //
        Role admin = new Role("admin");
        admin.addUser(bhatsha);
        roleRepository.save(admin);

        Permission print = new Permission("print", "database",
                "company == 'plexobjects'");
        print.addRole(normal);
        permissionRepository.save(print);
        Permission crud = new Permission("(read|write|update|delete)",
                "database", "amount <= 500 && dept == 'sales'");
        crud.addRole(normal);

        permissionRepository.save(crud);
        Permission wild = new Permission("*", "*", "");
        wild.addRole(admin);
        permissionRepository.save(crud);
    }
}
