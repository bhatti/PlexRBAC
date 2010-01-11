package com.plexobject.rbac.service.impl;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.plexobject.rbac.domain.Domain;
import com.plexobject.rbac.domain.Permission;
import com.plexobject.rbac.domain.Role;
import com.plexobject.rbac.domain.Subject;
import com.plexobject.rbac.repository.PagedList;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;
import com.sun.jersey.api.json.JSONConfiguration.MappedBuilder;

@Path("/jsonFormats")
@Component("jaxbContextResolver")
@Scope("singleton")
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JAXBContextResolver implements ContextResolver<JAXBContext> {
    private static final Logger LOGGER = Logger
            .getLogger(JAXBContextResolver.class);
    private static final Class<?>[] TYPES = new java.lang.Class[] {
            PagedList.class, Domain.class, Permission.class, Role.class,
            Subject.class };
    private JAXBContext context;

    public JAXBContextResolver() throws Exception {
        MappedBuilder config = JSONConfiguration.mapped(); // natural
        config.rootUnwrapping(true);
        config.arrays("domain");
        // config.nonStrings("totalCount");
        // this.context = new JSONJAXBContext(JSONConfiguration.mappedJettison()
        // .build(), "com.plexobject.rbac.domain.");

        this.context = new JSONJAXBContext(config.build(), TYPES);

    }

    @Override
    public JAXBContext getContext(Class<?> objectType) {
        for (Class<?> type : TYPES) {
            if (type.equals(objectType)) {
                LOGGER.warn("Found type " + objectType.getName());

                return this.context;
            }
        }
        LOGGER.warn("Found unknown type " + objectType.getName());
        return this.context;
    }

}
