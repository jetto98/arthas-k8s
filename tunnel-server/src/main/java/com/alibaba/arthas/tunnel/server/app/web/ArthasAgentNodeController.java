package com.alibaba.arthas.tunnel.server.app.web;

import com.alibaba.arthas.tunnel.server.AgentClusterInfo;
import com.alibaba.arthas.tunnel.server.app.exception.ServerException;
import com.alibaba.arthas.tunnel.server.cluster.TunnelClusterStore;
import com.alibaba.arthas.tunnel.server.model.NodeInfo;
import com.alibaba.arthas.tunnel.server.model.PodInfo;
import com.alibaba.arthas.tunnel.server.model.TunnelStatusInfo;
import com.alibaba.arthas.tunnel.server.node.DefaultNodeEndpoint;
import com.alibaba.arthas.tunnel.server.node.service.AgentInstallService;
import com.alibaba.arthas.tunnel.server.node.service.HealthChecker;
import com.alibaba.arthas.tunnel.server.node.store.InMemoryNodeStore;
import com.alibaba.arthas.tunnel.server.node.store.PodIpStore;
import com.alibaba.arthas.tunnel.server.node.store.RegisterKeyStore;
import io.kubernetes.client.openapi.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/node")
public class ArthasAgentNodeController {

    private final static Logger logger = LoggerFactory.getLogger(ArthasAgentNodeController.class);

    @Autowired
    private InMemoryNodeStore nodeStore;

    @Autowired
    private TunnelClusterStore clusterStore;

    @Autowired
    private PodIpStore podIpStore;

    @Autowired
    private RegisterKeyStore keyStore;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AgentInstallService installService;

    @Autowired
    private HealthChecker healthChecker;

    @Value("${self.host}")
    private String wsHost;

    @Value("${agent.port}")
    private String agentPort;

    @PostMapping("/init")
    public String initNode(@RequestBody PodInfo podInfo) throws IOException, InterruptedException, ApiException {
        String nodeName = podInfo.getNamespace() + "_" + podInfo.getName();
        if (null != nodeStore.getNode(nodeName)) {
            return "[warn]节点已存在";
        }
        if (healthChecker.checkPodAgent(podInfo)) {
            NodeInfo nodeInfo = new NodeInfo(podInfo.getPodIp(), agentPort, nodeName);
            nodeStore.addNode(nodeName, nodeInfo);
            return "[warn]节点已存在agent, 已重新添加";
        }
        String key = keyStore.generateKey();
        podIpStore.put(nodeName, podInfo.getPodIp());
        installService.install(podInfo.getNamespace(), podInfo.getName(), podInfo.getContainerName(), key, agentPort);
        return key;
    }

    @GetMapping("/init/status/{key}")
    public String initStatus(@PathVariable("key") String registerKey) {
        if (!keyStore.checkKey(registerKey)) {
            return "ok";
        }
        if (keyStore.secondsSinceGenerate(registerKey) > 60) {
            return "failed";
        }
        return "pending";
    }

    @PostMapping("/register/{key}")
    public String register(@PathVariable("key") String registerKey, @RequestBody NodeInfo nodeInfo) {
        if (!keyStore.checkKey(registerKey)) {
            logger.error("Invalid register key.");
            return "failed";
        }
        if (!StringUtils.hasLength(nodeInfo.getIp())) {
            String podIp = podIpStore.getPodIp(nodeInfo.getName());
            if (!StringUtils.hasLength(podIp)) {
                logger.info("node [{}] register failed, error: invalid ip.", nodeInfo.getName());
                return "failed";
            }
            nodeInfo.setIp(podIp);
        }
        nodeStore.addNode(nodeInfo.getName(), nodeInfo);
        keyStore.removeKey(registerKey);
        logger.info("node [{}] is registered.", nodeInfo.getName());
        return "ok";
    }

    @GetMapping("/all")
    public List<NodeInfo> getNodes() {
        List<NodeInfo> nodeInfos = (List<NodeInfo>) nodeStore.allNodes();
        nodeInfos.forEach(n -> n.setTunnelConnected(null != clusterStore.findAgent(n.getName())));
        return nodeInfos;
    }

    @GetMapping("/tunnel/{agentId}/status")
    public TunnelStatusInfo checkTunnelEnable(@PathVariable("agentId") String agentId) throws ServerException {
        AgentClusterInfo agent = clusterStore.findAgent(agentId);
        NodeInfo node = nodeStore.getNode(agentId);
        if (node == null) {
            throw new ServerException("该节点不存在！");
        }
        TunnelStatusInfo tunnelStatusInfo = new TunnelStatusInfo();
        tunnelStatusInfo.setAgentId(agentId);
        tunnelStatusInfo.setConnected(true);
        tunnelStatusInfo.setAttachedPid(node.getAttachedPid());
        if (agent == null) {
            tunnelStatusInfo.setConnected(false);
        }
        return tunnelStatusInfo;
    }

    @GetMapping("/{id}/jps")
    public List<?> getJavaProcess(@PathVariable("id") String nodeName) throws ServerException {
        NodeInfo node = nodeStore.getNode(nodeName);
        if (null == node) {
            throw new ServerException(String.format("Node name [%s] does not exist.", nodeName));
        }
        String url = node.httpAddress() + DefaultNodeEndpoint.JPS;
        return restTemplate.getForObject(url, List.class);
    }

    @GetMapping("/{id}/attach")
    public String attachJavaProcess(@PathVariable("id") String nodeName,
                                    @RequestParam("pid") String pid) throws ServerException {
        NodeInfo node = nodeStore.getNode(nodeName);
        if (null == node) {
            throw new ServerException(String.format("Node name [%s] does not exist.", nodeName));
        }
        HashMap<String, String> body = new HashMap<>();
        body.put("pid", pid);
        body.put("agentId", nodeName);
        // todo: dynamic get
        body.put("tunnelServer", String.format("ws://%s:7777/ws", wsHost));
        String url = node.httpAddress() + DefaultNodeEndpoint.ATTACH;
        ResponseEntity<String> res = restTemplate.postForEntity(url, body, String.class);
        if (!res.getStatusCode().is2xxSuccessful()) {
            throw new ServerException(String.format("Node [%s] attach failed.", nodeName));
        }
        node.setAttachedPid(pid);
        nodeStore.addNode(nodeName, node);
        return res.getBody();
    }

    @GetMapping("/{id}/stop")
    public String stopArthas(@PathVariable("id") String nodeName) throws ServerException {
        NodeInfo node = nodeStore.getNode(nodeName);
        if (null == node) {
            throw new ServerException(String.format("Node name [%s] does not exist.", nodeName));
        }
        String url = node.httpAddress() + DefaultNodeEndpoint.STOP;
        ResponseEntity<String> res = restTemplate.getForEntity(url, String.class);
        if (!res.getStatusCode().is2xxSuccessful()) {
            throw new ServerException(String.format("Node [%s] stop failed.", nodeName));
        }
        return res.getBody();
    }

    @PostMapping("/proxy/{id}")
    public ResponseEntity<?> apiForward(@PathVariable("id") String nodeName, @RequestBody String body) throws ServerException {
        logger.info("proxy:{},{}", nodeName, body);
        NodeInfo node = nodeStore.getNode(nodeName);
        if (null == node) {
            throw new ServerException(String.format("Node name [%s] does not exist.", nodeName));
        }
        String url = node.arthasApiAddress();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        return restTemplate.postForEntity(url, request, Object.class);
    }

}
