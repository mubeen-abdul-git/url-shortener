package com.project.url_shortener.exception;

public class DuplicateAliasException extends RuntimeException {

    public DuplicateAliasException(String message) {
        super(message);
    }
}
