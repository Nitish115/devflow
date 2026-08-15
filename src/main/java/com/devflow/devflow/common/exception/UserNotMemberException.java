package com.devflow.devflow.common.exception;

public class UserNotMemberException extends RuntimeException {
    public UserNotMemberException(String message) {
        super(message);
    }
}
