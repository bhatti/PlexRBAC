package com.plexobject.rbac.service.impl;

import java.util.ArrayList;
import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.plexobject.rbac.http.RestClient;
import com.plexobject.rbac.jmx.JMXRegistrar;
import com.plexobject.rbac.jmx.impl.ServiceJMXBeanImpl;
import com.plexobject.rbac.repository.SecurityRepository;
import com.plexobject.rbac.service.SecurityAdminService;
import com.sun.jersey.spi.inject.Inject;

@Path("/storage")
@Component("storageService")
@Scope("singleton")
public class SecurityAdminServiceImpl implements SecurityAdminService,
        InitializingBean {
    private static final Logger LOGGER = Logger
            .getLogger(SecurityAdminServiceImpl.class);
    @Autowired
    @Inject
    SecurityRepository securityRepository;

    private final ServiceJMXBeanImpl mbean;

    public SecurityAdminServiceImpl() {
        mbean = JMXRegistrar.getInstance().register(getClass());

    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes( { MediaType.WILDCARD })
    @Path("/{domain}/{role}")
    @Override
    public Response addPermissionsToRole(@PathParam("domain") String domain,
            @PathParam("role") String role,
            @FormParam("permissionIDs") String permissionIDsJSON) {
        if (GenericValidator.isBlankOrNull(domain)) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("domain not specified").build();
        }
        if (GenericValidator.isBlankOrNull(role)) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("role not specified").build();
        }

        if (GenericValidator.isBlankOrNull(permissionIDsJSON)) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("permission-ids not specified")
                    .build();
        }

        try {
            final JSONArray jsonIDs = new JSONArray(permissionIDsJSON);

            Collection<Integer> ids = new ArrayList<Integer>();
            for (int i = 0; i < jsonIDs.length(); i++) {
                ids.add(Integer.valueOf(jsonIDs.getString(i)));
            }
            securityRepository.addPermissionsToRole(domain, role, ids);
            mbean.incrementRequests();

            return Response.status(RestClient.OK_CREATED).entity(
                    "added permissions").build();
        } catch (Exception e) {
            LOGGER.error("failed to add permissions", e);
            mbean.incrementError();

            return Response.status(RestClient.SERVER_INTERNAL_ERROR).type(
                    "text/plain").entity("failed to add permission\n").build();
        }
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes( { MediaType.WILDCARD })
    @Path("/{domain}/{role}")
    @Override
    public Response removePermissionsToRole(@PathParam("domain") String domain,
            @PathParam("role") String role,
            @FormParam("permissionIDs") String permissionIDsJSON) {
        if (GenericValidator.isBlankOrNull(domain)) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("domain not specified").build();
        }
        if (GenericValidator.isBlankOrNull(role)) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("role not specified").build();
        }

        if (GenericValidator.isBlankOrNull(permissionIDsJSON)) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("permission-ids not specified")
                    .build();
        }

        try {
            final JSONArray jsonIDs = new JSONArray(permissionIDsJSON);

            Collection<Integer> ids = new ArrayList<Integer>();
            for (int i = 0; i < jsonIDs.length(); i++) {
                ids.add(Integer.valueOf(jsonIDs.getString(i)));
            }
            securityRepository.removePermissionsToRole(domain, role, ids);
            mbean.incrementRequests();

            return Response.status(RestClient.OK_CREATED).entity(
                    "removed permissions").build();
        } catch (Exception e) {
            LOGGER.error("failed to remove permissions", e);
            mbean.incrementError();

            return Response.status(RestClient.SERVER_INTERNAL_ERROR).type(
                    "text/plain").entity("failed to remove permission\n")
                    .build();
        }
    }

    @Override
    public Response addRolesToUser(String domain, String user, String roles) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Response removeRolesToUser(String domain, String user, String roles) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @return the documentRepository
     */
    public SecurityRepository getSecurityRepository() {
        return securityRepository;
    }

    /**
     * @param documentRepository
     *            the documentRepository to set
     */
    public void setSecurityRepository(SecurityRepository securityRepository) {
        this.securityRepository = securityRepository;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (securityRepository == null) {
            throw new IllegalStateException("documentRepository not set");
        }
    }

}
