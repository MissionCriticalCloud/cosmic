//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CheckHealthAnswer;
import com.cloud.agent.api.CheckHealthCommand;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

@ResourceWrapper(handles = CheckHealthCommand.class)
public final class CitrixCheckHealthCommandWrapper extends CommandWrapper<CheckHealthCommand, Answer, CitrixResourceBase> {

    @Override
    public Answer execute(final CheckHealthCommand command, final CitrixResourceBase citrixResourceBase) {
        final boolean result = citrixResourceBase.pingXAPI();
        return new CheckHealthAnswer(command, result);
    }
}
