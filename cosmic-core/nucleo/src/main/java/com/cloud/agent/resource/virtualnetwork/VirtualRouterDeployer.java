//

//

package com.cloud.agent.resource.virtualnetwork;

import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.utils.ExecutionResult;

public interface VirtualRouterDeployer {
    ExecutionResult executeInVR(String routerIp, String script, String args);

    /* timeout in seconds */
    ExecutionResult executeInVR(String routerIp, String script, String args, int timeout);

    ExecutionResult createFileInVR(String routerIp, String path, String filename, String content);

    ExecutionResult prepareCommand(NetworkElementCommand cmd);

    ExecutionResult cleanupCommand(NetworkElementCommand cmd);
}
