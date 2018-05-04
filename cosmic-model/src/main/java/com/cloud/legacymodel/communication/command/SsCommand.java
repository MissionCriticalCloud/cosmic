package com.cloud.legacymodel.communication.command;

public abstract class SsCommand extends Command {
    private String secUrl;

    public SsCommand() {
    }

    protected SsCommand(final SsCommand that) {
        this.secUrl = that.secUrl;
    }

    public SsCommand(final String secUrl) {
        this.secUrl = secUrl;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }

    public String getSecUrl() {
        return secUrl;
    }

    public void setSecUrl(final String secUrl) {
        this.secUrl = secUrl;
    }
}
