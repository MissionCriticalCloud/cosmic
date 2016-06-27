//

//

package com.cloud.network.nicira;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class VifAttachment extends Attachment {
    private final String type = "VifAttachment";
    private String vifUuid;

    public VifAttachment() {
    }

    public VifAttachment(final String vifUuid) {
        this.vifUuid = vifUuid;
    }

    public String getVifUuid() {
        return vifUuid;
    }

    public void setVifUuid(final String vifUuid) {
        this.vifUuid = vifUuid;
    }

    public String getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .append(this.getClass())
                .append(vifUuid)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(this.getClass().isInstance(obj))) {
            return false;
        }
        final VifAttachment another = (VifAttachment) obj;
        return new EqualsBuilder().append(vifUuid, another.vifUuid).isEquals();
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE, false);
    }
}
