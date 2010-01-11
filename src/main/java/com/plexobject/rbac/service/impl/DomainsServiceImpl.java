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

import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.plexobject.rbac.ServiceFactory;
import com.plexobject.rbac.domain.Domain;
import com.plexobject.rbac.domain.ValidationException;
import com.plexobject.rbac.http.RestClient;
import com.plexobject.rbac.jmx.JMXRegistrar;
import com.plexobject.rbac.jmx.impl.ServiceJMXBeanImpl;
import com.plexobject.rbac.repository.NotFoundException;
import com.plexobject.rbac.repository.PersistenceException;
import com.plexobject.rbac.repository.RepositoryFactory;
import com.plexobject.rbac.service.DomainsService;
import com.sun.jersey.spi.inject.Inject;

@Path("/security/domains")
@Component("domainsService")
@Scope("singleton")
public class DomainsServiceImpl implements DomainsService, InitializingBean {
    private static final Logger LOGGER = Logger
            .getLogger(AuthorizationServiceImpl.class);

    @Autowired
    @Inject
    RepositoryFactory repositoryFactory = ServiceFactory.getDefaultFactory();

    private final ServiceJMXBeanImpl mbean;

    public DomainsServiceImpl() {
        mbean = JMXRegistrar.getInstance().register(getClass());
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes( { MediaType.WILDCARD })
    @Path("/{domain}")
    @Override
    public Response delete(@PathParam("domain") String domainName) {
        if (GenericValidator.isBlankOrNull(domainName)) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("domain not specified").build();
        }
        try {
            if (repositoryFactory.getDomainRepository().remove(domainName)) {
                return Response.status(RestClient.OK).type(
                        MediaType.APPLICATION_JSON_TYPE).entity(domainName)
                        .build();
            }
            throw new RuntimeException("Failed to delete " + domainName);
        } catch (NotFoundException e) {
            mbean.incrementError();
            return Response.status(RestClient.CLIENT_ERROR_NOT_FOUND).type(
                    "text/plain").entity(
                    "failed to get domain " + domainName + "\n").build();
        } catch (Exception e) {
            LOGGER.error("permission failed", e);
            mbean.incrementError();

            return Response.status(RestClient.CLIENT_ERROR_UNAUTHORIZED).type(
                    "text/plain").entity("permission failed\n").build();
        }
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes( { MediaType.WILDCARD })
    @Override
    public Response delete() {
        try {
            repositoryFactory.getDomainRepository().clear();
            return Response.status(RestClient.OK).build();
        } catch (PersistenceException e) {
            LOGGER.error("failed to get domains", e);
            mbean.incrementError();
            return Response.status(RestClient.SERVER_INTERNAL_ERROR).type(
                    "text/plain").entity("failed to get domains\n").build();

        } catch (Exception e) {
            LOGGER.error("permission failed", e);
            mbean.incrementError();

            return Response.status(RestClient.CLIENT_ERROR_UNAUTHORIZED).type(
                    "text/plain").entity("permission failed\n").build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes( { MediaType.APPLICATION_JSON })
    @Path("/{domain}")
    @Override
    public Response get(@PathParam("domain") String domainName) {
        if (GenericValidator.isBlankOrNull(domainName)) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("domain not specified").build();
        }

        try {
            Domain domain = repositoryFactory.getDomainRepository().findById(
                    domainName);

            return Response.status(RestClient.OK).entity(domain).build();
        } catch (NotFoundException e) {
            mbean.incrementError();
            return Response.status(RestClient.CLIENT_ERROR_NOT_FOUND).type(
                    "text/plain").entity(
                    "failed to get domain " + domainName + "\n").build();

        } catch (Exception e) {
            LOGGER.error("failed to get domain " + domainName, e);
            mbean.incrementError();
            return Response.status(RestClient.SERVER_INTERNAL_ERROR).type(
                    "text/plain").entity(
                    "failed to get domain " + domainName + "\n").build();

        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes( { MediaType.WILDCARD })
    @Override
    public Response index(@QueryParam("last") String lastKey,
            @DefaultValue("20") @QueryParam("limit") int limit) {
        try {
            Collection<Domain> domains = repositoryFactory
                    .getDomainRepository().findAll(lastKey, limit);
            return Response.status(RestClient.OK).type(
                    MediaType.APPLICATION_JSON_TYPE).entity(
                    domains.toArray(new Domain[domains.size()])).build();
        } catch (Exception e) {
            LOGGER.error("failed to get domain", e);
            mbean.incrementError();
            return Response.status(RestClient.SERVER_INTERNAL_ERROR).type(
                    "text/plain").entity("failed to get domain\n").build();
        }
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes( { MediaType.APPLICATION_JSON })
    @Path("/{domain}")
    @Override
    public Response put(Domain domain) {
        if (domain == null) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("domain not specified").build();
        }

        try {
            domain.validate();
            repositoryFactory.getDomainRepository().save(domain);

            return Response.status(RestClient.OK_CREATED).entity(domain)
                    .build();
        } catch (ValidationException e) {
            LOGGER.error("failed to validate domain " + domain, e);

            mbean.incrementError();

            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity(e.toString()).build();
        } catch (Exception e) {
            LOGGER.error("failed to save domain " + domain, e);
            mbean.incrementError();
            return Response.status(RestClient.SERVER_INTERNAL_ERROR).type(
                    "text/plain").entity(
                    "failed to save domain " + domain + "\n").build();

        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (repositoryFactory == null) {
            throw new IllegalStateException("repositoryFactory is not set");
        }
    }

    public RepositoryFactory getRepositoryFactory() {
        return repositoryFactory;
    }

    public void setRepositoryFactory(RepositoryFactory repositoryFactory) {
        this.repositoryFactory = repositoryFactory;
    }

}
