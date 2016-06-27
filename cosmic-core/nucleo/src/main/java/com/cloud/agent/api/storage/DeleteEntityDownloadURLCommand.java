//

//

package com.cloud.agent.api.storage;

import com.cloud.storage.Upload;

public class DeleteEntityDownloadURLCommand extends AbstractDownloadCommand {

    private String path;
    private String extractUrl;
    private Upload.Type type;
    private String parentPath;

    public DeleteEntityDownloadURLCommand(final String path, final Upload.Type type, final String url, final String parentPath) {
        super();
        this.path = path;
        this.type = type;
        this.extractUrl = url;
        this.parentPath = parentPath;
    }

    public DeleteEntityDownloadURLCommand() {
        super();
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public Upload.Type getType() {
        return type;
    }

    public void setType(final Upload.Type type) {
        this.type = type;
    }

    public String getExtractUrl() {
        return extractUrl;
    }

    public void setExtractUrl(final String extractUrl) {
        this.extractUrl = extractUrl;
    }

    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(final String parentPath) {
        this.parentPath = parentPath;
    }
}
