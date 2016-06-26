package com.cloud.storage.download;

import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;
import org.apache.cloudstack.storage.command.DownloadProgressCommand.RequestType;

public class DownloadCompleteState extends DownloadInactiveState {

    public DownloadCompleteState(final DownloadListener dl) {
        super(dl);
    }

    @Override
    public String getName() {
        return Status.DOWNLOADED.toString();
    }

    @Override
    public void onEntry(final String prevState, final DownloadEvent event, final Object evtObj) {
        super.onEntry(prevState, event, evtObj);
        if (!prevState.equals(getName())) {
            if (event == DownloadEvent.DOWNLOAD_ANSWER) {
                getDownloadListener().scheduleImmediateStatusCheck(RequestType.PURGE);
            }
        } else {
            getDownloadListener().setDownloadInactive(Status.DOWNLOADED);
        }
    }
}
