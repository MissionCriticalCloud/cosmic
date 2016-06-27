//

//

package com.cloud.agent.api.routing;

import com.cloud.agent.api.to.DhcpTO;

import java.util.List;

public class DnsMasqConfigCommand extends NetworkElementCommand {
    List<DhcpTO> dhcpTOs;

    public DnsMasqConfigCommand(final List<DhcpTO> dhcpTOs) {
        this.dhcpTOs = dhcpTOs;
    }

    public List<DhcpTO> getIps() {
        return dhcpTOs;
    }
}
