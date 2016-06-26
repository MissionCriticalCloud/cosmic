//

//

package com.cloud.agent.transport;

import com.cloud.agent.api.Command;
import com.cloud.agent.api.LogLevel;
import com.cloud.agent.api.LogLevel.Log4jLevel;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import org.apache.log4j.Logger;

public class LoggingExclusionStrategy implements ExclusionStrategy {
    Logger _logger = null;

    public LoggingExclusionStrategy(final Logger logger) {
        _logger = logger;
    }

    @Override
    public boolean shouldSkipField(final FieldAttributes field) {
        final LogLevel level = field.getAnnotation(LogLevel.class);
        return level != null && !level.value().enabled(_logger);
    }

    @Override
    public boolean shouldSkipClass(final Class<?> clazz) {
        if (clazz.isArray() || !Command.class.isAssignableFrom(clazz)) {
            return false;
        }
        Log4jLevel log4jLevel = null;
        final LogLevel level = clazz.getAnnotation(LogLevel.class);
        if (level == null) {
            log4jLevel = LogLevel.Log4jLevel.Debug;
        } else {
            log4jLevel = level.value();
        }

        return !log4jLevel.enabled(_logger);
    }
}
