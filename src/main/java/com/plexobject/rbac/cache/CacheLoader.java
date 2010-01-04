package com.plexobject.rbac.cache;

public interface CacheLoader<K, V> {
    V get(K key);
}
