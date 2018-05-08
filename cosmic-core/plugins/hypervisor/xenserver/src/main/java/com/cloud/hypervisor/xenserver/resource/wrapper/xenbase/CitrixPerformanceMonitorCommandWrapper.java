package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.common.request.CommandWrapper;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.PerformanceMonitorAnswer;
import com.cloud.legacymodel.communication.command.PerformanceMonitorCommand;

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
