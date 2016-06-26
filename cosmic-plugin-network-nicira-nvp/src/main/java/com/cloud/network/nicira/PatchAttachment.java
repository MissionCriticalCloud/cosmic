//

//

package com.cloud.network.nicira;

/**
 *
 */
public class PatchAttachment extends Attachment {
    private final String type = "PatchAttachment";
    private String peerPortUuid;

    public PatchAttachment(final String peerPortUuid) {
        this.peerPortUuid = peerPortUuid;
    }

    public String getPeerPortUuid() {
        return peerPortUuid;
    }

    public void setPeerPortUuid(final String peerPortUuid) {
        this.peerPortUuid = peerPortUuid;
    }
}
