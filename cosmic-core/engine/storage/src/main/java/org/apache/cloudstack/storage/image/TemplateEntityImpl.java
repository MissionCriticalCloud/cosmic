package org.apache.cloudstack.storage.image;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.Storage.TemplateType;
import com.cloud.template.VirtualMachineTemplate;
import org.apache.cloudstack.engine.cloud.entity.api.TemplateEntity;
import org.apache.cloudstack.engine.subsystem.api.storage.TemplateInfo;
import org.apache.cloudstack.storage.image.datastore.ImageStoreInfo;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class TemplateEntityImpl implements TemplateEntity {
    protected TemplateInfo templateInfo;

    public TemplateEntityImpl(final TemplateInfo templateInfo) {
        this.templateInfo = templateInfo;
    }

    @Override
    public State getState() {
        return templateInfo.getState();
    }

    @Override
    public boolean isFeatured() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isPublicTemplate() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isExtractable() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ImageFormat getFormat() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isRequiresHvm() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getDisplayText() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean getEnablePassword() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean getEnableSshKey() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isCrossZones() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Date getCreated() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getGuestOSId() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isBootable() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public TemplateType getTemplateType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HypervisorType getHypervisorType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getBits() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getUniqueName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getUrl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getChecksum() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long getSourceTemplateId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTemplateTag() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isDynamicallyScalable() {
        return false;
    }

    @Override
    public long getUpdatedCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void incrUpdatedCount() {
        // TODO Auto-generated method stub
    }

    @Override
    public Date getUpdated() {
        // TODO Auto-generated method stub
        return null;
    }

    public long getImageDataStoreId() {
        return getImageDataStore().getImageStoreId();
    }

    public ImageStoreInfo getImageDataStore() {
        return (ImageStoreInfo) templateInfo.getDataStore();
    }

    public TemplateInfo getTemplateInfo() {
        return templateInfo;
    }

    @Override
    public String getUuid() {
        return templateInfo.getUuid();
    }

    @Override
    public long getId() {
        return templateInfo.getId();
    }

    @Override
    public String getCurrentState() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDesiredState() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Date getCreatedTime() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Date getLastUpdatedTime() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getOwner() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, String> getDetails() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addDetail(final String name, final String value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void delDetail(final String name, final String value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateDetail(final String name, final String value) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<Method> getApplicableActions() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getExternalId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getAccountId() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getDomainId() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getPhysicalSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getVirtualSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Class<?> getEntityType() {
        return VirtualMachineTemplate.class;
    }
}
