package com.plexobject.rbac.web.filter;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.plexobject.rbac.Configuration;
import com.plexobject.rbac.domain.Subject;
import com.plexobject.rbac.utils.PasswordUtils;

public class AuthenticationFilterTest {
    AuthenticationFilter filter;

    @Before
    public void setUp() throws Exception {
        filter = new AuthenticationFilter();
        filter.setRepositoryFactory(filter.getRepositoryFactory());
    }

    @After
    public void tearDown() throws Exception {
        filter.destroy();
    }

    @Test
    public final void testDefaultDoFilter() throws ServletException,
            IOException {
        filter.init(null);
        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest(), res,
                new MockFilterChain());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, res.getStatus());
        assertEquals("BASIC realm=\"default\"", res
                .getHeader(AuthenticationFilter.AUTHENTICATE_HEADER));
    }

    @Test
    public final void testDoFilterWithBasicHeader() throws ServletException,
            IOException {
        filter.init(null);
        MockHttpServletRequest req = new MockHttpServletRequest();
        String password = PasswordUtils.byteToBase64(new String(
                Subject.SUPER_ADMIN.getId()
                        + ":"
                        + Configuration.getInstance().getProperty(
                                "super_admin_credentials", "changeme"))
                .getBytes());
        req.addHeader(AuthenticationFilter.AUTHORIZATION_HEADER, "Basic "
                + password);
        filter.doFilter(req, new MockHttpServletResponse(),
                new MockFilterChain());
    }

    @Test
    public final void testDoFilterWithBasicHeaderAndDomain()
            throws ServletException, IOException {
        filter.init(null);
        MockHttpServletRequest req = new MockHttpServletRequest();
        String password = PasswordUtils.byteToBase64(new String("default:"
                + Subject.SUPER_ADMIN.getId()
                + ":"
                + Configuration.getInstance().getProperty(
                        "super_admin_credentials", "changeme")).getBytes());
        req.addHeader(AuthenticationFilter.AUTHORIZATION_HEADER, "Basic "
                + password);
        filter.doFilter(req, new MockHttpServletResponse(),
                new MockFilterChain());
    }

    @Test(expected = SecurityException.class)
    public final void testDoFilterWithBasicHeaderAndDomainFailed()
            throws ServletException, IOException {
        filter.init(null);
        MockHttpServletRequest req = new MockHttpServletRequest();
        String password = PasswordUtils.byteToBase64(new String("domain:"
                + Subject.SUPER_ADMIN.getId()
                + ":"
                + Configuration.getInstance().getProperty(
                        "super_admin_credentials", "changeme")).getBytes());
        req.addHeader(AuthenticationFilter.AUTHORIZATION_HEADER, "Basic "
                + password);
        filter.doFilter(req, new MockHttpServletResponse(),
                new MockFilterChain());
    }
}
