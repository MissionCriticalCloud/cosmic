package com.cloud.network.as;

import com.cloud.api.command.admin.autoscale.CreateCounterCmd;
import com.cloud.api.command.user.autoscale.CreateAutoScalePolicyCmd;
import com.cloud.api.command.user.autoscale.CreateAutoScaleVmGroupCmd;
import com.cloud.api.command.user.autoscale.CreateAutoScaleVmProfileCmd;
import com.cloud.api.command.user.autoscale.CreateConditionCmd;
import com.cloud.api.command.user.autoscale.ListAutoScalePoliciesCmd;
import com.cloud.api.command.user.autoscale.ListAutoScaleVmGroupsCmd;
import com.cloud.api.command.user.autoscale.ListAutoScaleVmProfilesCmd;
import com.cloud.api.command.user.autoscale.ListConditionsCmd;
import com.cloud.api.command.user.autoscale.ListCountersCmd;
import com.cloud.api.command.user.autoscale.UpdateAutoScalePolicyCmd;
import com.cloud.api.command.user.autoscale.UpdateAutoScaleVmGroupCmd;
import com.cloud.api.command.user.autoscale.UpdateAutoScaleVmProfileCmd;
import com.cloud.exception.ResourceInUseException;
import com.cloud.exception.ResourceUnavailableException;

import java.util.List;

public interface AutoScaleService {

    public AutoScalePolicy createAutoScalePolicy(CreateAutoScalePolicyCmd createAutoScalePolicyCmd);

    public boolean deleteAutoScalePolicy(long autoScalePolicyId);

    List<? extends AutoScalePolicy> listAutoScalePolicies(ListAutoScalePoliciesCmd cmd);

    AutoScalePolicy updateAutoScalePolicy(UpdateAutoScalePolicyCmd cmd);

    AutoScaleVmProfile createAutoScaleVmProfile(CreateAutoScaleVmProfileCmd cmd);

    boolean deleteAutoScaleVmProfile(long profileId);

    List<? extends AutoScaleVmProfile> listAutoScaleVmProfiles(ListAutoScaleVmProfilesCmd listAutoScaleVmProfilesCmd);

    AutoScaleVmProfile updateAutoScaleVmProfile(UpdateAutoScaleVmProfileCmd cmd);

    AutoScaleVmGroup createAutoScaleVmGroup(CreateAutoScaleVmGroupCmd cmd);

    boolean configureAutoScaleVmGroup(CreateAutoScaleVmGroupCmd cmd) throws ResourceUnavailableException;

    boolean deleteAutoScaleVmGroup(long vmGroupId);

    AutoScaleVmGroup updateAutoScaleVmGroup(UpdateAutoScaleVmGroupCmd cmd);

    AutoScaleVmGroup enableAutoScaleVmGroup(Long id);

    AutoScaleVmGroup disableAutoScaleVmGroup(Long id);

    List<? extends AutoScaleVmGroup> listAutoScaleVmGroups(ListAutoScaleVmGroupsCmd listAutoScaleVmGroupsCmd);

    Counter createCounter(CreateCounterCmd cmd);

    boolean deleteCounter(long counterId) throws ResourceInUseException;

    List<? extends Counter> listCounters(ListCountersCmd cmd);

    Condition createCondition(CreateConditionCmd cmd);

    List<? extends Condition> listConditions(ListConditionsCmd cmd);

    boolean deleteCondition(long conditionId) throws ResourceInUseException;
}
