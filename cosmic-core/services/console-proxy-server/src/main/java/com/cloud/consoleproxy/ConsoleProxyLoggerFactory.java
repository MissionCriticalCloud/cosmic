package com.cloud.consoleproxy;

import com.cloud.consoleproxy.util.Logger;
import com.cloud.consoleproxy.util.LoggerFactory;

public class ConsoleProxyLoggerFactory implements LoggerFactory {
    public ConsoleProxyLoggerFactory() {
    }

    @Override
    public Logger getLogger(final Class<?> clazz) {
        return new Log4jLogger(org.apache.log4j.Logger.getLogger(clazz));
    }

    public static class Log4jLogger extends Logger {
        private final org.apache.log4j.Logger logger;

        public Log4jLogger(final org.apache.log4j.Logger logger) {
            this.logger = logger;
        }

        @Override
        public boolean isTraceEnabled() {
            return logger.isTraceEnabled();
        }

        @Override
        public boolean isDebugEnabled() {
            return logger.isDebugEnabled();
        }

        @Override
        public boolean isInfoEnabled() {
            return logger.isInfoEnabled();
        }

        @Override
        public void trace(final Object message) {
            logger.trace(message);
        }

        @Override
        public void trace(final Object message, final Throwable exception) {
            logger.trace(message, exception);
        }

        @Override
        public void info(final Object message) {
            logger.info(message);
        }

        @Override
        public void info(final Object message, final Throwable exception) {
            logger.info(message, exception);
        }

        @Override
        public void debug(final Object message) {
            logger.debug(message);
        }

        @Override
        public void debug(final Object message, final Throwable exception) {
            logger.debug(message, exception);
        }

        @Override
        public void warn(final Object message) {
            logger.warn(message);
        }

        @Override
        public void warn(final Object message, final Throwable exception) {
            logger.warn(message, exception);
        }

        @Override
        public void error(final Object message) {
            logger.error(message);
        }

        @Override
        public void error(final Object message, final Throwable exception) {
            logger.error(message, exception);
        }
    }
}
