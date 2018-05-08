package com.cloud.storage.upload;

import com.cloud.legacymodel.communication.command.UploadProgressCommand.RequestType;
import com.cloud.legacymodel.storage.UploadStatus;

public class UploadAbandonedState extends UploadInactiveState {

    public UploadAbandonedState(final UploadListener dl) {
        super(dl);
    }

    @Override
    public String getName() {
        return UploadStatus.ABANDONED.toString();
    }

    @Override
    public void onEntry(final String prevState, final UploadEvent event, final Object evtObj) {
        super.onEntry(prevState, event, evtObj);
        if (!prevState.equalsIgnoreCase(getName())) {
            getUploadListener().updateDatabase(UploadStatus.ABANDONED, "Upload canceled");
            getUploadListener().cancelStatusTask();
            getUploadListener().cancelTimeoutTask();
            getUploadListener().sendCommand(RequestType.ABORT);
        }
    }
}
