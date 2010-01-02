package com.plexobject.rbac.security;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.plexobject.rbac.dao.bdb.ApplicationDAOBDB;
import com.plexobject.rbac.dao.bdb.PermissionDAOBDB;
import com.plexobject.rbac.domain.Application;
import com.plexobject.rbac.domain.Permission;
import com.plexobject.rbac.eval.simple.SimpleEvaluator;
import com.plexobject.rbac.utils.CurrentUserRequest;

public class PermissionManagerTest {
    private static final Logger LOGGER = Logger
            .getLogger(PermissionManagerTest.class);

    private ApplicationDAOBDB appDAO;

    private PermissionDAOBDB permissionDAO;
    private PermissionManager permissionManager;

    @Before
    public void setUp() throws Exception {
        CurrentUserRequest.startRequest("shahbhat", "127.0.0.1");

        FileUtils.deleteDirectory(new File("test_db_dir"));
        appDAO = new ApplicationDAOBDB("test_db_dir", "db");
        permissionDAO = new PermissionDAOBDB("test_db_dir", "db");
        permissionManager = new PermissionManager(permissionDAO,
                new SimpleEvaluator());

    }

    @After
    public void tearDown() throws Exception {
        if (permissionDAO != null) {
            permissionDAO.close();
        }
        if (appDAO != null) {
            appDAO.close();
        }
        FileUtils.deleteDirectory(new File("test_db_dir"));
        CurrentUserRequest.endRequest();
    }

    @Test
    public void testCheck() {
        final Application app = addPermissions();
        permissionManager.check(app, "read", toMap("amount", "100", "time",
                "12:00pm", "dept", "SALES"));
    }

    @Test(expected = SecurityException.class)
    public void testCheckBadOperation() {
        final Application app = addPermissions();
        permissionManager.check(app, "xread", toMap("amount", "100", "time",
                "12:00pm", "dept", "SALES"));
    }

    private Application addPermissions() {
        final Application app = new Application("app", "username");
        appDAO.save(app);
        for (int i = 0; i < 2; i++) {
            String operation = "(read|write|update|delete)";

            Permission permission = new Permission(app.getName(), operation,
                    "database",
                    "amount <= 500 && dept == 'sales' && time between 8:00am..5:00pm");
            LOGGER.info("Saving " + permission);
            permissionDAO.save(permission);
        }
        return app;
    }

    private static Map<String, String> toMap(final String... keyValues) {
        Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < keyValues.length - 1; i += 2) {
            map.put(keyValues[i], keyValues[i + 1]);
        }
        return map;
    }
}
