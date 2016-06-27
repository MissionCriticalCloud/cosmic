//

//

package com.cloud.agent.api.storage;

import com.cloud.agent.api.to.TemplateTO;
import com.cloud.storage.Upload.Type;
import com.cloud.template.VirtualMachineTemplate;
import org.apache.cloudstack.api.InternalIdentity;

public class UploadCommand extends AbstractUploadCommand implements InternalIdentity {

    private TemplateTO template;
    private String url;
    private String installPath;
    private boolean hvm;
    private String description;
    private String checksum;
    private PasswordAuth auth;
    private long templateSizeInBytes;
    private long id;
    private Type type;

    public UploadCommand(final VirtualMachineTemplate template, final String url, final String installPath, final long sizeInBytes) {

        this.template = new TemplateTO(template);
        this.url = url;
        this.installPath = installPath;
        checksum = template.getChecksum();
        id = template.getId();
        templateSizeInBytes = sizeInBytes;
    }

    public UploadCommand(final String url, final long id, final long sizeInBytes, final String installPath, final Type type) {
        template = null;
        this.url = url;
        this.installPath = installPath;
        this.id = id;
        this.type = type;
        templateSizeInBytes = sizeInBytes;
    }

    protected UploadCommand() {
    }

    public UploadCommand(final UploadCommand that) {
        template = that.template;
        url = that.url;
        installPath = that.installPath;
        checksum = that.getChecksum();
        id = that.id;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(final String checksum) {
        this.checksum = checksum;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public TemplateTO getTemplate() {
        return template;
    }

    public void setTemplate(final TemplateTO template) {
        this.template = template;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void setUrl(final String url) {
        this.url = url;
    }

    public boolean isHvm() {
        return hvm;
    }

    public void setHvm(final boolean hvm) {
        this.hvm = hvm;
    }

    public PasswordAuth getAuth() {
        return auth;
    }

    public void setAuth(final PasswordAuth auth) {
        this.auth = auth;
    }

    public Long getTemplateSizeInBytes() {
        return templateSizeInBytes;
    }

    public void setTemplateSizeInBytes(final Long templateSizeInBytes) {
        this.templateSizeInBytes = templateSizeInBytes;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getInstallPath() {
        return installPath;
    }

    public void setInstallPath(final String installPath) {
        this.installPath = installPath;
    }
}
