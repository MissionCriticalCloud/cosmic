package com.cloud.consoleproxy;

import com.cloud.common.resource.ServerResource;
import com.cloud.framework.config.dao.ConfigurationDao;
import com.cloud.host.HostVO;
import com.cloud.legacymodel.communication.command.startup.StartupCommand;
import com.cloud.legacymodel.communication.command.startup.StartupProxyCommand;
import com.cloud.legacymodel.exceptions.UnableDeleteHostException;
import com.cloud.model.enumeration.HostType;
import com.cloud.resource.ResourceManager;
import com.cloud.resource.ResourceStateAdapter;
import com.cloud.utils.NumbersUtil;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.dao.ConsoleProxyDao;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.List;
import java.util.Map;

public class StaticConsoleProxyManager extends AgentBasedConsoleProxyManager implements ConsoleProxyManager, ResourceStateAdapter {

    @Inject
    ConsoleProxyDao _proxyDao;
    @Inject
    ResourceManager _resourceMgr;
    @Inject
    ConfigurationDao _configDao;
    private String _ip = null;

    public StaticConsoleProxyManager() {

    }

    @Override
    public ConsoleProxyInfo assignProxy(final long dataCenterId, final long userVmId) {
        return new ConsoleProxyInfo(this._sslEnabled, this._ip, this._consoleProxyPort, this._consoleProxyUrlPort, this._consoleProxyUrlDomain);
    }

    @Override
    protected HostVO findHost(final VMInstanceVO vm) {

        final List<HostVO> hosts = this._resourceMgr.listAllUpAndEnabledHostsInOneZoneByType(HostType.ConsoleProxy, vm.getDataCenterId());

        return hosts.isEmpty() ? null : hosts.get(0);
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);
        this._ip = this._configDao.getValue("consoleproxy.static.publicIp");
        if (this._ip == null) {
            this._ip = "127.0.0.1";
        }

        final String value = (String) params.get("consoleproxy.sslEnabled");
        if (value != null && value.equalsIgnoreCase("true")) {
            this._sslEnabled = true;
        }
        int defaultPort = 8088;
        if (this._sslEnabled) {
            defaultPort = 8443;
        }
        this._consoleProxyUrlPort = NumbersUtil.parseInt(this._configDao.getValue("consoleproxy.static.port"), defaultPort);

        this._resourceMgr.registerResourceStateAdapter(this.getClass().getSimpleName(), this);

        return true;
    }

    @Override
    public HostVO createHostVOForConnectedAgent(final HostVO host, final StartupCommand[] cmd) {
        if (!(cmd[0] instanceof StartupProxyCommand)) {
            return null;
        }

        host.setType(HostType.ConsoleProxy);
        return host;
    }

    @Override
    public HostVO createHostVOForDirectConnectAgent(final HostVO host, final StartupCommand[] startup, final ServerResource resource, final Map<String, String> details, final
    List<String> hostTags) {
        return null;
    }

    @Override
    public DeleteHostAnswer deleteHost(final HostVO host, final boolean isForced, final boolean isForceDeleteStorage) throws UnableDeleteHostException {
        return null;
    }
}
