package com.cloud.agent.api;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 */
@Target({TYPE, FIELD})
@Retention(RUNTIME)
public @interface LogLevel {
    Log4jLevel value() default Log4jLevel.Debug;

    public enum Log4jLevel { // Had to do this because Level is not primitive.
        Off(Level.OFF), Trace(Level.TRACE), Debug(Level.DEBUG);

        Level _level;

        private Log4jLevel(final Level level) {
            _level = level;
        }

        public boolean enabled(final Logger logger) {
            return _level != Level.OFF && logger.isEnabledFor(_level);
        }
    }
}
