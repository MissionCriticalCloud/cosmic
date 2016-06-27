//

//

package com.cloud.agent.resource.virtualnetwork;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CheckRouterAnswer;
import com.cloud.agent.api.CheckRouterCommand;
import com.cloud.agent.api.CheckS2SVpnConnectionsAnswer;
import com.cloud.agent.api.CheckS2SVpnConnectionsCommand;
import com.cloud.agent.api.GetDomRVersionAnswer;
import com.cloud.agent.api.GetDomRVersionCmd;
import com.cloud.agent.api.GetRouterAlertsAnswer;
import com.cloud.agent.api.routing.AggregationControlCommand;
import com.cloud.agent.api.routing.AggregationControlCommand.Action;
import com.cloud.agent.api.routing.GetRouterAlertsCommand;
import com.cloud.agent.api.routing.GroupAnswer;
import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.resource.virtualnetwork.facade.AbstractConfigItemFacade;
import com.cloud.utils.ExecutionResult;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.exception.CloudRuntimeException;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VirtualNetworkResource controls and configures virtual networking
 *
 * @config {@table
 * || Param Name | Description | Values | Default ||
 * }
 **/
public class VirtualRoutingResource {

    private static final Logger s_logger = LoggerFactory.getLogger(VirtualRoutingResource.class);
    protected Map<String, Lock> _vrLockMap = new HashMap<>();
    private final VirtualRouterDeployer _vrDeployer;
    private Map<String, Queue<NetworkElementCommand>> _vrAggregateCommandsSet;
    private String _name;
    private int _sleep;
    private int _retry;
    private int _port;
    private int _eachTimeout;

    private final String _cfgVersion = "1.0";

    public VirtualRoutingResource(final VirtualRouterDeployer deployer) {
        _vrDeployer = deployer;
    }

    public Answer executeRequest(final NetworkElementCommand cmd) {
        boolean aggregated = false;
        final String routerName = cmd.getAccessDetail(NetworkElementCommand.ROUTER_NAME);
        final Lock lock;
        if (_vrLockMap.containsKey(routerName)) {
            lock = _vrLockMap.get(routerName);
        } else {
            lock = new ReentrantLock();
            _vrLockMap.put(routerName, lock);
        }
        lock.lock();

        try {
            final ExecutionResult rc = _vrDeployer.prepareCommand(cmd);
            if (!rc.isSuccess()) {
                s_logger.error("Failed to prepare VR command due to " + rc.getDetails());
                return new Answer(cmd, false, rc.getDetails());
            }

            assert cmd.getRouterAccessIp() != null : "Why there is no access IP for VR?";

            if (cmd.isQuery()) {
                return executeQueryCommand(cmd);
            }

            if (cmd instanceof AggregationControlCommand) {
                return execute((AggregationControlCommand) cmd);
            }

            if (_vrAggregateCommandsSet.containsKey(routerName)) {
                _vrAggregateCommandsSet.get(routerName).add(cmd);
                aggregated = true;
                // Clean up would be done after command has been executed
                //TODO: Deal with group answer as well
                return new Answer(cmd);
            }

            final List<ConfigItem> cfg = generateCommandCfg(cmd);
            if (cfg == null) {
                return Answer.createUnsupportedCommandAnswer(cmd);
            }

            return applyConfig(cmd, cfg);
        } catch (final IllegalArgumentException e) {
            return new Answer(cmd, false, e.getMessage());
        } finally {
            lock.unlock();
            if (!aggregated) {
                final ExecutionResult rc = _vrDeployer.cleanupCommand(cmd);
                if (!rc.isSuccess()) {
                    s_logger.error("Failed to cleanup VR command due to " + rc.getDetails());
                }
            }
        }
    }

    private Answer executeQueryCommand(final NetworkElementCommand cmd) {
        if (cmd instanceof CheckRouterCommand) {
            return execute((CheckRouterCommand) cmd);
        } else if (cmd instanceof GetDomRVersionCmd) {
            return execute((GetDomRVersionCmd) cmd);
        } else if (cmd instanceof CheckS2SVpnConnectionsCommand) {
            return execute((CheckS2SVpnConnectionsCommand) cmd);
        } else if (cmd instanceof GetRouterAlertsCommand) {
            return execute((GetRouterAlertsCommand) cmd);
        } else {
            s_logger.error("Unknown query command in VirtualRoutingResource!");
            return Answer.createUnsupportedCommandAnswer(cmd);
        }
    }

    private ExecutionResult applyConfigToVR(final String routerAccessIp, final ConfigItem c) {
        return applyConfigToVR(routerAccessIp, c, VRScripts.DEFAULT_EXECUTEINVR_TIMEOUT);
    }

    private ExecutionResult applyConfigToVR(final String routerAccessIp, final ConfigItem c, final int timeout) {
        if (c instanceof FileConfigItem) {
            final FileConfigItem configItem = (FileConfigItem) c;

            return _vrDeployer.createFileInVR(routerAccessIp, configItem.getFilePath(), configItem.getFileName(), configItem.getFileContents());
        } else if (c instanceof ScriptConfigItem) {
            final ScriptConfigItem configItem = (ScriptConfigItem) c;
            return _vrDeployer.executeInVR(routerAccessIp, configItem.getScript(), configItem.getArgs(), timeout);
        }
        throw new CloudRuntimeException("Unable to apply unknown configitem of type " + c.getClass().getSimpleName());
    }

    private Answer applyConfig(final NetworkElementCommand cmd, final List<ConfigItem> cfg) {

        if (cfg.isEmpty()) {
            return new Answer(cmd, true, "Nothing to do");
        }

        final List<ExecutionResult> results = new ArrayList<>();
        final List<String> details = new ArrayList<>();
        boolean finalResult = false;
        for (final ConfigItem configItem : cfg) {
            final long startTimestamp = System.currentTimeMillis();
            ExecutionResult result = applyConfigToVR(cmd.getRouterAccessIp(), configItem);
            if (s_logger.isDebugEnabled()) {
                final long elapsed = System.currentTimeMillis() - startTimestamp;
                s_logger.debug("Processing " + configItem + " took " + elapsed + "ms");
            }
            if (result == null) {
                result = new ExecutionResult(false, "null execution result");
            }
            results.add(result);
            details.add(configItem.getInfo() + (result.isSuccess() ? " - success: " : " - failed: ") + result.getDetails());
            finalResult = result.isSuccess();
        }

        // Not sure why this matters, but log it anyway
        if (cmd.getAnswersCount() != results.size()) {
            s_logger.warn("Expected " + cmd.getAnswersCount() + " answers while executing " + cmd.getClass().getSimpleName() + " but received " + results.size());
        }

        if (results.size() == 1) {
            return new Answer(cmd, finalResult, results.get(0).getDetails());
        } else {
            return new GroupAnswer(cmd, finalResult, results.size(), details.toArray(new String[details.size()]));
        }
    }

    private CheckS2SVpnConnectionsAnswer execute(final CheckS2SVpnConnectionsCommand cmd) {

        final StringBuffer buff = new StringBuffer();
        for (final String ip : cmd.getVpnIps()) {
            buff.append(ip);
            buff.append(" ");
        }
        final ExecutionResult result = _vrDeployer.executeInVR(cmd.getRouterAccessIp(), VRScripts.S2SVPN_CHECK, buff.toString());
        return new CheckS2SVpnConnectionsAnswer(cmd, result.isSuccess(), result.getDetails());
    }

    private GetRouterAlertsAnswer execute(final GetRouterAlertsCommand cmd) {

        final String routerIp = cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP);
        final String args = cmd.getPreviousAlertTimeStamp();

        final ExecutionResult result = _vrDeployer.executeInVR(routerIp, VRScripts.ROUTER_ALERTS, args);
        String alerts[] = null;
        String lastAlertTimestamp = null;

        if (result.isSuccess()) {
            if (!result.getDetails().isEmpty() && !result.getDetails().trim().equals("No Alerts")) {
                alerts = result.getDetails().trim().split("\\\\n");
                final String[] lastAlert = alerts[alerts.length - 1].split(",");
                lastAlertTimestamp = lastAlert[0];
            }
            return new GetRouterAlertsAnswer(cmd, alerts, lastAlertTimestamp);
        } else {
            return new GetRouterAlertsAnswer(cmd, result.getDetails());
        }
    }

    private Answer execute(final CheckRouterCommand cmd) {
        final ExecutionResult result = _vrDeployer.executeInVR(cmd.getRouterAccessIp(), VRScripts.RVR_CHECK, null);
        if (!result.isSuccess()) {
            return new CheckRouterAnswer(cmd, result.getDetails());
        }
        return new CheckRouterAnswer(cmd, result.getDetails(), true);
    }

    private Answer execute(final GetDomRVersionCmd cmd) {
        final ExecutionResult result = _vrDeployer.executeInVR(cmd.getRouterAccessIp(), VRScripts.VERSION, null);
        if (!result.isSuccess()) {
            return new GetDomRVersionAnswer(cmd, "GetDomRVersionCmd failed");
        }
        final String[] lines = result.getDetails().split("&");
        if (lines.length != 2) {
            return new GetDomRVersionAnswer(cmd, result.getDetails());
        }
        return new GetDomRVersionAnswer(cmd, result.getDetails(), lines[0], lines[1]);
    }

    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        _name = name;

        String value = (String) params.get("ssh.sleep");
        _sleep = NumbersUtil.parseInt(value, 10) * 1000;

        value = (String) params.get("ssh.retry");
        _retry = NumbersUtil.parseInt(value, 36);

        value = (String) params.get("ssh.port");
        _port = NumbersUtil.parseInt(value, 3922);

        value = (String) params.get("router.aggregation.command.each.timeout");
        _eachTimeout = NumbersUtil.parseInt(value, 3);

        if (_vrDeployer == null) {
            throw new ConfigurationException("Unable to find the resource for VirtualRouterDeployer!");
        }

        _vrAggregateCommandsSet = new HashMap<>();
        return true;
    }

    public boolean connect(final String ipAddress) {
        return connect(ipAddress, _port);
    }

    public boolean connect(final String ipAddress, final int port) {
        return connect(ipAddress, port, _sleep);
    }

    public boolean connect(final String ipAddress, final int retry, final int sleep) {
        for (int i = 0; i <= retry; i++) {
            SocketChannel sch = null;
            try {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Trying to connect to " + ipAddress);
                }
                sch = SocketChannel.open();
                sch.configureBlocking(true);

                final InetSocketAddress addr = new InetSocketAddress(ipAddress, _port);
                sch.connect(addr);
                return true;
            } catch (final IOException e) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Could not connect to " + ipAddress);
                }
            } finally {
                if (sch != null) {
                    try {
                        sch.close();
                    } catch (final IOException e) {
                    }
                }
            }
            try {
                Thread.sleep(sleep);
            } catch (final InterruptedException e) {
            }
        }

        s_logger.debug("Unable to logon to " + ipAddress);

        return false;
    }

    private List<ConfigItem> generateCommandCfg(final NetworkElementCommand cmd) {
        s_logger.debug("Transforming " + cmd.getClass().getCanonicalName() + " to ConfigItems");

        final AbstractConfigItemFacade configItemFacade = AbstractConfigItemFacade.getInstance(cmd.getClass());

        return configItemFacade.generateConfig(cmd);
    }

    private Answer execute(final AggregationControlCommand cmd) {
        final Action action = cmd.getAction();
        final String routerName = cmd.getAccessDetail(NetworkElementCommand.ROUTER_NAME);
        assert routerName != null;
        assert cmd.getRouterAccessIp() != null;

        if (action == Action.Start) {
            assert (!_vrAggregateCommandsSet.containsKey(routerName));

            final Queue<NetworkElementCommand> queue = new LinkedBlockingQueue<>();
            _vrAggregateCommandsSet.put(routerName, queue);
            return new Answer(cmd, true, "Command aggregation started");
        } else if (action == Action.Finish) {
            final Queue<NetworkElementCommand> queue = _vrAggregateCommandsSet.get(routerName);
            int answerCounts = 0;
            try {
                final StringBuilder sb = new StringBuilder();
                sb.append("#Apache CloudStack Virtual Router Config File\n");
                sb.append("<version>\n" + _cfgVersion + "\n</version>\n");
                for (final NetworkElementCommand command : queue) {
                    answerCounts += command.getAnswersCount();
                    final List<ConfigItem> cfg = generateCommandCfg(command);
                    if (cfg == null) {
                        s_logger.warn("Unknown commands for VirtualRoutingResource, but continue: " + cmd.toString());
                        continue;
                    }

                    for (final ConfigItem c : cfg) {
                        sb.append(c.getAggregateCommand());
                    }
                }

                // TODO replace with applyConfig with a stop on fail
                final String cfgFileName = "VR-" + UUID.randomUUID().toString() + ".cfg";
                final FileConfigItem fileConfigItem = new FileConfigItem(VRScripts.CONFIG_CACHE_LOCATION, cfgFileName, sb.toString());
                final ScriptConfigItem scriptConfigItem = new ScriptConfigItem(VRScripts.VR_CFG, "-c " + VRScripts.CONFIG_CACHE_LOCATION + cfgFileName);
                // 120s is the minimal timeout
                int timeout = answerCounts * _eachTimeout;
                if (timeout < 120) {
                    timeout = 120;
                }

                ExecutionResult result = applyConfigToVR(cmd.getRouterAccessIp(), fileConfigItem);
                if (!result.isSuccess()) {
                    return new Answer(cmd, false, result.getDetails());
                }

                result = applyConfigToVR(cmd.getRouterAccessIp(), scriptConfigItem, timeout);
                if (!result.isSuccess()) {
                    return new Answer(cmd, false, result.getDetails());
                }

                return new Answer(cmd, true, "Command aggregation finished");
            } finally {
                queue.clear();
                _vrAggregateCommandsSet.remove(routerName);
            }
        }
        return new Answer(cmd, false, "Fail to recongize aggregation action " + action.toString());
    }
}
