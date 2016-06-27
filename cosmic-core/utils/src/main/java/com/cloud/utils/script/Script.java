//

//

package com.cloud.utils.script;

import com.cloud.utils.PropertiesUtil;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.script.OutputInterpreter.TimedOutLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Script implements Callable<String> {
    public static final String ERR_EXECUTE = "execute.error";
    public static final String ERR_TIMEOUT = "timeout";
    private static final Logger s_logger = LoggerFactory.getLogger(Script.class);
    private static final ScheduledExecutorService s_executors = Executors.newScheduledThreadPool(10, new NamedThreadFactory("Script"));
    private final Logger _logger;
    String _workDir;
    ArrayList<String> _command;
    long _timeout;
    Process _process;
    Thread _thread;
    private final int _defaultTimeout = 3600 * 1000; /* 1 hour */
    private volatile boolean _isTimeOut = false;
    private boolean _passwordCommand = false;

    public Script(final boolean runWithSudo, final String command, final long timeout, final Logger logger) {
        this(command, timeout, logger);
        if (runWithSudo) {
            _command.add(0, "sudo");
        }
    }

    public Script(final String command, final long timeout, final Logger logger) {
        _command = new ArrayList<>();
        _command.add(command);
        _timeout = timeout;
        if (_timeout == 0) {
            /* always using default timeout 1 hour to avoid thread hang */
            _timeout = _defaultTimeout;
        }
        _process = null;
        _logger = logger != null ? logger : s_logger;
    }

    public Script(final String command, final Logger logger) {
        this(command, 0, logger);
    }

    public Script(final String command) {
        this(command, 0, s_logger);
    }

    public Script(final String command, final long timeout) {
        this(command, timeout, s_logger);
    }

    public static String findScript(String path, final String script) {
        s_logger.debug("Looking for " + script + " in the classpath");

        URL url = ClassLoader.getSystemResource(script);
        s_logger.debug("System resource: " + url);
        File file = null;
        if (url != null) {
            file = new File(url.getFile());
            s_logger.debug("Absolute path =  " + file.getAbsolutePath());
            return file.getAbsolutePath();
        }

        if (path == null) {
            s_logger.warn("No search path specified, unable to look for " + script);
            return null;
        }
        path = path.replace("/", File.separator);

        /**
         * Look in WEB-INF/classes of the webapp
         * URI workaround the URL encoding of url.getFile
         */
        if (path.endsWith(File.separator)) {
            url = Script.class.getClassLoader().getResource(path + script);
        } else {
            url = Script.class.getClassLoader().getResource(path + File.separator + script);
        }
        s_logger.debug("Classpath resource: " + url);
        if (url != null) {
            try {
                file = new File(new URI(url.toString()).getPath());
                s_logger.debug("Absolute path =  " + file.getAbsolutePath());
                return file.getAbsolutePath();
            } catch (final URISyntaxException e) {
                s_logger.warn("Unable to convert " + url.toString() + " to a URI");
            }
        }

        if (path.endsWith(File.separator)) {
            path = path.substring(0, path.lastIndexOf(File.separator));
        }

        if (path.startsWith(File.separator)) {
            // Path given was absolute so we assume the caller knows what they want.
            file = new File(path + File.separator + script);
            return file.exists() ? file.getAbsolutePath() : null;
        }

        s_logger.debug("Looking for " + script);
        String search = null;
        for (int i = 0; i < 3; i++) {
            if (i == 0) {
                String cp = Script.class.getResource(Script.class.getSimpleName() + ".class").toExternalForm();
                int begin = cp.indexOf(File.separator);

                // work around with the inconsistency of java classpath and file separator on Windows 7
                if (begin < 0) {
                    begin = cp.indexOf('/');
                }

                final int endBang = cp.lastIndexOf("!");
                int end = cp.lastIndexOf(File.separator, endBang);
                if (end < 0) {
                    end = cp.lastIndexOf('/', endBang);
                }
                if (end < 0) {
                    cp = cp.substring(begin);
                } else {
                    cp = cp.substring(begin, end);
                }

                s_logger.debug("Current binaries reside at " + cp);
                search = cp;
            } else if (i == 1) {
                s_logger.debug("Searching in environment.properties");
                try {
                    final File propsFile = PropertiesUtil.findConfigFile("environment.properties");
                    if (propsFile == null) {
                        s_logger.debug("environment.properties could not be opened");
                    } else {
                        final Properties props = PropertiesUtil.loadFromFile(propsFile);
                        search = props.getProperty("paths.script");
                    }
                } catch (final IOException e) {
                    s_logger.debug("environment.properties could not be opened");
                    continue;
                }
                s_logger.debug("environment.properties says scripts should be in " + search);
            } else {
                s_logger.debug("Searching in the current directory");
                search = ".";
            }

            search += File.separatorChar + path + File.separator;
            do {
                search = search.substring(0, search.lastIndexOf(File.separator));
                file = new File(search + File.separator + script);
                s_logger.debug("Looking for " + script + " in " + file.getAbsolutePath());
            } while (!file.exists() && search.lastIndexOf(File.separator) != -1);

            if (file.exists()) {
                return file.getAbsolutePath();
            }
        }

        search = System.getProperty("paths.script");

        search += File.separatorChar + path + File.separator;
        do {
            search = search.substring(0, search.lastIndexOf(File.separator));
            file = new File(search + File.separator + script);
            s_logger.debug("Looking for " + script + " in " + file.getAbsolutePath());
        } while (!file.exists() && search.lastIndexOf(File.separator) != -1);

        if (file.exists()) {
            return file.getAbsolutePath();
        }

        s_logger.warn("Unable to find script " + script);
        return null;
    }

    public static String runSimpleBashScript(final String command) {
        return Script.runSimpleBashScript(command, 0);
    }

    public static String runSimpleBashScript(final String command, final int timeout) {

        final Script s = new Script("/bin/bash", timeout);
        s.add("-c");
        s.add(command);

        final OutputInterpreter.OneLineParser parser = new OutputInterpreter.OneLineParser();
        if (s.execute(parser) != null) {
            return null;
        }

        final String result = parser.getLine();
        if (result == null || result.trim().isEmpty()) {
            return null;
        } else {
            return result.trim();
        }
    }

    public void add(final String param) {
        _command.add(param);
    }

    public String execute(final OutputInterpreter interpreter) {
        final String[] command = _command.toArray(new String[_command.size()]);

        if (_logger.isDebugEnabled()) {
            _logger.debug("Executing: " + buildCommandLine(command));
        }

        try {
            final ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            if (_workDir != null) {
                pb.directory(new File(_workDir));
            }

            _process = pb.start();
            if (_process == null) {
                _logger.warn("Unable to execute: " + buildCommandLine(command));
                return "Unable to execute the command: " + command[0];
            }

            final BufferedReader ir = new BufferedReader(new InputStreamReader(_process.getInputStream()));

            _thread = Thread.currentThread();
            ScheduledFuture<String> future = null;
            if (_timeout > 0) {
                future = s_executors.schedule(this, _timeout, TimeUnit.MILLISECONDS);
            }

            Task task = null;
            if (interpreter != null && interpreter.drain()) {
                task = new Task(interpreter, ir);
                s_executors.execute(task);
            }

            while (true) {
                try {
                    if (_process.waitFor() == 0) {
                        _logger.debug("Execution is successful.");
                        if (interpreter != null) {
                            return interpreter.drain() ? task.getResult() : interpreter.interpret(ir);
                        } else {
                            // null return exitValue apparently
                            return String.valueOf(_process.exitValue());
                        }
                    } else {
                        break;
                    }
                } catch (final InterruptedException e) {
                    if (!_isTimeOut) {
                        /*
                         * This is not timeout, we are interrupted by others,
                         * continue
                         */
                        _logger.debug("We are interrupted but it's not a timeout, just continue");
                        continue;
                    }

                    final TimedOutLogger log = new TimedOutLogger(_process);
                    final Task timedoutTask = new Task(log, ir);

                    timedoutTask.run();
                    if (!_passwordCommand) {
                        _logger.warn("Timed out: " + buildCommandLine(command) + ".  Output is: " + timedoutTask.getResult());
                    } else {
                        _logger.warn("Timed out: " + buildCommandLine(command));
                    }

                    return ERR_TIMEOUT;
                } finally {
                    if (future != null) {
                        future.cancel(false);
                    }
                    Thread.interrupted();
                }
            }

            _logger.debug("Exit value is " + _process.exitValue());

            final BufferedReader reader = new BufferedReader(new InputStreamReader(_process.getInputStream()), 128);

            final String error;
            if (interpreter != null) {
                error = interpreter.processError(reader);
            } else {
                error = String.valueOf(_process.exitValue());
            }

            if (_logger.isDebugEnabled()) {
                _logger.debug(error);
            }
            return error;
        } catch (final SecurityException ex) {
            _logger.warn("Security Exception....not running as root?", ex);
            return stackTraceAsString(ex);
        } catch (final Exception ex) {
            _logger.warn("Exception: " + buildCommandLine(command), ex);
            return stackTraceAsString(ex);
        } finally {
            if (_process != null) {
                IOUtils.closeQuietly(_process.getErrorStream());
                IOUtils.closeQuietly(_process.getOutputStream());
                IOUtils.closeQuietly(_process.getInputStream());
                _process.destroy();
            }
        }
    }

    protected String buildCommandLine(final String[] command) {
        final StringBuilder builder = new StringBuilder();
        boolean obscureParam = false;
        for (int i = 0; i < command.length; i++) {
            final String cmd = command[i];
            if (obscureParam) {
                builder.append("******").append(" ");
                obscureParam = false;
            } else {
                builder.append(command[i]).append(" ");
            }

            if ("-y".equals(cmd) || "-z".equals(cmd)) {
                obscureParam = true;
                _passwordCommand = true;
            }
        }
        return builder.toString();
    }

    static String stackTraceAsString(final Throwable throwable) {
        //TODO: a StringWriter is bit to heavy weight
        try (StringWriter out = new StringWriter(); PrintWriter writer = new PrintWriter(out)) {
            throwable.printStackTrace(writer);
            return out.toString();
        } catch (final IOException e) {
            return "";
        }
    }

    public static int runSimpleBashScriptForExitValue(final String command) {
        return runSimpleBashScriptForExitValue(command, 0);
    }

    public static int runSimpleBashScriptForExitValue(final String command, final int timeout) {

        final Script s = new Script("/bin/bash", timeout);
        s.add("-c");
        s.add(command);

        final String result = s.execute(null);
        if (result == null || result.trim().isEmpty()) {
            return -1;
        } else {
            try {
                return Integer.parseInt(result.trim());
            } catch (final NumberFormatException e) {
                return -1;
            }
        }
    }

    public int getExitValue() {
        return _process.exitValue();
    }

    public void add(final String... params) {
        for (final String param : params) {
            _command.add(param);
        }
    }

    public Script set(final String name, final String value) {
        _command.add(name);
        _command.add(value);
        return this;
    }

    public void setWorkDir(final String workDir) {
        _workDir = workDir;
    }

    protected String buildCommandLine(final List<String> command) {
        final StringBuilder builder = new StringBuilder();
        boolean obscureParam = false;
        for (final String cmd : command) {
            if (obscureParam) {
                builder.append("******").append(" ");
                obscureParam = false;
            } else {
                builder.append(cmd).append(" ");
            }

            if ("-y".equals(cmd) || "-z".equals(cmd)) {
                obscureParam = true;
                _passwordCommand = true;
            }
        }
        return builder.toString();
    }

    public long getTimeout() {
        return _timeout;
    }

    public String execute() {
        return execute(new OutputInterpreter.OutputLogger(_logger));
    }

    @Override
    public String toString() {
        final String[] command = _command.toArray(new String[_command.size()]);
        return buildCommandLine(command);
    }

    @Override
    public String call() {
        try {
            _logger.trace("Checking exit value of process");
            _process.exitValue();
            _logger.trace("Script ran within the alloted time");
        } catch (final IllegalThreadStateException e) {
            _logger.warn("Interrupting script.");
            _isTimeOut = true;
            _thread.interrupt();
        }
        return null;
    }

    public static class Task implements Runnable {
        OutputInterpreter interpreter;
        BufferedReader reader;
        String result;
        boolean done;

        public Task(final OutputInterpreter interpreter, final BufferedReader reader) {
            this.interpreter = interpreter;
            this.reader = reader;
            result = null;
        }

        @Override
        public void run() {
            synchronized (this) {
                done = false;
                try {
                    result = interpreter.interpret(reader);
                } catch (final IOException ex) {
                    result = stackTraceAsString(ex);
                } catch (final Exception ex) {
                    result = stackTraceAsString(ex);
                } finally {
                    done = true;
                    notifyAll();
                    IOUtils.closeQuietly(reader);
                }
            }
        }

        public synchronized String getResult() throws InterruptedException {
            if (!done) {
                wait();
            }
            return result;
        }
    }
}
