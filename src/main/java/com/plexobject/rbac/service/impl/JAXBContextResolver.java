package com.plexobject.rbac.service.impl;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.plexobject.rbac.domain.Domain;
import com.plexobject.rbac.domain.Permission;
import com.plexobject.rbac.domain.Role;
import com.plexobject.rbac.domain.User;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;
import com.sun.jersey.api.json.JSONConfiguration.NaturalBuilder;

@Component("jaxbContextResolver")
@Scope("singleton")
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JAXBContextResolver implements ContextResolver<JAXBContext> {

    private JAXBContext context;

    public JAXBContextResolver() throws Exception {

        NaturalBuilder config = JSONConfiguration.natural();
        config.rootUnwrapping(true);

        this.context = new JSONJAXBContext(config.build(),
                new java.lang.Class[] { Domain.class, Permission.class,
                        Role.class, User.class });

    }

    public JAXBContext getContext(Class<?> objectType) {
        return this.context;
    }

}
