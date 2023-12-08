package com.alibaba.arthas.tunnel.server.model;

public class JpsInfo {
    String pid;

    String packageName;

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
