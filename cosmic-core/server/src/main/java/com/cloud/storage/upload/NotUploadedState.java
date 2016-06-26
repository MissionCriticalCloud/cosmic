package com.cloud.storage.upload;

import com.cloud.agent.api.storage.UploadProgressCommand.RequestType;
import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;

public class NotUploadedState extends UploadActiveState {

    public NotUploadedState(final UploadListener uploadListener) {
        super(uploadListener);
    }

    @Override
    public String getName() {
        return Status.NOT_UPLOADED.toString();
    }

    @Override
    public void onEntry(final String prevState, final UploadEvent event, final Object evtObj) {
        super.onEntry(prevState, event, evtObj);
        getUploadListener().scheduleStatusCheck(RequestType.GET_STATUS);
    }
}
