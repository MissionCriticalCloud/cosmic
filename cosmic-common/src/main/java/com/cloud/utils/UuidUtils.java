package com.cloud.utils;

public class UuidUtils {

    public final static String first(final String uuid) {
        return uuid.substring(0, uuid.indexOf('-'));
    }
}
