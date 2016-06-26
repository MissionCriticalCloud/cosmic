//

//

package com.cloud.network.nicira;

import java.util.List;

public abstract class BaseNiciraNamedEntity extends BaseNiciraEntity {

    protected String displayName;
    protected List<NiciraNvpTag> tags;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public List<NiciraNvpTag> getTags() {
        return tags;
    }

    public void setTags(final List<NiciraNvpTag> tags) {
        this.tags = tags;
    }
}
