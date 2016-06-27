//

//

package com.cloud.agent.api.storage;

import com.cloud.agent.api.Answer;

public class DestroyAnswer extends Answer {
    public DestroyAnswer(final DestroyCommand cmd, final boolean result, final String details) {
        super(cmd, result, details);
    }

    // Constructor for gson.
    protected DestroyAnswer() {
        super();
    }
}
