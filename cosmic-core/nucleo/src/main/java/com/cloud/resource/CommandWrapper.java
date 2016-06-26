//

//

package com.cloud.resource;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;

public abstract class CommandWrapper<T extends Command, A extends Answer, R extends ServerResource> {

    /**
     * @param T is the command to be used.
     * @param R is the resource base to be used.
     * @return A and the Answer from the command.
     */
    public abstract A execute(T command, R serverResource);
}
