package com.cloud.utils.db;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Merovingian2Test extends TestCase {
    static final Logger s_logger = LoggerFactory.getLogger(Merovingian2Test.class);
    Merovingian2 _lockMaster = Merovingian2.createLockMaster(1234);

    @Override
    @Before
    protected void setUp() throws Exception {
        _lockMaster.cleanupThisServer();
    }

    @Override
    @After
    protected void tearDown() throws Exception {
        _lockMaster.cleanupThisServer();
    }

    @Test
    public void testLockAndRelease() {

        s_logger.info("Testing first acquire");
        boolean result = _lockMaster.acquire("first" + 1234, 5);
        Assert.assertTrue(result);

        s_logger.info("Testing acquire of different lock");
        result = _lockMaster.acquire("second" + 1234, 5);
        Assert.assertTrue(result);

        s_logger.info("Testing reacquire of the same lock");
        result = _lockMaster.acquire("first" + 1234, 5);
        Assert.assertTrue(result);

        int count = _lockMaster.owns("first" + 1234);
        Assert.assertEquals(count, 2);

        count = _lockMaster.owns("second" + 1234);
        Assert.assertEquals(count, 1);

        s_logger.info("Testing release of the first lock");
        result = _lockMaster.release("first" + 1234);
        Assert.assertTrue(result);

        count = _lockMaster.owns("first" + 1234);
        Assert.assertEquals(count, 1);

        s_logger.info("Testing release of the second lock");
        result = _lockMaster.release("second" + 1234);
        Assert.assertTrue(result);

        result = _lockMaster.release("first" + 1234);
        Assert.assertTrue(result);
    }
}
