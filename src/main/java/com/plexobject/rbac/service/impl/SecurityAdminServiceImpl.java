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

import com.plexobject.rbac.ServiceFactory;
import com.plexobject.rbac.http.RestClient;
import com.plexobject.rbac.jmx.JMXRegistrar;
import com.plexobject.rbac.jmx.impl.ServiceJMXBeanImpl;
import com.plexobject.rbac.repository.RepositoryFactory;
import com.plexobject.rbac.service.SecurityAdminService;
import com.sun.jersey.spi.inject.Inject;

@Path("/security/admin")
@Component("securityAdminService")
@Scope("singleton")
public class SecurityAdminServiceImpl implements SecurityAdminService,
        InitializingBean {
    private static final Logger LOGGER = Logger
            .getLogger(SecurityAdminServiceImpl.class);
    @Autowired
    @Inject
    RepositoryFactory repositoryFactory = ServiceFactory.getDefaultFactory();

    private final ServiceJMXBeanImpl mbean;

    public SecurityAdminServiceImpl() {
        mbean = JMXRegistrar.getInstance().register(getClass());

    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes( { MediaType.WILDCARD })
    @Path("/permissions/{domain}/{role}")
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

            repositoryFactory.getSecurityRepository().addPermissionsToRole(
                    domain, role, ids);
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
    @Path("/permissions/{domain}/{role}")
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
            repositoryFactory.getSecurityRepository().removePermissionsToRole(
                    domain, role, ids);
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

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes( { MediaType.WILDCARD })
    @Path("/roles/{domain}/{role}")
    @Override
    public Response addRolesToSubject(@PathParam("domain") String domain,
            @PathParam("subject") String subject,
            @FormParam("rolenames") String rolenamesJSON) {
        if (GenericValidator.isBlankOrNull(domain)) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("domain not specified").build();
        }
        if (GenericValidator.isBlankOrNull(subject)) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("subject not specified").build();
        }

        if (GenericValidator.isBlankOrNull(rolenamesJSON)) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("rolenames not specified").build();
        }

        try {
            final JSONArray jsonIDs = new JSONArray(rolenamesJSON);

            Collection<String> ids = new ArrayList<String>();
            for (int i = 0; i < jsonIDs.length(); i++) {
                ids.add(jsonIDs.getString(i));
            }
            repositoryFactory.getSecurityRepository().addRolesToSubject(domain,
                    subject, ids);
            mbean.incrementRequests();

            return Response.status(RestClient.OK_CREATED).entity("added roles")
                    .build();
        } catch (Exception e) {
            LOGGER.error("failed to add roles", e);
            mbean.incrementError();

            return Response.status(RestClient.SERVER_INTERNAL_ERROR).type(
                    "text/plain").entity("failed to add roles\n").build();
        }
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes( { MediaType.WILDCARD })
    @Path("/roles/{domain}/{role}")
    @Override
    public Response removeRolesToSubject(@PathParam("domain") String domain,
            @PathParam("subject") String subject,
            @FormParam("rolenames") String rolenamesJSON) {
        if (GenericValidator.isBlankOrNull(domain)) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("domain not specified").build();
        }
        if (GenericValidator.isBlankOrNull(subject)) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("subject not specified").build();
        }

        if (GenericValidator.isBlankOrNull(rolenamesJSON)) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("rolenames not specified").build();
        }

        try {
            final JSONArray jsonIDs = new JSONArray(rolenamesJSON);

            Collection<String> ids = new ArrayList<String>();
            for (int i = 0; i < jsonIDs.length(); i++) {
                ids.add(jsonIDs.getString(i));
            }
            repositoryFactory.getSecurityRepository().removeRolesToSubject(
                    domain, subject, ids);
            mbean.incrementRequests();

            return Response.status(RestClient.OK_CREATED).entity("added roles")
                    .build();
        } catch (Exception e) {
            LOGGER.error("failed to remove roles", e);
            mbean.incrementError();

            return Response.status(RestClient.SERVER_INTERNAL_ERROR).type(
                    "text/plain").entity("failed to remove roles\n").build();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (repositoryFactory == null) {
            throw new IllegalStateException("repositoryFactory not set");
        }
    }

    public RepositoryFactory getRepositoryFactory() {
        return repositoryFactory;
    }

    public void setRepositoryFactory(RepositoryFactory repositoryFactory) {
        this.repositoryFactory = repositoryFactory;
    }

}
