package com.cloud.upgrade.dao;

import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.script.Script;

import java.io.File;
import java.sql.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Upgrade452to453 implements DbUpgrade {
    final static Logger s_logger = LoggerFactory.getLogger(Upgrade452to453.class);

    @Override
    public String[] getUpgradableVersionRange() {
        return new String[]{"4.5.2", "4.5.3"};
    }

    @Override
    public String getUpgradedVersion() {
        return "4.5.3";
    }

    @Override
    public boolean supportsRollingUpgrade() {
        return false;
    }

    @Override
    public File[] getPrepareScripts() {
        final String script = Script.findScript("", "db/schema-452to453.sql");
        if (script == null) {
            throw new CloudRuntimeException("Unable to find db/schema-452to453.sql");
        }
        return new File[]{new File(script)};
    }

    @Override
    public void performDataMigration(final Connection conn) {
    }

    @Override
    public File[] getCleanupScripts() {
        final String script = Script.findScript("", "db/schema-452to453-cleanup.sql");
        if (script == null) {
            throw new CloudRuntimeException("Unable to find db/schema-452to453-cleanup.sql");
        }

        return new File[]{new File(script)};
    }
}
