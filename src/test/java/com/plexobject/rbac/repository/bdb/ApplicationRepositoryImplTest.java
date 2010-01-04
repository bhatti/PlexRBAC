package com.plexobject.rbac.repository.bdb;

import java.io.File;
import java.util.Collection;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.plexobject.rbac.repository.ApplicationRepository;
import com.plexobject.rbac.domain.Application;
import com.plexobject.rbac.utils.CurrentUserRequest;

public class ApplicationRepositoryImplTest {
    private static final String TEST_DB_DIR = "test_db_dirx";

    private static final Logger LOGGER = Logger
            .getLogger(ApplicationRepositoryImplTest.class);

    private ApplicationRepository repository;
    private DatabaseRegistry databaseRegistry;

    @Before
    public void setUp() throws Exception {
        FileUtils.deleteDirectory(new File(TEST_DB_DIR));

        CurrentUserRequest.startRequest("shahbhat", "127.0.0.1");
        databaseRegistry = new DatabaseRegistry(TEST_DB_DIR);
        repository = databaseRegistry.getApplicationRepository("appname");
    }

    @After
    public void tearDown() throws Exception {
        databaseRegistry.close("appname");
        FileUtils.deleteDirectory(new File(TEST_DB_DIR));
        CurrentUserRequest.endRequest();
    }

    @Test
    public void testFindAll() {
        try {
            Collection<Application> all = repository.findAll(null, 100);
            Assert.assertEquals(0, all.size());

            final String username = "user " + System.currentTimeMillis();
            for (int i = 0; i < 10; i++) {
                Application app = new Application("name" + i, username);
                repository.save(app);
            }
            all = repository.findAll(null, 10);
            for (Application app : all) {
                Assert.assertTrue(app.getID().startsWith("name"));
            }
            Assert.assertEquals(10, all.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFindByName() {
        final String username = "user " + System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            Application app = new Application("name" + i, username);
            repository.save(app);
        }
        for (int i = 0; i < 10; i++) {
            Application app = repository.findByID("name" + i);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("app " + app);
            }
            Assert.assertNotNull(app);
        }
    }

    @Test
    public void testFindByID() {
        final String username = "user " + System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            Application app = new Application("name" + i, username);
            repository.save(app);
        }
        for (int i = 0; i < 10; i++) {
            Application app = repository.findByID("name" + i);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("app " + app);
            }
            Assert.assertNotNull(app);
        }
    }

    @Test
    public void testRemove() {
        final String username = "user " + System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            Application app = new Application("name" + i, username);
            repository.save(app);
        }
        for (int i = 0; i < 10; i++) {
            Assert.assertTrue(repository.remove("name" + i));
        }
    }
}
