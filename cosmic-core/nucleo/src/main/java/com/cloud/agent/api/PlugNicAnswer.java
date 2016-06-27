//

//

package com.cloud.agent.api;

public class PlugNicAnswer extends Answer {
    public PlugNicAnswer() {
    }

    public PlugNicAnswer(final PlugNicCommand cmd, final boolean success, final String result) {
        super(cmd, success, result);
    }
}
