//

//

package com.cloud.network.resource;

import com.cloud.agent.IAgentControl;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.PingCommand;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupNiciraNvpCommand;
import com.cloud.host.Host;
import com.cloud.host.Host.Type;
import com.cloud.network.nicira.ControlClusterStatus;
import com.cloud.network.nicira.ControlClusterStatus.ClusterRoleConfig;
import com.cloud.network.nicira.DestinationNatRule;
import com.cloud.network.nicira.Match;
import com.cloud.network.nicira.NatRule;
import com.cloud.network.nicira.NiciraNvpApi;
import com.cloud.network.nicira.NiciraNvpApiException;
import com.cloud.network.nicira.SourceNatRule;
import com.cloud.network.utils.CommandRetryUtility;
import com.cloud.resource.ServerResource;
import com.cloud.utils.nicira.nvp.plugin.NiciraNvpApiVersion;
import com.cloud.utils.rest.CloudstackRESTException;
import com.cloud.utils.rest.HttpClientHelper;

import javax.naming.ConfigurationException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NiciraNvpResource implements ServerResource {

    public static final int NAME_MAX_LEN = 40;
    public static final int NUM_RETRIES = 2;
    private static final Logger s_logger = LoggerFactory.getLogger(NiciraNvpResource.class);
    private static final int MAX_REDIRECTS = 5;

    private String name;
    private String guid;
    private String zoneId;

    private NiciraNvpApi niciraNvpApi;
    private NiciraNvpUtilities niciraNvpUtilities;
    private CommandRetryUtility retryUtility;

    public NiciraNvpApi getNiciraNvpApi() {
        return niciraNvpApi;
    }

    protected NiciraNvpApi createNiciraNvpApi(final String host, final String username, final String password) throws CloudstackRESTException {
        try {
            return NiciraNvpApi.create().host(host).username(username).password(password).httpClient(HttpClientHelper.createHttpClient(MAX_REDIRECTS)).build();
        } catch (final KeyManagementException e) {
            throw new CloudstackRESTException("Could not create HTTP client", e);
        } catch (final NoSuchAlgorithmException e) {
            throw new CloudstackRESTException("Could not create HTTP client", e);
        } catch (final KeyStoreException e) {
            throw new CloudstackRESTException("Could not create HTTP client", e);
        }
    }

    public NiciraNvpUtilities getNiciraNvpUtilities() {
        return niciraNvpUtilities;
    }

    @Override
    public boolean configure(final String ignoredName, final Map<String, Object> params) throws ConfigurationException {

        name = (String) params.get("name");
        if (name == null) {
            throw new ConfigurationException("Unable to find name");
        }

        guid = (String) params.get("guid");
        if (guid == null) {
            throw new ConfigurationException("Unable to find the guid");
        }

        zoneId = (String) params.get("zoneId");
        if (zoneId == null) {
            throw new ConfigurationException("Unable to find zone");
        }

        final String ip = (String) params.get("ip");
        if (ip == null) {
            throw new ConfigurationException("Unable to find IP");
        }

        final String adminuser = (String) params.get("adminuser");
        if (adminuser == null) {
            throw new ConfigurationException("Unable to find admin username");
        }

        final String adminpass = (String) params.get("adminpass");
        if (adminpass == null) {
            throw new ConfigurationException("Unable to find admin password");
        }

        niciraNvpUtilities = NiciraNvpUtilities.getInstance();
        retryUtility = CommandRetryUtility.getInstance();
        retryUtility.setServerResource(this);

        try {
            niciraNvpApi = createNiciraNvpApi(ip, adminuser, adminpass);
        } catch (final CloudstackRESTException e) {
            throw new ConfigurationException("Could not create a Nicira Nvp API client: " + e.getMessage());
        }

        return true;
    }

    public CommandRetryUtility getRetryUtility() {
        return retryUtility;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(final String name) {
        // TODO Auto-generated method stub
    }

    @Override
    public Type getType() {
        // Think up a better name for this Type?
        return Host.Type.L2Networking;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public StartupCommand[] initialize() {
        final StartupNiciraNvpCommand sc = new StartupNiciraNvpCommand();
        sc.setGuid(guid);
        sc.setName(name);
        sc.setDataCenter(zoneId);
        sc.setPod("");
        sc.setPrivateIpAddress("");
        sc.setStorageIpAddress("");
        sc.setVersion(NiciraNvpResource.class.getPackage().getImplementationVersion());
        return new StartupCommand[]{sc};
    }

    @Override
    public boolean stop() {
        return true;
    }

    @Override
    public PingCommand getCurrentStatus(final long id) {
        try {
            final ControlClusterStatus ccs = niciraNvpApi.getControlClusterStatus();
            getApiProviderMajorityVersion(ccs);
            if (!"stable".equals(ccs.getClusterStatus())) {
                s_logger.error("ControlCluster state is not stable: " + ccs.getClusterStatus());
                return null;
            }
        } catch (final NiciraNvpApiException e) {
            s_logger.error("getControlClusterStatus failed", e);
            return null;
        }
        return new PingCommand(Host.Type.L2Networking, id);
    }

    private void getApiProviderMajorityVersion(final ControlClusterStatus ccs) {
        final ClusterRoleConfig[] configuredRoles = ccs.getConfiguredRoles();
        if (configuredRoles != null) {
            final String apiProviderMajorityVersion = searchApiProvider(configuredRoles);
            NiciraNvpApiVersion.setNiciraApiVersion(apiProviderMajorityVersion);
            NiciraNvpApiVersion.logNiciraApiVersion();
        }
    }

    private String searchApiProvider(final ClusterRoleConfig[] configuredRoles) {
        for (int i = 0; i < configuredRoles.length; i++) {
            if (configuredRoles[i].getRole() != null && configuredRoles[i].getRole().equals("api_provider")) {
                return configuredRoles[i].getMajorityVersion();
            }
        }
        return null;
    }

    @Override
    public Answer executeRequest(final Command cmd) {
        final NiciraNvpRequestWrapper wrapper = NiciraNvpRequestWrapper.getInstance();
        try {
            return wrapper.execute(cmd, this);
        } catch (final Exception e) {
            s_logger.debug("Received unsupported command " + cmd.toString());
            return Answer.createUnsupportedCommandAnswer(cmd);
        }
    }

    @Override
    public void disconnected() {
    }

    @Override
    public IAgentControl getAgentControl() {
        return null;
    }

    @Override
    public void setAgentControl(final IAgentControl agentControl) {
    }

    public String natRuleToString(final NatRule rule) {

        final StringBuilder natRuleStr = new StringBuilder();
        natRuleStr.append("Rule ");
        natRuleStr.append(rule.getUuid());
        natRuleStr.append(" (");
        natRuleStr.append(rule.getType());
        natRuleStr.append(") :");
        final Match m = rule.getMatch();
        natRuleStr.append("match (");
        natRuleStr.append(m.getProtocol());
        natRuleStr.append(" ");
        natRuleStr.append(m.getSourceIpAddresses());
        natRuleStr.append(" [");
        natRuleStr.append(m.getSourcePort());
        natRuleStr.append(" ] -> ");
        natRuleStr.append(m.getDestinationIpAddresses());
        natRuleStr.append(" [");
        natRuleStr.append(m.getDestinationPort());
        natRuleStr.append(" ]) -->");
        if ("SourceNatRule".equals(rule.getType())) {
            natRuleStr.append(((SourceNatRule) rule).getToSourceIpAddressMin());
            natRuleStr.append("-");
            natRuleStr.append(((SourceNatRule) rule).getToSourceIpAddressMax());
            natRuleStr.append(" [");
            natRuleStr.append(((SourceNatRule) rule).getToSourcePort());
            natRuleStr.append(" ])");
        } else {
            natRuleStr.append(((DestinationNatRule) rule).getToDestinationIpAddress());
            natRuleStr.append(" [");
            natRuleStr.append(((DestinationNatRule) rule).getToDestinationPort());
            natRuleStr.append(" ])");
        }
        return natRuleStr.toString();
    }

    public String truncate(final String string, final int length) {
        if (string.length() <= length) {
            return string;
        } else {
            return string.substring(0, length);
        }
    }

    public NatRule[] generatePortForwardingRulePair(final String insideIp, final int[] insidePorts, final String outsideIp, final int[] outsidePorts,
                                                    final String protocol) {
        // Start with a basic static nat rule, then add port and protocol details
        final NatRule[] rulepair = generateStaticNatRulePair(insideIp, outsideIp);

        ((DestinationNatRule) rulepair[0]).setToDestinationPort(insidePorts[0]);
        rulepair[0].getMatch().setDestinationPort(outsidePorts[0]);
        rulepair[0].setOrder(50);
        rulepair[0].getMatch().setEthertype("IPv4");
        if ("tcp".equals(protocol)) {
            rulepair[0].getMatch().setProtocol(6);
        } else if ("udp".equals(protocol)) {
            rulepair[0].getMatch().setProtocol(17);
        }

        ((SourceNatRule) rulepair[1]).setToSourcePort(outsidePorts[0]);
        rulepair[1].getMatch().setSourcePort(insidePorts[0]);
        rulepair[1].setOrder(50);
        rulepair[1].getMatch().setEthertype("IPv4");
        if ("tcp".equals(protocol)) {
            rulepair[1].getMatch().setProtocol(6);
        } else if ("udp".equals(protocol)) {
            rulepair[1].getMatch().setProtocol(17);
        }

        return rulepair;
    }

    public NatRule[] generateStaticNatRulePair(final String insideIp, final String outsideIp) {
        final NatRule[] rulepair = new NatRule[2];
        rulepair[0] = new DestinationNatRule();
        rulepair[0].setType("DestinationNatRule");
        rulepair[0].setOrder(100);
        rulepair[1] = new SourceNatRule();
        rulepair[1].setType("SourceNatRule");
        rulepair[1].setOrder(100);

        Match m = new Match();
        m.setDestinationIpAddresses(outsideIp);
        rulepair[0].setMatch(m);
        ((DestinationNatRule) rulepair[0]).setToDestinationIpAddress(insideIp);

        // create matching snat rule
        m = new Match();
        m.setSourceIpAddresses(insideIp);
        rulepair[1].setMatch(m);
        ((SourceNatRule) rulepair[1]).setToSourceIpAddressMin(outsideIp);
        ((SourceNatRule) rulepair[1]).setToSourceIpAddressMax(outsideIp);

        return rulepair;
    }

    @Override
    public void setConfigParams(final Map<String, Object> params) {
        // TODO Auto-generated method stub
    }

    @Override
    public Map<String, Object> getConfigParams() {
        // TODO Auto-generated method stub
        return null;
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
