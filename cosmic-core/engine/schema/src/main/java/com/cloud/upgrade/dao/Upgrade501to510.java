package com.cloud.upgrade.dao;

import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.script.Script;

import java.io.File;
import java.sql.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Upgrade501to510 implements DbUpgrade {

    final static Logger logger = LoggerFactory.getLogger(Upgrade501to510.class);
    private static final String PREVIOUS_VERSION = "5.0.1";
    private static final String NEXT_VERSION = "5.1.0";
    private static final String SCHEMA_SCRIPT = "db/schema-501to510.sql";
    private static final String SCHEMA_CLEANUP_SCRIPT = "db/schema-501to510-cleanup.sql";

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
        logger.info("Performing data migration from 5.0.1 to 5.1.0");
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
