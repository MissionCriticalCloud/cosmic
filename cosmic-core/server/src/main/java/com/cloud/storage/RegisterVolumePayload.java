package com.cloud.storage;

public class RegisterVolumePayload {
    private final String url;
    private final String checksum;
    private final String format;

    public RegisterVolumePayload(final String url, final String checksum, final String format) {
        this.url = url;
        this.checksum = checksum;
        this.format = format;
    }

    public String getUrl() {
        return this.url;
    }

    public String getChecksum() {
        return this.checksum;
    }

    public String getFormat() {
        return this.format;
    }
}
