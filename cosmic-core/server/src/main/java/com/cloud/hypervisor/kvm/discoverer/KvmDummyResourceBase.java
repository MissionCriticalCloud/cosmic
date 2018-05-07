package com.cloud.hypervisor.kvm.discoverer;

import com.cloud.common.resource.ServerResource;
import com.cloud.common.resource.ServerResourceBase;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.Command;
import com.cloud.legacymodel.communication.command.PingCommand;
import com.cloud.legacymodel.communication.command.StartupCommand;
import com.cloud.legacymodel.communication.command.StartupRoutingCommand;
import com.cloud.model.enumeration.HostType;
import com.cloud.model.enumeration.HypervisorType;

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
    public HostType getType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StartupCommand[] initialize() {
        final StartupRoutingCommand cmd = new StartupRoutingCommand(0, 0, 0, null, HypervisorType.KVM, new HashMap<>());
        cmd.setDataCenter(this._zoneId);
        cmd.setPod(this._podId);
        cmd.setCluster(this._clusterId);
        cmd.setGuid(this._guid);
        cmd.setName(this._agentIp);
        cmd.setPrivateIpAddress(this._agentIp);
        cmd.setStorageIpAddress(this._agentIp);
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
        this._zoneId = (String) params.get("zone");
        this._podId = (String) params.get("pod");
        this._clusterId = (String) params.get("cluster");
        this._guid = (String) params.get("guid");
        this._agentIp = (String) params.get("agentIp");
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
