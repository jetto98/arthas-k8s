package com.alibaba.arthas.tunnel.server.app.configuration;

import com.alibaba.arthas.tunnel.server.node.store.InMemoryNodeStore;
import com.alibaba.arthas.tunnel.server.node.store.PodIpStore;
import com.alibaba.arthas.tunnel.server.node.store.RegisterKeyStore;
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
    @Bean
    public RegisterKeyStore registerKeyStore(@Autowired CacheManager cacheManager) {
        Cache cache = cacheManager.getCache("registerKeyStore");
        RegisterKeyStore store = new RegisterKeyStore();
        store.setCache(cache);
        return store;
    }
    @Bean
    public PodIpStore podIpStore(@Autowired CacheManager cacheManager) {
        Cache cache = cacheManager.getCache("podIpStore");
        PodIpStore store = new PodIpStore();
        store.setCache(cache);
        return store;
    }
}
