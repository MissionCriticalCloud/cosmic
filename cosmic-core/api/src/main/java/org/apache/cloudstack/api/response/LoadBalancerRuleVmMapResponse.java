package org.apache.cloudstack.api.response;

import com.cloud.network.router.VirtualRouter;
import com.cloud.serializer.Param;
import com.cloud.uservm.UserVm;
import com.cloud.vm.VirtualMachine;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

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
