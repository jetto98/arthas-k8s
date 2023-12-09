package com.alibaba.arthas.tunnel.server.app.configuration;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.ClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Configuration
public class KubernetesClientConfig {

    private final static Logger logger = LoggerFactory.getLogger(KubernetesClientConfig.class);

    @PostConstruct
    public void initKubeConfig() throws IOException {
        logger.info("Init inCluster kube config");
        ApiClient apiClient = ClientBuilder.cluster().build();
        io.kubernetes.client.openapi.Configuration.setDefaultApiClient(apiClient);
    }
}
