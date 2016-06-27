package com.cloud.network.security;

import com.cloud.utils.Profiler;
import com.cloud.utils.component.ComponentContext;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/SecurityGroupManagerTestContext.xml")
public class SecurityGroupManagerImpl2Test extends TestCase {
    @Inject
    SecurityGroupManagerImpl2 _sgMgr = null;

    @Before
    public void setup() throws Exception {
        ComponentContext.initComponentsLifeCycle();
    }

    @Override
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSchedule() throws ConfigurationException {
        _schedule(1000);
    }

    protected void _schedule(final int numVms) {
        System.out.println("Starting");
        final List<Long> work = new ArrayList<>();
        for (long i = 100; i <= 100 + numVms; i++) {
            work.add(i);
        }
        final Profiler profiler = new Profiler();
        profiler.start();
        _sgMgr.scheduleRulesetUpdateToHosts(work, false, null);
        profiler.stop();

        System.out.println("Done " + numVms + " in " + profiler.getDurationInMillis() + " ms");
    }

    @Test
    public void testWork() throws ConfigurationException {
        _schedule(1000);
        _sgMgr.work();
    }
}
