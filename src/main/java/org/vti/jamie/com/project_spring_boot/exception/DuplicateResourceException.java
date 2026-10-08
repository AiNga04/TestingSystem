package org.vti.jamie.com.project_spring_boot.exception;

public class DuplicateResourceException
        extends ResourceConflictException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}