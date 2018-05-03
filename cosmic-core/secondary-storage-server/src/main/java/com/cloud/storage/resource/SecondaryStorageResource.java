package com.cloud.storage.resource;

import com.cloud.resource.ServerResource;

/**
 * SecondaryStorageServerResource is a generic container to execute commands sent
 */
public interface SecondaryStorageResource extends ServerResource {

    String getRootDir(String cmd);
}
