package com.cloud.legacymodel.to;

import com.cloud.model.enumeration.DataObjectType;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.model.enumeration.ImageFormat;

public class TemplateObjectTO implements DataTO {
    private String path;
    private String origUrl;
    private String uuid;
    private long id;
    private ImageFormat format;
    private long accountId;
    private String checksum;
    private String displayText;
    private DataStoreTO imageDataStore;
    private String name;
    private String guestOsType;
    private Long size;
    private Long physicalSize;
    private HypervisorType hypervisorType;

    public TemplateObjectTO() {

    }

    public TemplateObjectTO(final String path, final String origUrl, final String uuid, final long id, final ImageFormat format, final long accountId, final String checksum, final String
            displayText, final DataStoreTO imageDataStore, final String name, final String guestOsType, final Long size, final Long physicalSize, final HypervisorType hypervisorType) {
        this.path = path;
        this.origUrl = origUrl;
        this.uuid = uuid;
        this.id = id;
        this.format = format;
        this.accountId = accountId;
        this.checksum = checksum;
        this.displayText = displayText;
        this.imageDataStore = imageDataStore;
        this.name = name;
        this.guestOsType = guestOsType;
        this.size = size;
        this.physicalSize = physicalSize;
        this.hypervisorType = hypervisorType;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public ImageFormat getFormat() {
        return format;
    }

    public void setFormat(final ImageFormat format) {
        this.format = format;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(final long accountId) {
        this.accountId = accountId;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(final String checksum) {
        this.checksum = checksum;
    }

    public String getDescription() {
        return displayText;
    }

    public void setDescription(final String desc) {
        this.displayText = desc;
    }

    @Override
    public DataObjectType getObjectType() {
        return DataObjectType.TEMPLATE;
    }

    @Override
    public DataStoreTO getDataStore() {
        return this.imageDataStore;
    }

    @Override
    public HypervisorType getHypervisorType() {
        return this.hypervisorType;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public void setHypervisorType(final HypervisorType hypervisorType) {
        this.hypervisorType = hypervisorType;
    }

    public void setDataStore(final DataStoreTO store) {
        this.imageDataStore = store;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getOrigUrl() {
        return origUrl;
    }

    public void setOrigUrl(final String origUrl) {
        this.origUrl = origUrl;
    }

    public void setImageDataStore(final DataStoreTO imageDataStore) {
        this.imageDataStore = imageDataStore;
    }

    public String getGuestOsType() {
        return guestOsType;
    }

    public void setGuestOsType(final String guestOsType) {
        this.guestOsType = guestOsType;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(final Long size) {
        this.size = size;
    }

    public Long getPhysicalSize() {
        return physicalSize;
    }

    public void setPhysicalSize(final Long physicalSize) {
        this.physicalSize = physicalSize;
    }

    @Override
    public String toString() {
        return new StringBuilder("TemplateTO[id=").append(id).append("|origUrl=").append(origUrl).append("|name").append(name).append("]").toString();
    }
}
