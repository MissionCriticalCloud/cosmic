package com.cloud.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoCloseableUtil {
    private final static Logger s_logger = LoggerFactory.getLogger(AutoCloseableUtil.class);

    public static void closeAutoCloseable(final AutoCloseable ac, final String message) {
        try {

            if (ac != null) {
                ac.close();
            }
        } catch (final Exception e) {
            s_logger.warn("[ignored] " + message, e);
        }
    }
}
