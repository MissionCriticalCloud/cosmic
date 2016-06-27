package org.apache.cloudstack.affinity;

import com.cloud.uservm.UserVm;
import org.apache.cloudstack.api.command.user.affinitygroup.CreateAffinityGroupCmd;

import java.util.List;

public interface AffinityGroupService {

    /**
     * Creates an affinity/anti-affinity group for the given account/domain.
     *
     * @param accountName
     * @param projectId
     * @param domainId
     * @param affinityGroupName
     * @param affinityGroupType
     * @param description
     * @return AffinityGroup
     */
    AffinityGroup createAffinityGroup(String accountName, Long projectId, Long domainId, String affinityGroupName, String affinityGroupType, String description);

    AffinityGroup createAffinityGroup(CreateAffinityGroupCmd createAffinityGroupCmd);

    /**
     * Creates an affinity/anti-affinity group.
     *
     * @param affinityGroupId
     * @param accountName
     * @param domainId
     * @param affinityGroupName
     */
    boolean deleteAffinityGroup(Long affinityGroupId, String accountName, Long projectId, Long domainId, String affinityGroupName);

    /**
     * List group types available in deployment
     *
     * @return
     */
    List<String> listAffinityGroupTypes();

    AffinityGroup getAffinityGroup(Long groupId);

    UserVm updateVMAffinityGroups(Long vmId, List<Long> affinityGroupIds);

    boolean isAffinityGroupProcessorAvailable(String affinityGroupType);

    boolean isAdminControlledGroup(AffinityGroup group);

    boolean isAffinityGroupAvailableInDomain(long affinityGroupId, long domainId);
}
