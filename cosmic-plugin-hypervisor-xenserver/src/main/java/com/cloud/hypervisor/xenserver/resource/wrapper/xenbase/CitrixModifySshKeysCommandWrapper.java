//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.ModifySshKeysCommand;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

@ResourceWrapper(handles = ModifySshKeysCommand.class)
public final class CitrixModifySshKeysCommandWrapper extends CommandWrapper<ModifySshKeysCommand, Answer, CitrixResourceBase> {

    @Override
    public Answer execute(final ModifySshKeysCommand command, final CitrixResourceBase citrixResourceBase) {
        return new Answer(command);
    }
}
