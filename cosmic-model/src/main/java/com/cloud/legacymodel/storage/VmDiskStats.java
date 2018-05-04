package com.cloud.legacymodel.storage;

public interface VmDiskStats {
    long getIORead();

    long getIOWrite();

    long getBytesRead();

    long getBytesWrite();
}
