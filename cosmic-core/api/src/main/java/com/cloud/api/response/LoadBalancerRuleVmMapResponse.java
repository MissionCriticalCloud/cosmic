package com.cloud.api.response;

import com.cloud.api.BaseResponse;
import com.cloud.api.EntityReference;
import com.cloud.legacymodel.network.VirtualRouter;
import com.cloud.legacymodel.vm.VirtualMachine;
import com.cloud.serializer.Param;
import com.cloud.uservm.UserVm;

import java.util.List;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = {VirtualMachine.class, UserVm.class, VirtualRouter.class})
public class LoadBalancerRuleVmMapResponse extends BaseResponse {

    @SerializedName("loadbalancerruleinstance")
    @Param(description = "the user vm set for lb rule")
    private UserVmResponse UserVmResponse;

    @SerializedName("lbvmipaddresses")
    @Param(description = "IP addresses of the vm set of lb rule")
    private List<String> ipAddr;

    public void setIpAddr(final List<String> ipAddr) {
        this.ipAddr = ipAddr;
    }

    public void setUserVmResponse(final UserVmResponse userVmResponse) {
        this.UserVmResponse = userVmResponse;
    }
}
