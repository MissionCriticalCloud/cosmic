package com.cloud.upgrade.dao;

import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.script.Script;

import java.io.File;
import java.sql.Connection;

public class UpgradeSnapshot223to224 implements DbUpgrade {

    @Override
    public String[] getUpgradableVersionRange() {
        return new String[]{"2.2.3", "2.2.3"};
    }

    @Override
    public String getUpgradedVersion() {
        return "2.2.4";
    }

    @Override
    public boolean supportsRollingUpgrade() {
        return false;
    }

    @Override
    public File[] getPrepareScripts() {
        final String file = Script.findScript("", "db/schema-snapshot-223to224.sql");
        if (file == null) {
            throw new CloudRuntimeException("Unable to find the upgrade script, schema-snapshot-223to224.sql");
        }

        return new File[]{new File(file)};
    }

    @Override
    public void performDataMigration(final Connection conn) {
    }

    @Override
    public File[] getCleanupScripts() {
        return null;
    }
}
