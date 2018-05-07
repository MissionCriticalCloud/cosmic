package com.cloud.storage.upload;

import com.cloud.legacymodel.communication.command.UploadProgressCommand.RequestType;
import com.cloud.legacymodel.storage.VMTemplateStatus;

public class NotUploadedState extends UploadActiveState {

    public NotUploadedState(final UploadListener uploadListener) {
        super(uploadListener);
    }

    @Override
    public String getName() {
        return VMTemplateStatus.NOT_UPLOADED.toString();
    }

    @Override
    public void onEntry(final String prevState, final UploadEvent event, final Object evtObj) {
        super.onEntry(prevState, event, evtObj);
        getUploadListener().scheduleStatusCheck(RequestType.GET_STATUS);
    }
}
