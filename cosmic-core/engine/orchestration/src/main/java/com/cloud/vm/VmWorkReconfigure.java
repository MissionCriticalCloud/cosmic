package com.cloud.vm;

public class VmWorkReconfigure extends VmWork {
    private static final long serialVersionUID = -4517030323758086615L;

    Long newServiceOfferingId;
    boolean sameHost;

    public VmWorkReconfigure(final long userId, final long accountId, final long vmId, final String handlerName,
                             final Long newServiceOfferingId, final boolean sameHost) {

        super(userId, accountId, vmId, handlerName);

        this.newServiceOfferingId = newServiceOfferingId;
        this.sameHost = sameHost;
    }

    public Long getNewServiceOfferingId() {
        return newServiceOfferingId;
    }

    public boolean isSameHost() {
        return sameHost;
    }
}
