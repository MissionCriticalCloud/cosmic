//

//

package com.cloud.agent.api;

public class AttachIsoCommand extends Command {

    private String vmName;
    private String storeUrl;
    private String isoPath;
    private boolean attach;

    protected AttachIsoCommand() {
    }

    public AttachIsoCommand(final String vmName, final String isoPath, final boolean attach) {
        this.vmName = vmName;
        this.isoPath = isoPath;
        this.attach = attach;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }

    public String getVmName() {
        return vmName;
    }

    public String getIsoPath() {
        return isoPath;
    }

    public boolean isAttach() {
        return attach;
    }

    public String getStoreUrl() {
        return storeUrl;
    }

    public void setStoreUrl(final String url) {
        storeUrl = url;
    }
}
