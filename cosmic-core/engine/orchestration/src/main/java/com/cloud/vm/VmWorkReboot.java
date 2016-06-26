package com.cloud.vm;

import org.apache.cloudstack.framework.jobs.impl.JobSerializerHelper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class VmWorkReboot extends VmWork {
    private static final long serialVersionUID = 195907627459759933L;

    // use serialization friendly map
    private Map<String, String> rawParams;

    public VmWorkReboot(final long userId, final long accountId, final long vmId, final String handlerName, final Map<VirtualMachineProfile.Param, Object> params) {
        super(userId, accountId, vmId, handlerName);

        setParams(params);
    }

    public Map<VirtualMachineProfile.Param, Object> getParams() {
        final Map<VirtualMachineProfile.Param, Object> map = new HashMap<>();

        if (rawParams != null) {
            for (final Map.Entry<String, String> entry : rawParams.entrySet()) {
                final VirtualMachineProfile.Param key = new VirtualMachineProfile.Param(entry.getKey());
                final Object val = JobSerializerHelper.fromObjectSerializedString(entry.getValue());
                map.put(key, val);
            }
        }

        return map;
    }

    public void setParams(final Map<VirtualMachineProfile.Param, Object> params) {
        if (params != null) {
            rawParams = new HashMap<>();
            for (final Map.Entry<VirtualMachineProfile.Param, Object> entry : params.entrySet()) {
                rawParams.put(entry.getKey().getName(), JobSerializerHelper.toObjectSerializedString(
                        entry.getValue() instanceof Serializable ? (Serializable) entry.getValue() : entry.getValue().toString()));
            }
        }
    }
}
