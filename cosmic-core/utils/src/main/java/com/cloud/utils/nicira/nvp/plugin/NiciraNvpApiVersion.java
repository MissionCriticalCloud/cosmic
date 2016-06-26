//

//

package com.cloud.utils.nicira.nvp.plugin;

import com.cloud.maint.Version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NiciraNvpApiVersion {
    private static final Logger s_logger = LoggerFactory.getLogger(NiciraNvpApiVersion.class);

    private static String niciraApiVersion;

    public static synchronized void setNiciraApiVersion(final String apiVersion) {
        niciraApiVersion = apiVersion;
    }

    public static synchronized boolean isApiVersionLowerThan(final String apiVersion) {
        if (niciraApiVersion == null) {
            return false;
        }
        final int compare = Version.compare(niciraApiVersion, apiVersion);
        return (compare < 0);
    }

    public static synchronized void logNiciraApiVersion() {
        s_logger.info("NSX API VERSION: " + ((niciraApiVersion != null) ? niciraApiVersion : " NOT PRESENT"));
    }
}
