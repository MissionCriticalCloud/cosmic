//

//

package com.cloud.utils;

import java.io.File;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtils {
    public static final Logger s_logger = LoggerFactory.getLogger(LogUtils.class);

    public static void initLog4j(final String log4jConfigFileName) {
        assert (log4jConfigFileName != null);
        File file = PropertiesUtil.findConfigFile(log4jConfigFileName);
        if (file != null) {
            s_logger.info("log4j configuration found at " + file.getAbsolutePath());
            DOMConfigurator.configureAndWatch(file.getAbsolutePath());
        } else {
            final String nameWithoutExtension = log4jConfigFileName.substring(0, log4jConfigFileName.lastIndexOf('.'));
            file = PropertiesUtil.findConfigFile(nameWithoutExtension + ".properties");
            if (file != null) {
                s_logger.info("log4j configuration found at " + file.getAbsolutePath());
                DOMConfigurator.configureAndWatch(file.getAbsolutePath());
            }
        }
    }
}
