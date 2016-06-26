package com.cloud.storage.upload;

import com.cloud.agent.api.storage.UploadProgressCommand.RequestType;
import com.cloud.storage.Upload.Status;

public class UploadAbandonedState extends UploadInactiveState {

    public UploadAbandonedState(final UploadListener dl) {
        super(dl);
    }

    @Override
    public String getName() {
        return Status.ABANDONED.toString();
    }

    @Override
    public void onEntry(final String prevState, final UploadEvent event, final Object evtObj) {
        super.onEntry(prevState, event, evtObj);
        if (!prevState.equalsIgnoreCase(getName())) {
            getUploadListener().updateDatabase(Status.ABANDONED, "Upload canceled");
            getUploadListener().cancelStatusTask();
            getUploadListener().cancelTimeoutTask();
            getUploadListener().sendCommand(RequestType.ABORT);
        }
    }
}
