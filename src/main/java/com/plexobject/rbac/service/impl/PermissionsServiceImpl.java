package com.plexobject.rbac.service.impl;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.plexobject.rbac.ServiceFactory;
import com.plexobject.rbac.domain.Permission;
import com.plexobject.rbac.http.RestClient;
import com.plexobject.rbac.jmx.JMXRegistrar;
import com.plexobject.rbac.jmx.impl.ServiceJMXBeanImpl;
import com.plexobject.rbac.repository.RepositoryFactory;
import com.plexobject.rbac.service.PermissionsService;
import com.sun.jersey.spi.inject.Inject;

@Path("/security/domains")
@Component("domainsService")
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

    @GET
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

        try {
            // domainRepository.save(domain);

            return Response.status(RestClient.OK_CREATED).entity(domain)
                    .build();
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
        if (repositoryFactory != null) {
            throw new IllegalStateException("repositoryFactory is not set");
        }
    }

    @Override
    public Response delete(String domain, String id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Response get(String domain, String id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Response index(String domain, String lastKey, int limit) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Response delete(String domain) {
        // TODO Auto-generated method stub
        return null;
    }

}
