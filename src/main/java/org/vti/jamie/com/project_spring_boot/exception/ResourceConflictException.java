package org.vti.jamie.com.project_spring_boot.exception;

public class ResourceConflictException
        extends RuntimeException {

    public ResourceConflictException(String message) {
        super(message);
    }
}