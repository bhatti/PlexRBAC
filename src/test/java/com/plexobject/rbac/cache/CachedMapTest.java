package com.plexobject.rbac.cache;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CachedMapTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testDefaultSize() {
        CachedMap<String, String> cache = new CachedMap<String, String>();
        for (int i = 0; i < CachedMap.MAX_ITEMS * 2; i++) {
            cache.put("key" + i, "value");
        }
        Assert.assertEquals(CachedMap.MAX_ITEMS, cache.size());
    }

    @Test
    public void testSize() {
        CachedMap<String, String> cache = new CachedMap<String, String>(0, 1);
        for (int i = 0; i < CachedMap.MAX_ITEMS * 2; i++) {
            cache.put("key" + i, "value");
        }

        Assert.assertEquals(1, cache.size());
        Assert.assertFalse(cache.isEmpty());

    }

    @Test
    public void testIsEmpty() {
        CachedMap<String, String> cache = new CachedMap<String, String>(0, 1);
        for (int i = 0; i < CachedMap.MAX_ITEMS * 2; i++) {
            cache.put("key" + i, "value");
        }
        Assert.assertFalse(cache.isEmpty());
    }

    @Test
    public void testContainsKey() {
        CachedMap<String, String> cache = new CachedMap<String, String>();
        for (int i = 0; i < CachedMap.MAX_ITEMS * 2; i++) {
            cache.put("key" + i, "value");
        }
        for (int i = 0; i < CachedMap.MAX_ITEMS; i++) {
            Assert.assertFalse(cache.containsKey("key" + i));
        }
        for (int i = CachedMap.MAX_ITEMS; i < CachedMap.MAX_ITEMS * 2; i++) {
            Assert.assertTrue(cache.containsKey("key" + i));
        }
    }

    @Test
    public void testContainsValue() {
        CachedMap<String, String> cache = new CachedMap<String, String>(0, 10,
                null);
        for (int i = 0; i < 20; i++) {
            cache.put("key" + i, "value" + i);
        }
        for (int i = 0; i < 10; i++) {
            Assert.assertFalse(cache.containsValue("value" + i));
        }
        for (int i = 10; i < 20; i++) {
            Assert.assertTrue("value " + i + " could not be found in " + cache,
                    cache.containsValue("value" + i));
        }
    }

    @Test
    public void testPut() {
        CachedMap<String, String> cache = new CachedMap<String, String>(0, 10,
                null);
        for (int i = 0; i < 20; i++) {
            cache.put("key" + i, "value" + i);
        }
        Assert.assertEquals(10, cache.size());
    }

    @Test
    public void testRemove() {
        CachedMap<String, String> cache = new CachedMap<String, String>(0, 10,
                null);
        for (int i = 0; i < 20; i++) {
            cache.put("key" + i, "value" + i);
        }
        Assert.assertEquals(10, cache.size());
        for (int i = 10; i < 20; i++) {
            cache.remove("key" + i);
        }
        Assert.assertEquals(0, cache.size());
    }

    @Test
    public void testPutAll() {
        CachedMap<String, String> cache = new CachedMap<String, String>(0, 10,
                null);
        Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < 20; i++) {
            map.put("key" + i, "value" + i);
        }
        Assert.assertEquals(20, map.size());

        cache.putAll(map);
        Assert.assertEquals("unexpected size " + cache.size() + " " + cache,
                10, cache.size());
    }

    @Test
    public void testClear() {
        CachedMap<String, String> cache = new CachedMap<String, String>(0, 10,
                null);
        for (int i = 0; i < 20; i++) {
            cache.put("key" + i, "value" + i);
        }
        Assert.assertEquals("unexpected size " + cache.size() + " " + cache,
                10, cache.size());
        cache.clear();
        Assert.assertEquals("unexpected size " + cache.size() + " " + cache,
                0, cache.size());

    }

    @Test
    public void testKeySet() {
    }

    @Test
    public void testValues() {
    }

    @Test
    public void testEntrySet() {
    }

    @Test
    public void testGet() {
    }

    @Test
    public void testFlushCache() {
        CachedMap<String, String> cache = new CachedMap<String, String>(0, 10,
                null);
        for (int i = 0; i < 20; i++) {
            cache.put("key" + i, "value" + i);
        }
        Assert.assertEquals("unexpected size " + cache.size() + " " + cache,
                10, cache.size());
        cache.flushCache();
        Assert.assertEquals("unexpected size " + cache.size() + " " + cache,
                0, cache.size());
    }

    @Test
    public void testCacheSize() {
    }

    @Test
    public void testEqualsObject() {
    }

    @Test
    public void testToString() {
    }

}
