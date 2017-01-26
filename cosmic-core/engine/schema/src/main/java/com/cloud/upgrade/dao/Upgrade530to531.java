package com.cloud.upgrade.dao;

import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.script.Script;

import java.io.File;
import java.sql.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Upgrade530to531 implements DbUpgrade {

    final static Logger logger = LoggerFactory.getLogger(Upgrade530to531.class);
    private static final String PREVIOUS_VERSION = "5.3.0";
    private static final String NEXT_VERSION = "5.3.1";
    private static final String SCHEMA_SCRIPT = "db/schema-530to531.sql";
    private static final String SCHEMA_CLEANUP_SCRIPT = "db/schema-530to531-cleanup.sql";

    @Override
    public String[] getUpgradableVersionRange() {
        return new String[]{PREVIOUS_VERSION, NEXT_VERSION};
    }

    @Override
    public String getUpgradedVersion() {
        return NEXT_VERSION;
    }

    @Override
    public boolean supportsRollingUpgrade() {
        return false;
    }

    @Override
    public File[] getPrepareScripts() {
        return getScript(SCHEMA_SCRIPT);
    }

    @Override
    public void performDataMigration(final Connection conn) {
        logger.info("Performing data migration from 5.3.0 to 5.3.1");
        new BareMetalRemovalUpdater().updateConfigurationValuesThatReferenceBareMetal(conn);
    }

    @Override
    public File[] getCleanupScripts() {
        return getScript(SCHEMA_CLEANUP_SCRIPT);
    }

    private File[] getScript(final String scriptName) {
        final String script = Script.findScript("", scriptName);
        if (script == null) {
            throw new CloudRuntimeException("Unable to find " + scriptName);
        }

        return new File[]{new File(script)};
    }
}
