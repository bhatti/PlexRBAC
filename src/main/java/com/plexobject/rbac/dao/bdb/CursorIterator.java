package com.plexobject.rbac.dao.bdb;

import java.util.Iterator;

import org.apache.log4j.Logger;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;

public class CursorIterator<V> implements Iterator<V> {
    private static final Logger LOGGER = Logger.getLogger(CursorIterator.class);

    private EntityCursor<V> cursor;
    private Iterator<V> iterator;

    public CursorIterator(final EntityCursor<V> cursor) {
        this.cursor = cursor;
        this.iterator = cursor.iterator();
    }

    @Override
    public boolean hasNext() {
        if (iterator.hasNext()) {
            return true;
        } else {
            close();
            return false;
        }
    }

    public void close() {
        try {
            if (cursor != null) {
                cursor.close();
                cursor = null;
                iterator = null;
            }
        } catch (DatabaseException e) {
            LOGGER.error("failed to close cursor", e);
        }
    }

    @Override
    public V next() {
        return iterator.next();
    }

    @Override
    public void remove() {
        iterator.remove();
    }
}
