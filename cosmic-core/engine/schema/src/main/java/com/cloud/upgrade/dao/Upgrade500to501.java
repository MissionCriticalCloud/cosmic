package com.cloud.upgrade.dao;

import java.io.File;
import java.sql.Connection;

import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.script.Script;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Upgrade500to501 implements DbUpgrade {

  private static final String PREVIOUS_VERSION = "5.0.0";
  private static final String NEXT_VERSION = "5.0.1";
  private static final String SCHEMA_SCRIPT = "db/schema-500to501.sql";
  private static final String SCHEMA_CLEANUP_SCRIPT = "db/schema-500to501-cleanup.sql";

  final static Logger s_logger = LoggerFactory.getLogger(Upgrade500to501.class);

  @Override
  public String[] getUpgradableVersionRange() {
    return new String[] { PREVIOUS_VERSION, NEXT_VERSION };
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
  public File[] getCleanupScripts() {
    return getScript(SCHEMA_CLEANUP_SCRIPT);
  }

  @Override
  public void performDataMigration(Connection conn) {
  }

  private File[] getScript(String scriptName) {
    final String script = Script.findScript("", scriptName);
    if (script == null) {
      throw new CloudRuntimeException("Unable to find " + scriptName);
    }

    return new File[] { new File(script) };
  }
}
