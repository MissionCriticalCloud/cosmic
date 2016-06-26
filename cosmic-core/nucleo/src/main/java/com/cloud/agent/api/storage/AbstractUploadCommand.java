//

//

package com.cloud.agent.api.storage;

import com.cloud.storage.Storage.ImageFormat;

public class AbstractUploadCommand extends StorageCommand {

    private String url;
    private ImageFormat format;
    private long accountId;
    private String name;

    protected AbstractUploadCommand() {
    }

    protected AbstractUploadCommand(final AbstractUploadCommand that) {
        this(that.name, that.url, that.format, that.accountId);
    }

    protected AbstractUploadCommand(final String name, final String url, final ImageFormat format, final long accountId) {
        this.url = url;
        this.format = format;
        this.accountId = accountId;
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public ImageFormat getFormat() {
        return format;
    }

    public long getAccountId() {
        return accountId;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }
}
