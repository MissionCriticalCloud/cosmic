package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.template.VirtualMachineTemplate;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = VirtualMachineTemplate.class)
public class TemplateResponse extends BaseResponse implements ControlledViewEntityResponse {
    @SerializedName(ApiConstants.ID)
    @Param(description = "the template ID")
    private String id;

    @SerializedName(ApiConstants.NAME)
    @Param(description = "the template name")
    private String name;

    @SerializedName(ApiConstants.DISPLAY_TEXT)
    @Param(description = "the template display text")
    private String displayText;

    @SerializedName(ApiConstants.IS_PUBLIC)
    // propName="public"  (FIXME:  this used to be part of Param annotation, do we need it?)
    @Param(description = "true if this template is a public template, false otherwise")
    private boolean isPublic;

    @SerializedName(ApiConstants.CREATED)
    @Param(description = "the date this template was created")
    private Date created;

    @SerializedName("removed")
    @Param(description = "the date this template was removed")
    private Date removed;

    @SerializedName(ApiConstants.IS_READY)
    // propName="ready"  (FIXME:  this used to be part of Param annotation, do we need it?)
    @Param(description = "true if the template is ready to be deployed from, false otherwise.")
    private boolean isReady;

    @SerializedName(ApiConstants.PASSWORD_ENABLED)
    @Param(description = "true if the reset password feature is enabled, false otherwise")
    private Boolean passwordEnabled;

    @SerializedName(ApiConstants.FORMAT)
    @Param(description = "the format of the template.")
    private ImageFormat format;

    @SerializedName(ApiConstants.BOOTABLE)
    @Param(description = "true if the ISO is bootable, false otherwise")
    private Boolean bootable;

    @SerializedName(ApiConstants.IS_FEATURED)
    @Param(description = "true if this template is a featured template, false otherwise")
    private boolean featured;

    @SerializedName("crossZones")
    @Param(description = "true if the template is managed across all Zones, false otherwise")
    private boolean crossZones;

    @SerializedName(ApiConstants.OS_TYPE_ID)
    @Param(description = "the ID of the OS type for this template.")
    private String osTypeId;

    @SerializedName("ostypename")
    @Param(description = "the name of the OS type for this template.")
    private String osTypeName;

    @SerializedName(ApiConstants.ACCOUNT_ID)
    @Param(description = "the account id to which the template belongs")
    private String accountId;

    @SerializedName(ApiConstants.ACCOUNT)
    @Param(description = "the account name to which the template belongs")
    private String account;

    //TODO: since a template can be associated to more than one zones, this model is not accurate. For backward-compatibility, keep these fields
    // here, but add a zones field to capture multiple zones.
    @SerializedName(ApiConstants.ZONE_ID)
    @Param(description = "the ID of the zone for this template")
    private String zoneId;

    @SerializedName(ApiConstants.ZONE_NAME)
    @Param(description = "the name of the zone for this template")
    private String zoneName;

    @SerializedName(ApiConstants.STATUS)
    @Param(description = "the status of the template")
    private String status;

    @SerializedName(ApiConstants.SIZE)
    @Param(description = "the size of the template")
    private Long size;

    @SerializedName("templatetype")
    @Param(description = "the type of the template")
    private String templateType;

    @SerializedName(ApiConstants.HYPERVISOR)
    @Param(description = "the hypervisor on which the template runs")
    private String hypervisor;

    @SerializedName(ApiConstants.DOMAIN)
    @Param(description = "the name of the domain to which the template belongs")
    private String domainName;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the ID of the domain to which the template belongs")
    private String domainId;

    @SerializedName(ApiConstants.IS_EXTRACTABLE)
    @Param(description = "true if the template is extractable, false otherwise")
    private Boolean extractable;

    @SerializedName(ApiConstants.CHECKSUM)
    @Param(description = "checksum of the template")
    private String checksum;

    @SerializedName("sourcetemplateid")
    @Param(description = "the template ID of the parent template if present")
    private String sourcetemplateId;

    @SerializedName(ApiConstants.HOST_ID)
    @Param(description = "the ID of the secondary storage host for the template")
    private String hostId;

    @SerializedName("hostname")
    @Param(description = "the name of the secondary storage host for the template")
    private String hostName;

    @SerializedName(ApiConstants.TEMPLATE_TAG)
    @Param(description = "the tag of this template")
    private String templateTag;

    @SerializedName(ApiConstants.PROJECT_ID)
    @Param(description = "the project id of the template")
    private String projectId;

    @SerializedName(ApiConstants.PROJECT)
    @Param(description = "the project name of the template")
    private String projectName;

    @SerializedName(ApiConstants.DETAILS)
    @Param(description = "additional key/value details tied with template")
    private Map details;

    // To avoid breaking backwards compatibility, we still treat a template at different zones as different templates, so not embedding
    // template_zone information in this TemplateZoneResponse set.
    //    @SerializedName("zones")  @Param(description="list of zones associated with tempate", responseObject = TemplateZoneResponse.class)
    //    private Set<TemplateZoneResponse> zones;

    @SerializedName(ApiConstants.TAGS)
    @Param(description = "the list of resource tags associated with tempate", responseObject = ResourceTagResponse.class)
    private Set<ResourceTagResponse> tags;

    @SerializedName(ApiConstants.SSHKEY_ENABLED)
    @Param(description = "true if template is sshkey enabled, false otherwise")
    private Boolean sshKeyEnabled;

    @SerializedName(ApiConstants.IS_DYNAMICALLY_SCALABLE)
    @Param(description = "true if template contains XS tools inorder to support dynamic scaling of VM cpu/memory")
    private Boolean isDynamicallyScalable;

    public TemplateResponse() {
        //  zones = new LinkedHashSet<TemplateZoneResponse>();
        tags = new LinkedHashSet<>();
    }

    @Override
    public String getObjectId() {
        return getId();
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setZoneName(final String zoneName) {
        this.zoneName = zoneName;
    }

    public void setAccountId(final String accountId) {
        this.accountId = accountId;
    }

    @Override
    public void setAccountName(final String account) {
        this.account = account;
    }

    @Override
    public void setProjectId(final String projectId) {
        this.projectId = projectId;
    }

    @Override
    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    @Override
    public void setDomainId(final String domainId) {
        this.domainId = domainId;
    }

    @Override
    public void setDomainName(final String domainName) {
        this.domainName = domainName;
    }

    public void setOsTypeId(final String osTypeId) {
        this.osTypeId = osTypeId;
    }

    public void setOsTypeName(final String osTypeName) {
        this.osTypeName = osTypeName;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setDisplayText(final String displayText) {
        this.displayText = displayText;
    }

    public void setPublic(final boolean isPublic) {
        this.isPublic = isPublic;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public void setRemoved(final Date removed) {
        this.removed = removed;
    }

    public void setReady(final boolean isReady) {
        this.isReady = isReady;
    }

    public void setPasswordEnabled(final boolean passwordEnabled) {
        this.passwordEnabled = passwordEnabled;
    }

    public void setFormat(final ImageFormat format) {
        this.format = format;
    }

    public void setBootable(final Boolean bootable) {
        this.bootable = bootable;
    }

    public void setFeatured(final boolean featured) {
        this.featured = featured;
    }

    public void setCrossZones(final boolean crossZones) {
        this.crossZones = crossZones;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public void setSize(final Long size) {
        this.size = size;
    }

    public void setTemplateType(final String templateType) {
        this.templateType = templateType;
    }

    public void setHypervisor(final String hypervisor) {
        this.hypervisor = hypervisor;
    }

    public void setExtractable(final Boolean extractable) {
        this.extractable = extractable;
    }

    public void setChecksum(final String checksum) {
        this.checksum = checksum;
    }

    public void setSourceTemplateId(final String sourcetemplateId) {
        this.sourcetemplateId = sourcetemplateId;
    }

    public void setHostId(final String hostId) {
        this.hostId = hostId;
    }

    public void setHostName(final String hostName) {
        this.hostName = hostName;
    }

    public void setTemplateTag(final String templateTag) {
        this.templateTag = templateTag;
    }

    public Map getDetails() {
        return details;
    }

    public void setDetails(final Map details) {
        this.details = details;
    }

    public void setTags(final Set<ResourceTagResponse> tags) {
        this.tags = tags;
    }

    public void addTag(final ResourceTagResponse tag) {
        tags.add(tag);
    }

    public void setSshKeyEnabled(final boolean sshKeyEnabled) {
        this.sshKeyEnabled = sshKeyEnabled;
    }

    public void setDynamicallyScalable(final boolean isDynamicallyScalable) {
        this.isDynamicallyScalable = isDynamicallyScalable;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(final String zoneId) {
        this.zoneId = zoneId;
    }
}
