//

//

package com.cloud.agent.resource.virtualnetwork.facade;

import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.api.routing.Site2SiteVpnCfgCommand;
import com.cloud.agent.resource.virtualnetwork.ConfigItem;
import com.cloud.agent.resource.virtualnetwork.VRScripts;
import com.cloud.agent.resource.virtualnetwork.model.ConfigBase;
import com.cloud.agent.resource.virtualnetwork.model.Site2SiteVpn;

import java.util.List;

public class Site2SiteVpnConfigItem extends AbstractConfigItemFacade {

    @Override
    public List<ConfigItem> generateConfig(final NetworkElementCommand cmd) {
        final Site2SiteVpnCfgCommand command = (Site2SiteVpnCfgCommand) cmd;

        final Site2SiteVpn site2siteVpn = new Site2SiteVpn(command.getLocalPublicIp(), command.getLocalGuestCidr(), command.getLocalPublicGateway(), command.getPeerGatewayIp(),
                command.getPeerGuestCidrList(), command.getEspPolicy(), command.getIkePolicy(), command.getIpsecPsk(), command.getIkeLifetime(), command.getEspLifetime(),
                command.isCreate(), command.getDpd(),
                command.isPassive(), command.getEncap());
        return generateConfigItems(site2siteVpn);
    }

    @Override
    protected List<ConfigItem> generateConfigItems(final ConfigBase configuration) {
        destinationFile = VRScripts.SITE_2_SITE_VPN_CONFIG;

        return super.generateConfigItems(configuration);
    }
}
