package com.cloud.storage.download;

import com.cloud.agent.api.storage.DownloadAnswer;
import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;
import org.apache.cloudstack.storage.command.DownloadProgressCommand.RequestType;

public class DownloadAbandonedState extends DownloadInactiveState {

    public DownloadAbandonedState(final DownloadListener dl) {
        super(dl);
    }

    @Override
    public String getName() {
        return Status.ABANDONED.toString();
    }

    @Override
    public void onEntry(final String prevState, final DownloadEvent event, final Object evtObj) {
        super.onEntry(prevState, event, evtObj);
        if (!prevState.equalsIgnoreCase(getName())) {
            final DownloadAnswer answer = new DownloadAnswer("Download canceled", Status.ABANDONED);
            getDownloadListener().callback(answer);
            getDownloadListener().cancelStatusTask();
            getDownloadListener().cancelTimeoutTask();
            getDownloadListener().sendCommand(RequestType.ABORT);
        }
    }
}
