package com.alibaba.arthas.tunnel.server.app.configuration;

import com.alibaba.arthas.tunnel.server.node.InMemoryNodeStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NodeStoreConfig {
    @Bean
    public InMemoryNodeStore inMemoryNodeStore(@Autowired CacheManager cacheManager) {
        Cache cache = cacheManager.getCache("inMemoryNodeStore");
        InMemoryNodeStore store = new InMemoryNodeStore();
        store.setCache(cache);
        return store;
    }
}
