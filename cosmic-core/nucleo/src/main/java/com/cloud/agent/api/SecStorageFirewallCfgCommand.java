//

//

package com.cloud.agent.api;

import java.util.ArrayList;
import java.util.List;

public class SecStorageFirewallCfgCommand extends Command {

    private final List<PortConfig> portConfigs = new ArrayList<>();
    private boolean isAppendAIp = false;

    public SecStorageFirewallCfgCommand() {

    }

    public SecStorageFirewallCfgCommand(final boolean isAppend) {
        this.isAppendAIp = isAppend;
    }

    public void addPortConfig(final String sourceIp, final String port, final boolean add, final String intf) {
        final PortConfig pc = new PortConfig(sourceIp, port, add, intf);
        this.portConfigs.add(pc);
    }

    public boolean getIsAppendAIp() {
        return isAppendAIp;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    public List<PortConfig> getPortConfigs() {
        return portConfigs;
    }

    public static class PortConfig {
        boolean add;
        String sourceIp;
        String port;
        String intf;

        public PortConfig(final String sourceIp, final String port, final boolean add, final String intf) {
            this.add = add;
            this.sourceIp = sourceIp;
            this.port = port;
            this.intf = intf;
        }

        public PortConfig() {

        }

        public boolean isAdd() {
            return add;
        }

        public String getSourceIp() {
            return sourceIp;
        }

        public String getPort() {
            return port;
        }

        public String getIntf() {
            return intf;
        }
    }
}
