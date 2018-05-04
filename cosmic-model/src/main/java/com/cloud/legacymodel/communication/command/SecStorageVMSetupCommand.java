package com.cloud.legacymodel.communication.command;

public class SecStorageVMSetupCommand extends Command {
    String[] allowedInternalSites = new String[0];
    String[] allowedExternalCidrs = new String[0];
    String copyUserName;
    String copyPassword;

    public SecStorageVMSetupCommand() {
        super();
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }

    public String[] getAllowedInternalSites() {
        return allowedInternalSites;
    }

    public void setAllowedInternalSites(final String[] allowedInternalSites) {
        this.allowedInternalSites = allowedInternalSites;
    }

    public String[] getAllowedExternalCidrs() {
        return allowedExternalCidrs;
    }

    public void setAllowedExternalCidrs(final String[] allowedExternalCidrs) {
        this.allowedExternalCidrs = allowedExternalCidrs;
    }

    public String getCopyUserName() {
        return copyUserName;
    }

    public void setCopyUserName(final String copyUserName) {
        this.copyUserName = copyUserName;
    }

    public String getCopyPassword() {
        return copyPassword;
    }

    public void setCopyPassword(final String copyPassword) {
        this.copyPassword = copyPassword;
    }
}
