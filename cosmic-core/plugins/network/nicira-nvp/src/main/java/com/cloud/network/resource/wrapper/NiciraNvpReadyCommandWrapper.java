//

//

package com.cloud.network.resource.wrapper;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.ReadyAnswer;
import com.cloud.agent.api.ReadyCommand;
import com.cloud.network.resource.NiciraNvpResource;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

@ResourceWrapper(handles = ReadyCommand.class)
public final class NiciraNvpReadyCommandWrapper extends CommandWrapper<ReadyCommand, Answer, NiciraNvpResource> {

    @Override
    public Answer execute(final ReadyCommand command, final NiciraNvpResource niciraNvpResource) {
        return new ReadyAnswer(command);
    }
}
