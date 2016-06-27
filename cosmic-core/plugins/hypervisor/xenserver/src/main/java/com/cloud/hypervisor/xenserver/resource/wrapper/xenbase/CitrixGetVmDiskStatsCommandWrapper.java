//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.GetVmDiskStatsAnswer;
import com.cloud.agent.api.GetVmDiskStatsCommand;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

@ResourceWrapper(handles = GetVmDiskStatsCommand.class)
public final class CitrixGetVmDiskStatsCommandWrapper extends CommandWrapper<GetVmDiskStatsCommand, Answer, CitrixResourceBase> {

    @Override
    public Answer execute(final GetVmDiskStatsCommand command, final CitrixResourceBase citrixResourceBase) {
        return new GetVmDiskStatsAnswer(command, null, null, null);
    }
}
