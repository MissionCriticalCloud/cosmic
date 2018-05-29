package com.cloud.agent.resource.secondarystorage;

import com.cloud.agent.resource.AgentResource;

/**
 * SecondaryStorageServerResource is a generic container to execute commands sent
 */
public interface SecondaryStorageResource extends AgentResource {

    String getRootDir(String cmd);
}
