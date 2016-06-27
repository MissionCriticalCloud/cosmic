package com.cloud.storage.upload;

import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;

public class UploadInProgressState extends UploadActiveState {

    public UploadInProgressState(final UploadListener dl) {
        super(dl);
    }

    @Override
    public void onEntry(final String prevState, final UploadEvent event, final Object evtObj) {
        super.onEntry(prevState, event, evtObj);
        if (!prevState.equals(getName())) {
            getUploadListener().logUploadStart();
        }
    }

    @Override
    public String getName() {
        return Status.UPLOAD_IN_PROGRESS.toString();
    }
}
