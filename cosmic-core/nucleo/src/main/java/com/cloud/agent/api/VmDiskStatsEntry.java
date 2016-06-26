//

//

package com.cloud.agent.api;

import com.cloud.vm.VmDiskStats;

public class VmDiskStatsEntry implements VmDiskStats {

    String vmName;
    String path;
    long ioRead = 0;
    long ioWrite = 0;
    long bytesWrite = 0;
    long bytesRead = 0;

    public VmDiskStatsEntry() {
    }

    public VmDiskStatsEntry(final String vmName, final String path, final long ioWrite, final long ioRead, final long bytesWrite, final long bytesRead) {
        this.ioRead = ioRead;
        this.ioWrite = ioWrite;
        this.bytesRead = bytesRead;
        this.bytesWrite = bytesWrite;
        this.vmName = vmName;
        this.path = path;
    }

    public String getVmName() {
        return vmName;
    }

    public void setVmName(final String vmName) {
        this.vmName = vmName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    @Override
    public long getIORead() {
        return ioRead;
    }

    public void setIORead(final long ioRead) {
        this.ioRead = ioRead;
    }

    @Override
    public long getIOWrite() {
        return ioWrite;
    }

    @Override
    public long getBytesRead() {
        return bytesRead;
    }

    public void setBytesRead(final long bytesRead) {
        this.bytesRead = bytesRead;
    }

    @Override
    public long getBytesWrite() {
        return bytesWrite;
    }

    public void setBytesWrite(final long bytesWrite) {
        this.bytesWrite = bytesWrite;
    }

    public void setIOWrite(final long ioWrite) {
        this.ioWrite = ioWrite;
    }
}
