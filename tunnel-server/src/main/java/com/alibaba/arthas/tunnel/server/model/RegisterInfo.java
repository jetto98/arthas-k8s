package com.alibaba.arthas.tunnel.server.model;

public class RegisterInfo {
    private String agentId;

    private String podIp;

    private String port;

    private boolean arthasOnline;

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public boolean isArthasOnline() {
        return arthasOnline;
    }

    public void setArthasOnline(boolean arthasOnline) {
        this.arthasOnline = arthasOnline;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getPodIp() {
        return podIp;
    }

    public void setPodIp(String podIp) {
        this.podIp = podIp;
    }
}
