package com.cloud.agent.resource.virtualnetwork.facade;

import com.cloud.agent.api.SetupGuestNetworkCommand;
import com.cloud.agent.api.SetupVRCommand;
import com.cloud.agent.api.UpdateVmOverviewCommand;
import com.cloud.agent.api.routing.CreateIpAliasCommand;
import com.cloud.agent.api.routing.DeleteIpAliasCommand;
import com.cloud.agent.api.routing.DnsMasqConfigCommand;
import com.cloud.agent.api.routing.IpAssocCommand;
import com.cloud.agent.api.routing.IpAssocVpcCommand;
import com.cloud.agent.api.routing.LoadBalancerConfigCommand;
import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.api.routing.RemoteAccessVpnCfgCommand;
import com.cloud.agent.api.routing.SavePasswordCommand;
import com.cloud.agent.api.routing.SetFirewallRulesCommand;
import com.cloud.agent.api.routing.SetMonitorServiceCommand;
import com.cloud.agent.api.routing.SetNetworkACLCommand;
import com.cloud.agent.api.routing.SetPortForwardingRulesCommand;
import com.cloud.agent.api.routing.SetPortForwardingRulesVpcCommand;
import com.cloud.agent.api.routing.SetPublicIpACLCommand;
import com.cloud.agent.api.routing.SetSourceNatCommand;
import com.cloud.agent.api.routing.SetStaticNatRulesCommand;
import com.cloud.agent.api.routing.SetStaticRouteCommand;
import com.cloud.agent.api.routing.SetupPrivateGatewayCommand;
import com.cloud.agent.api.routing.Site2SiteVpnCfgCommand;
import com.cloud.agent.api.routing.VmDataCommand;
import com.cloud.agent.api.routing.VpnUsersCfgCommand;
import com.cloud.agent.resource.virtualnetwork.ConfigItem;
import com.cloud.agent.resource.virtualnetwork.FileConfigItem;
import com.cloud.agent.resource.virtualnetwork.ScriptConfigItem;
import com.cloud.agent.resource.virtualnetwork.VRScripts;
import com.cloud.utils.exception.CloudRuntimeException;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractConfigItemFacade {

    private static final Logger s_logger = LoggerFactory.getLogger(AbstractConfigItemFacade.class);

    private final static Gson gson;

    private static final Hashtable<Class<? extends NetworkElementCommand>, AbstractConfigItemFacade> flyweight = new Hashtable<>();

    static {
        gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .disableHtmlEscaping()
                .create();

        flyweight.put(SetPortForwardingRulesVpcCommand.class, new SetPortForwardingRulesVpcConfigItem());
        flyweight.put(SetPortForwardingRulesCommand.class, new SetPortForwardingRulesConfigItem());
        flyweight.put(SetStaticRouteCommand.class, new NetworkOverviewConfigItem());
        flyweight.put(SetStaticNatRulesCommand.class, new SetStaticNatRulesConfigItem());
        flyweight.put(LoadBalancerConfigCommand.class, new LoadBalancerConfigItem());
        flyweight.put(SavePasswordCommand.class, new SavePasswordConfigItem());
        flyweight.put(UpdateVmOverviewCommand.class, new VmOverviewConfigItem());
        flyweight.put(CreateIpAliasCommand.class, new CreateIpAliasConfigItem()); // Deprecated
        flyweight.put(DnsMasqConfigCommand.class, new DnsMasqConfigItem()); // Deprecated
        flyweight.put(DeleteIpAliasCommand.class, new DeleteIpAliasConfigItem()); // Deprecated
        flyweight.put(VmDataCommand.class, new VmDataConfigItem()); // Move to vm overview
        flyweight.put(SetFirewallRulesCommand.class, new SetFirewallRulesConfigItem());
        flyweight.put(RemoteAccessVpnCfgCommand.class, new RemoteAccessVpnConfigItem());
        flyweight.put(VpnUsersCfgCommand.class, new VpnUsersConfigItem());
        flyweight.put(Site2SiteVpnCfgCommand.class, new Site2SiteVpnConfigItem());
        flyweight.put(SetMonitorServiceCommand.class, new SetMonitorServiceConfigItem()); // Deprecated
        flyweight.put(SetupGuestNetworkCommand.class, new NetworkOverviewConfigItem());
        flyweight.put(SetupPrivateGatewayCommand.class, new NetworkOverviewConfigItem());
        flyweight.put(SetNetworkACLCommand.class, new SetNetworkAclConfigItem()); // Move to network overview
        flyweight.put(SetPublicIpACLCommand.class, new SetPublicIpAclConfigItem()); // Move to network overview
        flyweight.put(SetSourceNatCommand.class, new SetSourceNatConfigItem()); // Move to network overview
        flyweight.put(IpAssocCommand.class, new NetworkOverviewConfigItem());
        flyweight.put(IpAssocVpcCommand.class, new NetworkOverviewConfigItem());
        flyweight.put(SetupVRCommand.class, new SetVRConfigItem());
    }

    protected String destinationFile;

    public static AbstractConfigItemFacade getInstance(final Class<? extends NetworkElementCommand> key) {
        if (!flyweight.containsKey(key)) {
            throw new CloudRuntimeException("Unable to process the configuration for " + key.getClass().getName());
        }

        return flyweight.get(key);
    }

    protected List<ConfigItem> generateConfigItems(final Object configuration) {
        final List<ConfigItem> cfg = new LinkedList<>();

        final String remoteFilename = appendUuidToJsonFiles(destinationFile);
        s_logger.debug("Transformed filename " + destinationFile + " to " + remoteFilename);

        final String jsonConfigCommand = gson.toJson(configuration);
        s_logger.debug("Contents of jsonConfigCommand " + remoteFilename + " is: " + jsonConfigCommand);

        final ConfigItem configFile = new FileConfigItem(VRScripts.CONFIG_PERSIST_LOCATION, remoteFilename, jsonConfigCommand);
        cfg.add(configFile);

        final ConfigItem updateCommand = new ScriptConfigItem(VRScripts.UPDATE_CONFIG, remoteFilename);
        cfg.add(updateCommand);

        return cfg;
    }

    private static String appendUuidToJsonFiles(final String filename) {
        String remoteFileName = filename;
        if (remoteFileName.endsWith("json")) {
            remoteFileName += "." + UUID.randomUUID().toString();
        }
        return remoteFileName;
    }

    public abstract List<ConfigItem> generateConfig(NetworkElementCommand cmd);
}
