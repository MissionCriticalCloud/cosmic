package com.cloud.server.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;

import com.google.gson.annotations.SerializedName;

public class PxePingResponse extends NwDevicePxeServerResponse {
    @SerializedName(ApiConstants.PING_STORAGE_SERVER_IP)
    @Param(description = "IP of PING storage server")
    private String storageServerIp;

    @SerializedName(ApiConstants.PING_DIR)
    @Param(description = "Direcotry on PING server where to get restore image")
    private String pingDir;

    @SerializedName(ApiConstants.TFTP_DIR)
    @Param(description = "Tftp root directory of PXE server")
    private String tftpDir;

    public String getStorageServerIp() {
        return this.storageServerIp;
    }

    public void setStorageServerIp(final String ip) {
        this.storageServerIp = ip;
    }

    public String getPingDir() {
        return this.pingDir;
    }

    public void setPingDir(final String dir) {
        this.pingDir = dir;
    }

    public String getTftpDir() {
        return this.tftpDir;
    }

    public void setTftpDir(final String dir) {
        this.tftpDir = dir;
    }
}
