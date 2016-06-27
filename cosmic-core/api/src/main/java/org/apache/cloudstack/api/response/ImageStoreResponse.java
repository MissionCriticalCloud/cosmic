package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import com.cloud.storage.ImageStore;
import com.cloud.storage.ScopeType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.LinkedHashSet;
import java.util.Set;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = ImageStore.class)
public class ImageStoreResponse extends BaseResponse {
    @SerializedName("id")
    @Param(description = "the ID of the image store")
    private String id;

    @SerializedName("zoneid")
    @Param(description = "the Zone ID of the image store")
    private String zoneId;

    @SerializedName(ApiConstants.ZONE_NAME)
    @Param(description = "the Zone name of the image store")
    private String zoneName;

    @SerializedName("name")
    @Param(description = "the name of the image store")
    private String name;

    @SerializedName("url")
    @Param(description = "the url of the image store")
    private String url;

    @SerializedName("protocol")
    @Param(description = "the protocol of the image store")
    private String protocol;

    @SerializedName("providername")
    @Param(description = "the provider name of the image store")
    private String providerName;

    @SerializedName("scope")
    @Param(description = "the scope of the image store")
    private ScopeType scope;

    @SerializedName("details")
    @Param(description = "the details of the image store")
    private Set<ImageStoreDetailResponse> details;

    public ImageStoreResponse() {
        this.details = new LinkedHashSet<>();
    }

    @Override
    public String getObjectId() {
        return this.getId();
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(final String zoneId) {
        this.zoneId = zoneId;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(final String zoneName) {
        this.zoneName = zoneName;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(final String providerName) {
        this.providerName = providerName;
    }

    public ScopeType getScope() {
        return scope;
    }

    public void setScope(final ScopeType type) {
        this.scope = type;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    public Set<ImageStoreDetailResponse> getDetails() {
        return details;
    }

    public void setDetails(final Set<ImageStoreDetailResponse> details) {
        this.details = details;
    }

    public void addDetail(final ImageStoreDetailResponse detail) {
        this.details.add(detail);
    }
}
