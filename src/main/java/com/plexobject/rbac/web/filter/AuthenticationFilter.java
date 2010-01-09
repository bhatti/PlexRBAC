package com.plexobject.rbac.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.plexobject.rbac.ServiceFactory;
import com.plexobject.rbac.domain.User;
import com.plexobject.rbac.repository.RepositoryFactory;
import com.plexobject.rbac.service.impl.AuthenticationServiceImpl;
import com.plexobject.rbac.utils.CurrentUserRequest;
import com.plexobject.rbac.utils.PasswordUtils;
import com.plexobject.rbac.web.utils.WebUtils;
import com.sun.jersey.spi.inject.Inject;

public class AuthenticationFilter implements Filter {
    static final String AUTHORIZATION_HEADER = "Authorization";
    static final String AUTHENTICATE_HEADER = "WWW-Authenticate";
    private static final Logger LOGGER = Logger
            .getLogger(AuthenticationServiceImpl.class);

    private class AuthRequestWrapper extends HttpServletRequestWrapper {
        private final String domain;
        private final String username;

        public AuthRequestWrapper(final String domain, final String username,
                final HttpServletRequest request) {
            super(request);
            this.domain = domain;
            this.username = username;
        }

        public boolean isUserInRole(String role) {
            return repositoryFactory.getSecurityRepository().isUserInRole(
                    domain, username, role);
        }

        public String getRemoteUser() {
            return username;
        }
    }

    @Autowired
    @Inject
    RepositoryFactory repositoryFactory = ServiceFactory.getDefaultFactory();
    @SuppressWarnings("unused")
    private FilterConfig filterConfig;

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    public void destroy() {
        this.filterConfig = null;
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        boolean success = false;
        String domain = WebUtils.getDomain(req);
        String username = WebUtils.getUser(req);

        String authzHeader = req.getHeader(AUTHORIZATION_HEADER);
        if (GenericValidator.isBlankOrNull(authzHeader)) {
            if (WebUtils.verifySession(req.getCookies())) {
                success = true;
            }
        } else {
            String decoded = new String(PasswordUtils.base64ToByte(authzHeader));
            String[] prinCred = decoded.split(":");

            String password = null;
            if (prinCred.length == 3) {
                domain = prinCred[0];
                username = prinCred[1];
                password = prinCred[2];
            } else {
                username = prinCred[0];
                password = prinCred[1];
            }
            User user = repositoryFactory.getUserRepository(domain)
                    .authenticate(username, password);
            if (user != null) {
                success = true;
            }
        }

        if (success
                && !repositoryFactory.getDomainRepository().isUserOwner(domain,
                        username)) {
            LOGGER.warn("User " + username
                    + " is illegaly trying to access domain " + domain);
            success = false;
        }
        if (success) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Logged " + domain + ":" + username);
            }
            CurrentUserRequest.startRequest(domain, username, req
                    .getRemoteHost());
            chain.doFilter(new AuthRequestWrapper(domain, username, req),
                    response);
        } else {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            String authcHeader = "BASIC realm=\"" + domain + "\"";
            res.setHeader(AUTHENTICATE_HEADER, authcHeader);

        }
    }

    /**
     * @return the repositoryFactory
     */
    public RepositoryFactory getRepositoryFactory() {
        return repositoryFactory;
    }

    /**
     * @param repositoryFactory
     *            the repositoryFactory to set
     */
    public void setRepositoryFactory(RepositoryFactory repositoryFactory) {
        this.repositoryFactory = repositoryFactory;
    }

}
