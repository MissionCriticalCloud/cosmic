package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.Command;

public class MigrateVolumeAnswer extends Answer {
    private final String volumePath;
    private String volumeChain;

    public MigrateVolumeAnswer(final Command command, final boolean success, final String details, final String volumePath) {
        super(command, success, details);
        this.volumePath = volumePath;
    }

    public MigrateVolumeAnswer(final Command command) {
        super(command);
        this.volumePath = null;
    }

    public String getVolumePath() {
        return volumePath;
    }

    public String getVolumeChainInfo() {
        return volumeChain;
    }

    public void setVolumeChainInfo(final String chainInfo) {
        this.volumeChain = chainInfo;
    }
}
