package com.alibaba.arthas.tunnel.server.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = { "com.alibaba.arthas.tunnel.server.app",
        "com.alibaba.arthas.tunnel.server.endpoint", "com.alibaba.arthas.tunnel.server.node" })
@EnableCaching
@EnableScheduling
@EnableAsync
public class ArthasTunnelApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArthasTunnelApplication.class, args);
    }

}
