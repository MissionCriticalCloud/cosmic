package com.cloud.storage.download;

import com.cloud.legacymodel.communication.answer.DownloadAnswer;
import com.cloud.legacymodel.communication.command.DownloadProgressCommand.RequestType;
import com.cloud.legacymodel.storage.VMTemplateStatus;

public class DownloadErrorState extends DownloadInactiveState {

    public DownloadErrorState(final DownloadListener dl) {
        super(dl);
    }

    @Override
    public String handleAnswer(final DownloadAnswer answer) {
        switch (answer.getDownloadStatus()) {
            case DOWNLOAD_IN_PROGRESS:
                getDownloadListener().scheduleStatusCheck(RequestType.GET_STATUS);
                return VMTemplateStatus.DOWNLOAD_IN_PROGRESS.toString();
            case DOWNLOADED:
                getDownloadListener().scheduleImmediateStatusCheck(RequestType.PURGE);
                getDownloadListener().cancelTimeoutTask();
                return VMTemplateStatus.DOWNLOADED.toString();
            case NOT_DOWNLOADED:
                getDownloadListener().scheduleStatusCheck(RequestType.GET_STATUS);
                return VMTemplateStatus.NOT_DOWNLOADED.toString();
            case DOWNLOAD_ERROR:
                getDownloadListener().cancelStatusTask();
                getDownloadListener().cancelTimeoutTask();
                return VMTemplateStatus.DOWNLOAD_ERROR.toString();
            case UNKNOWN:
                getDownloadListener().cancelStatusTask();
                getDownloadListener().cancelTimeoutTask();
                return VMTemplateStatus.DOWNLOAD_ERROR.toString();
            default:
                return null;
        }
    }

    @Override
    public String handleAbort() {
        return VMTemplateStatus.ABANDONED.toString();
    }

    @Override
    public String getName() {
        return VMTemplateStatus.DOWNLOAD_ERROR.toString();
    }

    @Override
    public void onEntry(final String prevState, final DownloadEvent event, final Object evtObj) {
        super.onEntry(prevState, event, evtObj);
        if (event == DownloadEvent.DISCONNECT) {
            getDownloadListener().logDisconnect();
            getDownloadListener().cancelStatusTask();
            getDownloadListener().cancelTimeoutTask();
            final DownloadAnswer answer = new DownloadAnswer("Storage agent or storage VM disconnected", VMTemplateStatus.DOWNLOAD_ERROR);
            getDownloadListener().callback(answer);
            getDownloadListener().logWarn("Entering download error state because the storage host disconnected");
        } else if (event == DownloadEvent.TIMEOUT_CHECK) {
            final DownloadAnswer answer = new DownloadAnswer("Timeout waiting for response from storage host", VMTemplateStatus.DOWNLOAD_ERROR);
            getDownloadListener().callback(answer);
            getDownloadListener().logWarn("Entering download error state: timeout waiting for response from storage host");
        }
        getDownloadListener().setDownloadInactive(VMTemplateStatus.DOWNLOAD_ERROR);
    }
}
