package com.cloud.hypervisor.kvm.discoverer;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.PingCommand;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupRoutingCommand;
import com.cloud.host.Host.Type;
import com.cloud.hypervisor.Hypervisor;
import com.cloud.resource.ServerResource;
import com.cloud.resource.ServerResourceBase;

import javax.naming.ConfigurationException;
import java.util.HashMap;
import java.util.Map;

public class KvmDummyResourceBase extends ServerResourceBase implements ServerResource {
    private String _zoneId;
    private String _podId;
    private String _clusterId;
    private String _guid;
    private String _agentIp;

    @Override
    public Type getType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StartupCommand[] initialize() {
        final StartupRoutingCommand cmd =
                new StartupRoutingCommand(0, 0, 0, 0, null, Hypervisor.HypervisorType.KVM, new HashMap<>());
        cmd.setDataCenter(_zoneId);
        cmd.setPod(_podId);
        cmd.setCluster(_clusterId);
        cmd.setGuid(_guid);
        cmd.setName(_agentIp);
        cmd.setPrivateIpAddress(_agentIp);
        cmd.setStorageIpAddress(_agentIp);
        cmd.setVersion(KvmDummyResourceBase.class.getPackage().getImplementationVersion());
        return new StartupCommand[]{cmd};
    }

    @Override
    public PingCommand getCurrentStatus(final long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Answer executeRequest(final Command cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        _zoneId = (String) params.get("zone");
        _podId = (String) params.get("pod");
        _clusterId = (String) params.get("cluster");
        _guid = (String) params.get("guid");
        _agentIp = (String) params.get("agentIp");
        return true;
    }

    @Override
    protected String getDefaultScriptsDir() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setName(final String name) {
        // TODO Auto-generated method stub

    }

    @Override
    public Map<String, Object> getConfigParams() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setConfigParams(final Map<String, Object> params) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getRunLevel() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setRunLevel(final int level) {
        // TODO Auto-generated method stub

    }
}
