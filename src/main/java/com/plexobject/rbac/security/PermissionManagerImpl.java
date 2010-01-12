package com.plexobject.rbac.security;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.plexobject.rbac.Configuration;
import com.plexobject.rbac.domain.Permission;
import com.plexobject.rbac.domain.Role;
import com.plexobject.rbac.domain.SecurityError;
import com.plexobject.rbac.eval.PredicateEvaluator;
import com.plexobject.rbac.repository.RepositoryFactory;
import com.sun.jersey.spi.inject.Inject;

@Component("permissionManager")
public class PermissionManagerImpl implements InitializingBean,
        PermissionManager {
    private static final Logger LOGGER = Logger
            .getLogger(PermissionManagerImpl.class);
    private static final boolean STORE_ERRORS_IN_DB = Configuration
            .getInstance().getBoolean("store_errors_in_db", true);

    @Autowired
    @Inject
    private RepositoryFactory repositoryFactory;

    @Autowired
    @Inject
    private PredicateEvaluator evaluator;

    PermissionManagerImpl() {
    }

    public PermissionManagerImpl(final RepositoryFactory repositoryFactory,
            final PredicateEvaluator evaluator) {
        if (repositoryFactory == null) {
            throw new NullPointerException("null repositoryFactory");
        }
        if (evaluator == null) {
            throw new NullPointerException("null evaluator");
        }

        this.repositoryFactory = repositoryFactory;
        this.evaluator = evaluator;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.plexobject.rbac.security.PermissionManager#check(com.plexobject.rbac
     * .security.PermissionRequest)
     */
    public void check(PermissionRequest request) throws SecurityException {
        Collection<Role> roles = null;

        // by default subject gets anonymous role
        if (GenericValidator.isBlankOrNull(request.getSubjectName())) {
            roles = Arrays.asList(Role.ANONYMOUS);
        } else {
            roles = repositoryFactory.getRoleRepository(request.getDomain())
                    .getRolesForSubject(request.getSubjectName());
        }
        Collection<Permission> all = repositoryFactory.getPermissionRepository(
                request.getDomain()).getPermissionsForRoles(roles);
        for (Permission permission : all) {
            if (permission.impliesOperation(request.getOperation(), request
                    .getTarget())) {

                if (GenericValidator.isBlankOrNull(permission.getExpression())) {
                    return;
                } else {
                    if (evaluator.evaluate(permission.getExpression(), request
                            .getSubjectContext())) {
                        return;
                    }
                }
            }
        }

        try {
            if (STORE_ERRORS_IN_DB) {
                repositoryFactory.getSecurityErrorRepository(
                        request.getDomain()).save(
                        new SecurityError(request.getSubjectName(), request
                                .getOperation(), request.getTarget(), request
                                .getSubjectContext()));
            } else {
                LOGGER.warn("Permission failed for " + request);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save securit error for subjectName "
                    + request, e);
        }

        throw new SecurityException(request.getSubjectName()
                + " illegally accessing " + request.getOperation() + " for "
                + request.getTarget() + " using " + request.getSubjectContext()
                + ", permissions " + all);

    }

    public RepositoryFactory getRepositoryFactory() {
        return repositoryFactory;
    }

    public void setRepositoryFactory(RepositoryFactory repositoryFactory) {
        this.repositoryFactory = repositoryFactory;
    }

    public PredicateEvaluator getEvaluator() {
        return evaluator;
    }

    public void setEvaluator(PredicateEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (repositoryFactory == null) {
            throw new IllegalStateException("repositoryFactory not set");
        }
        if (evaluator == null) {
            throw new IllegalStateException("evaluator not set");
        }
    }
}
