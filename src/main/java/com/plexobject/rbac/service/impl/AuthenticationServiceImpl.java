package com.plexobject.rbac.service.impl;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.plexobject.rbac.domain.User;
import com.plexobject.rbac.http.RestClient;
import com.plexobject.rbac.jmx.JMXRegistrar;
import com.plexobject.rbac.jmx.impl.ServiceJMXBeanImpl;
import com.plexobject.rbac.repository.RepositoryFactory;
import com.plexobject.rbac.service.AuthenticationService;
import com.sun.jersey.spi.inject.Inject;

@Path("/authentication")
@Component("authenticationService")
@Scope("singleton")
public class AuthenticationServiceImpl implements AuthenticationService,
        InitializingBean {

    private static final Logger LOGGER = Logger
            .getLogger(AuthenticationServiceImpl.class);
    @Autowired
    @Inject
    RepositoryFactory repositoryFactory;

    private final ServiceJMXBeanImpl mbean;

    public AuthenticationServiceImpl() {
        mbean = JMXRegistrar.getInstance().register(getClass());

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes( { MediaType.WILDCARD })
    @Path("/{domain}")
    @Override
    public Response authenticate(@PathParam("domain") String domain,
            @QueryParam("username") String username,
            @QueryParam("password") String password) {
        if (GenericValidator.isBlankOrNull(domain)) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("domain not specified").build();
        }
        if (GenericValidator.isBlankOrNull(username)) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("username not specified").build();
        }

        if (GenericValidator.isBlankOrNull(password)) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("password not specified").build();
        }

        try {
            User user = repositoryFactory.getUserRepository(domain)
                    .authenticate(username, password);
            mbean.incrementRequests();

            return Response.status(RestClient.OK_CREATED).cookie(
                    new NewCookie("username", user.getID())).entity(
                    user.getID() + " logged in").build();
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
}
