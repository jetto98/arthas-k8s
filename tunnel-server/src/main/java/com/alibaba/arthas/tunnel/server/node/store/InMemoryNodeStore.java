package com.alibaba.arthas.tunnel.server.node.store;

import com.alibaba.arthas.tunnel.server.model.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class InMemoryNodeStore {
    private final static Logger logger = LoggerFactory.getLogger(InMemoryNodeStore.class);

    private Cache cache;

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public NodeInfo getNode(String name) {
        Cache.ValueWrapper valueWrapper = cache.get(name);
        if (valueWrapper == null) {
            return null;
        }

        return (NodeInfo) valueWrapper.get();
    }

    public void addNode(String name, NodeInfo nodeInfo) {
        cache.put(name, nodeInfo);
    }

    public void removeNode(String name) {
        cache.evict(name);
    }

    public Collection<String> allNodeNames() {
        CaffeineCache caffeineCache = (CaffeineCache) cache;
        com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
        return (Collection<String>) (Collection<?>) nativeCache.asMap().keySet();
    }

    public Collection<NodeInfo> allNodes() {
        Collection<NodeInfo> res = new ArrayList<>();
        CaffeineCache caffeineCache = (CaffeineCache) cache;
        com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
        ConcurrentMap<String, NodeInfo> map = (ConcurrentMap<String, NodeInfo>) (ConcurrentMap<?, ?>) nativeCache.asMap();
        for (Map.Entry<String, NodeInfo> item : map.entrySet()) {
            res.add(item.getValue());
        }
        return res;
    }
}
