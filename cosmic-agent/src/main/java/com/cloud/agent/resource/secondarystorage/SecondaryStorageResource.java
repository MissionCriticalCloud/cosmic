package com.cloud.agent.resource.secondarystorage;

import com.cloud.common.resource.ServerResource;

/**
 * SecondaryStorageServerResource is a generic container to execute commands sent
 */
public interface SecondaryStorageResource extends ServerResource {

    String getRootDir(String cmd);
}
