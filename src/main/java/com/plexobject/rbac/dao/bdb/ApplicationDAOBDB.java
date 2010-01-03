package com.plexobject.rbac.dao.bdb;

import com.plexobject.rbac.dao.ApplicationDAO;
import com.plexobject.rbac.domain.Application;
import com.sleepycat.persist.EntityStore;

public class ApplicationDAOBDB extends BaseDAOBDB<Application, String>
        implements ApplicationDAO {
    public ApplicationDAOBDB(final EntityStore store) {
        super(store);
    }
}
