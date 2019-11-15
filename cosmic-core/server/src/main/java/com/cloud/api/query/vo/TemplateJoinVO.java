package com.cloud.api.query.vo;

import com.cloud.legacymodel.storage.ObjectInDataStoreStateMachine;
import com.cloud.legacymodel.storage.TemplateType;
import com.cloud.legacymodel.storage.VMTemplateStatus;
import com.cloud.legacymodel.storage.VirtualMachineTemplate;
import com.cloud.legacymodel.storage.VirtualMachineTemplate.State;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.model.enumeration.ImageFormat;
import com.cloud.model.enumeration.MaintenancePolicy;
import com.cloud.model.enumeration.OptimiseFor;
import com.cloud.server.ResourceTag.ResourceObjectType;
import com.cloud.storage.ScopeType;
import com.cloud.utils.db.GenericDao;

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
    private ImageFormat format;
    @Column(name = "public")
    private boolean publicTemplate = true;
    @Column(name = "featured")
    private boolean featured;
    @Column(name = "type")
    private TemplateType templateType;
    @Column(name = "url")
    private String url = null;
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
    private VMTemplateStatus downloadState;
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
    @Column(name = "optimise_for")
    private OptimiseFor optimiseFor;
    @Column(name = "manufacturer_string")
    private String manufacturerString;
    @Column(name = "mac_learning")
    private String macLearning;
    @Column(name = "cpu_flags")
    private String cpuFlags;
    @Column(name = "maintenance_policy")
    private MaintenancePolicy maintenancePolicy;
    @Column(name = "is_remote_gateway_template")
    private boolean isRemoteGatewayTemplate;

    public TemplateJoinVO() {
    }

    @Override
    public long getId() {
        return this.id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Date getCreated() {
        return this.created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public Date getRemoved() {
        return this.removed;
    }

    public void setRemoved(final Date removed) {
        this.removed = removed;
    }

    @Override
    public long getAccountId() {
        return this.accountId;
    }

    public void setAccountId(final long accountId) {
        this.accountId = accountId;
    }

    @Override
    public long getDomainId() {
        return this.domainId;
    }

    public void setDomainId(final long domainId) {
        this.domainId = domainId;
    }

    @Override
    public String getDomainPath() {
        return this.domainPath;
    }

    @Override
    public short getAccountType() {
        return this.accountType;
    }

    @Override
    public String getAccountUuid() {
        return this.accountUuid;
    }

    @Override
    public String getAccountName() {
        return this.accountName;
    }

    @Override
    public String getDomainUuid() {
        return this.domainUuid;
    }

    @Override
    public String getDomainName() {
        return this.domainName;
    }

    @Override
    public String getProjectUuid() {
        return this.projectUuid;
    }

    @Override
    public String getProjectName() {
        return this.projectName;
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
        return this.projectId;
    }

    public void setProjectId(final long projectId) {
        this.projectId = projectId;
    }

    public boolean isExtractable() {
        return this.extractable;
    }

    public void setExtractable(final boolean extractable) {
        this.extractable = extractable;
    }

    public TemplateType getTemplateType() {
        return this.templateType;
    }

    public void setTemplateType(final TemplateType templateType) {
        this.templateType = templateType;
    }

    public long getTagId() {
        return this.tagId;
    }

    public void setTagId(final long tagId) {
        this.tagId = tagId;
    }

    public String getTagUuid() {
        return this.tagUuid;
    }

    public void setTagUuid(final String tagUuid) {
        this.tagUuid = tagUuid;
    }

    public String getTagKey() {
        return this.tagKey;
    }

    public void setTagKey(final String tagKey) {
        this.tagKey = tagKey;
    }

    public String getTagValue() {
        return this.tagValue;
    }

    public void setTagValue(final String tagValue) {
        this.tagValue = tagValue;
    }

    public long getTagDomainId() {
        return this.tagDomainId;
    }

    public void setTagDomainId(final long tagDomainId) {
        this.tagDomainId = tagDomainId;
    }

    public long getTagAccountId() {
        return this.tagAccountId;
    }

    public void setTagAccountId(final long tagAccountId) {
        this.tagAccountId = tagAccountId;
    }

    public long getTagResourceId() {
        return this.tagResourceId;
    }

    public void setTagResourceId(final long tagResourceId) {
        this.tagResourceId = tagResourceId;
    }

    public String getTagResourceUuid() {
        return this.tagResourceUuid;
    }

    public void setTagResourceUuid(final String tagResourceUuid) {
        this.tagResourceUuid = tagResourceUuid;
    }

    public ResourceObjectType getTagResourceType() {
        return this.tagResourceType;
    }

    public void setTagResourceType(final ResourceObjectType tagResourceType) {
        this.tagResourceType = tagResourceType;
    }

    public String getTagCustomer() {
        return this.tagCustomer;
    }

    public void setTagCustomer(final String tagCustomer) {
        this.tagCustomer = tagCustomer;
    }

    public long getDataCenterId() {
        return this.dataCenterId;
    }

    public void setDataCenterId(final long dataCenterId) {
        this.dataCenterId = dataCenterId;
    }

    public String getDataCenterUuid() {
        return this.dataCenterUuid;
    }

    public void setDataCenterUuid(final String dataCenterUuid) {
        this.dataCenterUuid = dataCenterUuid;
    }

    public String getDataCenterName() {
        return this.dataCenterName;
    }

    public void setDataCenterName(final String dataCenterName) {
        this.dataCenterName = dataCenterName;
    }

    public String getUniqueName() {
        return this.uniqueName;
    }

    public void setUniqueName(final String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public boolean isPublicTemplate() {
        return this.publicTemplate;
    }

    public void setPublicTemplate(final boolean publicTemplate) {
        this.publicTemplate = publicTemplate;
    }

    public boolean isFeatured() {
        return this.featured;
    }

    public void setFeatured(final boolean featured) {
        this.featured = featured;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public int getBits() {
        return this.bits;
    }

    public void setBits(final int bits) {
        this.bits = bits;
    }

    public String getChecksum() {
        return this.checksum;
    }

    public void setChecksum(final String checksum) {
        this.checksum = checksum;
    }

    public String getDisplayText() {
        return this.displayText;
    }

    public void setDisplayText(final String displayText) {
        this.displayText = displayText;
    }

    public boolean isEnablePassword() {
        return this.enablePassword;
    }

    public void setEnablePassword(final boolean enablePassword) {
        this.enablePassword = enablePassword;
    }

    public boolean isDynamicallyScalable() {
        return this.dynamicallyScalable;
    }

    public void setDynamicallyScalable(final boolean dynamicallyScalable) {
        this.dynamicallyScalable = dynamicallyScalable;
    }

    public long getGuestOSId() {
        return this.guestOSId;
    }

    public void setGuestOSId(final long guestOSId) {
        this.guestOSId = guestOSId;
    }

    public String getGuestOSUuid() {
        return this.guestOSUuid;
    }

    public void setGuestOSUuid(final String guestOSUuid) {
        this.guestOSUuid = guestOSUuid;
    }

    public String getGuestOSName() {
        return this.guestOSName;
    }

    public void setGuestOSName(final String guestOSName) {
        this.guestOSName = guestOSName;
    }

    public boolean isBootable() {
        return this.bootable;
    }

    public void setBootable(final boolean bootable) {
        this.bootable = bootable;
    }

    public boolean isPrepopulate() {
        return this.prepopulate;
    }

    public void setPrepopulate(final boolean prepopulate) {
        this.prepopulate = prepopulate;
    }

    public boolean isCrossZones() {
        return this.crossZones;
    }

    public void setCrossZones(final boolean crossZones) {
        this.crossZones = crossZones;
    }

    public HypervisorType getHypervisorType() {
        return this.hypervisorType;
    }

    public void setHypervisorType(final HypervisorType hypervisorType) {
        this.hypervisorType = hypervisorType;
    }

    public Long getSourceTemplateId() {
        return this.sourceTemplateId;
    }

    public void setSourceTemplateId(final Long sourceTemplateId) {
        this.sourceTemplateId = sourceTemplateId;
    }

    public String getSourceTemplateUuid() {
        return this.sourceTemplateUuid;
    }

    public void setSourceTemplateUuid(final String sourceTemplateUuid) {
        this.sourceTemplateUuid = sourceTemplateUuid;
    }

    public String getTemplateTag() {
        return this.templateTag;
    }

    public void setTemplateTag(final String templateTag) {
        this.templateTag = templateTag;
    }

    public int getSortKey() {
        return this.sortKey;
    }

    public void setSortKey(final int sortKey) {
        this.sortKey = sortKey;
    }

    public boolean isEnableSshKey() {
        return this.enableSshKey;
    }

    public void setEnableSshKey(final boolean enableSshKey) {
        this.enableSshKey = enableSshKey;
    }

    public VMTemplateStatus getDownloadState() {
        return this.downloadState;
    }

    public void setDownloadState(final VMTemplateStatus downloadState) {
        this.downloadState = downloadState;
    }

    public long getSize() {
        return this.size;
    }

    public void setSize(final long size) {
        this.size = size;
    }

    public boolean isDestroyed() {
        return this.destroyed;
    }

    public void setDestroyed(final boolean destroyed) {
        this.destroyed = destroyed;
    }

    public Long getSharedAccountId() {
        return this.sharedAccountId;
    }

    public void setSharedAccountId(final Long sharedAccountId) {
        this.sharedAccountId = sharedAccountId;
    }

    public String getDetailName() {
        return this.detailName;
    }

    public void setDetailName(final String detailName) {
        this.detailName = detailName;
    }

    public String getDetailValue() {
        return this.detailValue;
    }

    public void setDetailValue(final String detailValue) {
        this.detailValue = detailValue;
    }

    public Date getCreatedOnStore() {
        return this.createdOnStore;
    }

    public void setCreatedOnStore(final Date createdOnStore) {
        this.createdOnStore = createdOnStore;
    }

    public ImageFormat getFormat() {
        return this.format;
    }

    public void setFormat(final ImageFormat format) {
        this.format = format;
    }

    public int getDownloadPercent() {
        return this.downloadPercent;
    }

    public void setDownloadPercent(final int downloadPercent) {
        this.downloadPercent = downloadPercent;
    }

    public String getErrorString() {
        return this.errorString;
    }

    public void setErrorString(final String errorString) {
        this.errorString = errorString;
    }

    public Long getDataStoreId() {
        return this.dataStoreId;
    }

    public void setDataStoreId(final Long dataStoreId) {
        this.dataStoreId = dataStoreId;
    }

    public ObjectInDataStoreStateMachine.State getState() {
        return this.state;
    }

    public void setState(final ObjectInDataStoreStateMachine.State state) {
        this.state = state;
    }

    public ScopeType getDataStoreScope() {
        return this.dataStoreScope;
    }

    public void setDataStoreScope(final ScopeType dataStoreScope) {
        this.dataStoreScope = dataStoreScope;
    }

    public String getTempZonePair() {
        return this.tempZonePair;
    }

    public void setTempZonePair(final String tempZonePair) {
        this.tempZonePair = tempZonePair;
    }

    public State getTemplateState() {
        return this.templateState;
    }

    public void setTemplateState(final State templateState) {
        this.templateState = templateState;
    }

    public OptimiseFor getOptimiseFor() {
        return optimiseFor;
    }

    public void setOptimiseFor(final OptimiseFor optimiseFor) {
        this.optimiseFor = optimiseFor;
    }

    public String getManufacturerString() {
        return manufacturerString;
    }

    public void setManufacturerString(final String manufacturerString) {
        this.manufacturerString = manufacturerString;
    }

    public String getMacLearning() {
        return macLearning;
    }

    public void setMacLearning(final String macLearning) {
        this.macLearning = macLearning;
    }

    public String getCpuFlags() {
        return cpuFlags;
    }

    public void setCpuFlags(final String cpuFlags) {
        this.cpuFlags = cpuFlags;
    }

    public MaintenancePolicy getMaintenancePolicy() {
        return maintenancePolicy;
    }

    public void setMaintenancePolicy(final MaintenancePolicy maintenancePolicy) {
        this.maintenancePolicy = maintenancePolicy;
    }

    public boolean getIsRemoteGatewayTemplate() {
        return isRemoteGatewayTemplate;
    }

    public void setRemoteGatewayTemplate(final boolean isRemoteGatewayTemplate) {
        this.isRemoteGatewayTemplate = isRemoteGatewayTemplate;
    }

    @Override
    public Class<?> getEntityType() {
        return VirtualMachineTemplate.class;
    }
}
