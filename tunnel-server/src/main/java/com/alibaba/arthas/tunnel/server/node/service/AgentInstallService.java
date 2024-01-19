package com.alibaba.arthas.tunnel.server.node.service;

import io.kubernetes.client.Exec;
import io.kubernetes.client.openapi.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.StringJoiner;

@Service
public class AgentInstallService {
    private final static Logger logger = LoggerFactory.getLogger(AgentInstallService.class);

    @Value("${agent.tar.url}")
    private String tarDownUrl;

    @Value("${agent.tar.path}")
    private String agentPath;

    @Value("${self.host}")
    private String selfHost;

    private static final String AGENT_DEFAULT_PATH = "/as-agent.tar.gz";

    private static final String AND = " && ";

    @Async("asyncKubeExecExecutor")
    public void install(String namespace, String podName, String containerName, String registerKey, String agentPort) throws IOException, ApiException, InterruptedException {
        logger.info("start install agent to {}|{}|{}", namespace, podName, containerName);
        Exec exec = new Exec();
        String[] command = {"sh", "-c", generateInstallCmd(registerKey, namespace, podName, agentPort, checkCMDInPod(namespace, podName, containerName, "wget"))};
        logger.info(command[2]);
        exec.exec(namespace, podName, command, containerName, false, false);
    }

    private String generateInstallCmd(String registerKey, String namespace, String podName, String agentPort, boolean useWget) {
        String download = String.format("wget %s -O /as-agent.tar.gz", tarDownUrl);
        if (!useWget) {
            download = String.format("curl -o /as-agent.tar.gz %s", tarDownUrl);
        }
        String unzip = "tar zxvf /as-agent.tar.gz -C /";
        String chmod = "chmod 777 /as-agent/asnode";
        String registerUrl = String.format("http://%s:8080/api/node/register", selfHost);
        String agentId = namespace + "_" + podName;
        String runAgent = String.format("/as-agent/asnode -k %s -n %s --register-url %s -p %s",
                registerKey, agentId, registerUrl, agentPort);
        StringJoiner stringJoiner = new StringJoiner(AND);
        return stringJoiner
                .add(download)
                .add(unzip)
                .add(chmod)
                .add(runAgent)
                .toString();
    }

    private static boolean checkFileInPod(String namespace, String podName, String containerName, String filePath) {
        try {
            // 使用 Exec 对象执行命令
            Exec exec = new Exec();

            // 构建命
            String[] command = {
                    "sh", "-c", "test -e " + filePath
            };

            // 执行命令
            Process process = exec.exec(namespace, podName, command, containerName, true, true);
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            logger.error("checkFileInPod error", e);
            return false;
        }
    }

    private static boolean checkCMDInPod(String namespace, String podName, String containerName, String cmd) {
        try {
            // 使用 Exec 对象执行命令
            Exec exec = new Exec();

            // 构建命令，要执行的命令不要用逗号隔开！！！！
            String[] command = {
                    "sh", "-c", "type " + cmd
            };

            logger.info(Arrays.toString(command));
            // 执行命令
            Process process = exec.exec(namespace, podName, command, containerName, true, false);
            int e = process.waitFor();
            logger.info("exit code: {}", e);
            return process.exitValue() == 0;
        } catch (Exception e) {
            logger.error("checkCMDInPod error", e);
            return false;
        }
    }
}
