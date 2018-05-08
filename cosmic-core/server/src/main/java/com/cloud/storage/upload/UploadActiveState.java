package com.cloud.storage.upload;

import com.cloud.legacymodel.communication.answer.UploadAnswer;
import com.cloud.legacymodel.communication.command.UploadProgressCommand.RequestType;
import com.cloud.legacymodel.storage.VMTemplateStatus;

public abstract class UploadActiveState extends UploadState {

    public UploadActiveState(final UploadListener ul) {
        super(ul);
    }

    @Override
    public String handleAnswer(final UploadAnswer answer) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("handleAnswer, answer status=" + answer.getUploadStatus() + ", curr state=" + getName());
        }
        switch (answer.getUploadStatus()) {
            case UPLOAD_IN_PROGRESS:
                getUploadListener().scheduleStatusCheck(RequestType.GET_STATUS);
                return VMTemplateStatus.UPLOAD_IN_PROGRESS.toString();
            case UPLOADED:
                getUploadListener().scheduleImmediateStatusCheck(RequestType.PURGE);
                getUploadListener().cancelTimeoutTask();
                return VMTemplateStatus.UPLOADED.toString();
            case NOT_UPLOADED:
                getUploadListener().scheduleStatusCheck(RequestType.GET_STATUS);
                return VMTemplateStatus.NOT_UPLOADED.toString();
            case UPLOAD_ERROR:
                getUploadListener().cancelStatusTask();
                getUploadListener().cancelTimeoutTask();
                return VMTemplateStatus.UPLOAD_ERROR.toString();
            case UNKNOWN:
                getUploadListener().cancelStatusTask();
                getUploadListener().cancelTimeoutTask();
                return VMTemplateStatus.UPLOAD_ERROR.toString();
            default:
                return null;
        }
    }

    @Override
    public String handleAbort() {
        return VMTemplateStatus.ABANDONED.toString();
    }

    @Override
    public String handleTimeout(final long updateMs) {
        if (s_logger.isTraceEnabled()) {
            getUploadListener().logTrace("handleTimeout, updateMs=" + updateMs + ", curr state= " + getName());
        }
        String newState = getName();
        if (updateMs > 5 * UploadListener.STATUS_POLL_INTERVAL) {
            newState = VMTemplateStatus.UPLOAD_ERROR.toString();
            getUploadListener().logDebug("timeout: transitioning to upload error state, currstate=" + getName());
        } else if (updateMs > 3 * UploadListener.STATUS_POLL_INTERVAL) {
            getUploadListener().cancelStatusTask();
            getUploadListener().scheduleImmediateStatusCheck(RequestType.GET_STATUS);
            getUploadListener().scheduleTimeoutTask(3 * UploadListener.STATUS_POLL_INTERVAL);
            getUploadListener().logDebug(getName() + " first timeout: checking again ");
        } else {
            getUploadListener().scheduleTimeoutTask(3 * UploadListener.STATUS_POLL_INTERVAL);
        }
        return newState;
    }

    @Override
    public String handleDisconnect() {

        return VMTemplateStatus.UPLOAD_ERROR.toString();
    }

    @Override
    public void onEntry(final String prevState, final UploadEvent event, final Object evtObj) {
        super.onEntry(prevState, event, evtObj);

        if (event == UploadEvent.UPLOAD_ANSWER) {
            getUploadListener().setLastUpdated();
        }
    }

    @Override
    public void onExit() {
    }
}
