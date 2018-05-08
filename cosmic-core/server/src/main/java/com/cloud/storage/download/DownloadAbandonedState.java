package com.cloud.storage.download;

import com.cloud.legacymodel.communication.answer.DownloadAnswer;
import com.cloud.legacymodel.communication.command.DownloadProgressCommand.RequestType;
import com.cloud.legacymodel.storage.VMTemplateStatus;

public class DownloadAbandonedState extends DownloadInactiveState {

    public DownloadAbandonedState(final DownloadListener dl) {
        super(dl);
    }

    @Override
    public String getName() {
        return VMTemplateStatus.ABANDONED.toString();
    }

    @Override
    public void onEntry(final String prevState, final DownloadEvent event, final Object evtObj) {
        super.onEntry(prevState, event, evtObj);
        if (!prevState.equalsIgnoreCase(getName())) {
            final DownloadAnswer answer = new DownloadAnswer("Download canceled", VMTemplateStatus.ABANDONED);
            getDownloadListener().callback(answer);
            getDownloadListener().cancelStatusTask();
            getDownloadListener().cancelTimeoutTask();
            getDownloadListener().sendCommand(RequestType.ABORT);
        }
    }
}
