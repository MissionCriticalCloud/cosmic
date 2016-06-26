package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class TemplateZoneResponse extends BaseResponse {
    @SerializedName(ApiConstants.ZONE_ID)
    @Param(description = "the ID of the zone for the template")
    private String zoneId;

    @SerializedName(ApiConstants.ZONE_NAME)
    @Param(description = "the name of the zone for the template")
    private String zoneName;

    @SerializedName(ApiConstants.STATUS)
    @Param(description = "the status of the template")
    private String status;

    @SerializedName(ApiConstants.IS_READY)
    // propName="ready"  (FIXME:  this used to be part of Param annotation, do we need it?)
    @Param(description = "true if the template is ready to be deployed from, false otherwise.")
    private boolean isReady;

    @SerializedName(ApiConstants.CREATED)
    @Param(description = "the date this template was created")
    private Date created;

    public TemplateZoneResponse() {
        super();
    }

    public TemplateZoneResponse(final String zoneId, final String zoneName) {
        super();
        this.zoneId = zoneId;
        this.zoneName = zoneName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(final boolean isReady) {
        this.isReady = isReady;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        final String oid = this.getZoneId();
        result = prime * result + ((oid == null) ? 0 : oid.hashCode());
        return result;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(final String zoneId) {
        this.zoneId = zoneId;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TemplateZoneResponse other = (TemplateZoneResponse) obj;
        final String oid = this.getZoneId();
        if (oid == null) {
            if (other.getZoneId() != null) {
                return false;
            }
        } else if (!oid.equals(other.getZoneId())) {
            return false;
        } else if (this.getZoneName().equals(other.getZoneName())) {
            return false;
        }
        return true;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(final String zoneName) {
        this.zoneName = zoneName;
    }
}
