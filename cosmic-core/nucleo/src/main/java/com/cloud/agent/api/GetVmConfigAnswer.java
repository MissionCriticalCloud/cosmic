//

//

package com.cloud.agent.api;

import java.util.List;

public class GetVmConfigAnswer extends Answer {

    String vmName;
    List<NicDetails> nics;

    protected GetVmConfigAnswer() {
    }

    public GetVmConfigAnswer(final String vmName, final List<NicDetails> nics) {
        this.vmName = vmName;
        this.nics = nics;
    }

    public String getVmName() {
        return vmName;
    }

    public List<NicDetails> getNics() {
        return nics;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    public class NicDetails {
        String macAddress;
        String vlanid;
        boolean state;

        public NicDetails() {
        }

        public NicDetails(final String macAddress, final String vlanid, final boolean state) {
            this.macAddress = macAddress;
            this.vlanid = vlanid;
            this.state = state;
        }

        public String getMacAddress() {
            return macAddress;
        }

        public String getVlanid() {
            return vlanid;
        }

        public boolean getState() {
            return state;
        }
    }
}
