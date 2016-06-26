//

//

package com.cloud.utils.script;

import java.io.BufferedReader;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public abstract class OutputInterpreter {
    public static final OutputInterpreter NoOutputParser = new OutputInterpreter() {
        @Override
        public String interpret(final BufferedReader reader) throws IOException {
            return null;
        }
    };

    public boolean drain() {
        return false;
    }

    public String processError(final BufferedReader reader) throws IOException {
        final StringBuilder buff = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            buff.append(line);
        }
        return buff.toString();
    }

    public abstract String interpret(BufferedReader reader) throws IOException;

    public static class TimedOutLogger extends OutputInterpreter {
        private static final Logger s_logger = LoggerFactory.getLogger(TimedOutLogger.class);
        Process _process;

        public TimedOutLogger(final Process process) {
            _process = process;
        }

        @Override
        public boolean drain() {
            return true;
        }

        @Override
        public String interpret(final BufferedReader reader) throws IOException {
            final StringBuilder buff = new StringBuilder();

            while (reader.ready()) {
                buff.append(reader.readLine());
            }

            _process.destroy();

            try {
                while (reader.ready()) {
                    buff.append(reader.readLine());
                }
            } catch (final IOException e) {
                s_logger.info("[ignored] can not append line to buffer", e);
            }

            return buff.toString();
        }
    }

    public static class OutputLogger extends OutputInterpreter {
        Logger _logger;

        public OutputLogger(final Logger logger) {
            _logger = logger;
        }

        @Override
        public String interpret(final BufferedReader reader) throws IOException {
            final StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            if (builder.length() > 0) {
                _logger.debug(builder.toString());
            }
            return null;
        }
    }

    public static class OneLineParser extends OutputInterpreter {
        String line = null;

        public String getLine() {
            return line;
        }

        @Override
        public String interpret(final BufferedReader reader) throws IOException {
            line = reader.readLine();
            return null;
        }
    }

    public static class AllLinesParser extends OutputInterpreter {
        String allLines = null;

        public String getLines() {
            return allLines;
        }

        @Override
        public String interpret(final BufferedReader reader) throws IOException {
            final StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            allLines = builder.toString();
            return null;
        }
    }
}
