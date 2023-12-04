package com.alibaba.arthas.tunnel.server.app.web;

import com.alibaba.arthas.tunnel.server.model.NodeInfo;
import com.alibaba.arthas.tunnel.server.node.InMemoryNodeStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/node")
public class ArthasAgentNodeController {

    @Autowired
    private InMemoryNodeStore nodeStore;


    @PostMapping("/init")
    public String initNode() {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        return uuid;
    }

    @PostMapping("/register/{key}")
    public String register(@PathVariable("key") String registerKey, @RequestBody NodeInfo nodeInfo) {
        nodeStore.addNode(nodeInfo.getName(), nodeInfo);
        return "ok";
    }

    @GetMapping("/all")
    public List<NodeInfo> getNodes() {
        return (List<NodeInfo>) nodeStore.allNodes();
    }

}
