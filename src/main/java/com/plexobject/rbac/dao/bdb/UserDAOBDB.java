package com.plexobject.rbac.dao.bdb;

import com.plexobject.rbac.dao.PersistenceException;
import com.plexobject.rbac.dao.UserDAO;
import com.plexobject.rbac.domain.User;
import com.plexobject.rbac.metric.Metric;
import com.plexobject.rbac.metric.Timing;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.evolve.IncompatibleClassException;

public class UserDAOBDB extends BaseDAOBDB<User, Integer> implements UserDAO {
    private SecondaryIndex<String, Integer, User> usernameIndex;

    public UserDAOBDB(String databaseDir, String storeName)
            throws IncompatibleClassException, DatabaseException {
        super(databaseDir, storeName);
        usernameIndex = store.getSecondaryIndex(primaryIndex, String.class,
                "username");

    }

    @Override
    public User findByName(String username) {
        final Timing timer = Metric.newTiming(getClass().getName()
                + ".findByID");
        try {
            return usernameIndex.get(username);
        } catch (DatabaseException e) {
            throw new PersistenceException(e);
        } finally {
            timer.stop();
        }
    }
}
