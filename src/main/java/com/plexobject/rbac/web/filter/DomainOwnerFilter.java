package com.plexobject.rbac.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.plexobject.rbac.ServiceFactory;
import com.plexobject.rbac.repository.RepositoryFactory;
import com.plexobject.rbac.utils.CurrentRequest;
import com.sun.jersey.spi.inject.Inject;

public class DomainOwnerFilter implements Filter {

    private static final Logger LOGGER = Logger
            .getLogger(DomainOwnerFilter.class);
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
        HttpServletResponse res = (HttpServletResponse) response;
        if (repositoryFactory.getDomainRepository().isSubjectOwner(
                CurrentRequest.getDomain(), CurrentRequest.getSubjectName())) {
            chain.doFilter(request, response);
        } else {
            LOGGER.warn("Subject " + CurrentRequest.getSubjectName()
                    + " is illegaly trying to access domain "
                    + CurrentRequest.getDomain() + " from "
                    + CurrentRequest.getIPAddress());
            res.setHeader("WWW-Authenticate", CurrentRequest.getDomain());
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.flushBuffer();
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
