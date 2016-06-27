//

//

package com.cloud.agent.api.storage;

import com.cloud.agent.api.to.DataTO;

public class CreateEntityDownloadURLCommand extends AbstractDownloadCommand {

    private String installPath;
    private String parent;
    private String extractLinkUUID;
    private DataTO data;

    public CreateEntityDownloadURLCommand(final String parent, final String installPath, final String uuid, final DataTO data) { // this constructor is for creating template
        // download url
        super();
        this.parent = parent; // parent is required as not the template can be child of one of many parents
        this.installPath = installPath;
        this.extractLinkUUID = uuid;
        this.data = data;
    }

    public CreateEntityDownloadURLCommand(final String installPath, final String uuid) {
        super();
        this.installPath = installPath;
        this.extractLinkUUID = uuid;
    }

    public CreateEntityDownloadURLCommand() {
    }

    public DataTO getData() {
        return data;
    }

    public void setData(final DataTO data) {
        this.data = data;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    public String getInstallPath() {
        return installPath;
    }

    public void setInstallPath(final String installPath) {
        this.installPath = installPath;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(final String parent) {
        this.parent = parent;
    }

    public String getExtractLinkUUID() {
        return extractLinkUUID;
    }

    public void setExtractLinkUUID(final String extractLinkUUID) {
        this.extractLinkUUID = extractLinkUUID;
    }
}
