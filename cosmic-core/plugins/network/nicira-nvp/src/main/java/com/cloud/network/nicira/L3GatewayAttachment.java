//

//

package com.cloud.network.nicira;

/**
 *
 */
public class L3GatewayAttachment extends Attachment {
    private final String type = "L3GatewayAttachment";
    private String l3GatewayServiceUuid;
    private Long vlanId;

    public L3GatewayAttachment(final String l3GatewayServiceUuid) {
        this.l3GatewayServiceUuid = l3GatewayServiceUuid;
    }

    public L3GatewayAttachment(final String l3GatewayServiceUuid, final long vlanId) {
        this.l3GatewayServiceUuid = l3GatewayServiceUuid;
        this.vlanId = vlanId;
    }

    public String getL3GatewayServiceUuid() {
        return l3GatewayServiceUuid;
    }

    public void setL3GatewayServiceUuid(final String l3GatewayServiceUuid) {
        this.l3GatewayServiceUuid = l3GatewayServiceUuid;
    }

    public long getVlanId() {
        return vlanId;
    }

    public void setVlanId(final long vlanId) {
        this.vlanId = vlanId;
    }
}
