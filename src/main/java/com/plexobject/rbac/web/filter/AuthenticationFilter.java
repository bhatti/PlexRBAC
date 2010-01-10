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
import com.plexobject.rbac.domain.Tuple;
import com.plexobject.rbac.domain.Subject;
import com.plexobject.rbac.repository.RepositoryFactory;
import com.plexobject.rbac.service.impl.AuthenticationServiceImpl;
import com.plexobject.rbac.utils.CurrentRequest;
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
        private final String subjectname;

        public AuthRequestWrapper(final String domain,
                final String subjectname, final HttpServletRequest request) {
            super(request);
            this.domain = domain;
            this.subjectname = subjectname;
        }

        @Override
        public boolean isUserInRole(String role) {
            return repositoryFactory.getSecurityRepository().isSubjectInRole(
                    domain, subjectname, role);
        }

        @Override
        public String getRemoteUser() {
            return subjectname;
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
        String subjectname = null;

        String authzHeader = req.getHeader(AUTHORIZATION_HEADER);
        if (GenericValidator.isBlankOrNull(authzHeader)
                || !authzHeader.startsWith("Basic ")) {
            Tuple domainAndSubjectname = WebUtils.verifySession(req);
            if (domainAndSubjectname != null) {
                subjectname = domainAndSubjectname.second();
                success = true;
            }
        } else {
            String encodedSubjectnameCredentials = authzHeader.split(" ")[1];
            String subjectnameCredentials = new String(PasswordUtils
                    .base64ToByte(encodedSubjectnameCredentials));

            String[] prinCred = subjectnameCredentials.split(":");
            String credentials = null;
            if (prinCred.length == 3) {
                domain = prinCred[0];
                subjectname = prinCred[1];
                credentials = prinCred[2];
            } else {
                subjectname = prinCred[0];
                credentials = prinCred[1];
            }
            Subject subject = repositoryFactory.getSubjectRepository(domain)
                    .authenticate(subjectname, credentials);
            if (subject != null) {
                success = true;
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Filtering " + authzHeader + ", domain " + domain
                    + ", subject " + subjectname + ", success " + success);
        }
        if (success
                && !repositoryFactory.getDomainRepository().isSubjectOwner(
                        domain, subjectname)) {
            LOGGER.warn("Subject " + subjectname
                    + " is illegaly trying to access domain " + domain);
            success = false;
        }
        if (success) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Logged " + domain + ":" + subjectname);
            }
            CurrentRequest.startRequest(domain, subjectname, req
                    .getRemoteHost());
            chain.doFilter(new AuthRequestWrapper(domain, subjectname, req),
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
