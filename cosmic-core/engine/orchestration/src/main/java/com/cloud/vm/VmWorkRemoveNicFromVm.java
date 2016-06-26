package com.cloud.vm;

public class VmWorkRemoveNicFromVm extends VmWork {
    private static final long serialVersionUID = -4265657031064437923L;

    Long nicId;

    public VmWorkRemoveNicFromVm(final long userId, final long accountId, final long vmId, final String handlerName, final Long nicId) {
        super(userId, accountId, vmId, handlerName);

        this.nicId = nicId;
    }

    public Long getNicId() {
        return nicId;
    }
}
