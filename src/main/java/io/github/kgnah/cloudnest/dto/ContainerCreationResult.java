package io.github.kgnah.cloudnest.dto;

public class ContainerCreationResult {
    private String containerId;
    private int hostPort;

    // constructor
    public ContainerCreationResult(String containerId, int hostPort) {
        this.containerId = containerId;
        this.hostPort = hostPort;
    }

    // getters
    public String getContainerId() {return containerId;}
    public int getHostPort() {return hostPort;}
}
