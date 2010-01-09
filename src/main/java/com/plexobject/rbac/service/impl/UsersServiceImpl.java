package com.plexobject.rbac.service.impl;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.plexobject.rbac.ServiceFactory;
import com.plexobject.rbac.jmx.JMXRegistrar;
import com.plexobject.rbac.jmx.impl.ServiceJMXBeanImpl;
import com.plexobject.rbac.repository.DomainRepository;
import com.plexobject.rbac.service.UsersService;
import com.sun.jersey.spi.inject.Inject;

@Path("/security/users")
@Component("usersService")
@Scope("singleton")
public class UsersServiceImpl implements UsersService {
    private static final Logger LOGGER = Logger
            .getLogger(UsersServiceImpl.class);

    @Autowired
    @Inject
    DomainRepository domainRepository = ServiceFactory.getDefaultFactory()
            .getDomainRepository();

    private final ServiceJMXBeanImpl mbean;

    public UsersServiceImpl() {
        mbean = JMXRegistrar.getInstance().register(getClass());
    }

    @Override
    public Response delete(String domain, String username) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Response delete(String domain) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Response get(String domain, String username) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Response index(String domain, String lastKey, int limit) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Response post(String domain, String userJson) {
        // TODO Auto-generated method stub
        return null;
    }

}
