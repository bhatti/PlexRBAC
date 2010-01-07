package com.plexobject.rbac.security;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;

import com.plexobject.rbac.Configuration;
import com.plexobject.rbac.domain.Permission;
import com.plexobject.rbac.domain.Role;
import com.plexobject.rbac.domain.SecurityError;
import com.plexobject.rbac.eval.PredicateEvaluator;
import com.plexobject.rbac.repository.SecurityRepository;

public class PermissionManager {
    private static final Logger LOGGER = Logger
            .getLogger(PermissionManager.class);
    private static final boolean STORE_ERRORS_IN_DB = Configuration
            .getInstance().getBoolean("store_errors_in_db", true);
    private final SecurityRepository securityRepository;

    private final PredicateEvaluator evaluator;

    public PermissionManager(final SecurityRepository securityRepository,
            final PredicateEvaluator evaluator) {
        if (securityRepository == null) {
            throw new NullPointerException("null security repository");
        }
        if (evaluator == null) {
            throw new NullPointerException("null evaluator");
        }

        this.securityRepository = securityRepository;
        this.evaluator = evaluator;
    }

    public void check(PermissionRequest request) throws SecurityException {
        Collection<Role> roles = null;

        // by default user gets anonymous role
        if (GenericValidator.isBlankOrNull(request.getUsername())) {
            roles = Arrays.asList(Role.ANONYMOUS);
        } else {
            roles = securityRepository.getRoleRepository(request.getDomain())
                    .getRolesForUser(request.getUsername());
        }
        Collection<Permission> all = securityRepository
                .getPermissionRepository(request.getDomain())
                .getPermissionsForRoles(roles);
        for (Permission permission : all) {
            if (permission.impliesOperation(request.getOperation(), request
                    .getTarget())) {

                if (GenericValidator.isBlankOrNull(permission.getExpression())) {
                    return;
                } else {
                    if (evaluator.evaluate(permission.getExpression(), request
                            .getUserContext())) {
                        return;
                    }
                }
            }
        }

        try {
            if (STORE_ERRORS_IN_DB) {
                securityRepository.getSecurityErrorRepository(
                        request.getDomain()).save(
                        new SecurityError(request.getUsername(), request
                                .getOperation(), request.getTarget(), request
                                .getUserContext()));
            } else {
                LOGGER.warn("Permission failed for " + request);
            }
        } catch (Exception e) {
            LOGGER.error(
                    "Failed to save securit error for username " + request, e);
        }
        // throw new SecurityException(request.getUsername(), request
        // .getOperation(), request.getTarget(), request.getUserContext());
        throw new SecurityException("permissions " + all,
                request.getUsername(), request.getOperation(), request
                        .getTarget(), request.getUserContext());

    }
}
