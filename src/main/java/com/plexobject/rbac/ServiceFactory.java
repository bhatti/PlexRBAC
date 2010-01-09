package com.plexobject.rbac;

import com.plexobject.rbac.eval.js.JavascriptEvaluator;
import com.plexobject.rbac.repository.RepositoryFactory;
import com.plexobject.rbac.repository.bdb.RepositoryFactoryImpl;
import com.plexobject.rbac.security.PermissionManager;

public class ServiceFactory {
    private static RepositoryFactory REPOSITORY_FACTORY = new RepositoryFactoryImpl();
    private static PermissionManager PERMISSION_MANAGER = new PermissionManager(
            REPOSITORY_FACTORY, new JavascriptEvaluator());

    public static RepositoryFactory getDefaultFactory() {
        return REPOSITORY_FACTORY;
    }

    public static PermissionManager getPermissionManager() {
        return PERMISSION_MANAGER;
    }
}
