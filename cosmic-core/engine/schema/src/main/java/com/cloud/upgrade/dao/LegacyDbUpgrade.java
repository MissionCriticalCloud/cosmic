package com.cloud.upgrade.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LegacyDbUpgrade implements DbUpgrade {

    final static Logger s_logger = LoggerFactory.getLogger(LegacyDbUpgrade.class);

    public LegacyDbUpgrade() {
        super();
    }

    /**
     * @param closable
     */
    protected void closeAutoCloseable(final AutoCloseable closable) {
        if (closable != null) {
            try {
                closable.close();
            } catch (final Exception e) {
                s_logger.info("[ignored]", e);
            }
        }
    }
}
