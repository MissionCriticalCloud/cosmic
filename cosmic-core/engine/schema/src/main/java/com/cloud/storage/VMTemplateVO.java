package com.cloud.storage;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.Storage.TemplateType;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.utils.db.GenericDao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "vm_template")
public class VMTemplateVO implements VirtualMachineTemplate {
    @Column(name = "update_count", updatable = true)
    protected long updatedCount;
    @Column(name = "dynamically_scalable")
    protected boolean dynamicallyScalable;
    @Column(name = "updated")
    @Temporal(value = TemporalType.TIMESTAMP)
    Date updated;
    @Transient
    Map<String, String> details;
    @Transient
    String toString;
    @Id
    @TableGenerator(name = "vm_template_sq", table = "sequence", pkColumnName = "name", valueColumnName = "value", pkColumnValue = "vm_template_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private long id;
    @Column(name = "format")
    private Storage.ImageFormat format;
    @Column(name = "unique_name")
    private String uniqueName;
    @Column(name = "name")
    private String name = null;
    @Column(name = "public")
    private boolean publicTemplate = true;
    @Column(name = "featured")
    private boolean featured;
    @Column(name = "type")
    private Storage.TemplateType templateType;
    @Column(name = "url", length = 2048)
    private String url = null;
    @Column(name = "hvm")
    private boolean requiresHvm;
    @Column(name = "bits")
    private int bits;
    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = GenericDao.CREATED_COLUMN)
    private Date created = null;
    @Column(name = GenericDao.REMOVED_COLUMN)
    @Temporal(TemporalType.TIMESTAMP)
    private Date removed;
    @Column(name = "account_id")
    private long accountId;
    @Column(name = "checksum")
    private String checksum;
    @Column(name = "display_text", length = 4096)
    private String displayText;
    @Column(name = "enable_password")
    private boolean enablePassword;
    @Column(name = "guest_os_id")
    private long guestOSId;
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
    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    private State state;
    @Column(name = "template_tag")
    private String templateTag;
    @Column(name = "uuid")
    private String uuid;
    @Column(name = "sort_key")
    private int sortKey;
    @Column(name = "enable_sshkey")
    private boolean enableSshKey;
    @Column(name = "size")
    private Long size;

    public VMTemplateVO() {
        uuid = UUID.randomUUID().toString();
    }

    public VMTemplateVO(final long id, final String name, final ImageFormat format, final boolean isPublic, final boolean featured, final boolean isExtractable, final
    TemplateType type, final String url,
                        final boolean requiresHvm, final int bits, final long accountId, final String cksum, final String displayText, final boolean enablePassword, final long
                                guestOSId, final boolean bootable,
                        final HypervisorType hyperType, final String templateTag, final Map<String, String> details, final boolean sshKeyEnabled, final boolean
                                isDynamicallyScalable) {
        this(id,
                name,
                format,
                isPublic,
                featured,
                isExtractable,
                type,
                url,
                requiresHvm,
                bits,
                accountId,
                cksum,
                displayText,
                enablePassword,
                guestOSId,
                bootable,
                hyperType,
                details);
        this.templateTag = templateTag;
        uuid = UUID.randomUUID().toString();
        enableSshKey = sshKeyEnabled;
        dynamicallyScalable = isDynamicallyScalable;
        state = State.Active;
    }

    //FIXME - Remove unwanted constructors.
    private VMTemplateVO(final long id, final String name, final ImageFormat format, final boolean isPublic, final boolean featured, final boolean isExtractable, final
    TemplateType type, final String url,
                         final boolean requiresHvm, final int bits, final long accountId, final String cksum, final String displayText, final boolean enablePassword, final long
                                 guestOSId, final boolean bootable,
                         final HypervisorType hyperType, final Map<String, String> details) {
        this(id,
                generateUniqueName(id, accountId, name),
                name,
                format,
                isPublic,
                featured,
                isExtractable,
                type,
                url,
                null,
                requiresHvm,
                bits,
                accountId,
                cksum,
                displayText,
                enablePassword,
                guestOSId,
                bootable,
                hyperType,
                details);
        uuid = UUID.randomUUID().toString();
    }

    //FIXME - Remove unwanted constructors. Made them private for now
    private VMTemplateVO(final Long id, final String uniqueName, final String name, final ImageFormat format, final boolean isPublic, final boolean featured, final boolean
            isExtractable, final TemplateType type,
                         final String url, final Date created, final boolean requiresHvm, final int bits, final long accountId, final String cksum, final String displayText,
                         final boolean enablePassword, final long guestOSId,
                         final boolean bootable, final HypervisorType hyperType, final Map<String, String> details) {
        this(id,
                uniqueName,
                name,
                format,
                isPublic,
                featured,
                type,
                url,
                created,
                requiresHvm,
                bits,
                accountId,
                cksum,
                displayText,
                enablePassword,
                guestOSId,
                bootable,
                hyperType);
        extractable = isExtractable;
        uuid = UUID.randomUUID().toString();
        this.details = details;
        state = State.Active;
    }

    private static String generateUniqueName(final long id, final long userId, final String displayName) {
        final StringBuilder name = new StringBuilder();
        name.append(id);
        name.append("-");
        name.append(userId);
        name.append("-");
        name.append(UUID.nameUUIDFromBytes((displayName + System.currentTimeMillis()).getBytes()).toString());
        return name.toString();
    }

    public VMTemplateVO(final Long id, final String uniqueName, final String name, final ImageFormat format, final boolean isPublic, final boolean featured, final TemplateType
            type, final String url, final Date created,
                        final boolean requiresHvm, final int bits, final long accountId, final String cksum, final String displayText, final boolean enablePassword, final long
                                guestOSId, final boolean bootable,
                        final HypervisorType hyperType) {
        this.id = id;
        this.name = name;
        publicTemplate = isPublic;
        this.featured = featured;
        templateType = type;
        this.url = url;
        this.requiresHvm = requiresHvm;
        this.bits = bits;
        this.accountId = accountId;
        checksum = cksum;
        this.uniqueName = uniqueName;
        this.displayText = displayText;
        this.enablePassword = enablePassword;
        this.format = format;
        this.created = created;
        this.guestOSId = guestOSId;
        this.bootable = bootable;
        hypervisorType = hyperType;
        uuid = UUID.randomUUID().toString();
        state = State.Active;
    }

    public static VMTemplateVO createPreHostIso(final Long id, final String uniqueName, final String name, final ImageFormat format, final boolean isPublic, final boolean
            featured, final TemplateType type,
                                                final String url, final Date created, final boolean requiresHvm, final int bits, final long accountId, final String cksum, final
                                                String displayText, final boolean
                                                        enablePassword, final long guestOSId,
                                                final boolean bootable, final HypervisorType hyperType) {
        final VMTemplateVO template =
                new VMTemplateVO(id, uniqueName, name, format, isPublic, featured, type, url, created, requiresHvm, bits, accountId, cksum, displayText, enablePassword,
                        guestOSId, bootable, hyperType);
        return template;
    }

    @Override
    public State getState() {
        return state;
    }

    public void setState(final State state) {
        this.state = state;
    }

    @Override
    public boolean isFeatured() {
        return featured;
    }

    @Override
    public boolean isPublicTemplate() {
        return publicTemplate;
    }

    public void setPublicTemplate(final boolean publicTemplate) {
        this.publicTemplate = publicTemplate;
    }

    @Override
    public boolean isExtractable() {
        return extractable;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Storage.ImageFormat getFormat() {
        return format;
    }

    public void setFormat(final ImageFormat format) {
        this.format = format;
    }

    @Override
    public boolean isRequiresHvm() {
        return requiresHvm;
    }

    public void setRequiresHvm(final boolean value) {
        requiresHvm = value;
    }

    @Override
    public String getDisplayText() {
        return displayText;
    }

    @Override
    public boolean getEnablePassword() {
        return enablePassword;
    }

    public void setEnablePassword(final boolean enablePassword) {
        this.enablePassword = enablePassword;
    }

    @Override
    public boolean getEnableSshKey() {
        return enableSshKey;
    }

    @Override
    public boolean isCrossZones() {
        return crossZones;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    @Override
    public long getGuestOSId() {
        return guestOSId;
    }

    public void setGuestOSId(final long guestOSId) {
        this.guestOSId = guestOSId;
    }

    @Override
    public boolean isBootable() {
        return bootable;
    }

    @Override
    public TemplateType getTemplateType() {
        return templateType;
    }

    public void setTemplateType(final TemplateType type) {
        templateType = type;
    }

    @Override
    public HypervisorType getHypervisorType() {
        return hypervisorType;
    }

    @Override
    public int getBits() {
        return bits;
    }

    @Override
    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(final String uniqueName) {
        this.uniqueName = uniqueName;
    }

    @Override
    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    @Override
    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(final String checksum) {
        this.checksum = checksum;
    }

    @Override
    public Long getSourceTemplateId() {
        return sourceTemplateId;
    }

    public void setSourceTemplateId(final Long sourceTemplateId) {
        this.sourceTemplateId = sourceTemplateId;
    }

    @Override
    public String getTemplateTag() {
        return templateTag;
    }

    public void setTemplateTag(final String templateTag) {
        this.templateTag = templateTag;
    }

    @Override
    public Map<String, String> getDetails() {
        return details;
    }

    public void setDetails(final Map<String, String> details) {
        this.details = details;
    }

    @Override
    public boolean isDynamicallyScalable() {
        return dynamicallyScalable;
    }

    public void setDynamicallyScalable(final boolean dynamicallyScalable) {
        this.dynamicallyScalable = dynamicallyScalable;
    }

    @Override
    public long getUpdatedCount() {
        return updatedCount;
    }

    @Override
    public void incrUpdatedCount() {
        updatedCount++;
    }

    @Override
    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(final Date updated) {
        this.updated = updated;
    }

    public void setBits(final int bits) {
        this.bits = bits;
    }

    public void setHypervisorType(final HypervisorType hyperType) {
        hypervisorType = hyperType;
    }

    public void setBootable(final boolean bootable) {
        this.bootable = bootable;
    }

    public void setCrossZones(final boolean crossZones) {
        this.crossZones = crossZones;
    }

    public void setEnableSshKey(final boolean enable) {
        enableSshKey = enable;
    }

    public void setDisplayText(final String displayText) {
        this.displayText = displayText;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setExtractable(final boolean extractable) {
        this.extractable = extractable;
    }

    public void setFeatured(final boolean featured) {
        this.featured = featured;
    }

    @Override
    public long getId() {
        return id;
    }

    public boolean requiresHvm() {
        return requiresHvm;
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

    public boolean isPrepopulate() {
        return prepopulate;
    }

    public void setPrepopulate(final boolean prepopulate) {
        this.prepopulate = prepopulate;
    }

    @Override
    public long getDomainId() {
        return -1;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    @Override
    public int hashCode() {
        return uniqueName.hashCode();
    }

    @Override
    public boolean equals(final Object that) {
        if (this == that) {
            return true;
        }
        if (!(that instanceof VMTemplateVO)) {
            return false;
        }
        final VMTemplateVO other = (VMTemplateVO) that;

        return ((getUniqueName().equals(other.getUniqueName())));
    }

    @Override
    public String toString() {
        if (toString == null) {
            toString = new StringBuilder("Tmpl[").append(id).append("-").append(format).append("-").append(uniqueName).toString();
        }
        return toString;
    }

    public int getSortKey() {
        return sortKey;
    }

    public void setSortKey(final int key) {
        sortKey = key;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(final Long size) {
        this.size = size;
    }

    public void decrUpdatedCount() {
        updatedCount--;
    }

    @Override
    public Class<?> getEntityType() {
        return VirtualMachineTemplate.class;
    }
}
