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
import com.plexobject.rbac.domain.Subject;
import com.plexobject.rbac.domain.ValidationException;
import com.plexobject.rbac.http.RestClient;
import com.plexobject.rbac.jmx.JMXRegistrar;
import com.plexobject.rbac.jmx.impl.ServiceJMXBeanImpl;
import com.plexobject.rbac.repository.RepositoryFactory;
import com.plexobject.rbac.service.SubjectsService;
import com.plexobject.rbac.utils.PasswordUtils;
import com.sun.jersey.spi.inject.Inject;

@Path("/security/subjects")
@Component("subjectsService")
@Scope("singleton")
public class SubjectsServiceImpl implements SubjectsService, InitializingBean {
    private static final Logger LOGGER = Logger
            .getLogger(SubjectsServiceImpl.class);

    @Autowired
    @Inject
    RepositoryFactory repositoryFactory = ServiceFactory.getDefaultFactory();

    private final ServiceJMXBeanImpl mbean;

    public SubjectsServiceImpl() {
        mbean = JMXRegistrar.getInstance().register(getClass());
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes( { MediaType.WILDCARD })
    @Path("/{domain}/{subjectName}")
    @Override
    public Response delete(@PathParam("domain") String domain,
            @PathParam("subjectName") String subjectName) {
        if (domain == null) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("domain not specified").build();
        }
        if (subjectName == null) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("subjectName not specified").build();
        }
        try {
            repositoryFactory.getSubjectRepository(domain).remove(subjectName);

            return Response.status(RestClient.OK).entity(subjectName).build();
        } catch (ValidationException e) {
            mbean.incrementError();

            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity(e.toString()).build();
        } catch (Exception e) {
            LOGGER.error("failed to delete subjectName " + subjectName, e);
            mbean.incrementError();
            return Response.status(RestClient.SERVER_INTERNAL_ERROR).type(
                    "text/plain").entity(
                    "failed to delete subjectName " + subjectName + "\n")
                    .build();

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
            repositoryFactory.getSubjectRepository(domain).clear();

            return Response.status(RestClient.OK).entity("all deleted").build();
        } catch (Exception e) {
            LOGGER.error("failed to delete subjects", e);
            mbean.incrementError();
            return Response.status(RestClient.SERVER_INTERNAL_ERROR).type(
                    "text/plain").entity("failed to delete subjects\n").build();

        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes( { MediaType.WILDCARD })
    @Path("/{domain}/{subjectName}")
    @Override
    public Response get(@PathParam("domain") String domain,
            @PathParam("subjectName") String subjectName) {
        if (domain == null) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("domain not specified").build();
        }
        if (subjectName == null) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("subjectName not specified").build();
        }
        try {
            Subject subject = repositoryFactory.getSubjectRepository(domain)
                    .findById(subjectName);

            return Response.status(RestClient.OK).entity(subject).build();
        } catch (Exception e) {
            LOGGER.error("failed to get subject " + subjectName, e);
            mbean.incrementError();
            return Response.status(RestClient.SERVER_INTERNAL_ERROR).type(
                    "text/plain").entity(
                    "failed to get subject " + subjectName + "\n").build();

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
            Collection<Subject> subjects = repositoryFactory
                    .getSubjectRepository(domain).findAll(lastKey, limit);

            return Response.status(RestClient.OK).entity(subjects).build();
        } catch (Exception e) {
            LOGGER.error("failed to get subjects", e);
            mbean.incrementError();
            return Response.status(RestClient.SERVER_INTERNAL_ERROR).type(
                    "text/plain").entity("failed to get subjects\n").build();

        }
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes( { MediaType.APPLICATION_JSON })
    @Path("/{domain}")
    @Override
    public Response put(@PathParam("domain") String domain, Subject subject) {
        if (domain == null) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("domain not specified").build();
        }
        if (subject == null) {
            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity("subject not specified").build();
        }
        try {
            subject.validate();
            subject.setCredentials(PasswordUtils.getHash(subject
                    .getCredentials()));
            repositoryFactory.getSubjectRepository(domain).save(subject);

            return Response.status(RestClient.OK_CREATED).entity(subject)
                    .build();
        } catch (ValidationException e) {
            LOGGER.error("failed to validate subject " + subject, e);

            mbean.incrementError();

            return Response.status(RestClient.CLIENT_ERROR_BAD_REQUEST).type(
                    "text/plain").entity(e.toString()).build();
        } catch (Exception e) {
            LOGGER.error("failed to save subject " + subject, e);
            mbean.incrementError();
            return Response.status(RestClient.SERVER_INTERNAL_ERROR).type(
                    "text/plain").entity(
                    "failed to save subject " + subject + "\n").build();

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
