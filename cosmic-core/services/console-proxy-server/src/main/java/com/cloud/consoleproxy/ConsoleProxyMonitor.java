package com.cloud.consoleproxy;

import com.cloud.consoleproxy.util.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.xml.DOMConfigurator;

//
//
// I switched to a simpler solution to monitor only unrecoverable exceptions, under these cases, console proxy process will exit
// itself and the shell script will re-launch console proxy
//
public class ConsoleProxyMonitor {
    private static final Logger s_logger = Logger.getLogger(ConsoleProxyMonitor.class);

    private final String[] _argv;
    private final Map<String, String> _argMap = new HashMap<>();

    private volatile Process _process;
    private boolean _quit = false;

    public ConsoleProxyMonitor(final String[] argv) {
        _argv = argv;

        for (final String arg : _argv) {
            final String[] tokens = arg.split("=");
            if (tokens.length == 2) {
                s_logger.info("Add argument " + tokens[0] + "=" + tokens[1] + " to the argument map");

                _argMap.put(tokens[0].trim(), tokens[1].trim());
            } else {
                s_logger.warn("unrecognized argument, skip adding it to argument map");
            }
        }
    }

    public static void main(final String[] argv) {
        configLog4j();
        (new ConsoleProxyMonitor(argv)).run();
    }

    private static void configLog4j() {
        URL configUrl = System.class.getResource("/conf/log4j-cloud.xml");
        if (configUrl == null) {
            configUrl = ClassLoader.getSystemResource("log4j-cloud.xml");
        }

        if (configUrl == null) {
            configUrl = ClassLoader.getSystemResource("conf/log4j-cloud.xml");
        }

        if (configUrl != null) {
            try {
                System.out.println("Configure log4j using " + configUrl.toURI().toString());
            } catch (final URISyntaxException e1) {
                e1.printStackTrace();
            }

            try {
                final File file = new File(configUrl.toURI());

                System.out.println("Log4j configuration from : " + file.getAbsolutePath());
                DOMConfigurator.configureAndWatch(file.getAbsolutePath(), 10000);
            } catch (final URISyntaxException e) {
                System.out.println("Unable to convert log4j configuration Url to URI");
            }
        } else {
            System.out.println("Configure log4j with default properties");
        }
    }

    private void run() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                _quit = true;
                onShutdown();
            }
        });

        while (!_quit) {
            final String cmdLine = getLaunchCommandLine();

            s_logger.info("Launch console proxy process with command line: " + cmdLine);

            try {
                _process = Runtime.getRuntime().exec(cmdLine);
            } catch (final IOException e) {
                s_logger.error("Unexpected exception ", e);
                System.exit(1);
            }

            boolean waitSucceeded = false;
            int exitCode = 0;
            while (!waitSucceeded) {
                try {
                    exitCode = _process.waitFor();
                    waitSucceeded = true;

                    if (s_logger.isInfoEnabled()) {
                        s_logger.info("Console proxy process exits with code: " + exitCode);
                    }
                } catch (final InterruptedException e) {
                    if (s_logger.isInfoEnabled()) {
                        s_logger.info("InterruptedException while waiting for termination of console proxy, will retry");
                    }
                }
            }
        }
    }

    private void onShutdown() {
        if (_process != null) {
            if (s_logger.isInfoEnabled()) {
                s_logger.info("Console proxy monitor shuts dwon, terminate console proxy process");
            }
            _process.destroy();
        }
    }

    private String getLaunchCommandLine() {
        final StringBuffer sb = new StringBuffer("java ");
        final String jvmOptions = _argMap.get("jvmoptions");

        if (jvmOptions != null) {
            sb.append(jvmOptions);
        }

        for (final Map.Entry<String, String> entry : _argMap.entrySet()) {
            if (!"jvmoptions".equalsIgnoreCase(entry.getKey())) {
                sb.append(" ").append(entry.getKey()).append("=").append(entry.getValue());
            }
        }

        return sb.toString();
    }
}
