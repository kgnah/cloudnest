package io.github.kgnah.cloudnest.exception;

public class DeploymentNotFoundException extends RuntimeException{

    public DeploymentNotFoundException(String message) {
        super(message);
    }
}
