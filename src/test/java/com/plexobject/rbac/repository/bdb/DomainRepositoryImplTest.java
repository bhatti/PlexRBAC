package com.plexobject.rbac.repository.bdb;

import java.io.File;
import java.util.Collection;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.plexobject.rbac.repository.DomainRepository;
import com.plexobject.rbac.repository.SecurityRepository;
import com.plexobject.rbac.domain.Domain;
import com.plexobject.rbac.utils.CurrentUserRequest;

public class DomainRepositoryImplTest {
    private static final String TEST_DB_DIR = "test_db_dirx";

    private static final Logger LOGGER = Logger
            .getLogger(DomainRepositoryImplTest.class);

    private DomainRepository repository;
    private SecurityRepository securityRegistry;

    @Before
    public void setUp() throws Exception {
        new File(TEST_DB_DIR, "je.lck").delete();
        FileUtils.deleteDirectory(new File(TEST_DB_DIR));

        CurrentUserRequest.startRequest("shahbhat", "127.0.0.1");
        securityRegistry = new SecurityRepositoryImpl(TEST_DB_DIR);
        repository = securityRegistry.getDomainRepository();
    }

    @After
    public void tearDown() throws Exception {
        ((SecurityRepositoryImpl) securityRegistry).closeDefault();
        new File(TEST_DB_DIR, "je.lck").delete();
        FileUtils.deleteDirectory(new File(TEST_DB_DIR));
        CurrentUserRequest.endRequest();
    }

    @Test
    public void testRemove() {
        final String username = "user " + System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            Domain app = new Domain("name" + i, username);
            repository.save(app);
        }
        for (int i = 0; i < 10; i++) {
            LOGGER.info("deleting name" + i);
            Assert.assertTrue(repository.remove("name" + i));
        }
    }

    @Test
    public void testFindAll() {
        try {
            Collection<Domain> all = repository.findAll(null, 100);
            Assert.assertEquals(0, all.size());

            final String username = "user " + System.currentTimeMillis();
            for (int i = 0; i < 10; i++) {
                Domain app = new Domain("name" + i, username);
                repository.save(app);
            }
            all = repository.findAll(null, 10);
            for (Domain app : all) {
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
            Domain app = new Domain("name" + i, username);
            repository.save(app);
        }
        for (int i = 0; i < 10; i++) {
            Domain app = repository.findByID("name" + i);
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
            Domain app = new Domain("name" + i, username);
            repository.save(app);
        }
        for (int i = 0; i < 10; i++) {
            Domain app = repository.findByID("name" + i);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("app " + app);
            }
            Assert.assertNotNull(app);
        }
    }

}
