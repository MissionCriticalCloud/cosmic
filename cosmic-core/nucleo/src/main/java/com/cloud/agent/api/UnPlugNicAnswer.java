//

//

package com.cloud.agent.api;

public class UnPlugNicAnswer extends Answer {
    public UnPlugNicAnswer() {
    }

    public UnPlugNicAnswer(final UnPlugNicCommand cmd, final boolean success, final String result) {
        super(cmd, success, result);
    }
}
