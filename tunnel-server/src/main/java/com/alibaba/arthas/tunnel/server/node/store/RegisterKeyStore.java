package com.alibaba.arthas.tunnel.server.node.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;

import java.util.UUID;

public class RegisterKeyStore {
    private final static Logger logger = LoggerFactory.getLogger(RegisterKeyStore.class);

    private Cache cache;

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public boolean checkKey(String key) {
        Cache.ValueWrapper valueWrapper = cache.get(key);
        if (valueWrapper == null) {
            return false;
        }
        cache.evict(key);
        return true;
    }

    public String generateKey() {
        String uuid = UUID.randomUUID().toString();
        cache.put(uuid, 1);
        return uuid;
    }

}
