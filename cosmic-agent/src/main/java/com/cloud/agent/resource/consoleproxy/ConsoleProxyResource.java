package com.cloud.agent.resource.consoleproxy;

import com.cloud.agent.Agent.ExitStatus;
import com.cloud.agent.api.AgentControlAnswer;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CheckHealthAnswer;
import com.cloud.agent.api.CheckHealthCommand;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.ConsoleAccessAuthenticationAnswer;
import com.cloud.agent.api.ConsoleAccessAuthenticationCommand;
import com.cloud.agent.api.ConsoleProxyLoadReportCommand;
import com.cloud.agent.api.PingCommand;
import com.cloud.agent.api.ReadyAnswer;
import com.cloud.agent.api.ReadyCommand;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupProxyCommand;
import com.cloud.agent.api.proxy.CheckConsoleProxyLoadCommand;
import com.cloud.agent.api.proxy.ConsoleProxyLoadAnswer;
import com.cloud.agent.api.proxy.StartConsoleProxyAgentHttpHandlerCommand;
import com.cloud.agent.api.proxy.WatchConsoleProxyLoadCommand;
import com.cloud.exception.AgentControlChannelException;
import com.cloud.host.Host;
import com.cloud.host.Host.Type;
import com.cloud.resource.ServerResource;
import com.cloud.resource.ServerResourceBase;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.net.NetUtils;
import com.cloud.utils.script.Script;
import org.apache.cloudstack.managed.context.ManagedContextRunnable;

import javax.naming.ConfigurationException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * I don't want to introduce extra cross-cutting concerns into console proxy
 * process, as it involves configurations like zone/pod, agent auto self-upgrade
 * etc. I also don't want to introduce more module dependency issues into our
 * build system, cross-communication between this resource and console proxy
 * will be done through reflection. As a result, come out with following
 * solution to solve the problem of building a communication channel between
 * consoole proxy and management server.
 * <p>
 * We will deploy an agent shell inside console proxy VM, and this agent shell
 * will launch current console proxy from within this special server resource,
 * through it console proxy can build a communication channel with management
 * server.
 */
public class ConsoleProxyResource extends ServerResourceBase implements ServerResource {
    static final Logger s_logger = LoggerFactory.getLogger(ConsoleProxyResource.class);

    private final Properties _properties = new Properties();
    long _proxyVmId;
    int _proxyPort;
    String _localgw;
    String _eth1ip;
    String _eth1mask;
    String _pubIp;
    private Thread _consoleProxyMain = null;

    @Override
    public Type getType() {
        return Host.Type.ConsoleProxy;
    }

    @Override
    public synchronized StartupCommand[] initialize() {
        final StartupProxyCommand cmd = new StartupProxyCommand();
        fillNetworkInformation(cmd);
        cmd.setProxyPort(_proxyPort);
        cmd.setProxyVmId(_proxyVmId);
        if (_pubIp != null) {
            cmd.setPublicIpAddress(_pubIp);
        }
        return new StartupCommand[]{cmd};
    }

    @Override
    public PingCommand getCurrentStatus(final long id) {
        return new PingCommand(Type.ConsoleProxy, id);
    }

    @Override
    public Answer executeRequest(final Command cmd) {
        if (cmd instanceof CheckConsoleProxyLoadCommand) {
            return execute((CheckConsoleProxyLoadCommand) cmd);
        } else if (cmd instanceof WatchConsoleProxyLoadCommand) {
            return execute((WatchConsoleProxyLoadCommand) cmd);
        } else if (cmd instanceof ReadyCommand) {
            s_logger.info("Receive ReadyCommand, response with ReadyAnswer");
            return new ReadyAnswer((ReadyCommand) cmd);
        } else if (cmd instanceof CheckHealthCommand) {
            return new CheckHealthAnswer((CheckHealthCommand) cmd, true);
        } else if (cmd instanceof StartConsoleProxyAgentHttpHandlerCommand) {
            return execute((StartConsoleProxyAgentHttpHandlerCommand) cmd);
        } else {
            return Answer.createUnsupportedCommandAnswer(cmd);
        }
    }

    protected Answer execute(final CheckConsoleProxyLoadCommand cmd) {
        return executeProxyLoadScan(cmd, cmd.getProxyVmId(), cmd.getProxyVmName(), cmd.getProxyManagementIp(), cmd.getProxyCmdPort());
    }

    protected Answer execute(final WatchConsoleProxyLoadCommand cmd) {
        return executeProxyLoadScan(cmd, cmd.getProxyVmId(), cmd.getProxyVmName(), cmd.getProxyManagementIp(), cmd.getProxyCmdPort());
    }

    private Answer execute(final StartConsoleProxyAgentHttpHandlerCommand cmd) {
        s_logger.info("Invoke launchConsoleProxy() in responding to StartConsoleProxyAgentHttpHandlerCommand");
        launchConsoleProxy(cmd.getKeystoreBits(), cmd.getKeystorePassword(), cmd.getEncryptorPassword());
        return new Answer(cmd);
    }

    private Answer executeProxyLoadScan(final Command cmd, final long proxyVmId, final String proxyVmName, final String proxyManagementIp, final int cmdPort) {
        String result = null;

        final StringBuffer sb = new StringBuffer();
        sb.append("http://").append(proxyManagementIp).append(":" + cmdPort).append("/cmd/getstatus");

        boolean success = true;
        try {
            final URL url = new URL(sb.toString());
            final URLConnection conn = url.openConnection();

            final InputStream is = conn.getInputStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            final StringBuilder sb2 = new StringBuilder();
            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    sb2.append(line + "\n");
                }
                result = sb2.toString();
            } catch (final IOException e) {
                success = false;
            } finally {
                try {
                    is.close();
                } catch (final IOException e) {
                    s_logger.warn("Exception when closing , console proxy address : " + proxyManagementIp);
                    success = false;
                }
            }
        } catch (final IOException e) {
            s_logger.warn("Unable to open console proxy command port url, console proxy address : " + proxyManagementIp);
            success = false;
        }

        return new ConsoleProxyLoadAnswer(cmd, proxyVmId, proxyVmName, success, result);
    }

    private void launchConsoleProxy(final byte[] ksBits, final String ksPassword, final String encryptorPassword) {
        final Object resource = this;
        if (_consoleProxyMain == null) {
            _consoleProxyMain = new Thread(new ManagedContextRunnable() {
                @Override
                protected void runInContext() {
                    try {
                        final Class<?> consoleProxyClazz = Class.forName("com.cloud.consoleproxy.ConsoleProxy");
                        try {
                            s_logger.info("Invoke setEncryptorPassword(), ecnryptorPassword: " + encryptorPassword);
                            final Method methodSetup = consoleProxyClazz.getMethod("setEncryptorPassword", String.class);
                            methodSetup.invoke(null, encryptorPassword);

                            s_logger.info("Invoke startWithContext()");
                            final Method method = consoleProxyClazz.getMethod("startWithContext", Properties.class, Object.class, byte[].class, String.class);
                            method.invoke(null, _properties, resource, ksBits, ksPassword);
                        } catch (final SecurityException e) {
                            s_logger.error("Unable to launch console proxy due to SecurityException", e);
                            System.exit(ExitStatus.Error.value());
                        } catch (final NoSuchMethodException e) {
                            s_logger.error("Unable to launch console proxy due to NoSuchMethodException", e);
                            System.exit(ExitStatus.Error.value());
                        } catch (final IllegalArgumentException e) {
                            s_logger.error("Unable to launch console proxy due to IllegalArgumentException", e);
                            System.exit(ExitStatus.Error.value());
                        } catch (final IllegalAccessException e) {
                            s_logger.error("Unable to launch console proxy due to IllegalAccessException", e);
                            System.exit(ExitStatus.Error.value());
                        } catch (final InvocationTargetException e) {
                            s_logger.error("Unable to launch console proxy due to InvocationTargetException " + e.getTargetException().toString(), e);
                            System.exit(ExitStatus.Error.value());
                        }
                    } catch (final ClassNotFoundException e) {
                        s_logger.error("Unable to launch console proxy due to ClassNotFoundException");
                        System.exit(ExitStatus.Error.value());
                    }
                }
            }, "Console-Proxy-Main");
            _consoleProxyMain.setDaemon(true);
            _consoleProxyMain.start();
        } else {
            s_logger.info("com.cloud.consoleproxy.ConsoleProxy is already running");

            try {
                final Class<?> consoleProxyClazz = Class.forName("com.cloud.consoleproxy.ConsoleProxy");
                final Method methodSetup = consoleProxyClazz.getMethod("setEncryptorPassword", String.class);
                methodSetup.invoke(null, encryptorPassword);
            } catch (final SecurityException e) {
                s_logger.error("Unable to launch console proxy due to SecurityException", e);
                System.exit(ExitStatus.Error.value());
            } catch (final NoSuchMethodException e) {
                s_logger.error("Unable to launch console proxy due to NoSuchMethodException", e);
                System.exit(ExitStatus.Error.value());
            } catch (final IllegalArgumentException e) {
                s_logger.error("Unable to launch console proxy due to IllegalArgumentException", e);
                System.exit(ExitStatus.Error.value());
            } catch (final IllegalAccessException e) {
                s_logger.error("Unable to launch console proxy due to IllegalAccessException", e);
                System.exit(ExitStatus.Error.value());
            } catch (final InvocationTargetException e) {
                s_logger.error("Unable to launch console proxy due to InvocationTargetException " + e.getTargetException().toString(), e);
                System.exit(ExitStatus.Error.value());
            } catch (final ClassNotFoundException e) {
                s_logger.error("Unable to launch console proxy due to ClassNotFoundException", e);
                System.exit(ExitStatus.Error.value());
            }
        }
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        _localgw = (String) params.get("localgw");
        _eth1mask = (String) params.get("eth1mask");
        _eth1ip = (String) params.get("eth1ip");
        if (_eth1ip != null) {
            params.put("private.network.device", "eth1");
        } else {
            s_logger.info("eth1ip parameter has not been configured, assuming that we are not inside a system vm");
        }

        final String eth2ip = (String) params.get("eth2ip");
        if (eth2ip != null) {
            params.put("public.network.device", "eth2");
        } else {
            s_logger.info("eth2ip parameter is not found, assuming that we are not inside a system vm");
        }

        super.configure(name, params);

        for (final Map.Entry<String, Object> entry : params.entrySet()) {
            _properties.put(entry.getKey(), entry.getValue());
        }

        String value = (String) params.get("premium");
        if (value != null && value.equals("premium")) {
            _proxyPort = 443;
        } else {
            value = (String) params.get("consoleproxy.httpListenPort");
            _proxyPort = NumbersUtil.parseInt(value, 80);
        }

        value = (String) params.get("proxy_vm");
        _proxyVmId = NumbersUtil.parseLong(value, 0);

        if (_localgw != null) {
            final String mgmtHost = (String) params.get("host");
            if (_eth1ip != null) {
                addRouteToInternalIpOrCidr(_localgw, _eth1ip, _eth1mask, mgmtHost);
                final String internalDns1 = (String) params.get("internaldns1");
                if (internalDns1 == null) {
                    s_logger.warn("No DNS entry found during configuration of NfsSecondaryStorage");
                } else {
                    addRouteToInternalIpOrCidr(_localgw, _eth1ip, _eth1mask, internalDns1);
                }
                final String internalDns2 = (String) params.get("internaldns2");
                if (internalDns2 != null) {
                    addRouteToInternalIpOrCidr(_localgw, _eth1ip, _eth1mask, internalDns2);
                }
            }
        }

        _pubIp = (String) params.get("public.ip");

        value = (String) params.get("disable_rp_filter");
        if (value != null && value.equalsIgnoreCase("true")) {
            disableRpFilter();
        }

        if (s_logger.isInfoEnabled()) {
            s_logger.info("Receive proxyVmId in ConsoleProxyResource configuration as " + _proxyVmId);
        }

        return true;
    }

    @Override
    protected String getDefaultScriptsDir() {
        return null;
    }

    @Override
    public void disconnected() {
    }

    private void addRouteToInternalIpOrCidr(final String localgw, final String eth1ip, final String eth1mask, final String destIpOrCidr) {
        s_logger.debug("addRouteToInternalIp: localgw=" + localgw + ", eth1ip=" + eth1ip + ", eth1mask=" + eth1mask + ",destIp=" + destIpOrCidr);
        if (destIpOrCidr == null) {
            s_logger.debug("addRouteToInternalIp: destIp is null");
            return;
        }
        if (!NetUtils.isValidIp(destIpOrCidr) && !NetUtils.isValidCIDR(destIpOrCidr)) {
            s_logger.warn(" destIp is not a valid ip address or cidr destIp=" + destIpOrCidr);
            return;
        }
        boolean inSameSubnet = false;
        if (NetUtils.isValidIp(destIpOrCidr)) {
            if (eth1ip != null && eth1mask != null) {
                inSameSubnet = NetUtils.sameSubnet(eth1ip, destIpOrCidr, eth1mask);
            } else {
                s_logger.warn("addRouteToInternalIp: unable to determine same subnet: _eth1ip=" + eth1ip + ", dest ip=" + destIpOrCidr + ", _eth1mask=" + eth1mask);
            }
        } else {
            inSameSubnet = NetUtils.isNetworkAWithinNetworkB(destIpOrCidr, NetUtils.ipAndNetMaskToCidr(eth1ip, eth1mask));
        }
        if (inSameSubnet) {
            s_logger.debug("addRouteToInternalIp: dest ip " + destIpOrCidr + " is in the same subnet as eth1 ip " + eth1ip);
            return;
        }
        Script command = new Script("/bin/bash", s_logger);
        command.add("-c");
        command.add("ip route delete " + destIpOrCidr);
        command.execute();
        command = new Script("/bin/bash", s_logger);
        command.add("-c");
        command.add("ip route add " + destIpOrCidr + " via " + localgw);
        final String result = command.execute();
        if (result != null) {
            s_logger.warn("Error in configuring route to internal ip err=" + result);
        } else {
            s_logger.debug("addRouteToInternalIp: added route to internal ip=" + destIpOrCidr + " via " + localgw);
        }
    }

    private void disableRpFilter() {
        try (FileWriter fstream = new FileWriter("/proc/sys/net/ipv4/conf/eth2/rp_filter");
             BufferedWriter out = new BufferedWriter(fstream)) {
            out.write("0");
        } catch (final IOException e) {
            s_logger.warn("Unable to disable rp_filter");
        }
    }

    @Override
    public void setName(final String name) {
    }

    @Override
    public Map<String, Object> getConfigParams() {
        return new HashMap<>();
    }

    @Override
    public void setConfigParams(final Map<String, Object> params) {
    }

    @Override
    public int getRunLevel() {
        return 0;
    }

    @Override
    public void setRunLevel(final int level) {
    }

    public String authenticateConsoleAccess(final String host, final String port, final String vmId, final String sid, final String ticket, final Boolean isReauthentication) {

        final ConsoleAccessAuthenticationCommand cmd = new ConsoleAccessAuthenticationCommand(host, port, vmId, sid, ticket);
        cmd.setReauthenticating(isReauthentication);

        final ConsoleProxyAuthenticationResult result = new ConsoleProxyAuthenticationResult();
        result.setSuccess(false);
        result.setReauthentication(isReauthentication);

        try {
            final AgentControlAnswer answer = getAgentControl().sendRequest(cmd, 10000);

            if (answer != null) {
                final ConsoleAccessAuthenticationAnswer authAnswer = (ConsoleAccessAuthenticationAnswer) answer;
                result.setSuccess(authAnswer.succeeded());
                result.setHost(authAnswer.getHost());
                result.setPort(authAnswer.getPort());
                result.setTunnelUrl(authAnswer.getTunnelUrl());
                result.setTunnelSession(authAnswer.getTunnelSession());
            } else {
                s_logger.error("Authentication failed for vm: " + vmId + " with sid: " + sid);
            }
        } catch (final AgentControlChannelException e) {
            s_logger.error("Unable to send out console access authentication request due to " + e.getMessage(), e);
        }

        return new Gson().toJson(result);
    }

    public void reportLoadInfo(final String gsonLoadInfo) {
        final ConsoleProxyLoadReportCommand cmd = new ConsoleProxyLoadReportCommand(_proxyVmId, gsonLoadInfo);
        try {
            getAgentControl().postRequest(cmd);

            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Report proxy load info, proxy : " + _proxyVmId + ", load: " + gsonLoadInfo);
            }
        } catch (final AgentControlChannelException e) {
            s_logger.error("Unable to send out load info due to " + e.getMessage(), e);
        }
    }

    public void ensureRoute(final String address) {
        if (_localgw != null) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Ensure route for " + address + " via " + _localgw);
            }

            // this method won't be called in high frequency, serialize access
            // to script execution
            synchronized (this) {
                try {
                    addRouteToInternalIpOrCidr(_localgw, _eth1ip, _eth1mask, address);
                } catch (final Throwable e) {
                    s_logger.warn("Unexpected exception while adding internal route to " + address, e);
                }
            }
        }
    }
}
