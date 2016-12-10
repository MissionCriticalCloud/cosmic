package com.cloud.storage.download;

import com.cloud.agent.api.storage.DownloadAnswer;
import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;
import com.cloud.storage.command.DownloadProgressCommand.RequestType;

public abstract class DownloadActiveState extends DownloadState {

    public DownloadActiveState(final DownloadListener dl) {
        super(dl);
    }

    @Override
    public String handleAnswer(final DownloadAnswer answer) {
        if (s_logger.isTraceEnabled()) {
            s_logger.trace("handleAnswer, answer status=" + answer.getDownloadStatus() + ", curr state=" + getName());
        }
        switch (answer.getDownloadStatus()) {
            case DOWNLOAD_IN_PROGRESS:
                getDownloadListener().scheduleStatusCheck(RequestType.GET_STATUS);
                return Status.DOWNLOAD_IN_PROGRESS.toString();
            case DOWNLOADED:
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
    public String handleTimeout(final long updateMs) {
        if (s_logger.isTraceEnabled()) {
            getDownloadListener().logTrace("handleTimeout, updateMs=" + updateMs + ", curr state= " + getName());
        }
        String newState = getName();
        if (updateMs > 5 * DownloadListener.STATUS_POLL_INTERVAL) {
            newState = Status.DOWNLOAD_ERROR.toString();
            getDownloadListener().logDebug("timeout: transitioning to download error state, currstate=" + getName());
        } else if (updateMs > 3 * DownloadListener.STATUS_POLL_INTERVAL) {
            getDownloadListener().cancelStatusTask();
            getDownloadListener().scheduleImmediateStatusCheck(RequestType.GET_STATUS);
            getDownloadListener().scheduleTimeoutTask(3 * DownloadListener.STATUS_POLL_INTERVAL);
            getDownloadListener().logDebug(getName() + " first timeout: checking again ");
        } else {
            getDownloadListener().scheduleTimeoutTask(3 * DownloadListener.STATUS_POLL_INTERVAL);
        }
        return newState;
    }

    @Override
    public String handleDisconnect() {

        return Status.DOWNLOAD_ERROR.toString();
    }

    @Override
    public void onEntry(final String prevState, final DownloadEvent event, final Object evtObj) {
        super.onEntry(prevState, event, evtObj);

        if (event == DownloadEvent.DOWNLOAD_ANSWER) {
            getDownloadListener().setLastUpdated();
        }
    }

    @Override
    public void onExit() {
    }
}
