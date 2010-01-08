package com.plexobject.rbac.repository.bdb;

import org.apache.commons.validator.GenericValidator;

import com.plexobject.rbac.domain.User;
import com.plexobject.rbac.repository.PersistenceException;
import com.plexobject.rbac.repository.UserRepository;
import com.plexobject.rbac.utils.PasswordUtils;
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
            user = new User(username, PasswordUtils.generatePassword());
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

    @Override
    public User authenticate(String username, String password)
            throws SecurityException {
        User user = findByID(username);
        if (user == null) {
            throw new SecurityException("Failed to find user");
        }
        if (PasswordUtils.getHash(password).equals(user.getPassword())) {
            return user;
        }
        throw new SecurityException("Password mismatch");
    }
}
