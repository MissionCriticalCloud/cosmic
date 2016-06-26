package com.cloud.upgrade.dao;

import java.io.File;
import java.sql.Connection;

public interface DbUpgrade {
    String[] getUpgradableVersionRange();

    String getUpgradedVersion();

    boolean supportsRollingUpgrade();

    /**
     * @return the script to prepare the database schema for the
     * data migration step.
     */
    File[] getPrepareScripts();

    /**
     * Performs the actual data migration.
     */
    void performDataMigration(Connection conn);

    /**
     * @return
     */
    File[] getCleanupScripts();
}
