//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xen620sp1;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.GetGPUStatsAnswer;
import com.cloud.agent.api.GetGPUStatsCommand;
import com.cloud.agent.api.VgpuTypesInfo;
import com.cloud.hypervisor.xenserver.resource.XenServer620SP1Resource;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import java.util.HashMap;

import com.xensource.xenapi.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = GetGPUStatsCommand.class)
public final class XenServer620SP1GetGPUStatsCommandWrapper extends CommandWrapper<GetGPUStatsCommand, Answer, XenServer620SP1Resource> {

    private static final Logger s_logger = LoggerFactory.getLogger(XenServer620SP1GetGPUStatsCommandWrapper.class);

    @Override
    public Answer execute(final GetGPUStatsCommand command, final XenServer620SP1Resource xenServer620SP1Resource) {
        final Connection conn = xenServer620SP1Resource.getConnection();
        HashMap<String, HashMap<String, VgpuTypesInfo>> groupDetails = new HashMap<>();
        try {
            groupDetails = xenServer620SP1Resource.getGPUGroupDetails(conn);
        } catch (final Exception e) {
            final String msg = "Unable to get GPU stats" + e.toString();
            s_logger.warn(msg, e);
            return new GetGPUStatsAnswer(command, false, msg);
        }
        return new GetGPUStatsAnswer(command, groupDetails);
    }
}
