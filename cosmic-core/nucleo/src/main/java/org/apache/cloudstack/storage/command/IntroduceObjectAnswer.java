//

//

package org.apache.cloudstack.storage.command;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.to.DataTO;

public class IntroduceObjectAnswer extends Answer {
    private final DataTO dataTO;

    public IntroduceObjectAnswer(final DataTO dataTO) {
        this.dataTO = dataTO;
    }

    public DataTO getDataTO() {
        return dataTO;
    }
}
