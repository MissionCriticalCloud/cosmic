//

//

package com.cloud.maint;

public class Version {
    public static String trimToPatch(String version) {
        final int index = version.indexOf("-");

        if (index > 0) {
            version = version.substring(0, index);
        }

        final String[] tokens = version.split("[.]");

        if (tokens.length < 3) {
            return "0";
        }
        return tokens[0] + "." + tokens[1] + "." + tokens[2];
    }

    public static String trimRouterVersion(final String version) {
        final String[] tokens = version.split(" ");
        if (tokens.length >= 3 && tokens[2].matches("[0-9]+(\\.[0-9]+)*")) {
            return tokens[2];
        }
        return "0";
    }

    public static void main(final String[] args) {
        System.out.println("Result is " + compare(args[0], args[1]));
    }

    /**
     * Compares two version strings and see which one is higher version.
     *
     * @param ver1
     * @param ver2
     * @return positive if ver1 is higher.  negative if ver1 is lower; zero if the same.
     */
    public static int compare(final String ver1, final String ver2) {
        final String[] tokens1 = ver1.split("[.]");
        final String[] tokens2 = ver2.split("[.]");
        //        assert(tokens1.length <= tokens2.length);

        final int compareLength = Math.min(tokens1.length, tokens2.length);
        for (int i = 0; i < compareLength; i++) {
            final long version1 = Long.parseLong(tokens1[i]);
            final long version2 = Long.parseLong(tokens2[i]);
            if (version1 != version2) {
                return version1 < version2 ? -1 : 1;
            }
        }

        if (tokens1.length > tokens2.length) {
            return 1;
        } else if (tokens1.length < tokens2.length) {
            return -1;
        }

        return 0;
    }
}
