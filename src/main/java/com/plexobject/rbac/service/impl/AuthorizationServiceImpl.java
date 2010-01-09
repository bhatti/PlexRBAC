package com.plexobject.rbac.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.plexobject.rbac.ServiceFactory;
import com.plexobject.rbac.http.RestClient;
import com.plexobject.rbac.jmx.JMXRegistrar;
import com.plexobject.rbac.jmx.impl.ServiceJMXBeanImpl;
import com.plexobject.rbac.security.PermissionManager;
import com.plexobject.rbac.security.PermissionRequest;
import com.plexobject.rbac.service.AuthorizationService;
import com.sun.jersey.spi.inject.Inject;

@Path("/security/auth")
@Component("authorizationService")
@Scope("singleton")
public class AuthorizationServiceImpl implements AuthorizationService,
        InitializingBean {
    private static final Logger LOGGER = Logger
            .getLogger(AuthorizationServiceImpl.class);

    @Autowired
    @Inject
    PermissionManager permissionManager = ServiceFactory.getPermissionManager();
    private final ServiceJMXBeanImpl mbean;

    public AuthorizationServiceImpl() {
        mbean = JMXRegistrar.getInstance().register(getClass());

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes( { MediaType.WILDCARD })
    @Path("/{domain}")
    @Override
    public Response authorize(@Context UriInfo ui,
            @PathParam("domain") String domain,
            @QueryParam("username") String username,
            @QueryParam("operation") String operation,
            @QueryParam("target") String target,
            MultivaluedMap<String, String> mmUserContext) {
        if (GenericValidator.isBlankOrNull(domain)) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("domain not specified").build();
        }
        if (GenericValidator.isBlankOrNull(username)) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("username not specified").build();
        }

        if (GenericValidator.isBlankOrNull(operation)) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("operation not specified").build();
        }

        if (GenericValidator.isBlankOrNull(target)) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("target not specified").build();
        }

        try {
            final Map<String, String> userContext = new HashMap<String, String>();
            for (Entry<String, List<String>> e : mmUserContext.entrySet()) {
                userContext.put(e.getKey(), e.getValue().get(0));
            }

            PermissionRequest request = new PermissionRequest(domain, username,
                    operation, target, userContext);
            permissionManager.check(request);
            mbean.incrementRequests();

            return Response.status(RestClient.OK_CREATED).entity(
                    "permission granted").build();
        } catch (Exception e) {
            LOGGER.error("permission failed", e);
            mbean.incrementError();

            return Response.status(RestClient.CLIENT_ERROR_UNAUTHORIZED).type(
                    "text/plain").entity("permission failed\n").build();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (permissionManager == null) {
            throw new IllegalStateException("permissionManager not set");
        }
    }

    /**
     * @return the permissionManager
     */
    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    /**
     * @param permissionManager
     *            the permissionManager to set
     */
    public void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

}