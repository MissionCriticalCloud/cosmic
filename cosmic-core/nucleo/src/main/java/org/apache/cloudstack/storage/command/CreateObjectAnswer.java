//

//

package org.apache.cloudstack.storage.command;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.to.DataTO;

public final class CreateObjectAnswer extends Answer {
    private DataTO data;

    protected CreateObjectAnswer() {
        super();
    }

    public CreateObjectAnswer(final DataTO data) {
        super();
        this.data = data;
    }

    public CreateObjectAnswer(final String errMsg) {
        super(null, false, errMsg);
    }

    public DataTO getData() {
        return data;
    }
}
