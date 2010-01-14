package com.plexobject.rbac.service.impl;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.plexobject.rbac.ServiceFactory;
import com.plexobject.rbac.domain.Domain;
import com.plexobject.rbac.domain.Permission;
import com.plexobject.rbac.domain.Role;
import com.plexobject.rbac.domain.Subject;
import com.plexobject.rbac.security.PermissionManager;
import com.plexobject.rbac.security.PermissionRequest;
import com.plexobject.rbac.service.AuthorizationService;
import com.plexobject.rbac.utils.CurrentRequest;

public class AuthorizationServiceImplTest {
    private static final Logger LOGGER = Logger
            .getLogger(AuthorizationServiceImplTest.class);
    private static final String USER_NAME = "shahbhat";
    private static final String APP_NAME = "xappname";

    class TestMultivaluedMap extends HashMap<String, List<String>> implements
            MultivaluedMap<String, String> {

        private static final long serialVersionUID = 1L;

        @Override
        public void add(String key, String value) {
            put(key, Arrays.asList(value));
        }

        @Override
        public String getFirst(String key) {
            return get(key).get(0);
        }

        @Override
        public void putSingle(String key, String value) {
            List<String> l = get(key);
            if (l == null) {
                l = new ArrayList<String>();
            }
            l.add(value);
            put(key, l);
        }
    };

    class TestUriInfo implements UriInfo {

        @Override
        public URI getAbsolutePath() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public UriBuilder getAbsolutePathBuilder() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public URI getBaseUri() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public UriBuilder getBaseUriBuilder() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public List<Object> getMatchedResources() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public List<String> getMatchedURIs() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public List<String> getMatchedURIs(boolean arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getPath() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getPath(boolean arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public MultivaluedMap<String, String> getPathParameters() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public MultivaluedMap<String, String> getPathParameters(boolean arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public List<PathSegment> getPathSegments() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public List<PathSegment> getPathSegments(boolean arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public MultivaluedMap<String, String> getQueryParameters() {
            return new TestMultivaluedMap();
        }

        @Override
        public MultivaluedMap<String, String> getQueryParameters(boolean arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public URI getRequestUri() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public UriBuilder getRequestUriBuilder() {
            // TODO Auto-generated method stub
            return null;
        }
    };

    AuthorizationService service;
    TestMultivaluedMap map;

    @Before
    public void setUp() throws Exception {
        CurrentRequest.startRequest(APP_NAME, USER_NAME, "127.0.0.1");

        map = new TestMultivaluedMap();
        service = new AuthorizationServiceImpl();
        ((AuthorizationServiceImpl) service)
                .setPermissionManager(((AuthorizationServiceImpl) service)
                        .getPermissionManager());
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public final void testAuthorizeFailed() throws Exception {
        ((AuthorizationServiceImpl) service).afterPropertiesSet();
        String domain = "default";
        String operation = "op";
        String target = "xx";
        UriInfo ui = new TestUriInfo();
        Response response = service.authorize(ui, domain, operation, target);

        assertEquals(401, response.getStatus());
        assertEquals("denied", response.getEntity());
    }

    @Test
    public final void testAuthorize() throws Exception {
        PermissionManager mgr = EasyMock.createMock(PermissionManager.class);
        ((AuthorizationServiceImpl) service).setPermissionManager(mgr);
        String domain = "mydomain";
        String operation = "myoperation";
        String target = "mytarget";
        UriInfo ui = new TestUriInfo();
        PermissionRequest request = new PermissionRequest(domain, "shahbhat",
                operation, target, null);
        mgr.check(request);
        EasyMock.expectLastCall();
        EasyMock.replay(mgr);
        Response response = service.authorize(ui, domain, operation, target);
        assertEquals("granted", response.getEntity());

        assertEquals(200, response.getStatus());
        EasyMock.verify(mgr);

        // String domain = "mine";
        // String operation = "(read|write|delete)";
        // String target = "database";
        // String expr = "";
        // Role role = new Role("accountant");
        // UriInfo ui = new TestUriInfo();
        // Subject user = new Subject(USER_NAME, "");
        // Domain d = ServiceFactory.getDefaultFactory().getDomainRepository()
        // .getOrCreateDomain(domain);
        // d.addOwner(USER_NAME);
        // ServiceFactory.getDefaultFactory().getDomainRepository().save(d);
        // ServiceFactory.getDefaultFactory().getSubjectRepository(domain).save(
        // user);
        // ServiceFactory.getDefaultFactory().getRoleRepository(domain).save(role);
        // Permission perm = new Permission(operation, target, expr);
        //
        // ServiceFactory.getDefaultFactory().getPermissionRepository(domain)
        // .save(perm);
        // ServiceFactory.getDefaultFactory().getSecurityRepository()
        // .addRolesToSubject(domain, user.getId(),
        // Arrays.asList(role.getId()));
        // ServiceFactory.getDefaultFactory().getSecurityRepository()
        // .addPermissionsToRole(domain, role.getId(),
        // Arrays.asList(perm.getId()));
        // Response response = service.authorize(ui, domain, operation, target);
        //
        // assertEquals(200, response.getStatus());
        // assertEquals("denied", response.getEntity());
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testNoUri() throws Exception {
        ((AuthorizationServiceImpl) service).afterPropertiesSet();
        String domain = "default";
        String operation = "op";
        String target = "xxx";
        UriInfo ui = null;
        Response response = service.authorize(ui, domain, operation, target);
        assertEquals(500, response.getStatus());
    }

    @Test
    public final void testNoDomain() throws Exception {
        ((AuthorizationServiceImpl) service).afterPropertiesSet();
        String domain = "";
        String operation = "";
        String target = "";
        UriInfo ui = new TestUriInfo();
        Response response = service.authorize(ui, domain, operation, target);
        assertEquals(400, response.getStatus());
        assertEquals("domain not specified", response.getEntity());
    }

    @Test
    public final void testNoOperation() throws Exception {
        ((AuthorizationServiceImpl) service).afterPropertiesSet();
        String domain = "xxx";
        String operation = "";
        String target = "";
        UriInfo ui = new TestUriInfo();
        Response response = service.authorize(ui, domain, operation, target);
        assertEquals(400, response.getStatus());
        assertEquals("operation not specified", response.getEntity());
    }

    @Test
    public final void testNoTarget() throws Exception {
        ((AuthorizationServiceImpl) service).afterPropertiesSet();
        String domain = "xx";
        String operation = "xx";
        String target = "";
        UriInfo ui = new TestUriInfo();
        Response response = service.authorize(ui, domain, operation, target);
        assertEquals(400, response.getStatus());
        assertEquals("target not specified", response.getEntity());
    }

    @Test
    public final void testMockFailed() throws Exception {
        PermissionManager mgr = EasyMock.createMock(PermissionManager.class);
        ((AuthorizationServiceImpl) service).setPermissionManager(mgr);
        String domain = "mydomain";
        String operation = "myoperation";
        String target = "mytarget";
        UriInfo ui = new TestUriInfo();
        PermissionRequest request = new PermissionRequest(domain, "shahbhat",
                operation, target, null);
        mgr.check(request);
        EasyMock.expectLastCall().andThrow(new RuntimeException());
        EasyMock.replay(mgr);
        Response response = service.authorize(ui, domain, operation, target);
        assertEquals("denied", response.getEntity());

        assertEquals(500, response.getStatus());
        EasyMock.verify(mgr);

    }
}
