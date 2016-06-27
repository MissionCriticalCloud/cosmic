package com.cloud.network.as;

import com.cloud.exception.ResourceInUseException;
import com.cloud.exception.ResourceUnavailableException;
import org.apache.cloudstack.api.command.admin.autoscale.CreateCounterCmd;
import org.apache.cloudstack.api.command.user.autoscale.CreateAutoScalePolicyCmd;
import org.apache.cloudstack.api.command.user.autoscale.CreateAutoScaleVmGroupCmd;
import org.apache.cloudstack.api.command.user.autoscale.CreateAutoScaleVmProfileCmd;
import org.apache.cloudstack.api.command.user.autoscale.CreateConditionCmd;
import org.apache.cloudstack.api.command.user.autoscale.ListAutoScalePoliciesCmd;
import org.apache.cloudstack.api.command.user.autoscale.ListAutoScaleVmGroupsCmd;
import org.apache.cloudstack.api.command.user.autoscale.ListAutoScaleVmProfilesCmd;
import org.apache.cloudstack.api.command.user.autoscale.ListConditionsCmd;
import org.apache.cloudstack.api.command.user.autoscale.ListCountersCmd;
import org.apache.cloudstack.api.command.user.autoscale.UpdateAutoScalePolicyCmd;
import org.apache.cloudstack.api.command.user.autoscale.UpdateAutoScaleVmGroupCmd;
import org.apache.cloudstack.api.command.user.autoscale.UpdateAutoScaleVmProfileCmd;

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
