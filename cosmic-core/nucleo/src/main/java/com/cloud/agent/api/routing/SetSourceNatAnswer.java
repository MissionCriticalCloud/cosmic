//

//

package com.cloud.agent.api.routing;

import com.cloud.agent.api.Answer;

public class SetSourceNatAnswer extends Answer {
    public SetSourceNatAnswer() {
    }

    public SetSourceNatAnswer(final SetSourceNatCommand cmd, final boolean success, final String result) {
        super(cmd, success, result);
    }
}
