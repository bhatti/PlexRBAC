package com.plexobject.rbac.repository.bdb;

import java.util.UUID;

import org.apache.commons.validator.GenericValidator;

import com.plexobject.rbac.domain.User;
import com.plexobject.rbac.repository.PersistenceException;
import com.plexobject.rbac.repository.UserRepository;
import com.sleepycat.persist.EntityStore;

public class UserRepositoryImpl extends BaseRepositoryImpl<User, String>
        implements UserRepository {
    public UserRepositoryImpl(final EntityStore store) {
        super(store);
    }

    @Override
    public User getOrCreateUser(String username) {
        if (GenericValidator.isBlankOrNull(username)) {
            throw new IllegalArgumentException("username is not specified");
        }
        User user = super.findByID(username);
        if (user == null) {
            user = new User(username, generatePassword());
            save(user);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Created user " + user);
            }
        }
        return user;
    }

    @Override
    public boolean remove(final String username) throws PersistenceException {
        if (GenericValidator.isBlankOrNull(username)) {
            throw new IllegalArgumentException("username is not specified");
        }
        if (User.SUPER_ADMIN_USERNAME.equals(username)) {
            throw new IllegalStateException(User.SUPER_ADMIN_USERNAME
                    + " cannot be removed");
        }
        return super.remove(username);
    }

    private static String generatePassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
