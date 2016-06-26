package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

public class ImageStoreDetailResponse extends BaseResponse {
    @SerializedName("name")
    @Param(description = "detail property name of the image store")
    private String name;

    @SerializedName("value")
    @Param(description = "detail property value of the image store")
    private String value;

    public ImageStoreDetailResponse() {
        super();
    }

    public ImageStoreDetailResponse(final String name, final String val) {
        super();
        this.name = name;
        this.value = val;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        final String oid = this.getName();
        result = prime * result + ((oid == null) ? 0 : oid.hashCode());
        return result;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
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
        final ImageStoreDetailResponse other = (ImageStoreDetailResponse) obj;
        final String oid = this.getName();
        if (oid == null) {
            if (other.getName() != null) {
                return false;
            }
        } else if (!oid.equals(other.getName())) {
            return false;
        } else if (this.getValue().equals(other.getValue())) {
            return false;
        }
        return true;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }
}
