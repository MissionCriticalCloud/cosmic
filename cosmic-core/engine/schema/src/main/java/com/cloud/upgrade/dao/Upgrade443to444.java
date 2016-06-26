package com.cloud.upgrade.dao;

import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.script.Script;

import java.io.File;
import java.sql.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Upgrade443to444 implements DbUpgrade {
    final static Logger s_logger = LoggerFactory.getLogger(Upgrade443to444.class);

    @Override
    public String[] getUpgradableVersionRange() {
        return new String[]{"4.4.3", "4.4.4"};
    }

    @Override
    public String getUpgradedVersion() {
        return "4.4.4";
    }

    @Override
    public boolean supportsRollingUpgrade() {
        return false;
    }

    @Override
    public File[] getPrepareScripts() {
        final String script = Script.findScript("", "db/schema-443to444.sql");
        if (script == null) {
            throw new CloudRuntimeException("Unable to find db/schema-empty.sql");
        }

        return new File[]{new File(script)};
    }

    @Override
    public void performDataMigration(final Connection conn) {
    }

    @Override
    public File[] getCleanupScripts() {
        return null;
    }
}
