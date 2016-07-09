package com.cloud.agent.api;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.slf4j.Logger;

@Target({TYPE, FIELD})
@Retention(RUNTIME)
public @interface LogLevel {
    Level value() default Level.Debug;

    enum Level {
        Off(LoggingLevel.Off), Trace(LoggingLevel.Trace), Debug(LoggingLevel.Debug), Info(LoggingLevel.Info);

        LoggingLevel _level;

        Level(final LoggingLevel level) {
            _level = level;
        }

        public boolean enabled(final Logger logger) {
            final boolean ret;
            switch (_level) {
                case Trace:
                    ret = logger.isTraceEnabled();
                    break;
                case Debug:
                    ret = logger.isDebugEnabled();
                    break;
                case Info:
                    ret = logger.isInfoEnabled();
                    break;
                default:
                    ret = false;
                    break;
            }
            return ret;
        }
    }

    enum LoggingLevel {
        Off, Trace, Debug, Info
    }
}