package com.alibaba.arthas.tunnel.server.node.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;

import java.util.Date;
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
        return true;
    }

    public void removeKey(String key) {
        cache.evict(key);
    }

    public long secondsSinceGenerate(String key) {
        Cache.ValueWrapper valueWrapper = cache.get(key);
        if (valueWrapper == null) {
            return Long.MAX_VALUE;
        }
        Date start = (Date) valueWrapper;
        long ms = (new Date()).getTime() - start.getTime();
        return ms / 1000;
    }

    public String generateKey() {
        String uuid = UUID.randomUUID().toString();
        cache.put(uuid, new Date());
        return uuid;
    }

}
