//

//

package com.cloud.agent.api.storage;

import com.cloud.agent.api.Command;

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
