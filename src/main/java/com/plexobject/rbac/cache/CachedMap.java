package com.plexobject.rbac.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;

import com.plexobject.rbac.domain.Pair;
import com.plexobject.rbac.utils.TimeUtils;

/**
 * CacheMap - provides lightweight caching based on LRU size and timeout and
 * asynchronous reloads.
 * 
 */
public class CachedMap<K, V> implements Map<K, V>, CacheFlushable {
    private static final Logger LOGGER = Logger.getLogger(CachedMap.class);
    private final static int MAX_THREADS = 2; // for all cache items across VM
    final static int MAX_ITEMS = 1000; // max size
    private final static int EXPIRES_IN_SECS = 0; // indefinite

    private final static ExecutorService executorService = Executors
            .newFixedThreadPool(MAX_THREADS);

    class FixedSizeLruLinkedTreeMap<KK, VV> extends LinkedHashMap<KK, VV> {
        private static final long serialVersionUID = 1L;
        private final int maxSize;

        public FixedSizeLruLinkedTreeMap(int initialCapacity, float loadFactor,
                int maxSize) {
            super(initialCapacity, loadFactor, true);
            this.maxSize = maxSize;
        }

        @Override
        protected boolean removeEldestEntry(java.util.Map.Entry<KK, VV> eldest) {
            return super.size() > maxSize;
        }
    }

    final long expiresInSecs;
    private final CacheLoader<K, V> cacheLoader;
    private final Map<K, Pair<Long, V>> map;
    private final Map<Object, ReentrantLock> locks;

    public CachedMap() {
        this(EXPIRES_IN_SECS, MAX_ITEMS, null);
    }

    public CachedMap(final long expiresInSecs, final int maxSize) {
        this(expiresInSecs, maxSize, null);
    }

    public CachedMap(final long expiresInSecs, final int maxSize,
            final CacheLoader<K, V> cacheLoader) {
        this.expiresInSecs = expiresInSecs;
        this.map = Collections
                .synchronizedMap(new FixedSizeLruLinkedTreeMap<K, Pair<Long, V>>(
                        maxSize / 10, 0.75f, maxSize));
        this.cacheLoader = cacheLoader;
        this.locks = new TreeMap<Object, ReentrantLock>();
        CacheFlusher.getInstance().addCacheFlushable(this);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        for (Map.Entry<K, V> e : entrySet()) {
            if (value == e.getValue()
                    || (value != null && value.equals(e.getValue()))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V put(K key, V value) {
        Pair<Long, V> previous = map.put(key, new Pair<Long, V>(TimeUtils
                .getCurrentTimeMillis(), value));
        return previous != null ? previous.getSecond() : null;
    }

    @Override
    public V remove(Object key) {
        Pair<Long, V> previous = map.remove(key);
        return previous != null ? previous.getSecond() : null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        List<V> list = new ArrayList<V>();
        for (Pair<Long, V> e : map.values()) {
            list.add(e.getSecond());
        }
        return list;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K, V>> set = new HashSet<Map.Entry<K, V>>();
        for (final Map.Entry<K, Pair<Long, V>> e : map.entrySet()) {
            set.add(new Map.Entry<K, V>() {
                public K getKey() {
                    return e.getKey();
                }

                public V getValue() {
                    return e.getValue().getSecond();
                }

                public V setValue(V value) {
                    Pair<Long, V> pair = e.getValue();
                    pair.setSecond(value);
                    return pair.getSecond();
                }
            });
        }
        return set;
    }

    /**
     * This method is simple get without any loading behavior
     */
    @SuppressWarnings("unchecked")
    @Override
    public V get(Object key) {
        Pair<Long, V> pair = this.map.get(key);

        if (pair == null) {
            if (cacheLoader != null) {
                load((K) key, true, false);
                pair = this.map.get(key);
            } else {
                return null;
            }
        }
        if (expiresInSecs > 0
                && TimeUtils.getCurrentTimeMillis() - pair.getFirst() > expiresInSecs * 1000) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("expiring " + key);
            }
            map.remove(key);
            if (cacheLoader != null) {
                load((K) key, false, false);
            }
        }

        return pair.getSecond();
    }

    private void load(final K key, final boolean synchronizeAccess,
            final boolean lockAccess) {
        ReentrantLock lock = null;
        try {
            synchronized (this) {
                if (lockAccess) {
                    lock = lock(key);
                }
            }
            if (synchronizeAccess) {
                put(key, cacheLoader.get(key));
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Loaded " + key);
                }
            } else {
                executorService.submit(new Callable<Object>() {
                    public Object call() throws Exception {
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("Loading " + key + " asynchronously");
                        }
                        load(key, true, false);
                        return null;
                    }
                });
            }
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }
    }

    private ReentrantLock lock(Object key) {
        ReentrantLock lock = null;
        synchronized (locks) {
            lock = (ReentrantLock) locks.get(key);
            if (lock == null)
                lock = new ReentrantLock();
        }
        lock.lock();
        return lock;
    }

    @Override
    public void flushCache() {
        map.clear();
    }

    @Override
    public int cacheSize() {
        return map.size();
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof CachedMap)) {
            return false;
        }
        CachedMap<K, V> rhs = (CachedMap<K, V>) object;
        return new EqualsBuilder().append(this.map, rhs.map).isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(786529047, 1924536713).append(this.map)
                .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (Map.Entry<K, V> e : entrySet()) {
            sb.append(e.getKey() + "=" + e.getValue() + ";");
        }
        return sb.toString();
    }
}
