package com.cloud.storage;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.Storage.TemplateType;

import java.util.Map;

public class TemplateProfile {
    Long userId;
    String name;
    String displayText;
    Integer bits;
    Boolean passwordEnabled;
    Boolean sshKeyEnbaled;
    Boolean requiresHvm;
    String url;
    Boolean isPublic;
    Boolean featured;
    Boolean isExtractable;
    ImageFormat format;
    Long guestOsId;
    Long zoneId;
    HypervisorType hypervisorType;
    String accountName;
    Long domainId;
    Long accountId;
    String chksum;
    Boolean bootable;
    Long templateId;
    VMTemplateVO template;
    String templateTag;
    Map details;
    Boolean isDynamicallyScalable;
    TemplateType templateType;

    public TemplateProfile(final Long userId, final VMTemplateVO template, final Long zoneId) {
        this.userId = userId;
        this.template = template;
        this.zoneId = zoneId;
    }

    public TemplateProfile(final Long templateId, final Long userId, final String name, final String displayText, final Integer bits, final Boolean passwordEnabled, final
    Boolean requiresHvm, final String url,
                           final Boolean isPublic, final Boolean featured, final Boolean isExtractable, final ImageFormat format, final Long guestOsId, final Long zoneId,

                           final HypervisorType hypervisorType, final String accountName, final Long domainId, final Long accountId, final String chksum, final Boolean bootable,
                           final String templateTag, final Map details,
                           final Boolean sshKeyEnabled, final Long imageStoreId, final Boolean isDynamicallyScalable, final TemplateType templateType) {
        this(templateId,
                userId,
                name,
                displayText,
                bits,
                passwordEnabled,
                requiresHvm,
                url,
                isPublic,
                featured,
                isExtractable,
                format,
                guestOsId,
                zoneId,
                hypervisorType,
                accountName,
                domainId,
                accountId,
                chksum,
                bootable,
                details,
                sshKeyEnabled);
        this.templateTag = templateTag;
        this.isDynamicallyScalable = isDynamicallyScalable;
        this.templateType = templateType;
    }

    public TemplateProfile(final Long templateId, final Long userId, final String name, final String displayText, final Integer bits, final Boolean passwordEnabled, final
    Boolean requiresHvm, final String url,
                           final Boolean isPublic, final Boolean featured, final Boolean isExtractable, final ImageFormat format, final Long guestOsId, final Long zoneId, final
                           HypervisorType hypervisorType,
                           final String accountName, final Long domainId, final Long accountId, final String chksum, final Boolean bootable, final Map details, final Boolean
                                   sshKeyEnabled) {
        this.templateId = templateId;
        this.userId = userId;
        this.name = name;
        this.displayText = displayText;
        this.bits = bits;
        this.passwordEnabled = passwordEnabled;
        this.requiresHvm = requiresHvm;
        this.url = url;
        this.isPublic = isPublic;
        this.featured = featured;
        this.isExtractable = isExtractable;
        this.format = format;
        this.guestOsId = guestOsId;
        this.zoneId = zoneId;
        this.hypervisorType = hypervisorType;
        this.accountName = accountName;
        this.domainId = domainId;
        this.accountId = accountId;
        this.chksum = chksum;
        this.bootable = bootable;
        this.details = details;
        this.sshKeyEnbaled = sshKeyEnabled;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(final Long id) {
        this.templateId = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(final Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(final String text) {
        this.displayText = text;
    }

    public Integer getBits() {
        return bits;
    }

    public void setBits(final Integer bits) {
        this.bits = bits;
    }

    public Boolean getPasswordEnabled() {
        return passwordEnabled;
    }

    public void setPasswordEnabled(final Boolean enabled) {
        this.passwordEnabled = enabled;
    }

    public Boolean getRequiresHVM() {
        return requiresHvm;
    }

    public void setRequiresHVM(final Boolean hvm) {
        this.requiresHvm = hvm;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(final Boolean is) {
        this.isPublic = is;
    }

    public Boolean getFeatured() {
        return featured;
    }

    public void setFeatured(final Boolean featured) {
        this.featured = featured;
    }

    public Boolean getIsExtractable() {
        return isExtractable;
    }

    public void setIsExtractable(final Boolean is) {
        this.isExtractable = is;
    }

    public ImageFormat getFormat() {
        return format;
    }

    public void setFormat(final ImageFormat format) {
        this.format = format;
    }

    public Long getGuestOsId() {
        return guestOsId;
    }

    public void setGuestOsId(final Long id) {
        this.guestOsId = id;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public void setZoneId(final Long id) {
        this.zoneId = id;
    }

    public HypervisorType getHypervisorType() {
        return hypervisorType;
    }

    public void setHypervisorType(final HypervisorType type) {
        this.hypervisorType = type;
    }

    public Long getDomainId() {
        return domainId;
    }

    public void setDomainId(final Long id) {
        this.domainId = id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(final Long id) {
        this.accountId = id;
    }

    public String getCheckSum() {
        return chksum;
    }

    public void setCheckSum(final String chksum) {
        this.chksum = chksum;
    }

    public Boolean getBootable() {
        return this.bootable;
    }

    public void setBootable(final Boolean bootable) {
        this.bootable = bootable;
    }

    public VMTemplateVO getTemplate() {
        return template;
    }

    public void setTemplate(final VMTemplateVO template) {
        this.template = template;
    }

    public String getTemplateTag() {
        return templateTag;
    }

    public void setTemplateTag(final String templateTag) {
        this.templateTag = templateTag;
    }

    public Map getDetails() {
        return this.details;
    }

    public void setDetails(final Map details) {
        this.details = details;
    }

    public Boolean getSshKeyEnabled() {
        return this.sshKeyEnbaled;
    }

    public void setSshKeyEnabled(final Boolean enabled) {
        this.sshKeyEnbaled = enabled;
    }

    public Boolean IsDynamicallyScalable() {
        return this.isDynamicallyScalable;
    }

    public void setScalabe(final Boolean isDynamicallyScalabe) {
        this.isDynamicallyScalable = isDynamicallyScalabe;
    }

    public TemplateType getTemplateType() {
        return templateType;
    }

    public void setTemplateType(final TemplateType templateType) {
        this.templateType = templateType;
    }
}
