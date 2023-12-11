package com.alibaba.arthas.tunnel.server.node.service;

import io.kubernetes.client.Exec;
import io.kubernetes.client.openapi.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringJoiner;

@Service
public class AgentInstallService {
    private final static Logger logger = LoggerFactory.getLogger(AgentInstallService.class);

    @Value("${agent.shell.url}")
    private String shellDownUrl;

    @Value("${agent.tar.url}")
    private String tarDownUrl;

    @Value("${self.host}")
    private String selfHost;

    private static final String AND = " && ";

    @Async("asyncKubeExecExecutor")
    public void install(String namespace, String podName, String containerName, String registerKey, String agentPort) throws IOException, ApiException, InterruptedException {
        Exec exec = new Exec();
        String[] command = {"sh", "-c", generateInstallCmd(registerKey, namespace, podName, agentPort)};
        Process process = exec.exec(namespace, podName, command, containerName, false, false);
        logger.info(command[2]);
        process.waitFor();
        try (InputStream inputStream = process.getInputStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("/n");
            }
            logger.info(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
    }

    private String generateInstallCmd(String registerKey, String namespace, String podName, String agentPort) {
        String remove = "rm -f as-agent.*";
        String getShell = String.format("wget %s -O as-agent.sh", shellDownUrl);
        String chmod = "chmod +x as-agent.sh";
        String registerUrl = String.format("http://%s:8080/api/node/register", selfHost);
        String agentId = namespace + "_" + podName;
        String runAgent = String.format("sh as-agent.sh %s %s %s %s %s",
                tarDownUrl, registerKey, agentId, registerUrl, agentPort);
        StringJoiner stringJoiner = new StringJoiner(AND);
        return stringJoiner
                .add(remove)
                .add(getShell)
                .add(chmod)
                .add(runAgent)
                .toString();
    }
}
