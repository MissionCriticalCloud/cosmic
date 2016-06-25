package com.cloud.hypervisor.xenserver.resource;

import com.cloud.agent.api.StartupCommand;
import com.cloud.resource.ServerResource;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.ssh.SSHCmdHelper;

import javax.ejb.Local;

import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Host;
import com.xensource.xenapi.Network;
import com.xensource.xenapi.PIF;
import com.xensource.xenapi.Types.XenAPIException;
import com.xensource.xenapi.VLAN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Local(value = ServerResource.class)
public class XenServer56Resource extends CitrixResourceBase {
    private final static Logger s_logger = LoggerFactory.getLogger(XenServer56Resource.class);

    @Override
    public String networkUsage(final Connection conn, final String privateIpAddress, final String option, final String vif) {
        String args = "";
        if (option.equals("get")) {
            args += "-g";
        } else if (option.equals("create")) {
            args += "-c";
        } else if (option.equals("reset")) {
            args += "-r";
        } else if (option.equals("addVif")) {
            args += "-a ";
            args += vif;
        } else if (option.equals("deleteVif")) {
            args += "-d ";
            args += vif;
        }

        return executeInVR(privateIpAddress, "netusage.sh", args).getDetails();
    }

    @Override
    public StartupCommand[] initialize() {
        pingXAPI();
        final StartupCommand[] cmds = super.initialize();
        return cmds;
    }

    @Override
    public void disableVlanNetwork(final Connection conn, final Network network) {
        try {
            final Network.Record networkr = network.getRecord(conn);
            if (!networkr.nameLabel.startsWith("VLAN")) {
                return;
            }
            final String bridge = networkr.bridge.trim();
            for (final PIF pif : networkr.PIFs) {
                final PIF.Record pifr = pif.getRecord(conn);
                if (!pifr.host.getUuid(conn).equalsIgnoreCase(_host.getUuid())) {
                    continue;
                }

                final VLAN vlan = pifr.VLANMasterOf;
                if (vlan != null) {
                    final String vlannum = pifr.VLAN.toString();
                    final String device = pifr.device.trim();
                    if (vlannum.equals("-1")) {
                        return;
                    }
                    try {
                        vlan.destroy(conn);
                        final Host host = Host.getByUuid(conn, _host.getUuid());
                        host.forgetDataSourceArchives(conn, "pif_" + bridge + "_tx");
                        host.forgetDataSourceArchives(conn, "pif_" + bridge + "_rx");
                        host.forgetDataSourceArchives(conn, "pif_" + device + "." + vlannum + "_tx");
                        host.forgetDataSourceArchives(conn, "pif_" + device + "." + vlannum + "_rx");
                    } catch (final XenAPIException e) {
                        s_logger.trace("Catch " + e.getClass().getName() + ": failed to destory VLAN " + device + " on host " + _host.getUuid() + " due to " + e.toString());
                    }
                }
                return;
            }
        } catch (final XenAPIException e) {
            final String msg = "Unable to disable VLAN network due to " + e.toString();
            s_logger.warn(msg, e);
        } catch (final Exception e) {
            final String msg = "Unable to disable VLAN network due to " + e.getMessage();
            s_logger.warn(msg, e);
        }
    }

    @Override
    protected String getPatchFilePath() {
        return "scripts/vm/hypervisor/xenserver/xenserver56/patch";
    }

    public Boolean checkHeartbeat(final String hostuuid) {
        final com.trilead.ssh2.Connection sshConnection = new com.trilead.ssh2.Connection(_host.getIp(), 22);
        try {
            sshConnection.connect(null, 60000, 60000);
            if (!sshConnection.authenticateWithPassword(_username, _password.peek())) {
                throw new CloudRuntimeException("Unable to authenticate");
            }

            final String shcmd = "/opt/cloud/bin/check_heartbeat.sh " + hostuuid + " " + Integer.toString(_heartbeatInterval * 2);
            if (!SSHCmdHelper.sshExecuteCmd(sshConnection, shcmd)) {
                s_logger.debug("Heart beat is gone so dead.");
                return false;
            }
            s_logger.debug("Heart beat is still going");
            return true;
        } catch (final Exception e) {
            s_logger.debug("health check failed due to catch exception " + e.toString());
            return null;
        } finally {
            sshConnection.close();
        }
    }
}
