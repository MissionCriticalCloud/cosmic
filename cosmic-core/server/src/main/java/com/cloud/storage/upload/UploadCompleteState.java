package com.cloud.storage.upload;

import com.cloud.legacymodel.communication.command.UploadProgressCommand.RequestType;
import com.cloud.legacymodel.storage.UploadStatus;

public class UploadCompleteState extends UploadInactiveState {

    public UploadCompleteState(final UploadListener ul) {
        super(ul);
    }

    @Override
    public String getName() {
        return UploadStatus.UPLOADED.toString();
    }

    @Override
    public void onEntry(final String prevState, final UploadEvent event, final Object evtObj) {
        super.onEntry(prevState, event, evtObj);
        if (!prevState.equals(getName())) {
            if (event == UploadEvent.UPLOAD_ANSWER) {
                getUploadListener().scheduleImmediateStatusCheck(RequestType.PURGE);
            }
            getUploadListener().setUploadInactive(UploadStatus.UPLOADED);
        }
    }
}
