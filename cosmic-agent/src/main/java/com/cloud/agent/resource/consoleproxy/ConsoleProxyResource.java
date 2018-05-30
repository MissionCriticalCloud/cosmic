package com.cloud.agent.resource.consoleproxy;

import com.cloud.agent.resource.AgentResource;
import com.cloud.agent.resource.AgentResourceBase;
import com.cloud.agent.service.AgentConfiguration;
import com.cloud.common.managed.context.ManagedContextRunnable;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.CheckHealthAnswer;
import com.cloud.legacymodel.communication.answer.ConsoleProxyLoadAnswer;
import com.cloud.legacymodel.communication.answer.ReadyAnswer;
import com.cloud.legacymodel.communication.command.CheckConsoleProxyLoadCommand;
import com.cloud.legacymodel.communication.command.CheckHealthCommand;
import com.cloud.legacymodel.communication.command.Command;
import com.cloud.legacymodel.communication.command.PingCommand;
import com.cloud.legacymodel.communication.command.ReadyCommand;
import com.cloud.legacymodel.communication.command.StartConsoleProxyAgentHttpHandlerCommand;
import com.cloud.legacymodel.communication.command.WatchConsoleProxyLoadCommand;
import com.cloud.legacymodel.communication.command.startup.StartupCommand;
import com.cloud.legacymodel.communication.command.startup.StartupProxyCommand;
import com.cloud.model.enumeration.ExitStatus;
import com.cloud.model.enumeration.HostType;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.net.NetUtils;
import com.cloud.utils.script.Script;

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
import java.util.Map;
import java.util.Properties;

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
public class ConsoleProxyResource extends AgentResourceBase implements AgentResource {
    static final Logger s_logger = LoggerFactory.getLogger(ConsoleProxyResource.class);

    private AgentConfiguration agentConfiguration;

    private final Properties _properties = new Properties();
    long _proxyVmId;
    int _proxyPort;
    String _localgw;
    String _eth1ip;
    String _eth1mask;
    String _pubIp;
    private Thread _consoleProxyMain = null;

    @Override
    public HostType getType() {
        return HostType.ConsoleProxy;
    }

    @Override
    public synchronized StartupCommand[] initialize() {
        final StartupProxyCommand cmd = new StartupProxyCommand();
        fillNetworkInformation(cmd);
        cmd.setProxyPort(this._proxyPort);
        cmd.setProxyVmId(this._proxyVmId);
        cmd.setVersion(ConsoleProxyResource.class.getPackage().getImplementationVersion());
        if (this._pubIp != null) {
            cmd.setPublicIpAddress(this._pubIp);
        }
        return new StartupCommand[]{cmd};
    }

    @Override
    public PingCommand getCurrentStatus(final long id) {
        return new PingCommand(HostType.ConsoleProxy, id);
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
        if (this._consoleProxyMain == null) {
            this._consoleProxyMain = new Thread(new ManagedContextRunnable() {
                @Override
                protected void runInContext() {
                    try {
                        final Class<?> consoleProxyClazz = Class.forName("com.cloud.agent.resource.consoleproxy.ConsoleProxy");
                        try {
                            s_logger.info("Invoke setEncryptorPassword(), ecnryptorPassword: " + encryptorPassword);
                            final Method methodSetup = consoleProxyClazz.getMethod("setEncryptorPassword", String.class);
                            methodSetup.invoke(null, encryptorPassword);

                            s_logger.info("Invoke startWithContext()");
                            final Method method = consoleProxyClazz.getMethod("startWithContext", Properties.class, Object.class, byte[].class, String.class);
                            method.invoke(null, ConsoleProxyResource.this._properties, resource, ksBits, ksPassword);
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
            this._consoleProxyMain.setDaemon(true);
            this._consoleProxyMain.start();
        } else {
            s_logger.info("com.cloud.consoleproxy.ConsoleProxy is already running");

            try {
                final Class<?> consoleProxyClazz = Class.forName("com.cloud.agent.resource.consoleproxy.ConsoleProxy");
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
    public boolean configure(final Map<String, Object> params) throws ConfigurationException {
        this._localgw = (String) params.get("localgw");
        this._eth1mask = (String) params.get("eth1mask");
        this._eth1ip = (String) params.get("eth1ip");
        if (this._eth1ip != null) {
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

        super.configure(params);

        for (final Map.Entry<String, Object> entry : params.entrySet()) {
            this._properties.put(entry.getKey(), entry.getValue());
        }

        String value = (String) params.get("premium");
        if (value != null && value.equals("premium")) {
            this._proxyPort = 443;
        } else {
            value = (String) params.get("consoleproxy.httpListenPort");
            this._proxyPort = NumbersUtil.parseInt(value, 80);
        }

        value = (String) params.get("proxy_vm");
        this._proxyVmId = NumbersUtil.parseLong(value, 0);

        if (this._localgw != null) {
            final String mgmtHost = (String) params.get("host");
            if (this._eth1ip != null) {
                addRouteToInternalIpOrCidr(this._localgw, this._eth1ip, this._eth1mask, mgmtHost);
                final String internalDns1 = (String) params.get("internaldns1");
                if (internalDns1 == null) {
                    s_logger.warn("No DNS entry found during configuration of NfsSecondaryStorage");
                } else {
                    addRouteToInternalIpOrCidr(this._localgw, this._eth1ip, this._eth1mask, internalDns1);
                }
                final String internalDns2 = (String) params.get("internaldns2");
                if (internalDns2 != null) {
                    addRouteToInternalIpOrCidr(this._localgw, this._eth1ip, this._eth1mask, internalDns2);
                }
            }
        }

        this._pubIp = (String) params.get("public.ip");

        value = (String) params.get("disable_rp_filter");
        if (value != null && value.equalsIgnoreCase("true")) {
            disableRpFilter();
        }

        if (s_logger.isInfoEnabled()) {
            s_logger.info("Receive proxyVmId in ConsoleProxyResource configuration as " + this._proxyVmId);
        }

        return true;
    }

    @Override
    public void configure(final AgentConfiguration agentConfiguration) {
        this.agentConfiguration = agentConfiguration;
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
        if (!NetUtils.isValidIp4(destIpOrCidr) && !NetUtils.isValidIp4Cidr(destIpOrCidr)) {
            s_logger.warn(" destIp is not a valid ip address or cidr destIp=" + destIpOrCidr);
            return;
        }
        boolean inSameSubnet = false;
        if (NetUtils.isValidIp4(destIpOrCidr)) {
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
        try (final FileWriter fstream = new FileWriter("/proc/sys/net/ipv4/conf/eth2/rp_filter");
             final BufferedWriter out = new BufferedWriter(fstream)) {
            out.write("0");
        } catch (final IOException e) {
            s_logger.warn("Unable to disable rp_filter");
        }
    }

    @Override
    public void setName(final String name) {
    }
}
