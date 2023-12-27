package com.alibaba.arthas.tunnel.server.model;

public class NodeInfo {

    private String ip;

    private String port;

    private String name;

    private boolean tunnelConnected;

    private String attachedPid;

    public String getAttachedPid() {
        return attachedPid;
    }

    public void setAttachedPid(String attachedPid) {
        this.attachedPid = attachedPid;
    }


    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isTunnelConnected() {
        return tunnelConnected;
    }

    public void setTunnelConnected(boolean tunnelConnected) {
        this.tunnelConnected = tunnelConnected;
    }

    public String httpAddress() {
        return "http://" + this.ip + ":" + this.port;
    }
}
