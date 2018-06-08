package com.cloud.common.resource;

import com.cloud.common.agent.IAgentControl;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.Command;
import com.cloud.legacymodel.communication.command.startup.StartupCommand;
import com.cloud.utils.net.NetUtils;
import com.cloud.utils.script.Script;

import javax.naming.ConfigurationException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ServerResourceBase implements ServerResource {
    private static final Logger s_logger = LoggerFactory.getLogger(ServerResourceBase.class);
    protected String _name;
    protected NetworkInterface _publicNic;
    protected NetworkInterface _privateNic;
    protected NetworkInterface _storageNic;
    protected NetworkInterface _storageNic2;
    protected IAgentControl _agentControl;
    private final ArrayList<String> _warnings = new ArrayList<>();
    private final ArrayList<String> _errors = new ArrayList<>();

    @Override
    public String getName() {
        return this._name;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        this._name = name;

        String publicNic = (String) params.get("public.network.device");
        if (publicNic == null) {
            publicNic = "xenbr1";
        }
        String privateNic = (String) params.get("private.network.device");
        if (privateNic == null) {
            privateNic = "xenbr0";
        }
        final String storageNic = (String) params.get("storage.network.device");
        final String storageNic2 = (String) params.get("storage.network.device.2");

        this._privateNic = getNetworkInterface(privateNic);
        this._publicNic = getNetworkInterface(publicNic);
        this._storageNic = getNetworkInterface(storageNic);
        this._storageNic2 = getNetworkInterface(storageNic2);

        if (this._privateNic == null) {
            s_logger.warn("Nics are not specified in properties file/db, will try to autodiscover");

            Enumeration<NetworkInterface> nics = null;
            try {
                nics = NetworkInterface.getNetworkInterfaces();
                if (nics == null || !nics.hasMoreElements()) {
                    throw new ConfigurationException("Private NIC is not configured");
                }
            } catch (final SocketException e) {
                throw new ConfigurationException("Private NIC is not configured");
            }

            while (nics.hasMoreElements()) {
                final NetworkInterface nic = nics.nextElement();
                final String nicName = nic.getName();
                //  try {
                if (//!nic.isLoopback() &&
                    //nic.isUp() &&
                        !nic.isVirtual() && !nicName.startsWith("vnif") && !nicName.startsWith("vnbr") && !nicName.startsWith("peth") && !nicName.startsWith("vif") &&
                                !nicName.startsWith("virbr") && !nicName.contains(":")) {
                    final String[] info = NetUtils.getNicParams(nicName);
                    if (info != null && info[0] != null) {
                        this._privateNic = nic;
                        s_logger.info("Designating private to be nic " + nicName);
                        break;
                    }
                }
                //      } catch (final SocketException e) {
                //        s_logger.warn("Error looking at " + nicName, e);
                //  }
                s_logger.debug("Skipping nic " + nicName);
            }

            if (this._privateNic == null) {
                throw new ConfigurationException("Private NIC is not configured");
            }
        }
        final String[] infos = NetUtils.getNetworkParams(this._privateNic);
        if (infos == null) {
            s_logger.warn("Incorrect details for private Nic during initialization of ServerResourceBase");
            return false;
        }
        params.put("host.ip", infos[0]);
        params.put("host.mac.address", infos[1]);

        return true;
    }

    protected NetworkInterface getNetworkInterface(String nicName) {
        s_logger.debug("Retrieving network interface: " + nicName);
        if (nicName == null) {
            return null;
        }

        if (nicName.trim().length() == 0) {
            return null;
        }

        nicName = nicName.trim();

        final NetworkInterface nic;
        try {
            nic = NetworkInterface.getByName(nicName);
            if (nic == null) {
                s_logger.debug("Unable to get network interface for " + nicName);
                return null;
            }

            return nic;
        } catch (final SocketException e) {
            s_logger.warn("Unable to get network interface for " + nicName, e);
            return null;
        }
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }

    protected String findScript(final String script) {
        return Script.findScript(getDefaultScriptsDir(), script);
    }

    protected abstract String getDefaultScriptsDir();

    protected void fillNetworkInformation(final StartupCommand cmd) {
        String[] info = null;
        if (this._privateNic != null) {
            info = NetUtils.getNetworkParams(this._privateNic);
            if (info != null) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Parameters for private nic: " + info[0] + " - " + info[1] + "-" + info[2]);
                }
                cmd.setPrivateIpAddress(info[0]);
                cmd.setPrivateMacAddress(info[1]);
                cmd.setPrivateNetmask(info[2]);
            }
        }

        if (this._storageNic != null) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Storage has its now nic: " + this._storageNic.getName());
            }
            info = NetUtils.getNetworkParams(this._storageNic);
        }

        // NOTE: In case you're wondering, this is not here by mistake.
        if (info != null) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Parameters for storage nic: " + info[0] + " - " + info[1] + "-" + info[2]);
            }
            cmd.setStorageIpAddress(info[0]);
            cmd.setStorageMacAddress(info[1]);
            cmd.setStorageNetmask(info[2]);
        }

        if (this._publicNic != null) {
            info = NetUtils.getNetworkParams(this._publicNic);
            if (info != null) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Parameters for public nic: " + info[0] + " - " + info[1] + "-" + info[2]);
                }
                cmd.setPublicIpAddress(info[0]);
                cmd.setPublicMacAddress(info[1]);
                cmd.setPublicNetmask(info[2]);
            }
        }

        if (this._storageNic2 != null) {
            info = NetUtils.getNetworkParams(this._storageNic2);
            if (info != null) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Parameters for storage nic 2: " + info[0] + " - " + info[1] + "-" + info[2]);
                }
                cmd.setStorageIpAddressDeux(info[0]);
                cmd.setStorageMacAddressDeux(info[1]);
                cmd.setStorageNetmaskDeux(info[2]);
            }
        }
    }

    @Override
    public void disconnected() {
    }

    @Override
    public IAgentControl getAgentControl() {
        return this._agentControl;
    }

    @Override
    public void setAgentControl(final IAgentControl agentControl) {
        this._agentControl = agentControl;
    }

    protected void recordWarning(final String msg) {
        recordWarning(msg, null);
    }

    protected void recordWarning(final String msg, final Throwable th) {
        final String str = getLogStr(msg, th);
        synchronized (this._warnings) {
            this._warnings.add(str);
        }
    }

    protected String getLogStr(final String msg, final Throwable th) {
        final StringWriter writer = new StringWriter();
        writer.append(new Date().toString()).append(": ").append(msg);
        if (th != null) {
            writer.append("\n  Exception: ");
            th.printStackTrace(new PrintWriter(writer));
        }
        return writer.toString();
    }

    protected List<String> getWarnings() {
        synchronized (this._warnings) {
            final List<String> results = new LinkedList<>(this._warnings);
            this._warnings.clear();
            return results;
        }
    }

    protected List<String> getErrors() {
        synchronized (this._errors) {
            final List<String> result = new LinkedList<>(this._errors);
            this._errors.clear();
            return result;
        }
    }

    protected void recordError(final String msg) {
        recordError(msg, null);
    }

    protected void recordError(final String msg, final Throwable th) {
        final String str = getLogStr(msg, th);
        synchronized (this._errors) {
            this._errors.add(str);
        }
    }

    protected Answer createErrorAnswer(final Command cmd, final String msg, final Throwable th) {
        final StringWriter writer = new StringWriter();
        if (msg != null) {
            writer.append(msg);
        }
        writer.append("===>Stack<===");
        th.printStackTrace(new PrintWriter(writer));
        return new Answer(cmd, false, writer.toString());
    }

    protected String createErrorDetail(final String msg, final Throwable th) {
        final StringWriter writer = new StringWriter();
        if (msg != null) {
            writer.append(msg);
        }
        writer.append("===>Stack<===");
        th.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
}
