//

//

package com.cloud.utils;

import org.apache.xerces.impl.xpath.regex.RegularExpression;

public class UuidUtils {

    public final static String first(final String uuid) {
        return uuid.substring(0, uuid.indexOf('-'));
    }

    public static boolean validateUUID(final String uuid) {
        final RegularExpression regex = new RegularExpression("[0-9a-fA-F]{8}(?:-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}");
        return regex.matches(uuid);
    }
}
