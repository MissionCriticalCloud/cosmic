package com.cloud.upgrade.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Upgrade431to440 extends Upgrade430to440 implements DbUpgrade {
    final static Logger s_logger = LoggerFactory.getLogger(Upgrade431to440.class);

    @Override
    public String[] getUpgradableVersionRange() {
        return new String[]{"4.3.1", "4.4.0"};
    }

    @Override
    public String getUpgradedVersion() {
        return "4.4.0";
    }

    @Override
    public boolean supportsRollingUpgrade() {
        return false;
    }
}
