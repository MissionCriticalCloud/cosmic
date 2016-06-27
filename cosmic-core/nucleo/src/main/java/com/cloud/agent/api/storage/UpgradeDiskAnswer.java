//

//

package com.cloud.agent.api.storage;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;

public class UpgradeDiskAnswer extends Answer {

    public UpgradeDiskAnswer() {
        super();
    }

    public UpgradeDiskAnswer(final Command cmd, final boolean success, final String details) {
        super(cmd, success, details);
    }
}
