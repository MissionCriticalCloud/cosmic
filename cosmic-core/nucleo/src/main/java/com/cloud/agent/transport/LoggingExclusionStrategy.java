package com.cloud.agent.transport;

import com.cloud.legacymodel.communication.LogLevel;
import com.cloud.legacymodel.communication.LogLevel.Level;
import com.cloud.legacymodel.communication.command.Command;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import org.slf4j.Logger;

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

        Level loglevel = Level.Debug;
        final LogLevel level = clazz.getAnnotation(LogLevel.class);
        if (level != null) {
            loglevel = level.value();
        }

        return !loglevel.enabled(_logger);
    }
}
