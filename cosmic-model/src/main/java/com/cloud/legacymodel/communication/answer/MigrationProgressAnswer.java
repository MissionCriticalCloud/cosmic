package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.MigrationProgressCommand;

public class MigrationProgressAnswer extends Answer {
    long timeElapsed;
    long timeRemaining;
    long dataTotal;
    long dataProcessed;
    long dataRemaining;
    long memTotal;
    long memProcessed;
    long memRemaining;
    long fileTotal;
    long fileProcessed;
    long fileRemaining;

    protected MigrationProgressAnswer() {
    }

    public MigrationProgressAnswer(final MigrationProgressCommand cmd, final boolean success, final String details) {
        super(cmd, success, details);
    }

    public MigrationProgressAnswer(final MigrationProgressCommand cmd, final boolean success, final String details,
                                   final long timeElapsed, final long timeRemaining,
                                   final long dataTotal, final long dataProcessed, final long dataRemaining,
                                   final long memTotal, final long memProcessed, final long memRemaining,
                                   final long fileTotal, final long fileProcessed, final long fileRemaining) {
        super(cmd, success, details);

        this.timeElapsed = timeElapsed;
        this.timeRemaining = timeRemaining;
        this.dataTotal = dataTotal;
        this.dataProcessed = dataProcessed;
        this.dataRemaining = dataRemaining;
        this.memTotal = memTotal;
        this.memProcessed = memProcessed;
        this.memRemaining = memRemaining;
        this.fileTotal = fileTotal;
        this.fileProcessed = fileProcessed;
        this.fileRemaining = fileRemaining;
    }

    public long getTimeElapsed() {
        return timeElapsed;
    }

    public long getTimeRemaining() {
        return timeRemaining;
    }

    public long getDataTotal() {
        return dataTotal;
    }

    public long getDataProcessed() {
        return dataProcessed;
    }

    public long getDataRemaining() {
        return dataRemaining;
    }

    public long getMemTotal() {
        return memTotal;
    }

    public long getMemProcessed() {
        return memProcessed;
    }

    public long getMemRemaining() {
        return memRemaining;
    }

    public long getFileTotal() {
        return fileTotal;
    }

    public long getFileProcessed() {
        return fileProcessed;
    }

    public long getFileRemaining() {
        return fileRemaining;
    }
}
