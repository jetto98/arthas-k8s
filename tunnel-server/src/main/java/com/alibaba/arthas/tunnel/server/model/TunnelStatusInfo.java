package com.alibaba.arthas.tunnel.server.model;

public class TunnelStatusInfo {
    private String agentId;

    private String attachedPid;

    private boolean isConnected;

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getAttachedPid() {
        return attachedPid;
    }

    public void setAttachedPid(String attachedPid) {
        this.attachedPid = attachedPid;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }
}
