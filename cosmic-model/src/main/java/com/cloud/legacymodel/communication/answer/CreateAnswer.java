package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.CreateCommand;
import com.cloud.legacymodel.to.VolumeTO;

public class CreateAnswer extends Answer {
    VolumeTO volume;
    boolean requestTemplateReload = false;

    protected CreateAnswer() {
        super();
    }

    public CreateAnswer(final CreateCommand cmd, final VolumeTO volume) {
        super(cmd, true, null);
        this.volume = volume;
    }

    public CreateAnswer(final CreateCommand cmd, final String details) {
        super(cmd, false, details);
    }

    public CreateAnswer(final CreateCommand cmd, final String details, final boolean requestTemplateReload) {
        super(cmd, false, details);
        this.requestTemplateReload = requestTemplateReload;
    }

    public CreateAnswer(final CreateCommand cmd, final Exception e) {
        super(cmd, false, e.getMessage());
    }

    public VolumeTO getVolume() {
        return volume;
    }

    public boolean templateReloadRequested() {
        return requestTemplateReload;
    }
}
