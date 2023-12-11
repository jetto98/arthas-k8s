package com.alibaba.arthas.tunnel.server.node.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;

public class PodIpStore {
    private final static Logger logger = LoggerFactory.getLogger(PodIpStore.class);

    private Cache cache;

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public String getPodIp(String podName) {
        Cache.ValueWrapper valueWrapper = cache.get(podName);
        if (valueWrapper == null) {
            return null;
        }
        return (String) valueWrapper.get();
    }

    public void put(String podName, String podIp) {
        cache.put(podName, podIp);
    }

}
