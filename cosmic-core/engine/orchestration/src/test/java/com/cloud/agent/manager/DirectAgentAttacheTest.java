package com.cloud.agent.manager;

import com.cloud.resource.ServerResource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DirectAgentAttacheTest {
    long _id = 0L;
    @Mock
    private AgentManagerImpl _agentMgr;
    @Mock
    private ServerResource _resource;
    private DirectAgentAttache directAgentAttache;

    @Before
    public void setup() {
        directAgentAttache = new DirectAgentAttache(_agentMgr, _id, "myDirectAgentAttache", _resource, false);

        MockitoAnnotations.initMocks(directAgentAttache);
    }

    @Test
    public void testPingTask() throws Exception {
        final DirectAgentAttache.PingTask pt = directAgentAttache.new PingTask();
        Mockito.doReturn(2).when(_agentMgr).getDirectAgentThreadCap();
        pt.runInContext();
        Mockito.verify(_resource, Mockito.times(1)).getCurrentStatus(_id);
    }
}
