package com.cloud.storage.upload;

import com.cloud.agent.api.storage.UploadAnswer;

public abstract class UploadInactiveState extends UploadState {

    public UploadInactiveState(final UploadListener ul) {
        super(ul);
    }

    @Override
    public String handleAnswer(final UploadAnswer answer) {
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
