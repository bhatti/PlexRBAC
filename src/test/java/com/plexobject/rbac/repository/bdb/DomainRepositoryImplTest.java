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
import com.plexobject.rbac.repository.PersistenceException;
import com.plexobject.rbac.repository.RepositoryFactory;
import com.plexobject.rbac.domain.Domain;
import com.plexobject.rbac.domain.Subject;
import com.plexobject.rbac.utils.CurrentRequest;

public class DomainRepositoryImplTest {
    private static final String DOMAIN = "domain";

    private static final String USERNAME = "shahbhat";

    private static final String TEST_DB_DIR = "test_db_dirx";

    private static final Logger LOGGER = Logger
            .getLogger(DomainRepositoryImplTest.class);

    private DomainRepository repository;
    private RepositoryFactory repositoryFactory;

    @Before
    public void setUp() throws Exception {
        new File(TEST_DB_DIR, "je.lck").delete();
        FileUtils.deleteDirectory(new File(TEST_DB_DIR));

        CurrentRequest.startRequest(DOMAIN, USERNAME, "127.0.0.1");
        repositoryFactory = new RepositoryFactoryImpl(TEST_DB_DIR);
        repository = repositoryFactory.getDomainRepository();
        repositoryFactory.getDefaultSubjectRepository().getOrCreateSubject(
                new Subject(USERNAME, "pass"));
    }

    @After
    public void tearDown() throws Exception {
        if (repositoryFactory != null) {
            ((RepositoryFactoryImpl) repositoryFactory).closeDefault();
        }
        new File(TEST_DB_DIR, "je.lck").delete();
        FileUtils.deleteDirectory(new File(TEST_DB_DIR));
        CurrentRequest.endRequest();
    }

    @Test
    public void testRemove() {
        final String subjectName = "subject " + System.currentTimeMillis();

        for (int i = 0; i < 10; i++) {
            repositoryFactory.getSubjectRepository("name" + i)
                    .getOrCreateSubject(new Subject(USERNAME, "pass"));
            Domain app = new Domain("name" + i, subjectName);
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
            Collection<Domain> all = repository.findAll(null, -1);
            Assert.assertEquals(1, all.size()); // default domain

            final String subjectName = "subject " + System.currentTimeMillis();
            for (int i = 0; i < 10; i++) {
                repositoryFactory.getSubjectRepository("name" + i)
                        .getOrCreateSubject(new Subject(USERNAME, "pass"));
                Domain app = new Domain("name" + i, subjectName);
                repository.save(app);
            }
            all = repository.findAll(null, 100);
            for (Domain app : all) {
                Assert.assertTrue(app.getId().startsWith("name")
                        || app.getId().startsWith(Domain.DEFAULT_DOMAIN_NAME));
            }
            Assert.assertEquals(11, all.size()); // 10 + default
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFindByName() {
        final String subjectName = "subject " + System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            repositoryFactory.getSubjectRepository("name" + i)
                    .getOrCreateSubject(new Subject(USERNAME, "pass"));
            Domain app = new Domain("name" + i, subjectName);
            repository.save(app);
        }
        for (int i = 0; i < 10; i++) {
            Domain app = repository.findById("name" + i);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("app " + app);
            }
            Assert.assertNotNull(app);
        }
    }

    @Test
    public void testFindById() {
        final String subjectName = "subject " + System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            repositoryFactory.getSubjectRepository("name" + i)
                    .getOrCreateSubject(new Subject(USERNAME, "pass"));
            Domain app = new Domain("name" + i, subjectName);
            repository.save(app);
        }
        for (int i = 0; i < 10; i++) {
            Domain app = repository.findById("name" + i);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("app " + app);
            }
            Assert.assertNotNull(app);
        }
    }

    @Test(expected = PersistenceException.class)
    public void testClose() {
        ((DomainRepositoryImpl) repository).close();
        repository.findAll(null, 100); // should fail
    }

}
