package com.plexobject.rbac.cache;

public interface CacheFlushable {
    void flushCache();
    int cacheSize();
}
