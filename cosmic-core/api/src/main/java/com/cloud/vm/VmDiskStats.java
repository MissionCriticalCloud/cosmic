package com.cloud.vm;

public interface VmDiskStats {
    // vm related disk stats

    public long getIORead();

    public long getIOWrite();

    public long getBytesRead();

    public long getBytesWrite();
}
