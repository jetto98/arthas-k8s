package com.alibaba.arthas.tunnel.server;

import com.alibaba.arthas.tunnel.server.model.HealthCheckInfo;
import com.alibaba.arthas.tunnel.server.node.InMemoryNodeStore;

public class HealthChecker implements Runnable {
    private InMemoryNodeStore nodeStore;

    private HealthCheckInfo info;

    public HealthChecker(InMemoryNodeStore nodeStore, HealthCheckInfo info) {
        this.nodeStore = nodeStore;
        this.info = info;
    }

    @Override
    public void run() {

    }
}
