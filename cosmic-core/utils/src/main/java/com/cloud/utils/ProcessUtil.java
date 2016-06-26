//

//

package com.cloud.utils;

import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.script.OutputInterpreter;
import com.cloud.utils.script.Script;

import javax.naming.ConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessUtil {
    private static final Logger s_logger = LoggerFactory.getLogger(ProcessUtil.class.getName());

    // paths cannot be hardcoded
    public static void pidCheck(final String pidDir, final String run) throws ConfigurationException {

        String dir = pidDir == null ? "/var/run" : pidDir;

        try {
            final File propsFile = PropertiesUtil.findConfigFile("environment.properties");
            if (propsFile == null) {
                s_logger.debug("environment.properties could not be opened");
            } else {
                final Properties props = PropertiesUtil.loadFromFile(propsFile);
                dir = props.getProperty("paths.pid");
                if (dir == null) {
                    dir = pidDir == null ? "/var/run" : pidDir;
                }
            }
        } catch (final IOException e) {
            s_logger.debug("environment.properties could not be opened");
        }

        final File pidFile = new File(dir + File.separator + run);
        try {
            if (!pidFile.createNewFile()) {
                if (!pidFile.exists()) {
                    throw new ConfigurationException("Unable to write to " + pidFile.getAbsolutePath() + ".  Are you sure you're running as root?");
                }

                final String pidLine = FileUtils.readFileToString(pidFile).trim();
                if (pidLine.isEmpty()) {
                    throw new ConfigurationException("Java process is being started twice.  If this is not true, remove " + pidFile.getAbsolutePath());
                }
                try {
                    final long pid = Long.parseLong(pidLine);
                    final Script script = new Script("bash", 120000, s_logger);
                    script.add("-c", "ps -p " + pid);
                    final String result = script.execute();
                    if (result == null) {
                        throw new ConfigurationException("Java process is being started twice.  If this is not true, remove " + pidFile.getAbsolutePath());
                    }
                    if (!pidFile.delete()) {
                        throw new ConfigurationException("Java process is being started twice.  If this is not true, remove " + pidFile.getAbsolutePath());
                    }
                    if (!pidFile.createNewFile()) {
                        throw new ConfigurationException("Java process is being started twice.  If this is not true, remove " + pidFile.getAbsolutePath());
                    }
                } catch (final NumberFormatException e) {
                    throw new ConfigurationException("Java process is being started twice.  If this is not true, remove " + pidFile.getAbsolutePath());
                }
            }
            pidFile.deleteOnExit();

            final Script script = new Script("bash", 120000, s_logger);
            script.add("-c", "echo $PPID");
            final OutputInterpreter.OneLineParser parser = new OutputInterpreter.OneLineParser();
            script.execute(parser);

            final String pid = parser.getLine();

            FileUtils.writeStringToFile(pidFile, pid + "\n");
        } catch (final IOException e) {
            throw new CloudRuntimeException("Unable to create the " + pidFile.getAbsolutePath() + ".  Are you running as root?", e);
        }
    }

    public static String dumpStack() {
        final StringBuilder sb = new StringBuilder();
        final StackTraceElement[] elems = Thread.currentThread().getStackTrace();
        if (elems != null && elems.length > 0) {
            for (final StackTraceElement elem : elems) {
                sb.append("\tat ").append(elem.getMethodName()).append("(").append(elem.getFileName()).append(":").append(elem.getLineNumber()).append(")\n");
            }
        }
        return sb.toString();
    }
}
