package com.example.Diplom.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class ResourceAuthorizedException extends RuntimeException {

    public ResourceAuthorizedException(String message) {
        super(message);
    }
}
