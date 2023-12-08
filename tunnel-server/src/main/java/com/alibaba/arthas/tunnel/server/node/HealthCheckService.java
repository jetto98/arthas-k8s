package com.alibaba.arthas.tunnel.server.node;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@DependsOn({"inMemoryNodeStore"})
public class HealthCheckService {


    @Autowired
    private InMemoryNodeStore inMemoryNodeStore;

    @Autowired
    private HealthChecker healthChecker;

    @Scheduled(cron = "0/15 * * * * ?")
    public void checkNodes() {
        Collection<String> nodes = inMemoryNodeStore.allNodeNames();
        nodes.forEach(n -> {
            try {
                healthChecker.checkNode(n);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}
