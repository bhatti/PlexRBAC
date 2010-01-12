package com.plexobject.rbac.service.impl;

import static org.junit.Assert.*;

import javax.ws.rs.core.Response;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.plexobject.rbac.Configuration;
import com.plexobject.rbac.domain.Subject;
import com.plexobject.rbac.repository.RepositoryFactory;
import com.plexobject.rbac.service.AuthenticationService;

public class AuthenticationServiceImplTest {
    AuthenticationService service;

    @Before
    public void setUp() throws Exception {
        service = new AuthenticationServiceImpl();
        ((AuthenticationServiceImpl) service)
                .setRepositoryFactory(((AuthenticationServiceImpl) service)
                        .getRepositoryFactory());
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public final void testAuthenticateNullDomain() throws Exception {
        ((AuthenticationServiceImpl) service).afterPropertiesSet();
        String domain = null;
        String subjectName = Subject.SUPER_ADMIN.getId();
        String credentials = Configuration.getInstance().getProperty(
                "super_admin_credentials", "changeme");
        Response response = service.authenticate(domain, subjectName,
                credentials);
        assertEquals(400, response.getStatus());
    }

    @Test
    public final void testAuthenticateNullSubject() throws Exception {
        ((AuthenticationServiceImpl) service).afterPropertiesSet();
        String domain = "default";
        String subjectName = "";
        String credentials = Configuration.getInstance().getProperty(
                "super_admin_credentials", "changeme");
        Response response = service.authenticate(domain, subjectName,
                credentials);
        assertEquals(400, response.getStatus());
    }

    @Test
    public final void testAuthenticateNullCredentials() throws Exception {
        ((AuthenticationServiceImpl) service).afterPropertiesSet();
        String domain = "default";
        String subjectName = Subject.SUPER_ADMIN.getId();
        String credentials = "";
        Response response = service.authenticate(domain, subjectName,
                credentials);
        assertEquals(400, response.getStatus());
    }

    @Test
    public final void testAuthenticate() throws Exception {
        ((AuthenticationServiceImpl) service).afterPropertiesSet();
        String domain = "default";
        String subjectName = Subject.SUPER_ADMIN.getId();
        String credentials = Configuration.getInstance().getProperty(
                "super_admin_credentials", "changeme");
        Response response = service.authenticate(domain, subjectName,
                credentials);
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    public final void testAuthenticateFailed() throws Exception {
        ((AuthenticationServiceImpl) service).afterPropertiesSet();
        String domain = "default";
        String subjectName = Subject.SUPER_ADMIN.getId();
        String credentials = Configuration.getInstance().getProperty(
                "super_admin_credentials", "xchangeme");
        Response response = service.authenticate(domain, subjectName,
                credentials);
        assertEquals(401, response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    public final void testAuthenticateMockFailed() throws Exception {
        RepositoryFactory factory = EasyMock
                .createMock(RepositoryFactory.class);
        ((AuthenticationServiceImpl) service).setRepositoryFactory(factory);
        ((AuthenticationServiceImpl) service).afterPropertiesSet();
        String domain = "default";
        String subjectName = Subject.SUPER_ADMIN.getId();
        String credentials = Configuration.getInstance().getProperty(
                "super_admin_credentials", "xchangeme");
        EasyMock.expect(factory.getSubjectRepository("default")).andThrow(
                new RuntimeException());

        EasyMock.replay(factory);

        Response response = service.authenticate(domain, subjectName,
                credentials);
        EasyMock.verify(factory);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test(expected = IllegalStateException.class)
    public final void testAfterPropertiesSet() throws Exception {
        ((AuthenticationServiceImpl) service).setRepositoryFactory(null);

        ((AuthenticationServiceImpl) service).afterPropertiesSet();
    }
}
