package com.plexobject.rbac.repository.bdb;

import com.plexobject.rbac.repository.ApplicationRepository;
import com.plexobject.rbac.domain.Application;
import com.sleepycat.persist.EntityStore;

public class ApplicationRepositoryImpl extends
        BaseRepositoryImpl<Application, String> implements
        ApplicationRepository {
    public ApplicationRepositoryImpl(final EntityStore store) {
        super(store);
    }
}
