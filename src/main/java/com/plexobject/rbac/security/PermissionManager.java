package com.plexobject.rbac.security;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;

import com.plexobject.rbac.dao.PermissionDAO;
import com.plexobject.rbac.dao.bdb.CursorIterator;
import com.plexobject.rbac.domain.Application;
import com.plexobject.rbac.domain.Permission;
import com.plexobject.rbac.eval.PredicateEvaluator;
import com.plexobject.rbac.eval.simple.SimpleEvaluator;
import com.plexobject.rbac.utils.CurrentUserRequest;

public class PermissionManager {
    private static final Logger LOGGER = Logger
            .getLogger(PermissionManager.class);
    private final PredicateEvaluator evaluator;
    private final PermissionDAO permissionDAO;

    public PermissionManager(final PermissionDAO permissionDAO,
            final PredicateEvaluator evaluator) {
        this.permissionDAO = permissionDAO;
        this.evaluator = evaluator;
    }

    public void check(final Application application, final String operation,
            final Map<String, String> userContext) throws SecurityException {
        String username = CurrentUserRequest.getUsername();
        if (GenericValidator.isBlankOrNull(username)) {
            throw new SecurityException("Username is not specified", username,
                    operation, userContext);
        }
        Iterator<Permission> it = permissionDAO
                .getPermissionsForApplication(application.getName());
        try {
            while (it.hasNext()) {
                Permission permission = it.next();
                if (!permission.impliesOperation(operation)) {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Operation " + operation
                                + " is not permitted by permission "
                                + permission);
                    }
                    throw new SecurityException(permission, username,
                            operation, userContext);
                }
                if (!GenericValidator.isBlankOrNull(permission.getExpression())) {
                    if (!evaluator.evaluate(permission.getExpression(),
                            userContext)) {
                        throw new SecurityException(permission, username,
                                operation, userContext);
                    }
                }
            }
        } finally {
            ((CursorIterator<Permission>) it).close();
        }
    }
}
