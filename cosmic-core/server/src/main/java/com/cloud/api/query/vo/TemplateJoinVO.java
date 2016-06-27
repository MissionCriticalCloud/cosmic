package com.cloud.api.query.vo;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.server.ResourceTag.ResourceObjectType;
import com.cloud.storage.ScopeType;
import com.cloud.storage.Storage;
import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.template.VirtualMachineTemplate.State;
import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "template_view")
public class TemplateJoinVO extends BaseViewVO implements ControlledViewEntity {

    @Column(name = "destroyed")
    boolean destroyed = false;
    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    ObjectInDataStoreStateMachine.State state;
    @Id
    @Column(name = "id")
    private long id;
    @Column(name = "uuid")
    private String uuid;
    @Column(name = "unique_name")
    private String uniqueName;
    @Column(name = "name")
    private String name;
    @Column(name = "format")
    private Storage.ImageFormat format;
    @Column(name = "public")
    private boolean publicTemplate = true;
    @Column(name = "featured")
    private boolean featured;
    @Column(name = "type")
    private Storage.TemplateType templateType;
    @Column(name = "url")
    private String url = null;
    @Column(name = "hvm")
    private boolean requiresHvm;
    @Column(name = "bits")
    private int bits;
    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = GenericDao.CREATED_COLUMN)
    private Date created = null;
    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = "created_on_store")
    private Date createdOnStore = null;
    @Column(name = GenericDao.REMOVED_COLUMN)
    @Temporal(TemporalType.TIMESTAMP)
    private Date removed;
    @Column(name = "checksum")
    private String checksum;
    @Column(name = "display_text", length = 4096)
    private String displayText;
    @Column(name = "enable_password")
    private boolean enablePassword;
    @Column(name = "dynamically_scalable")
    private boolean dynamicallyScalable;
    @Column(name = "guest_os_id")
    private long guestOSId;
    @Column(name = "guest_os_uuid")
    private String guestOSUuid;
    @Column(name = "guest_os_name")
    private String guestOSName;
    @Column(name = "bootable")
    private boolean bootable = true;
    @Column(name = "prepopulate")
    private boolean prepopulate = false;
    @Column(name = "cross_zones")
    private boolean crossZones = false;
    @Column(name = "hypervisor_type")
    @Enumerated(value = EnumType.STRING)
    private HypervisorType hypervisorType;
    @Column(name = "extractable")
    private boolean extractable = true;
    @Column(name = "source_template_id")
    private Long sourceTemplateId;
    @Column(name = "source_template_uuid")
    private String sourceTemplateUuid;
    @Column(name = "template_tag")
    private String templateTag;
    @Column(name = "sort_key")
    private int sortKey;
    @Column(name = "enable_sshkey")
    private boolean enableSshKey;
    @Column(name = "account_id")
    private long accountId;
    @Column(name = "account_uuid")
    private String accountUuid;
    @Column(name = "account_name")
    private String accountName = null;
    @Column(name = "account_type")
    private short accountType;
    @Column(name = "domain_id")
    private long domainId;
    @Column(name = "domain_uuid")
    private String domainUuid;
    @Column(name = "domain_name")
    private String domainName = null;
    @Column(name = "domain_path")
    private String domainPath = null;
    @Column(name = "project_id")
    private long projectId;
    @Column(name = "project_uuid")
    private String projectUuid;
    @Column(name = "project_name")
    private String projectName;
    @Column(name = "data_center_id")
    private long dataCenterId;
    @Column(name = "data_center_uuid")
    private String dataCenterUuid;
    @Column(name = "data_center_name")
    private String dataCenterName;
    @Column(name = "store_scope")
    @Enumerated(value = EnumType.STRING)
    private ScopeType dataStoreScope;
    @Column(name = "store_id")
    private Long dataStoreId;
    @Column(name = "download_state")
    @Enumerated(EnumType.STRING)
    private Status downloadState;
    @Column(name = "download_pct")
    private int downloadPercent;
    @Column(name = "error_str")
    private String errorString;
    @Column(name = "size")
    private long size;
    @Column(name = "template_state")
    @Enumerated(EnumType.STRING)
    private State templateState;
    @Column(name = "lp_account_id")
    private Long sharedAccountId;
    @Column(name = "detail_name")
    private String detailName;
    @Column(name = "detail_value")
    private String detailValue;
    @Column(name = "tag_id")
    private long tagId;
    @Column(name = "tag_uuid")
    private String tagUuid;
    @Column(name = "tag_key")
    private String tagKey;
    @Column(name = "tag_value")
    private String tagValue;
    @Column(name = "tag_domain_id")
    private long tagDomainId;
    @Column(name = "tag_account_id")
    private long tagAccountId;
    @Column(name = "tag_resource_id")
    private long tagResourceId;
    @Column(name = "tag_resource_uuid")
    private String tagResourceUuid;
    @Column(name = "tag_resource_type")
    @Enumerated(value = EnumType.STRING)
    private ResourceObjectType tagResourceType;
    @Column(name = "tag_customer")
    private String tagCustomer;
    @Column(name = "temp_zone_pair")
    private String tempZonePair; // represent a distinct (templateId, data_center_id) pair

    public TemplateJoinVO() {
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public Date getRemoved() {
        return removed;
    }

    public void setRemoved(final Date removed) {
        this.removed = removed;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(final long accountId) {
        this.accountId = accountId;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    public void setDomainId(final long domainId) {
        this.domainId = domainId;
    }

    @Override
    public String getDomainPath() {
        return domainPath;
    }

    @Override
    public short getAccountType() {
        return accountType;
    }

    @Override
    public String getAccountUuid() {
        return accountUuid;
    }

    @Override
    public String getAccountName() {
        return accountName;
    }

    @Override
    public String getDomainUuid() {
        return domainUuid;
    }

    @Override
    public String getDomainName() {
        return domainName;
    }

    @Override
    public String getProjectUuid() {
        return projectUuid;
    }

    @Override
    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    public void setProjectUuid(final String projectUuid) {
        this.projectUuid = projectUuid;
    }

    public void setDomainName(final String domainName) {
        this.domainName = domainName;
    }

    public void setDomainUuid(final String domainUuid) {
        this.domainUuid = domainUuid;
    }

    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    public void setAccountUuid(final String accountUuid) {
        this.accountUuid = accountUuid;
    }

    public void setAccountType(final short accountType) {
        this.accountType = accountType;
    }

    public void setDomainPath(final String domainPath) {
        this.domainPath = domainPath;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(final long projectId) {
        this.projectId = projectId;
    }

    public boolean isExtractable() {
        return extractable;
    }

    public void setExtractable(final boolean extractable) {
        this.extractable = extractable;
    }

    public Storage.TemplateType getTemplateType() {
        return templateType;
    }

    public void setTemplateType(final Storage.TemplateType templateType) {
        this.templateType = templateType;
    }

    public long getTagId() {
        return tagId;
    }

    public void setTagId(final long tagId) {
        this.tagId = tagId;
    }

    public String getTagUuid() {
        return tagUuid;
    }

    public void setTagUuid(final String tagUuid) {
        this.tagUuid = tagUuid;
    }

    public String getTagKey() {
        return tagKey;
    }

    public void setTagKey(final String tagKey) {
        this.tagKey = tagKey;
    }

    public String getTagValue() {
        return tagValue;
    }

    public void setTagValue(final String tagValue) {
        this.tagValue = tagValue;
    }

    public long getTagDomainId() {
        return tagDomainId;
    }

    public void setTagDomainId(final long tagDomainId) {
        this.tagDomainId = tagDomainId;
    }

    public long getTagAccountId() {
        return tagAccountId;
    }

    public void setTagAccountId(final long tagAccountId) {
        this.tagAccountId = tagAccountId;
    }

    public long getTagResourceId() {
        return tagResourceId;
    }

    public void setTagResourceId(final long tagResourceId) {
        this.tagResourceId = tagResourceId;
    }

    public String getTagResourceUuid() {
        return tagResourceUuid;
    }

    public void setTagResourceUuid(final String tagResourceUuid) {
        this.tagResourceUuid = tagResourceUuid;
    }

    public ResourceObjectType getTagResourceType() {
        return tagResourceType;
    }

    public void setTagResourceType(final ResourceObjectType tagResourceType) {
        this.tagResourceType = tagResourceType;
    }

    public String getTagCustomer() {
        return tagCustomer;
    }

    public void setTagCustomer(final String tagCustomer) {
        this.tagCustomer = tagCustomer;
    }

    public long getDataCenterId() {
        return dataCenterId;
    }

    public void setDataCenterId(final long dataCenterId) {
        this.dataCenterId = dataCenterId;
    }

    public String getDataCenterUuid() {
        return dataCenterUuid;
    }

    public void setDataCenterUuid(final String dataCenterUuid) {
        this.dataCenterUuid = dataCenterUuid;
    }

    public String getDataCenterName() {
        return dataCenterName;
    }

    public void setDataCenterName(final String dataCenterName) {
        this.dataCenterName = dataCenterName;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(final String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public boolean isPublicTemplate() {
        return publicTemplate;
    }

    public void setPublicTemplate(final boolean publicTemplate) {
        this.publicTemplate = publicTemplate;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(final boolean featured) {
        this.featured = featured;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public boolean isRequiresHvm() {
        return requiresHvm;
    }

    public void setRequiresHvm(final boolean requiresHvm) {
        this.requiresHvm = requiresHvm;
    }

    public int getBits() {
        return bits;
    }

    public void setBits(final int bits) {
        this.bits = bits;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(final String checksum) {
        this.checksum = checksum;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(final String displayText) {
        this.displayText = displayText;
    }

    public boolean isEnablePassword() {
        return enablePassword;
    }

    public void setEnablePassword(final boolean enablePassword) {
        this.enablePassword = enablePassword;
    }

    public boolean isDynamicallyScalable() {
        return dynamicallyScalable;
    }

    public void setDynamicallyScalable(final boolean dynamicallyScalable) {
        this.dynamicallyScalable = dynamicallyScalable;
    }

    public long getGuestOSId() {
        return guestOSId;
    }

    public void setGuestOSId(final long guestOSId) {
        this.guestOSId = guestOSId;
    }

    public String getGuestOSUuid() {
        return guestOSUuid;
    }

    public void setGuestOSUuid(final String guestOSUuid) {
        this.guestOSUuid = guestOSUuid;
    }

    public String getGuestOSName() {
        return guestOSName;
    }

    public void setGuestOSName(final String guestOSName) {
        this.guestOSName = guestOSName;
    }

    public boolean isBootable() {
        return bootable;
    }

    public void setBootable(final boolean bootable) {
        this.bootable = bootable;
    }

    public boolean isPrepopulate() {
        return prepopulate;
    }

    public void setPrepopulate(final boolean prepopulate) {
        this.prepopulate = prepopulate;
    }

    public boolean isCrossZones() {
        return crossZones;
    }

    public void setCrossZones(final boolean crossZones) {
        this.crossZones = crossZones;
    }

    public HypervisorType getHypervisorType() {
        return hypervisorType;
    }

    public void setHypervisorType(final HypervisorType hypervisorType) {
        this.hypervisorType = hypervisorType;
    }

    public Long getSourceTemplateId() {
        return sourceTemplateId;
    }

    public void setSourceTemplateId(final Long sourceTemplateId) {
        this.sourceTemplateId = sourceTemplateId;
    }

    public String getSourceTemplateUuid() {
        return sourceTemplateUuid;
    }

    public void setSourceTemplateUuid(final String sourceTemplateUuid) {
        this.sourceTemplateUuid = sourceTemplateUuid;
    }

    public String getTemplateTag() {
        return templateTag;
    }

    public void setTemplateTag(final String templateTag) {
        this.templateTag = templateTag;
    }

    public int getSortKey() {
        return sortKey;
    }

    public void setSortKey(final int sortKey) {
        this.sortKey = sortKey;
    }

    public boolean isEnableSshKey() {
        return enableSshKey;
    }

    public void setEnableSshKey(final boolean enableSshKey) {
        this.enableSshKey = enableSshKey;
    }

    public Status getDownloadState() {
        return downloadState;
    }

    public void setDownloadState(final Status downloadState) {
        this.downloadState = downloadState;
    }

    public long getSize() {
        return size;
    }

    public void setSize(final long size) {
        this.size = size;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void setDestroyed(final boolean destroyed) {
        this.destroyed = destroyed;
    }

    public Long getSharedAccountId() {
        return sharedAccountId;
    }

    public void setSharedAccountId(final Long sharedAccountId) {
        this.sharedAccountId = sharedAccountId;
    }

    public String getDetailName() {
        return detailName;
    }

    public void setDetailName(final String detailName) {
        this.detailName = detailName;
    }

    public String getDetailValue() {
        return detailValue;
    }

    public void setDetailValue(final String detailValue) {
        this.detailValue = detailValue;
    }

    public Date getCreatedOnStore() {
        return createdOnStore;
    }

    public void setCreatedOnStore(final Date createdOnStore) {
        this.createdOnStore = createdOnStore;
    }

    public Storage.ImageFormat getFormat() {
        return format;
    }

    public void setFormat(final Storage.ImageFormat format) {
        this.format = format;
    }

    public int getDownloadPercent() {
        return downloadPercent;
    }

    public void setDownloadPercent(final int downloadPercent) {
        this.downloadPercent = downloadPercent;
    }

    public String getErrorString() {
        return errorString;
    }

    public void setErrorString(final String errorString) {
        this.errorString = errorString;
    }

    public Long getDataStoreId() {
        return dataStoreId;
    }

    public void setDataStoreId(final Long dataStoreId) {
        this.dataStoreId = dataStoreId;
    }

    public ObjectInDataStoreStateMachine.State getState() {
        return state;
    }

    public void setState(final ObjectInDataStoreStateMachine.State state) {
        this.state = state;
    }

    public ScopeType getDataStoreScope() {
        return dataStoreScope;
    }

    public void setDataStoreScope(final ScopeType dataStoreScope) {
        this.dataStoreScope = dataStoreScope;
    }

    public String getTempZonePair() {
        return tempZonePair;
    }

    public void setTempZonePair(final String tempZonePair) {
        this.tempZonePair = tempZonePair;
    }

    public State getTemplateState() {
        return templateState;
    }

    public void setTemplateState(final State templateState) {
        this.templateState = templateState;
    }

    @Override
    public Class<?> getEntityType() {
        return VirtualMachineTemplate.class;
    }
}
