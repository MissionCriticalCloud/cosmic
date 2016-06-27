//

//

package com.cloud.agent.api;

public class StopAnswer extends RebootAnswer {

    private String platform;

    protected StopAnswer() {
    }

    public StopAnswer(final StopCommand cmd, final String details, final String platform, final boolean success) {
        super(cmd, details, success);
        this.platform = platform;
    }

    public StopAnswer(final StopCommand cmd, final String details, final boolean success) {
        super(cmd, details, success);
        this.platform = null;
    }

    public StopAnswer(final StopCommand cmd, final Exception e) {
        super(cmd, e);
        this.platform = null;
    }

    public String getPlatform() {
        return platform;
    }
}
