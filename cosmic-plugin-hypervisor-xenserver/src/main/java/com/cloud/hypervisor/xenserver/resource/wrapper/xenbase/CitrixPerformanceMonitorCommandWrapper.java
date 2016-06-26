//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.PerformanceMonitorAnswer;
import com.cloud.agent.api.PerformanceMonitorCommand;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import com.xensource.xenapi.Connection;

@ResourceWrapper(handles = PerformanceMonitorCommand.class)
public final class CitrixPerformanceMonitorCommandWrapper extends CommandWrapper<PerformanceMonitorCommand, Answer, CitrixResourceBase> {

    @Override
    public Answer execute(final PerformanceMonitorCommand command, final CitrixResourceBase citrixResourceBase) {
        final Connection conn = citrixResourceBase.getConnection();
        final String perfMon = citrixResourceBase.getPerfMon(conn, command.getParams(), command.getWait());
        if (perfMon == null) {
            return new PerformanceMonitorAnswer(command, false, perfMon);
        } else {
            return new PerformanceMonitorAnswer(command, true, perfMon);
        }
    }
}
