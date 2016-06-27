package org.apache.cloudstack.storage.volume.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/resource/testContext.xml")
public class TestInProcessAsync {
    Server svr;

    @Before
    public void setup() {
        svr = new Server();
    }

    @Test
    public void testRpc() {
        svr.foo();
    }
}
