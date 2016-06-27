package com.cloud.upgrade.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Upgrade443to450 extends Upgrade442to450 implements DbUpgrade {

    final static Logger s_logger = LoggerFactory.getLogger(Upgrade443to450.class);

    @Override
    public String[] getUpgradableVersionRange() {
        return new String[]{"4.4.3", "4.5.0"};
    }
}
