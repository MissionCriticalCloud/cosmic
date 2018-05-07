package com.cloud.legacymodel.network.rules;

public class VmPassword extends ConfigBase {
    private String ipAddress;
    private String password;

    public VmPassword() {
        super(ConfigBase.VM_PASSWORD);
    }

    public VmPassword(final String ipAddress, final String password) {
        super(ConfigBase.VM_PASSWORD);
        this.ipAddress = ipAddress;
        this.password = password;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }
}
