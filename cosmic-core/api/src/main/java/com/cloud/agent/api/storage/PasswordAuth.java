package com.cloud.agent.api.storage;

/**
 * Password authentication
 */
public class PasswordAuth {

    String userName;
    String password;

    public PasswordAuth() {

    }

    public PasswordAuth(final String user, final String password) {
        this.userName = user;
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
}
