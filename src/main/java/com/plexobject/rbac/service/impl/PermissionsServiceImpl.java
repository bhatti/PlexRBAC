package com.plexobject.rbac.service.impl;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.plexobject.rbac.ServiceFactory;
import com.plexobject.rbac.domain.Permission;
import com.plexobject.rbac.domain.ValidationException;
import com.plexobject.rbac.http.RestClient;
import com.plexobject.rbac.jmx.JMXRegistrar;
import com.plexobject.rbac.jmx.impl.ServiceJMXBeanImpl;
import com.plexobject.rbac.repository.RepositoryFactory;
import com.plexobject.rbac.service.PermissionsService;
import com.sun.jersey.spi.inject.Inject;

@Path("/security/permissions")
@Component("permissionsService")
@Scope("singleton")
public class PermissionsServiceImpl implements PermissionsService,
        InitializingBean {
    private static final Logger LOGGER = Logger
            .getLogger(AuthorizationServiceImpl.class);

    @Autowired
    @Inject
    RepositoryFactory repositoryFactory = ServiceFactory.getDefaultFactory();

    private final ServiceJMXBeanImpl mbean;

    public PermissionsServiceImpl() {
        mbean = JMXRegistrar.getInstance().register(getClass());
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes( { MediaType.APPLICATION_JSON })
    @Path("/{domain}")
    @Override
    public Response post(@PathParam("domain") String domain,
            Permission permission) {
        if (domain == null) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("domain not specified").build();
        }
        if (permission == null) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("permission not specified").build();
        }
        try {
            permission.validate();
            repositoryFactory.getPermissionRepository(domain).save(permission);

            return Response.status(RestClient.OK_CREATED).entity(permission)
                    .build();
        } catch (ValidationException e) {
            LOGGER.error("failed to validate permission " + permission, e);

            mbean.incrementError();

            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity(e.toString()).build();
        } catch (Exception e) {
            LOGGER.error("failed to save permission " + permission, e);
            mbean.incrementError();
            return Response.status(RestClient.SERVER_INTERNAL_ERROR).type(
                    "text/plain").entity(
                    "failed to save permission " + permission + "\n").build();

        }
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes( { MediaType.WILDCARD })
    @Path("/{domain}/{id}")
    @Override
    public Response delete(@PathParam("domain") String domain,
            @PathParam("id") Integer id) {
        if (domain == null) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("domain not specified").build();
        }
        if (id == null) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("id not specified").build();
        }
        try {
            repositoryFactory.getPermissionRepository(domain).remove(id);

            return Response.status(RestClient.OK).entity(id).build();
        } catch (Exception e) {
            LOGGER.error("failed to delete permission " + id, e);
            mbean.incrementError();
            return Response.status(RestClient.SERVER_INTERNAL_ERROR).type(
                    "text/plain").entity(
                    "failed to delete permission " + id + "\n").build();

        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes( { MediaType.WILDCARD })
    @Path("/{domain}/{id}")
    @Override
    public Response get(@PathParam("domain") String domain,
            @PathParam("id") Integer id) {
        if (domain == null) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("domain not specified").build();
        }
        if (id == null) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("id not specified").build();
        }
        try {
            Permission permission = repositoryFactory.getPermissionRepository(
                    domain).findById(id);

            return Response.status(RestClient.OK).entity(permission).build();
        } catch (Exception e) {
            LOGGER.error("failed to get permission " + id, e);
            mbean.incrementError();
            return Response.status(RestClient.SERVER_INTERNAL_ERROR).type(
                    "text/plain").entity(
                    "failed to get permission " + id + "\n").build();

        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes( { MediaType.WILDCARD })
    @Path("/{domain}")
    @Override
    public Response index(@PathParam("domain") String domain,
            @DefaultValue("-1") @QueryParam("last") Integer lastKey,
            @DefaultValue("20") @QueryParam("limit") int limit) {
        if (domain == null) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("domain not specified").build();
        }
        if (lastKey <= 0) {
            lastKey = null;
        }
        try {
            Collection<Permission> permissions = repositoryFactory
                    .getPermissionRepository(domain).findAll(lastKey, limit);

            return Response.status(RestClient.OK).entity(permissions).build();
        } catch (Exception e) {
            LOGGER.error("failed to get permissions", e);
            mbean.incrementError();
            return Response.status(RestClient.SERVER_INTERNAL_ERROR).type(
                    "text/plain").entity("failed to get permissions\n").build();

        }
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes( { MediaType.WILDCARD })
    @Path("/{domain}")
    @Override
    public Response delete(String domain) {
        if (domain == null) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("domain not specified").build();
        }

        try {
            repositoryFactory.getPermissionRepository(domain).clear();

            return Response.status(RestClient.OK).entity("all deleted").build();
        } catch (Exception e) {
            LOGGER.error("failed to delete permissions", e);
            mbean.incrementError();
            return Response.status(RestClient.SERVER_INTERNAL_ERROR).type(
                    "text/plain").entity("failed to delete permissions\n")
                    .build();

        }
    }

    public RepositoryFactory getRepositoryFactory() {
        return repositoryFactory;
    }

    public void setRepositoryFactory(RepositoryFactory repositoryFactory) {
        this.repositoryFactory = repositoryFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (repositoryFactory == null) {
            throw new IllegalStateException("repositoryFactory is not set");
        }
    }
}
