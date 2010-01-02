package com.plexobject.rbac.dao.bdb;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.plexobject.rbac.domain.Application;
import com.plexobject.rbac.domain.Permission;
import com.plexobject.rbac.utils.CurrentUserRequest;

public class PermissionDAOBDBTest {
    private static final Logger LOGGER = Logger
            .getLogger(PermissionDAOBDBTest.class);
    private ApplicationDAOBDB appDAO;

    private PermissionDAOBDB permissionDAO;

    @Before
    public void setUp() throws Exception {
        CurrentUserRequest.startRequest("shahbhat", "127.0.0.1");

        FileUtils.deleteDirectory(new File("test_db_dir"));
        appDAO = new ApplicationDAOBDB("test_db_dir", "permissions");
        permissionDAO = new PermissionDAOBDB("test_db_dir", "permissions");
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
    public void testGetAllDatabases() {
    }

    @Test
    public void testRemoveDatabase() {
    }

    @Test
    public void testCreateDatabase() {
    }

    @Test
    public void testFindAll() {
        final Application app = new Application("app", "username");
        appDAO.save(app);
        List<Permission> saved = new ArrayList<Permission>();

        for (int i = 0; i < 10; i++) {
            String operation = null;
            if (i == 0) {
                operation = "(read|write|update|delete)";
            } else if (i == 10 - 1) {
                operation = "*";
            } else if (i % 2 == 0) {
                operation = "read";
            } else {
                operation = "(read|write)";
            }
            Permission permission = new Permission(app.getName(), operation,
                    "database",
                    "amount <= 500 && dept == 'sales' && time between 8:00am..5:00pm");
            LOGGER.debug("Saving " + permission);
            permissionDAO.save(permission);
            saved.add(permission);

        }

        Iterator<Permission> it = permissionDAO.findAll();
        int count = 0;
        while (it.hasNext()) {
            Permission permission = it.next();
            Assert.assertEquals("expected " + saved.get(count) + ", but was "
                    + permission, permission, saved.get(count));
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("permission " + permission);
            }
            count++;
        }
        Assert.assertEquals(10, count);
    }

    @Test
    public void testFindByID() {
    }

    @Test
    public void testRemove() {
    }

    @Test
    public void testSave() {
    }

}
