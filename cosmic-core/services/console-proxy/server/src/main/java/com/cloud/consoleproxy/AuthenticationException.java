package com.cloud.consoleproxy;

public class AuthenticationException extends Exception {
    private static final long serialVersionUID = -393139302884898842L;

    public AuthenticationException() {
        super();
    }

    public AuthenticationException(final String s) {
        super(s);
    }

    public AuthenticationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public AuthenticationException(final Throwable cause) {
        super(cause);
    }
}
