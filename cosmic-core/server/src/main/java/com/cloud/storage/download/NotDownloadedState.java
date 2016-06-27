package com.cloud.storage.download;

import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;
import org.apache.cloudstack.storage.command.DownloadProgressCommand.RequestType;

public class NotDownloadedState extends DownloadActiveState {

    public NotDownloadedState(final DownloadListener downloadListener) {
        super(downloadListener);
    }

    @Override
    public String getName() {
        return Status.NOT_DOWNLOADED.toString();
    }

    @Override
    public void onEntry(final String prevState, final DownloadEvent event, final Object evtObj) {
        super.onEntry(prevState, event, evtObj);
        getDownloadListener().scheduleStatusCheck(RequestType.GET_STATUS);
    }
}
