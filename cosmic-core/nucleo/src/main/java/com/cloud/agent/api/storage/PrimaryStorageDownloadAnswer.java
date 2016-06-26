//

//

package com.cloud.agent.api.storage;

import com.cloud.agent.api.Answer;

public class PrimaryStorageDownloadAnswer extends Answer {
    private String installPath;
    private long templateSize = 0L;

    protected PrimaryStorageDownloadAnswer() {
        super();
    }

    public PrimaryStorageDownloadAnswer(final String detail) {
        super(null, false, detail);
    }

    public PrimaryStorageDownloadAnswer(final String installPath, final long templateSize) {
        super(null);
        this.installPath = installPath;
        this.templateSize = templateSize;
    }

    public String getInstallPath() {
        return installPath;
    }

    public void setInstallPath(final String installPath) {
        this.installPath = installPath;
    }

    public Long getTemplateSize() {
        return templateSize;
    }

    public void setTemplateSize(final long templateSize) {
        this.templateSize = templateSize;
    }
}
