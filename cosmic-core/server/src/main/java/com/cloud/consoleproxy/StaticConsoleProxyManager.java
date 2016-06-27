package com.cloud.consoleproxy;

import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupProxyCommand;
import com.cloud.host.Host.Type;
import com.cloud.host.HostVO;
import com.cloud.info.ConsoleProxyInfo;
import com.cloud.resource.ResourceManager;
import com.cloud.resource.ResourceStateAdapter;
import com.cloud.resource.ServerResource;
import com.cloud.resource.UnableDeleteHostException;
import com.cloud.utils.NumbersUtil;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.dao.ConsoleProxyDao;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;

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
        return new ConsoleProxyInfo(_sslEnabled, _ip, _consoleProxyPort, _consoleProxyUrlPort, _consoleProxyUrlDomain);
    }

    @Override
    protected HostVO findHost(final VMInstanceVO vm) {

        final List<HostVO> hosts = _resourceMgr.listAllUpAndEnabledHostsInOneZoneByType(Type.ConsoleProxy, vm.getDataCenterId());

        return hosts.isEmpty() ? null : hosts.get(0);
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);
        _ip = _configDao.getValue("consoleproxy.static.publicIp");
        if (_ip == null) {
            _ip = "127.0.0.1";
        }

        final String value = (String) params.get("consoleproxy.sslEnabled");
        if (value != null && value.equalsIgnoreCase("true")) {
            _sslEnabled = true;
        }
        int defaultPort = 8088;
        if (_sslEnabled) {
            defaultPort = 8443;
        }
        _consoleProxyUrlPort = NumbersUtil.parseInt(_configDao.getValue("consoleproxy.static.port"), defaultPort);

        _resourceMgr.registerResourceStateAdapter(this.getClass().getSimpleName(), this);

        return true;
    }

    @Override
    public HostVO createHostVOForConnectedAgent(final HostVO host, final StartupCommand[] cmd) {
        if (!(cmd[0] instanceof StartupProxyCommand)) {
            return null;
        }

        host.setType(com.cloud.host.Host.Type.ConsoleProxy);
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
