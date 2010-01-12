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
import com.plexobject.rbac.domain.Subject;
import com.plexobject.rbac.eval.js.JavascriptEvaluator;
import com.plexobject.rbac.repository.RepositoryFactory;
import com.plexobject.rbac.repository.bdb.RepositoryFactoryImpl;
import com.plexobject.rbac.utils.CurrentRequest;

public class PermissionManagerTest {
    private static final String USER_NAME = "shahbhat";
    private static final String APP_NAME = "pappname";
    private static final String TEST_DB_DIR = "test_db_dir_perms";
    private RepositoryFactory repositoryFactory;
    private PermissionManager permissionManager;

    @Before
    public void setUp() throws Exception {
        FileUtils.deleteDirectory(new File(TEST_DB_DIR));

        CurrentRequest.startRequest(APP_NAME, USER_NAME, "127.0.0.1");
        repositoryFactory = new RepositoryFactoryImpl(TEST_DB_DIR);

        permissionManager = new PermissionManagerImpl(repositoryFactory,
                new JavascriptEvaluator());
        addPermissions();

    }

    @After
    public void tearDown() throws Exception {
        ((RepositoryFactoryImpl) repositoryFactory).close(APP_NAME);
        FileUtils.deleteDirectory(new File(TEST_DB_DIR));
        CurrentRequest.endRequest();
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
                "write", "database", toMap("amount", "1000", "dept", "sales")));
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
        Subject shahbhat = new Subject(USER_NAME, "credentials");
        repositoryFactory.getSubjectRepository(APP_NAME).save(shahbhat);

        Subject bhatsha = new Subject("bhatsha", "credentials");
        repositoryFactory.getSubjectRepository(APP_NAME).save(bhatsha);

        repositoryFactory.getDomainRepository().save(new Domain(APP_NAME, ""));

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
        repositoryFactory.getSecurityRepository().addRolesToSubject(APP_NAME,
                shahbhat.getId(), Arrays.asList("normal"));

        repositoryFactory.getSecurityRepository().addRolesToSubject(APP_NAME,
                bhatsha.getId(), Arrays.asList("admin"));

        repositoryFactory.getSecurityRepository().addPermissionsToRole(
                APP_NAME, anonymous.getId(), Arrays.asList(read.getId()));

        repositoryFactory.getSecurityRepository().addPermissionsToRole(
                APP_NAME, normal.getId(),
                Arrays.asList(print.getId(), crud.getId()));

        repositoryFactory.getSecurityRepository().addPermissionsToRole(
                APP_NAME, admin.getId(), Arrays.asList(wild.getId()));
    }
}
