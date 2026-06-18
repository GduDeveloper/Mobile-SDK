package com.gdu.demo.utils;

import android.util.LruCache;

/**
 * 缓存工具，主要用于存取缓存，避免多次重复读取sp文件，减少io消耗
 */
public class LRUCacheUtil {

    private static final LruCache<String, Object> lruCache = new LruCache<String, Object>(512) {
        @Override
        protected int sizeOf(String key, Object value) {
            return 1;
        }
    };

    public static void put(String key, Object val) {
        lruCache.put(key, val);
    }

    public static boolean remove(String key) {
        Object obj = lruCache.remove(key);
        return obj != null;
    }

    public static String getString(String key) {
        Object result = lruCache.get(key);
        if (result == null) {
            return null;
        }
        if (result instanceof String) {
            return (String) result;
        }
        return null;
    }

    public static Boolean getBool(String key) {
        Object result = lruCache.get(key);
        if (result == null) {
            return null;
        }
        if (result instanceof Boolean) {
            return (Boolean) result;
        }
        return null;
    }

    public static Integer getInt(String key) {
        Object result = lruCache.get(key);
        if (result == null) {
            return null;
        }
        if (result instanceof Integer) {
            return (Integer) result;
        }
        return null;
    }

    public static Long getLong(String key) {
        Object result = lruCache.get(key);
        if (result == null) {
            return null;
        }
        if (result instanceof Long) {
            return (Long) result;
        }
        return null;
    }

    public static Float getFloat(String key) {
        Object result = lruCache.get(key);
        if (result == null) {
            return null;
        }
        if (result instanceof Float) {
            return (Float) result;
        }
        return null;
    }

}
