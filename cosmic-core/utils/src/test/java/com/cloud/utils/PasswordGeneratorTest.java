//

//

package com.cloud.utils;

import org.junit.Assert;
import org.junit.Test;

public class PasswordGeneratorTest {
    @Test
    public void generateRandomPassword() {
        // actual length is requested length, minimum length is 3
        Assert.assertTrue(PasswordGenerator.generateRandomPassword(0).length() == 3);
        Assert.assertTrue(PasswordGenerator.generateRandomPassword(1).length() == 3);
        Assert.assertTrue(PasswordGenerator.generateRandomPassword(5).length() == 5);
        final String password = PasswordGenerator.generateRandomPassword(8);

        Assert.assertTrue(containsDigit(password));
        Assert.assertTrue(containsLowercase(password));
        Assert.assertTrue(containsUppercase(password));
    }

    private boolean containsDigit(final String password) {
        for (final char c : password.toCharArray()) {
            if (Character.isDigit(c)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsLowercase(final String password) {
        for (final char c : password.toCharArray()) {
            if (Character.isLowerCase(c)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsUppercase(final String password) {
        for (final char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                return true;
            }
        }
        return false;
    }
}
