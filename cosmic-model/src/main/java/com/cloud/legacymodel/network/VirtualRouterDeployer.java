package com.cloud.legacymodel.network;

import com.cloud.legacymodel.ExecutionResult;
import com.cloud.legacymodel.communication.command.NetworkElementCommand;

public interface VirtualRouterDeployer {
    ExecutionResult executeInVR(String routerIp, String script, String args);

    /* timeout in seconds */
    ExecutionResult executeInVR(String routerIp, String script, String args, int timeout);

    ExecutionResult createFileInVR(String routerIp, String path, String filename, String content);

    ExecutionResult prepareCommand(NetworkElementCommand cmd);

    ExecutionResult cleanupCommand(NetworkElementCommand cmd);
}
