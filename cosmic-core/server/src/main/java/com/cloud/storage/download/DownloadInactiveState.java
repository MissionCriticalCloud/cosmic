package com.cloud.storage.download;

import com.cloud.agent.api.storage.DownloadAnswer;

public abstract class DownloadInactiveState extends DownloadState {

    public DownloadInactiveState(final DownloadListener dl) {
        super(dl);
    }

    @Override
    public String handleAnswer(final DownloadAnswer answer) {
        // ignore and stay put
        return getName();
    }

    @Override
    public String handleAbort() {
        // ignore and stay put
        return getName();
    }

    @Override
    public String handleTimeout(final long updateMs) {
        // ignore and stay put
        return getName();
    }

    @Override
    public String handleDisconnect() {
        //ignore and stay put
        return getName();
    }
}
