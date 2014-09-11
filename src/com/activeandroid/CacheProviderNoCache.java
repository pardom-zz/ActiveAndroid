package com.activeandroid;

public class CacheProviderNoCache<K, V> implements CacheProvider<K, V> {

    @Override
    public V get(K key) {
        return null;
    }

    @Override
    public V put(K key, V value) {
        return null;
    }

    @Override
    public V remove(K key) {
        return null;
    }

    @Override
    public void evictAll() {

    }
}
