package org.apache.cloudstack.affinity;

import com.cloud.deploy.DeployDestination;
import com.cloud.deploy.DeploymentPlan;
import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.exception.AffinityConflictException;
import com.cloud.utils.component.AdapterBase;
import com.cloud.vm.VirtualMachineProfile;

public class AffinityProcessorBase extends AdapterBase implements AffinityGroupProcessor {

    protected String _type;

    @Override
    public void process(final VirtualMachineProfile vm, final DeploymentPlan plan, final ExcludeList avoid) throws AffinityConflictException {

    }

    @Override
    public String getType() {
        return _type;
    }

    public void setType(final String type) {
        _type = type;
    }

    @Override
    public boolean check(final VirtualMachineProfile vm, final DeployDestination plannedDestination) throws AffinityConflictException {
        return true;
    }

    @Override
    public boolean isAdminControlledGroup() {
        return false;
    }

    @Override
    public boolean canBeSharedDomainWide() {
        return false;
    }

    @Override
    public boolean subDomainAccess() {
        return false;
    }

    @Override
    public void handleDeleteGroup(final AffinityGroup group) {
        // TODO Auto-generated method stub
        return;
    }
}
