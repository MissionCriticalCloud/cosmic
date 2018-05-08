package com.cloud.agent.manager;

import com.cloud.common.resource.ServerResource;

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
        this.directAgentAttache = new DirectAgentAttache(this._agentMgr, this._id, "myDirectAgentAttache", this._resource, false);

        MockitoAnnotations.initMocks(this.directAgentAttache);
    }

    @Test
    public void testPingTask() throws Exception {
        final DirectAgentAttache.PingTask pt = this.directAgentAttache.new PingTask();
        Mockito.doReturn(2).when(this._agentMgr).getDirectAgentThreadCap();
        pt.runInContext();
        Mockito.verify(this._resource, Mockito.times(1)).getCurrentStatus(this._id);
    }
}
