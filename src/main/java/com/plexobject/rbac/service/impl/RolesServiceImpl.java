package com.plexobject.rbac.service.impl;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
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
import com.plexobject.rbac.domain.Role;
import com.plexobject.rbac.domain.ValidationException;
import com.plexobject.rbac.http.RestClient;
import com.plexobject.rbac.jmx.JMXRegistrar;
import com.plexobject.rbac.jmx.impl.ServiceJMXBeanImpl;
import com.plexobject.rbac.repository.RepositoryFactory;
import com.plexobject.rbac.service.RolesService;
import com.sun.jersey.spi.inject.Inject;

@Path("/security/roles")
@Component("rolesService")
@Scope("singleton")
public class RolesServiceImpl implements RolesService, InitializingBean {
    private static final Logger LOGGER = Logger
            .getLogger(AuthorizationServiceImpl.class);

    @Autowired
    @Inject
    RepositoryFactory repositoryFactory = ServiceFactory.getDefaultFactory();

    private final ServiceJMXBeanImpl mbean;

    public RolesServiceImpl() {
        mbean = JMXRegistrar.getInstance().register(getClass());
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes( { MediaType.WILDCARD })
    @Path("/{domain}/{rolename}")
    @Override
    public Response delete(@PathParam("domain") String domain,
            @PathParam("rolename") String rolename) {
        if (domain == null) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("domain not specified").build();
        }
        if (rolename == null) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("rolename not specified").build();
        }
        try {
            repositoryFactory.getRoleRepository(domain).remove(rolename);

            return Response.status(RestClient.OK).entity(rolename).build();
        } catch (Exception e) {
            LOGGER.error("failed to delete rolename " + rolename, e);
            mbean.incrementError();
            return Response.status(RestClient.SERVER_INTERNAL_ERROR).type(
                    "text/plain").entity(
                    "failed to delete rolename " + rolename + "\n").build();

        }

    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes( { MediaType.WILDCARD })
    @Path("/{domain}")
    @Override
    public Response delete(@PathParam("domain") String domain) {
        if (domain == null) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("domain not specified").build();
        }

        try {
            repositoryFactory.getRoleRepository(domain).clear();

            return Response.status(RestClient.OK).entity("all deleted").build();
        } catch (Exception e) {
            LOGGER.error("failed to delete roles", e);
            mbean.incrementError();
            return Response.status(RestClient.SERVER_INTERNAL_ERROR).type(
                    "text/plain").entity("failed to delete roles\n").build();

        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes( { MediaType.WILDCARD })
    @Path("/{domain}/{rolename}")
    @Override
    public Response get(@PathParam("domain") String domain,
            @PathParam("rolename") String rolename) {
        if (domain == null) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("domain not specified").build();
        }
        if (rolename == null) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("rolename not specified").build();
        }
        try {
            Role role = repositoryFactory.getRoleRepository(domain).findById(
                    rolename);

            return Response.status(RestClient.OK).entity(role).build();
        } catch (Exception e) {
            LOGGER.error("failed to get role " + rolename, e);
            mbean.incrementError();
            return Response.status(RestClient.SERVER_INTERNAL_ERROR).type(
                    "text/plain").entity(
                    "failed to get role " + rolename + "\n").build();

        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes( { MediaType.WILDCARD })
    @Path("/{domain}")
    @Override
    public Response index(@PathParam("domain") String domain,
            @QueryParam("last") String lastKey,
            @DefaultValue("20") @QueryParam("limit") int limit) {
        if (domain == null) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("domain not specified").build();
        }
        try {
            Collection<Role> roles = repositoryFactory
                    .getRoleRepository(domain).findAll(lastKey, limit);

            return Response.status(RestClient.OK).entity(roles).build();
        } catch (Exception e) {
            LOGGER.error("failed to get roles", e);
            mbean.incrementError();
            return Response.status(RestClient.SERVER_INTERNAL_ERROR).type(
                    "text/plain").entity("failed to get roles\n").build();

        }
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes( { MediaType.APPLICATION_JSON })
    @Path("/{domain}")
    @Override
    public Response put(@PathParam("domain") String domain, Role role) {
        if (domain == null) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("domain not specified").build();
        }
        if (role == null) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("role not specified").build();
        }
        try {
            role.validate();
            repositoryFactory.getRoleRepository(domain).save(role);

            return Response.status(RestClient.OK_CREATED).entity(role).build();
        } catch (ValidationException e) {
            LOGGER.error("failed to validate role " + role, e);

            mbean.incrementError();

            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity(e.toString()).build();
        } catch (Exception e) {
            LOGGER.error("failed to save role " + role, e);
            mbean.incrementError();
            return Response.status(RestClient.SERVER_INTERNAL_ERROR).type(
                    "text/plain").entity("failed to save role " + role + "\n")
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
