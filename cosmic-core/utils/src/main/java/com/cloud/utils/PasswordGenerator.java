//

//

package com.cloud.utils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Generate random passwords
 */
public class PasswordGenerator {
    //Leave out visually confusing  l,L,1,o,O,0
    static private final char[] lowerCase = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    static private final char[] upperCase = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
    static private final char[] numeric = new char[]{'2', '3', '4', '5', '6', '7', '8', '9'};

    static private final char[] alphaNumeric = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y',
            'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '2', '3', '4', '5', '6', '7', '8', '9'};

    static private final int minLength = 3;

    public static String generateRandomPassword(final int num) {
        final Random r = new SecureRandom();
        final StringBuilder password = new StringBuilder();

        //Guard for num < minLength
        if (num < minLength) {
            //Add alphanumeric chars at random
            for (int i = 0; i < minLength; i++) {
                password.append(generateAlphaNumeric(r));
            }
        } else {
            final List<Character> passwordChars = new ArrayList<>();
            passwordChars.add(generateLowercaseChar(r));
            passwordChars.add(generateUppercaseChar(r));
            passwordChars.add(generateDigit(r));

            for (int i = passwordChars.size(); i < num; i++) {
                passwordChars.add(generateAlphaNumeric(r));
            }

            Collections.shuffle(passwordChars, new SecureRandom());

            for (final char c : passwordChars) {
                password.append(c);
            }
        }

        return password.toString();
    }

    private static char generateAlphaNumeric(final Random r) {
        return alphaNumeric[r.nextInt(alphaNumeric.length)];
    }

    private static char generateLowercaseChar(final Random r) {
        return lowerCase[r.nextInt(lowerCase.length)];
    }

    private static char generateUppercaseChar(final Random r) {
        return upperCase[r.nextInt(upperCase.length)];
    }

    private static char generateDigit(final Random r) {
        return numeric[r.nextInt(numeric.length)];
    }

    public static String generatePresharedKey(final int numChars) {
        final Random r = new SecureRandom();
        final StringBuilder psk = new StringBuilder();
        for (int i = 0; i < numChars; i++) {
            psk.append(generateAlphaNumeric(r));
        }
        return psk.toString();
    }
}
