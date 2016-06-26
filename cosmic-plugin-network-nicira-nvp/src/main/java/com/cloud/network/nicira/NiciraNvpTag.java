//

//

package com.cloud.network.nicira;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NiciraNvpTag {
    private static final int TAG_MAX_LEN = 40;
    private static final Logger s_logger = LoggerFactory.getLogger(NiciraNvpTag.class);
    private String scope;
    private String tag;

    public NiciraNvpTag() {
    }

    public NiciraNvpTag(final String scope, final String tag) {
        this.scope = scope;
        if (tag.length() > 40) {
            s_logger.warn("tag \"" + tag + "\" too long, truncating to 40 characters");
            this.tag = tag.substring(0, TAG_MAX_LEN);
        } else {
            this.tag = tag;
        }
    }

    public String getScope() {
        return scope;
    }

    public void setScope(final String scope) {
        this.scope = scope;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(final String tag) {
        if (tag.length() > 40) {
            s_logger.warn("tag \"" + tag + "\" too long, truncating to 40 characters");
            this.tag = tag.substring(0, 40);
        } else {
            this.tag = tag;
        }
    }
}
