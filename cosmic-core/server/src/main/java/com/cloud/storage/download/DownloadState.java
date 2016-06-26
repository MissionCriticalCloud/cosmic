package com.cloud.storage.download;

import com.cloud.agent.api.storage.DownloadAnswer;

import java.util.Date;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DownloadState {
    protected static final Logger s_logger = LoggerFactory.getLogger(DownloadListener.class.getName());

    private final DownloadListener dl;

    public DownloadState(final DownloadListener dl) {
        this.dl = dl;
    }

    public String handleEvent(final DownloadEvent event, final Object eventObj) {
        if (s_logger.isTraceEnabled()) {
            getDownloadListener().log("handleEvent, event type=" + event + ", curr state=" + getName(), Level.TRACE);
        }
        switch (event) {
            case DOWNLOAD_ANSWER:
                final DownloadAnswer answer = (DownloadAnswer) eventObj;
                return handleAnswer(answer);
            case ABANDON_DOWNLOAD:
                return handleAbort();
            case TIMEOUT_CHECK:
                final Date now = new Date();
                final long update = now.getTime() - dl.getLastUpdated().getTime();
                return handleTimeout(update);
            case DISCONNECT:
                return handleDisconnect();
        }
        return null;
    }

    protected DownloadListener getDownloadListener() {
        return dl;
    }

    public abstract String getName();

    public abstract String handleAnswer(DownloadAnswer answer);

    public abstract String handleAbort();

    public abstract String handleTimeout(long updateMs);

    public abstract String handleDisconnect();

    public void onEntry(final String prevState, final DownloadEvent event, final Object evtObj) {
        if (s_logger.isTraceEnabled()) {
            getDownloadListener().log("onEntry, event type=" + event + ", curr state=" + getName(), Level.TRACE);
        }
        if (event == DownloadEvent.DOWNLOAD_ANSWER) {
            getDownloadListener().callback((DownloadAnswer) evtObj);
        }
    }

    public void onExit() {

    }

    public static enum DownloadEvent {
        DOWNLOAD_ANSWER, ABANDON_DOWNLOAD, TIMEOUT_CHECK, DISCONNECT
    }
}
