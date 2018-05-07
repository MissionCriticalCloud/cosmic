package com.cloud.storage.upload;

import com.cloud.legacymodel.communication.answer.UploadAnswer;
import com.cloud.legacymodel.communication.command.UploadProgressCommand.RequestType;
import com.cloud.legacymodel.storage.UploadStatus;

public class UploadErrorState extends UploadInactiveState {

    public UploadErrorState(final UploadListener ul) {
        super(ul);
    }

    @Override
    public String handleAnswer(final UploadAnswer answer) {
        switch (answer.getUploadStatus()) {
            case UPLOAD_IN_PROGRESS:
                getUploadListener().scheduleStatusCheck(RequestType.GET_STATUS);
                return UploadStatus.UPLOAD_IN_PROGRESS.toString();
            case UPLOADED:
                getUploadListener().scheduleImmediateStatusCheck(RequestType.PURGE);
                getUploadListener().cancelTimeoutTask();
                return UploadStatus.UPLOADED.toString();
            case NOT_UPLOADED:
                getUploadListener().scheduleStatusCheck(RequestType.GET_STATUS);
                return UploadStatus.NOT_UPLOADED.toString();
            case UPLOAD_ERROR:
                getUploadListener().cancelStatusTask();
                getUploadListener().cancelTimeoutTask();
                return UploadStatus.UPLOAD_ERROR.toString();
            case UNKNOWN:
                getUploadListener().cancelStatusTask();
                getUploadListener().cancelTimeoutTask();
                return UploadStatus.UPLOAD_ERROR.toString();
            default:
                return null;
        }
    }

    @Override
    public String handleAbort() {
        return UploadStatus.ABANDONED.toString();
    }

    @Override
    public String getName() {
        return UploadStatus.UPLOAD_ERROR.toString();
    }

    @Override
    public void onEntry(final String prevState, final UploadEvent event, final Object evtObj) {
        super.onEntry(prevState, event, evtObj);
        if (event == UploadEvent.DISCONNECT) {
            getUploadListener().logDisconnect();
            getUploadListener().cancelStatusTask();
            getUploadListener().cancelTimeoutTask();
            getUploadListener().updateDatabase(UploadStatus.UPLOAD_ERROR, "Storage agent or storage VM disconnected");
            getUploadListener().logWarn("Entering upload error state because the storage host disconnected");
        } else if (event == UploadEvent.TIMEOUT_CHECK) {
            getUploadListener().updateDatabase(UploadStatus.UPLOAD_ERROR, "Timeout waiting for response from storage host");
            getUploadListener().logWarn("Entering upload error state: timeout waiting for response from storage host");
        }
        getUploadListener().setUploadInactive(UploadStatus.UPLOAD_ERROR);
    }
}
