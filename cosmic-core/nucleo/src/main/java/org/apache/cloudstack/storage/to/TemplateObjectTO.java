//

//

package org.apache.cloudstack.storage.to;

import com.cloud.agent.api.to.DataObjectType;
import com.cloud.agent.api.to.DataStoreTO;
import com.cloud.agent.api.to.DataTO;
import com.cloud.hypervisor.Hypervisor;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.template.VirtualMachineTemplate;
import org.apache.cloudstack.engine.subsystem.api.storage.TemplateInfo;

public class TemplateObjectTO implements DataTO {
    private String path;
    private String origUrl;
    private String uuid;
    private long id;
    private ImageFormat format;
    private long accountId;
    private String checksum;
    private boolean hvm;
    private String displayText;
    private DataStoreTO imageDataStore;
    private String name;
    private String guestOsType;
    private Long size;
    private Long physicalSize;
    private Hypervisor.HypervisorType hypervisorType;

    public TemplateObjectTO() {

    }

    public TemplateObjectTO(final VirtualMachineTemplate template) {
        this.uuid = template.getUuid();
        this.id = template.getId();
        this.origUrl = template.getUrl();
        this.displayText = template.getDisplayText();
        this.checksum = template.getChecksum();
        this.hvm = template.isRequiresHvm();
        this.accountId = template.getAccountId();
        this.name = template.getUniqueName();
        this.format = template.getFormat();
        this.hypervisorType = template.getHypervisorType();
    }

    public TemplateObjectTO(final TemplateInfo template) {
        this.path = template.getInstallPath();
        this.uuid = template.getUuid();
        this.id = template.getId();
        this.origUrl = template.getUrl();
        this.displayText = template.getDisplayText();
        this.checksum = template.getChecksum();
        this.hvm = template.isRequiresHvm();
        this.accountId = template.getAccountId();
        this.name = template.getUniqueName();
        this.format = template.getFormat();
        if (template.getDataStore() != null) {
            this.imageDataStore = template.getDataStore().getTO();
        }
        this.hypervisorType = template.getHypervisorType();
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

    public boolean isRequiresHvm() {
        return hvm;
    }

    public void setRequiresHvm(final boolean hvm) {
        this.hvm = hvm;
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
    public Hypervisor.HypervisorType getHypervisorType() {
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

    public void setHypervisorType(final Hypervisor.HypervisorType hypervisorType) {
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
