package com.plexobject.rbac.cache;

import java.util.ArrayList;
import java.util.List;

public class CacheFlusher {
    private List<CacheFlushable> caches = new ArrayList<CacheFlushable>();
    private static final CacheFlusher INSTANCE = new CacheFlusher();

    private CacheFlusher() {
    }

    public static CacheFlusher getInstance() {
        return INSTANCE;
    }

    public synchronized void addCacheFlushable(CacheFlushable cf) {
        caches.add(cf);
    }

    public synchronized void removeCacheFlushable(CacheFlushable cf) {
        caches.remove(cf);
    }

    public synchronized void flushCaches() {
        for (CacheFlushable cf : caches) {
            cf.flushCache();
        }
    }

    public synchronized int[] cacheSizes() {
        int[] sizes = new int[caches.size()];
        int i = 0;
        for (CacheFlushable cf : caches) {
            sizes[i++] = cf.cacheSize();
        }
        return sizes;
    }
}
