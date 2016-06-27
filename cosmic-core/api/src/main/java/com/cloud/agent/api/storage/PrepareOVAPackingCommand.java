package com.cloud.agent.api.storage;

import com.cloud.agent.api.Command;

public class PrepareOVAPackingCommand extends Command {
    private String templatePath;
    private String secUrl;

    public PrepareOVAPackingCommand() {
    }

    public PrepareOVAPackingCommand(final String secUrl, final String templatePath) {
        this.secUrl = secUrl;
        this.templatePath = templatePath;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }

    public String getTemplatePath() {
        return this.templatePath;
    }

    public String getSecondaryStorageUrl() {
        return this.secUrl;
    }
}
