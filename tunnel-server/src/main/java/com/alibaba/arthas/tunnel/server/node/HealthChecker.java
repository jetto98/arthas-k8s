package com.alibaba.arthas.tunnel.server.node;

import com.alibaba.arthas.tunnel.server.model.HealthCheckInfo;
import com.alibaba.arthas.tunnel.server.model.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class HealthChecker {

    private final static Logger logger = LoggerFactory.getLogger(HealthChecker.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private InMemoryNodeStore nodeStore;

    private HealthCheckInfo info;

    @Async("asyncHealthCheckExecutor")
    public void checkNode(String name) throws InterruptedException {
        NodeInfo node = nodeStore.getNode(name);
        String url = node.httpAddress() + DefaultNodeEndpoint.HEALTH_CHECK;
        int n = 0;
        int err = 0;
        while (n < 3) {
            n++;
            try {
                String res = restTemplate.getForObject(url, String.class);
                if ("ok".equals(res)) {
                    logger.info("Node [{}] check ok", name);
                    return;
                } else {
                    err++;
                    logger.error("Node [{}] check failed, err: {}, try: {}", name, res, err);
                }
            } catch (RestClientException e) {
                err++;
                logger.error("Node [{}] check failed, err: {}, try: {}", name, e.getMessage(), err);
            }
            Thread.sleep(1000);
        }
        if (err > 0) {
            logger.info("Node [{}] offline", name);
            nodeStore.removeNode(name);
        }
    }
}
