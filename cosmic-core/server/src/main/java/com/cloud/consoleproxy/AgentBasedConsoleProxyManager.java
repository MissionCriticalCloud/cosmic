package com.cloud.consoleproxy;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.GetVncPortAnswer;
import com.cloud.agent.api.GetVncPortCommand;
import com.cloud.agent.api.StartupProxyCommand;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.info.ConsoleProxyInfo;
import com.cloud.server.ManagementServer;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.component.ManagerBase;
import com.cloud.vm.ConsoleProxyVO;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachineManager;
import com.cloud.vm.dao.ConsoleProxyDao;
import com.cloud.vm.dao.UserVmDao;
import com.cloud.vm.dao.VMInstanceDao;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.framework.security.keys.KeysManager;
import org.apache.cloudstack.framework.security.keystore.KeystoreManager;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentBasedConsoleProxyManager extends ManagerBase implements ConsoleProxyManager {
    private static final Logger s_logger = LoggerFactory.getLogger(AgentBasedConsoleProxyManager.class);

    @Inject
    protected HostDao _hostDao;
    @Inject
    protected UserVmDao _userVmDao;
    protected String _consoleProxyUrlDomain;
    protected int _consoleProxyUrlPort = ConsoleProxyManager.DEFAULT_PROXY_URL_PORT;
    protected int _consoleProxyPort = ConsoleProxyManager.DEFAULT_PROXY_VNC_PORT;
    protected boolean _sslEnabled = false;
    @Inject
    protected ConsoleProxyDao _cpDao;
    @Inject
    protected KeystoreManager _ksMgr;
    @Inject
    AgentManager _agentMgr;
    @Inject
    VirtualMachineManager _itMgr;
    @Inject
    ConfigurationDao _configDao;
    @Inject
    ManagementServer _ms;
    @Inject
    KeysManager _keysMgr;
    @Inject
    private VMInstanceDao _instanceDao;
    private ConsoleProxyListener _listener;

    public int getVncPort(final VMInstanceVO vm) {
        if (vm.getHostId() == null) {
            return -1;
        }
        final GetVncPortAnswer answer = (GetVncPortAnswer) _agentMgr.easySend(vm.getHostId(), new GetVncPortCommand(vm.getId(), vm.getHostName()));
        return (answer == null || !answer.getResult()) ? -1 : answer.getPort();
    }

    @Override
    public ConsoleProxyInfo assignProxy(final long dataCenterId, final long userVmId) {
        final UserVmVO userVm = _userVmDao.findById(userVmId);
        if (userVm == null) {
            s_logger.warn("User VM " + userVmId + " no longer exists, return a null proxy for user vm:" + userVmId);
            return null;
        }

        final HostVO host = findHost(userVm);
        if (host != null) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Assign embedded console proxy running at " + host.getName() + " to user vm " + userVmId + " with public IP " + host.getPublicIpAddress());
            }

            // only private IP, public IP, host id have meaningful values, rest
            // of all are place-holder values
            String publicIp = host.getPublicIpAddress();
            if (publicIp == null) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Host " + host.getName() + "/" + host.getPrivateIpAddress() +
                            " does not have public interface, we will return its private IP for cosole proxy.");
                }
                publicIp = host.getPrivateIpAddress();
            }

            int urlPort = _consoleProxyUrlPort;

            if (host.getProxyPort() != null && host.getProxyPort().intValue() > 0) {
                urlPort = host.getProxyPort().intValue();
            }

            return new ConsoleProxyInfo(_sslEnabled, publicIp, _consoleProxyPort, urlPort, _consoleProxyUrlDomain);
        } else {
            s_logger.warn("Host that VM is running is no longer available, console access to VM " + userVmId + " will be temporarily unavailable.");
        }
        return null;
    }

    HostVO findHost(final VMInstanceVO vm) {
        return _hostDao.findById(vm.getHostId());
    }

    @Override
    public ConsoleProxyManagementState getManagementState() {
        return null;
    }

    @Override
    public void setManagementState(final ConsoleProxyManagementState state) {
    }

    @Override
    public void resumeLastManagementState() {
    }

    @Override
    public ConsoleProxyVO startProxy(final long proxyVmId, final boolean ignoreRestartSetting) {
        return null;
    }

    @Override
    public boolean stopProxy(final long proxyVmId) {
        return false;
    }

    @Override
    public boolean rebootProxy(final long proxyVmId) {
        return false;
    }

    @Override
    public boolean destroyProxy(final long proxyVmId) {
        return false;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {

        if (s_logger.isInfoEnabled()) {
            s_logger.info("Start configuring AgentBasedConsoleProxyManager");
        }

        final Map<String, String> configs = _configDao.getConfiguration("management-server", params);
        String value = configs.get("consoleproxy.url.port");
        if (value != null) {
            _consoleProxyUrlPort = NumbersUtil.parseInt(value, ConsoleProxyManager.DEFAULT_PROXY_URL_PORT);
        }

        value = configs.get("consoleproxy.port");
        if (value != null) {
            _consoleProxyPort = NumbersUtil.parseInt(value, ConsoleProxyManager.DEFAULT_PROXY_VNC_PORT);
        }

        value = configs.get("consoleproxy.sslEnabled");
        if (value != null && value.equalsIgnoreCase("true")) {
            _sslEnabled = true;
        }

        _consoleProxyUrlDomain = configs.get("consoleproxy.url.domain");

        _listener = new ConsoleProxyListener(new AgentBasedAgentHook(_instanceDao, _hostDao, _configDao, _ksMgr, _agentMgr, _keysMgr));
        _agentMgr.registerForHostEvents(_listener, true, true, false);

        if (s_logger.isInfoEnabled()) {
            s_logger.info("AgentBasedConsoleProxyManager has been configured. SSL enabled: " + _sslEnabled);
        }
        return true;
    }

    public class AgentBasedAgentHook extends AgentHookBase {

        public AgentBasedAgentHook(final VMInstanceDao instanceDao, final HostDao hostDao, final ConfigurationDao cfgDao, final KeystoreManager ksMgr, final AgentManager
                agentMgr, final KeysManager keysMgr) {
            super(instanceDao, hostDao, cfgDao, ksMgr, agentMgr, keysMgr);
        }

        @Override
        protected HostVO findConsoleProxyHost(final StartupProxyCommand cmd) {
            return _hostDao.findByGuid(cmd.getGuid());
        }
    }
}
