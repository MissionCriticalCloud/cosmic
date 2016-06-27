package com.cloud.upgrade.dao;

import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.script.Script;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Upgrade461to470 implements DbUpgrade {
    final static Logger s_logger = LoggerFactory.getLogger(Upgrade461to470.class);

    @Override
    public String[] getUpgradableVersionRange() {
        return new String[]{"4.6.1", "4.7.0"};
    }

    @Override
    public String getUpgradedVersion() {
        return "4.7.0";
    }

    @Override
    public boolean supportsRollingUpgrade() {
        return false;
    }

    @Override
    public File[] getPrepareScripts() {
        final String script = Script.findScript("", "db/schema-461to470.sql");
        if (script == null) {
            throw new CloudRuntimeException("Unable to find db/schema-461to470.sql");
        }
        return new File[]{new File(script)};
    }

    @Override
    public void performDataMigration(final Connection conn) {
        alterAddColumnToCloudUsage(conn);
    }

    public void alterAddColumnToCloudUsage(final Connection conn) {
        final String alterTableSql = "ALTER TABLE `cloud_usage`.`cloud_usage` ADD COLUMN `quota_calculated` tinyint(1) DEFAULT 0 NOT NULL COMMENT 'quota calculation status'";
        try (PreparedStatement pstmt = conn.prepareStatement(alterTableSql)) {
            pstmt.executeUpdate();
            s_logger.info("Altered cloud_usage.cloud_usage table and added column quota_calculated");
        } catch (final SQLException e) {
            if (e.getMessage().contains("quota_calculated")) {
                s_logger.warn("cloud_usage.cloud_usage table already has a column called quota_calculated");
            } else {
                throw new CloudRuntimeException("Unable to create column quota_calculated in table cloud_usage.cloud_usage", e);
            }
        }
    }

    @Override
    public File[] getCleanupScripts() {
        final String script = Script.findScript("", "db/schema-461to470-cleanup.sql");
        if (script == null) {
            throw new CloudRuntimeException("Unable to find db/schema-461to470-cleanup.sql");
        }

        return new File[]{new File(script)};
    }
}
