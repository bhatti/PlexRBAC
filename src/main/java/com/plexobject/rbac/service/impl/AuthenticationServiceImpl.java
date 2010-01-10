package com.plexobject.rbac.service.impl;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.plexobject.rbac.ServiceFactory;
import com.plexobject.rbac.domain.Subject;
import com.plexobject.rbac.http.RestClient;
import com.plexobject.rbac.jmx.JMXRegistrar;
import com.plexobject.rbac.jmx.impl.ServiceJMXBeanImpl;
import com.plexobject.rbac.repository.RepositoryFactory;
import com.plexobject.rbac.service.AuthenticationService;
import com.plexobject.rbac.web.utils.WebUtils;
import com.sun.jersey.spi.inject.Inject;

@Path("/login")
@Component("authenticationService")
@Scope("singleton")
public class AuthenticationServiceImpl implements AuthenticationService,
        InitializingBean {

    private static final Logger LOGGER = Logger
            .getLogger(AuthenticationServiceImpl.class);
    @Autowired
    @Inject
    RepositoryFactory repositoryFactory = ServiceFactory.getDefaultFactory();

    private final ServiceJMXBeanImpl mbean;

    public AuthenticationServiceImpl() {
        mbean = JMXRegistrar.getInstance().register(getClass());

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes( { MediaType.WILDCARD })
    @Path("/{domain}")
    @Override
    public Response authenticate(@PathParam("domain") String domain,
            @FormParam("subjectname") String subjectname,
            @FormParam("credentials") String credentials) {
        if (GenericValidator.isBlankOrNull(domain)) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("domain not specified").build();
        }
        if (GenericValidator.isBlankOrNull(subjectname)) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("subjectname not specified").build();
        }

        if (GenericValidator.isBlankOrNull(credentials)) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("credentials not specified").build();
        }

        try {

            Subject subject = repositoryFactory.getSubjectRepository(domain)
                    .authenticate(subjectname, credentials);
            if (subject != null) {
                mbean.incrementRequests();
                final NewCookie cookie = WebUtils.createSessionCookie(domain,
                        subjectname);
                return Response.status(RestClient.OK_CREATED).cookie(cookie)
                        .entity(cookie.getValue()).build();
            } else {
                return Response.status(RestClient.CLIENT_ERROR_UNAUTHORIZED)
                        .type("text/plain").entity("login error\n").build();
            }
        } catch (Exception e) {
            LOGGER.error("failed to login", e);
            mbean.incrementError();

            return Response.status(RestClient.CLIENT_ERROR_UNAUTHORIZED).type(
                    "text/plain").entity("login error\n").build();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (repositoryFactory == null) {
            throw new IllegalStateException("repositoryFactory not set");
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
