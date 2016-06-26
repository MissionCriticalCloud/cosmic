package com.cloud.storage.download;

import com.cloud.agent.api.storage.DownloadAnswer;
import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;
import org.apache.cloudstack.storage.command.DownloadProgressCommand.RequestType;

import org.apache.log4j.Level;

public class DownloadErrorState extends DownloadInactiveState {

    public DownloadErrorState(final DownloadListener dl) {
        super(dl);
    }

    @Override
    public String handleAnswer(final DownloadAnswer answer) {
        switch (answer.getDownloadStatus()) {
            case DOWNLOAD_IN_PROGRESS:
                getDownloadListener().scheduleStatusCheck(RequestType.GET_STATUS);
                return Status.DOWNLOAD_IN_PROGRESS.toString();
            case DOWNLOADED:
                getDownloadListener().scheduleImmediateStatusCheck(RequestType.PURGE);
                getDownloadListener().cancelTimeoutTask();
                return Status.DOWNLOADED.toString();
            case NOT_DOWNLOADED:
                getDownloadListener().scheduleStatusCheck(RequestType.GET_STATUS);
                return Status.NOT_DOWNLOADED.toString();
            case DOWNLOAD_ERROR:
                getDownloadListener().cancelStatusTask();
                getDownloadListener().cancelTimeoutTask();
                return Status.DOWNLOAD_ERROR.toString();
            case UNKNOWN:
                getDownloadListener().cancelStatusTask();
                getDownloadListener().cancelTimeoutTask();
                return Status.DOWNLOAD_ERROR.toString();
            default:
                return null;
        }
    }

    @Override
    public String handleAbort() {
        return Status.ABANDONED.toString();
    }

    @Override
    public String getName() {
        return Status.DOWNLOAD_ERROR.toString();
    }

    @Override
    public void onEntry(final String prevState, final DownloadEvent event, final Object evtObj) {
        super.onEntry(prevState, event, evtObj);
        if (event == DownloadEvent.DISCONNECT) {
            getDownloadListener().logDisconnect();
            getDownloadListener().cancelStatusTask();
            getDownloadListener().cancelTimeoutTask();
            final DownloadAnswer answer = new DownloadAnswer("Storage agent or storage VM disconnected", Status.DOWNLOAD_ERROR);
            getDownloadListener().callback(answer);
            getDownloadListener().log("Entering download error state because the storage host disconnected", Level.WARN);
        } else if (event == DownloadEvent.TIMEOUT_CHECK) {
            final DownloadAnswer answer = new DownloadAnswer("Timeout waiting for response from storage host", Status.DOWNLOAD_ERROR);
            getDownloadListener().callback(answer);
            getDownloadListener().log("Entering download error state: timeout waiting for response from storage host", Level.WARN);
        }
        getDownloadListener().setDownloadInactive(Status.DOWNLOAD_ERROR);
    }
}
