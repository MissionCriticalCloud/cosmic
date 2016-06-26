package com.cloud.vm;

/**
 * Secondary Storage VM is a system VM instance that is used
 * to interface the management server to secondary storage
 */
public interface SecondaryStorageVm extends SystemVm {
    enum Role {
        templateProcessor, commandExecutor
    }
}
