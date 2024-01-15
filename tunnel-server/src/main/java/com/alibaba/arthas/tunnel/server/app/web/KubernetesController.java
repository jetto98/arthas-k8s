package com.alibaba.arthas.tunnel.server.app.web;

import com.alibaba.arthas.tunnel.server.model.PodInfo;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/k8s")
public class KubernetesController {

    @Value("${kubernetes.namespace.whitelist}")
    private String whitelist;

    @GetMapping("/pods/{namespace}")
    public List<PodInfo> listPod(@PathVariable(value = "namespace") String namespace) throws ApiException {
        if (!isNamespaceValid(namespace)) {
            return new ArrayList<>();
        }
        CoreV1Api api = new CoreV1Api();
        V1PodList v1PodList = api.listNamespacedPod(namespace, null, null, null, null, null, null, null, null, null, false);
        List<PodInfo> res = new ArrayList<>();
        for (V1Pod item : v1PodList.getItems()) {
            if ("Running".equals(item.getStatus().getPhase()) && item.getMetadata().getDeletionTimestamp() == null) {
                PodInfo podInfo = new PodInfo();
                podInfo.setName(Objects.requireNonNull(item.getMetadata()).getName());
                podInfo.setPodIp(Objects.requireNonNull(item.getStatus()).getPodIP());
                podInfo.setNamespace(item.getMetadata().getNamespace());
                res.add(podInfo);
            }
        }
        return res;
    }

    @GetMapping("/pods/{namespace}/{name}")
    public List<String> getPodContainers(@PathVariable("namespace") String namespace,
                                         @PathVariable("name") String name) throws ApiException {
        if (!isNamespaceValid(namespace)) {
            return new ArrayList<>();
        }
        CoreV1Api api = new CoreV1Api();
        V1Pod pod = api.readNamespacedPod(name, namespace, null);
        return Objects.requireNonNull(Objects.requireNonNull(pod.getStatus()).getContainerStatuses())
                .stream()
                .map(V1ContainerStatus::getName)
                .collect(Collectors.toList());
    }

    @GetMapping("/namespaces")
    public List<String> listNamespaces() throws ApiException {
        CoreV1Api api = new CoreV1Api();
        List<String> res = new ArrayList<>();
        V1NamespaceList namespaces = api.listNamespace(null, null, null, null, null, null, null, null, null, null);
        for (V1Namespace item : namespaces.getItems()) {
            String ns = Objects.requireNonNull(item.getMetadata()).getName();
            if (isNamespaceValid(ns)) {
                res.add(ns);
            }
        }
        return res;
    }

    private boolean isNamespaceValid(String namespace) {
        return Arrays.asList(whitelist.split(",")).contains(namespace);
    }
}
