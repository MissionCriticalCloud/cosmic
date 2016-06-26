package com.cloud.storage.upload;

import com.cloud.agent.api.storage.UploadAnswer;

import java.util.Date;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class UploadState {

    protected static final Logger s_logger = LoggerFactory.getLogger(UploadListener.class.getName());

    private final UploadListener ul;

    public UploadState(final UploadListener ul) {
        this.ul = ul;
    }

    public String handleEvent(final UploadEvent event, final Object eventObj) {
        if (s_logger.isTraceEnabled()) {
            getUploadListener().log("handleEvent, event type=" + event + ", curr state=" + getName(), Level.TRACE);
        }
        switch (event) {
            case UPLOAD_ANSWER:
                final UploadAnswer answer = (UploadAnswer) eventObj;
                return handleAnswer(answer);
            case ABANDON_UPLOAD:
                return handleAbort();
            case TIMEOUT_CHECK:
                final Date now = new Date();
                final long update = now.getTime() - ul.getLastUpdated().getTime();
                return handleTimeout(update);
            case DISCONNECT:
                return handleDisconnect();
        }
        return null;
    }

    protected UploadListener getUploadListener() {
        return ul;
    }

    public abstract String getName();

    public abstract String handleAnswer(UploadAnswer answer);

    public abstract String handleAbort();

    public abstract String handleTimeout(long updateMs);

    public abstract String handleDisconnect();

    public void onEntry(final String prevState, final UploadEvent event, final Object evtObj) {
        if (s_logger.isTraceEnabled()) {
            getUploadListener().log("onEntry, event type=" + event + ", curr state=" + getName(), Level.TRACE);
        }
        if (event == UploadEvent.UPLOAD_ANSWER) {
            getUploadListener().updateDatabase((UploadAnswer) evtObj);
        }
    }

    public void onExit() {

    }

    public static enum UploadEvent {
        UPLOAD_ANSWER, ABANDON_UPLOAD, TIMEOUT_CHECK, DISCONNECT
    }
}
