package com.sparta.velog.exception.runtime;

/**
 * 회원가입 시 DB에 username, email, nickname이 이미 존재할 경우 발생
 */
public class DuplicateUserInfoException extends RuntimeException {
    public DuplicateUserInfoException(String message) {
        super(message);
    }

    public DuplicateUserInfoException(String message, Throwable cause) {
        super(message, cause);
    }
}