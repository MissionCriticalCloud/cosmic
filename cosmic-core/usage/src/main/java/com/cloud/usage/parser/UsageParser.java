package com.cloud.usage.parser;

import org.apache.cloudstack.managed.context.ManagedContextRunnable;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class UsageParser extends ManagedContextRunnable {
    public static final Logger s_logger = LoggerFactory.getLogger(UsageParser.class.getName());

    @Override
    protected void runInContext() {
        try {
            parse(null);
        } catch (final Exception e) {
            s_logger.warn("Error while parsing usage events", e);
        }
    }

    public abstract void parse(Date endDate);
}
