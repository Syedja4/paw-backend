package com.pawnavz.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "RES_001", message);
    }

    public ResourceNotFoundException(String resource, String id) {
        super(HttpStatus.NOT_FOUND, "RES_001", resource + " not found with id: " + id);
    }
}
