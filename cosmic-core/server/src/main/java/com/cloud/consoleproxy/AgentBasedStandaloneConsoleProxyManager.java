package com.cloud.consoleproxy;

import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.info.ConsoleProxyInfo;
import com.cloud.vm.UserVmVO;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentBasedStandaloneConsoleProxyManager extends AgentBasedConsoleProxyManager {
    private static final Logger s_logger = LoggerFactory.getLogger(AgentBasedStandaloneConsoleProxyManager.class);

    @Override
    public ConsoleProxyInfo assignProxy(final long dataCenterId, final long userVmId) {
        final UserVmVO userVm = _userVmDao.findById(userVmId);
        if (userVm == null) {
            s_logger.warn("User VM " + userVmId + " no longer exists, return a null proxy for user vm:" + userVmId);
            return null;
        }

        final HostVO host = findHost(userVm);
        if (host != null) {
            HostVO allocatedHost = null;
            /*Is there a consoleproxy agent running on the same machine?*/
            final List<HostVO> hosts = _hostDao.listAllIncludingRemoved();
            for (final HostVO hv : hosts) {
                if (hv.getType() == Host.Type.ConsoleProxy && hv.getPublicIpAddress().equalsIgnoreCase(host.getPublicIpAddress())) {
                    allocatedHost = hv;
                    break;
                }
            }
            if (allocatedHost == null) {
                /*Is there a consoleproxy agent running in the same pod?*/
                for (final HostVO hv : hosts) {
                    if (hv.getType() == Host.Type.ConsoleProxy && hv.getPodId().equals(host.getPodId())) {
                        allocatedHost = hv;
                        break;
                    }
                }
            }
            if (allocatedHost == null) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Failed to find a console proxy at host: " + host.getName() + " and in the pod: " + host.getPodId() + " to user vm " + userVmId);
                }
                return null;
            }
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Assign standalone console proxy running at " + allocatedHost.getName() + " to user vm " + userVmId + " with public IP " +
                        allocatedHost.getPublicIpAddress());
            }

            // only private IP, public IP, host id have meaningful values, rest of all are place-holder values
            String publicIp = allocatedHost.getPublicIpAddress();
            if (publicIp == null) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Host " + allocatedHost.getName() + "/" + allocatedHost.getPrivateIpAddress() +
                            " does not have public interface, we will return its private IP for cosole proxy.");
                }
                publicIp = allocatedHost.getPrivateIpAddress();
            }

            int urlPort = _consoleProxyUrlPort;
            if (allocatedHost.getProxyPort() != null && allocatedHost.getProxyPort().intValue() > 0) {
                urlPort = allocatedHost.getProxyPort().intValue();
            }

            return new ConsoleProxyInfo(_sslEnabled, publicIp, _consoleProxyPort, urlPort, _consoleProxyUrlDomain);
        } else {
            s_logger.warn("Host that VM is running is no longer available, console access to VM " + userVmId + " will be temporarily unavailable.");
        }
        return null;
    }
}
