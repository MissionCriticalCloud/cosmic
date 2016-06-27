//

//

package com.cloud.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UsernamePasswordValidator {
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9][a-zA-Z0-9@._-]{2,63}$";
    private static final String PASSWORD_PATTERN = "^[a-zA-Z0-9][a-zA-Z0-9@#+=._-]{2,31}$";
    private final Pattern usernamePattern;
    private final Pattern passwordPattern;
    private Matcher matcher;

    public UsernamePasswordValidator() {
        usernamePattern = Pattern.compile(USERNAME_PATTERN);
        passwordPattern = Pattern.compile(PASSWORD_PATTERN);
    }

    public boolean validateUsername(final String username) {
        matcher = usernamePattern.matcher(username);
        return matcher.matches();
    }

    public boolean validatePassword(final String password) {
        matcher = passwordPattern.matcher(password);
        return matcher.matches();
    }
}
