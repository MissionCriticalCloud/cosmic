package com.cloud.storage.download;

import com.cloud.legacymodel.communication.command.DownloadProgressCommand.RequestType;
import com.cloud.legacymodel.storage.VMTemplateStatus;

public class DownloadCompleteState extends DownloadInactiveState {

    public DownloadCompleteState(final DownloadListener dl) {
        super(dl);
    }

    @Override
    public String getName() {
        return VMTemplateStatus.DOWNLOADED.toString();
    }

    @Override
    public void onEntry(final String prevState, final DownloadEvent event, final Object evtObj) {
        super.onEntry(prevState, event, evtObj);
        if (!prevState.equals(getName())) {
            if (event == DownloadEvent.DOWNLOAD_ANSWER) {
                getDownloadListener().scheduleImmediateStatusCheck(RequestType.PURGE);
            }
        } else {
            getDownloadListener().setDownloadInactive(VMTemplateStatus.DOWNLOADED);
        }
    }
}
