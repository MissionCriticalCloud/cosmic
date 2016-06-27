package com.cloud.upgrade.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Upgrade453to460 extends Upgrade452to460 implements DbUpgrade {
    final static Logger s_logger = LoggerFactory.getLogger(Upgrade453to460.class);

    @Override
    public String[] getUpgradableVersionRange() {
        return new String[]{"4.5.3", "4.6.0"};
    }
}
