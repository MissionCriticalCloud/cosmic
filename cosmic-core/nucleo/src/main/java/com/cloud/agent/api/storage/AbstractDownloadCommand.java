//

//

package com.cloud.agent.api.storage;

import com.cloud.storage.Storage.ImageFormat;

public abstract class AbstractDownloadCommand extends SsCommand {

    private String url;
    private ImageFormat format;
    private long accountId;
    private String name;

    protected AbstractDownloadCommand() {
    }

    protected AbstractDownloadCommand(final String name, String url, final ImageFormat format, final Long accountId) {
        assert url != null;
        url = url.replace('\\', '/');

        this.url = url;
        this.format = format;
        this.accountId = accountId;
        this.name = name;
    }

    protected AbstractDownloadCommand(final AbstractDownloadCommand that) {
        super(that);
        assert that.url != null;

        url = that.url.replace('\\', '/');
        format = that.format;
        accountId = that.accountId;
        name = that.name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        assert url != null;
        url = url.replace('\\', '/');
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
