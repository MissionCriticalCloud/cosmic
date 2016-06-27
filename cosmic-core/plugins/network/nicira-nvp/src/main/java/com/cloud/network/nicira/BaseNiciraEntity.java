//

//

package com.cloud.network.nicira;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public abstract class BaseNiciraEntity implements Serializable {
    protected String href;
    protected String schema;
    protected String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public String getHref() {
        return href;
    }

    public void setHref(final String href) {
        this.href = href;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(final String schema) {
        this.schema = schema;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .append(this.getClass())
                .append(uuid)
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
        final BaseNiciraEntity another = (BaseNiciraEntity) obj;
        return new EqualsBuilder().append(uuid, another.uuid).isEquals();
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE, false);
    }
}
