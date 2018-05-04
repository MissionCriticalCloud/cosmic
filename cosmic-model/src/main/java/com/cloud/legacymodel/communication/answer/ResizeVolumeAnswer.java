package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.ResizeVolumeCommand;

public class ResizeVolumeAnswer extends Answer {
    private long newSize;

    protected ResizeVolumeAnswer() {
        super();
    }

    public ResizeVolumeAnswer(final ResizeVolumeCommand cmd, final boolean result, final String details, final long newSize) {
        super(cmd, result, details);
        this.newSize = newSize;
    }

    public ResizeVolumeAnswer(final ResizeVolumeCommand cmd, final boolean result, final String details) {
        super(cmd, result, details);
    }

    public long getNewSize() {
        return newSize;
    }
}
