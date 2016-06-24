package com.cloud.upgrade.dao;

import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.script.Script;

import java.io.File;
import java.sql.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Upgrade480to500 implements DbUpgrade {
    final static Logger s_logger = LoggerFactory.getLogger(Upgrade480to500.class);

    @Override
    public String[] getUpgradableVersionRange() {
        return new String[]{"4.8.0", "5.0.0"};
    }

    @Override
    public String getUpgradedVersion() {
        return "5.0.0";
    }

    @Override
    public boolean supportsRollingUpgrade() {
        return false;
    }

    @Override
    public File[] getPrepareScripts() {
        String script = Script.findScript("", "db/schema-480to500.sql");
        if (script == null) {
            throw new CloudRuntimeException("Unable to find db/schema-480to500.sql");
        }
        return new File[]{new File(script)};
    }

    @Override
    public void performDataMigration(Connection conn) {
    }

    @Override
    public File[] getCleanupScripts() {
        String script = Script.findScript("", "db/schema-480to500-cleanup.sql");
        if (script == null) {
            throw new CloudRuntimeException("Unable to find db/schema-480to500-cleanup.sql");
        }

        return new File[]{new File(script)};
    }
}
