package com.cloud.api.response;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseResponse;
import com.cloud.serializer.Param;

import com.google.gson.annotations.SerializedName;

public class VmProgressResponse extends BaseResponse {

    @SerializedName("timeelapsed")
    @Param(description = "Time elapsed")
    long timeElapsed;

    @SerializedName("timeremaining")
    @Param(description = "Time remaining")
    long timeRemaining;

    @SerializedName("datatotal")
    @Param(description = "Data total")
    long dataTotal;

    @SerializedName("dataprocessed")
    @Param(description = "Data processed")
    long dataProcessed;

    @SerializedName("dataremaining")
    @Param(description = "Data remaining")
    long dataRemaining;

    @SerializedName("memorytotal")
    @Param(description = "Memory total")
    long memTotal;

    @SerializedName("memoryprocessed")
    @Param(description = "Memory processed")
    long memProcessed;

    @SerializedName("memoryremaining")
    @Param(description = "Memory remaining")
    long memRemaining;

    @SerializedName("filetotal")
    @Param(description = "File total")
    long fileTotal;

    @SerializedName("fileprocessed")
    @Param(description = "File processed")
    long fileProcessed;

    @SerializedName("fileremaining")
    @Param(description = "File remaining")
    long fileRemaining;

    public long getTimeElapsed() {
        return timeElapsed;
    }

    public void setTimeElapsed(final long timeElapsed) {
        this.timeElapsed = timeElapsed;
    }

    public long getTimeRemaining() {
        return timeRemaining;
    }

    public void setTimeRemaining(final long timeRemaining) {
        this.timeRemaining = timeRemaining;
    }

    public long getDataTotal() {
        return dataTotal;
    }

    public void setDataTotal(final long dataTotal) {
        this.dataTotal = dataTotal;
    }

    public long getDataProcessed() {
        return dataProcessed;
    }

    public void setDataProcessed(final long dataProcessed) {
        this.dataProcessed = dataProcessed;
    }

    public long getDataRemaining() {
        return dataRemaining;
    }

    public void setDataRemaining(final long dataRemaining) {
        this.dataRemaining = dataRemaining;
    }

    public long getMemTotal() {
        return memTotal;
    }

    public void setMemTotal(final long memTotal) {
        this.memTotal = memTotal;
    }

    public long getMemProcessed() {
        return memProcessed;
    }

    public void setMemProcessed(final long memProcessed) {
        this.memProcessed = memProcessed;
    }

    public long getMemRemaining() {
        return memRemaining;
    }

    public void setMemRemaining(final long memRemaining) {
        this.memRemaining = memRemaining;
    }

    public long getFileTotal() {
        return fileTotal;
    }

    public void setFileTotal(final long fileTotal) {
        this.fileTotal = fileTotal;
    }

    public long getFileProcessed() {
        return fileProcessed;
    }

    public void setFileProcessed(final long fileProcessed) {
        this.fileProcessed = fileProcessed;
    }

    public long getFileRemaining() {
        return fileRemaining;
    }

    public void setFileRemaining(final long fileRemaining) {
        this.fileRemaining = fileRemaining;
    }
}
