package com.activeandroid;

import android.support.v4.util.LruCache;

public class CacheProviderLruCache<K, V> implements CacheProvider<K, V> {

    private LruCache lruCache;

    public CacheProviderLruCache(int maxSize) {
        lruCache = new LruCache<K, V>(maxSize);
    }

    @Override
    public V get(K key) {
        return (V) lruCache.get(key);
    }

    @Override
    public V put(K key, V value) {
        return (V) lruCache.put(key, value);
    }

    @Override
    public V remove(K key) {
        return (V) lruCache.remove(key);
    }

    @Override
    public void evictAll() {
        lruCache.evictAll();
    }
}
