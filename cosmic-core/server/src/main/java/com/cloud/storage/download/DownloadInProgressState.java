package com.cloud.storage.download;

import com.cloud.legacymodel.storage.VMTemplateStatus;

public class DownloadInProgressState extends DownloadActiveState {

    public DownloadInProgressState(final DownloadListener dl) {
        super(dl);
    }

    @Override
    public void onEntry(final String prevState, final DownloadEvent event, final Object evtObj) {
        super.onEntry(prevState, event, evtObj);
        if (!prevState.equals(getName())) {
            getDownloadListener().logDownloadStart();
        }
    }

    @Override
    public String getName() {
        return VMTemplateStatus.DOWNLOAD_IN_PROGRESS.toString();
    }
}
