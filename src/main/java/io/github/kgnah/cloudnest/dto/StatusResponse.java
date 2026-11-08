package io.github.kgnah.cloudnest.dto;

public class StatusResponse {

    private String message;
    private String version;
    private String status;

    public StatusResponse(String message, String version, String status) {
        this.message = message;
        this.version = version;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public String getVersion(){
        return version;
    }

    public String getStatus() {
        return status;
    }
}
