package com.alibaba.arthas.tunnel.server.app.web;

import com.alibaba.arthas.tunnel.server.app.exception.ServerException;
import com.alibaba.arthas.tunnel.server.model.NodeInfo;
import com.alibaba.arthas.tunnel.server.node.DefaultNodeEndpoint;
import com.alibaba.arthas.tunnel.server.node.InMemoryNodeStore;
import com.alibaba.arthas.tunnel.server.node.RegisterKeyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/node")
public class ArthasAgentNodeController {

    private final static Logger logger = LoggerFactory.getLogger(ArthasAgentNodeController.class);

    @Autowired
    private InMemoryNodeStore nodeStore;

    @Autowired
    private RegisterKeyStore keyStore;

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/init")
    public String initNode() {
        String uuid = keyStore.generateKey();
        return uuid;
    }

    @PostMapping("/register/{key}")
    public String register(@PathVariable("key") String registerKey, @RequestBody NodeInfo nodeInfo) {
        if (!keyStore.checkKey(registerKey)) {
            logger.error("Invalid register key.");
//            return "failed";
        }
        nodeStore.addNode(nodeInfo.getName(), nodeInfo);
        logger.info("node [{}] is registered.", nodeInfo.getName());
        return "ok";
    }

    @GetMapping("/all")
    public List<NodeInfo> getNodes() {
        return (List<NodeInfo>) nodeStore.allNodes();
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
        body.put("tunnelServer", "ws://127.0.0.1:7777/ws");
        String url = node.httpAddress() + DefaultNodeEndpoint.ATTACH;
        ResponseEntity<String> res = restTemplate.postForEntity(url, body, String.class);
        if (!res.getStatusCode().is2xxSuccessful()) {
            throw new ServerException(String.format("Node [%s] attach failed.", nodeName));
        }
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
}
