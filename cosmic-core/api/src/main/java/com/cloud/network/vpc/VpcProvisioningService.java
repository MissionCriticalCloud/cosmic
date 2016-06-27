package com.cloud.network.vpc;

import com.cloud.utils.Pair;

import java.util.List;
import java.util.Map;

public interface VpcProvisioningService {

    public VpcOffering getVpcOffering(long vpcOfferingId);

    public VpcOffering createVpcOffering(String name, String displayText, List<String> supportedServices,
                                         Map<String, List<String>> serviceProviders,
                                         Map serviceCapabilitystList,
                                         Long serviceOfferingId);

    Pair<List<? extends VpcOffering>, Integer> listVpcOfferings(Long id, String name, String displayText, List<String> supportedServicesStr, Boolean isDefault, String keyword,
                                                                String state, Long startIndex, Long pageSizeVal);

    /**
     * @param offId
     * @return
     */
    public boolean deleteVpcOffering(long offId);

    /**
     * @param vpcOffId
     * @param vpcOfferingName
     * @param displayText
     * @param state
     * @return
     */
    public VpcOffering updateVpcOffering(long vpcOffId, String vpcOfferingName, String displayText, String state);
}
