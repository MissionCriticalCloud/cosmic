package com.cloud.deploy;

import com.cloud.vm.VirtualMachineProfile;

import javax.naming.ConfigurationException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SkipHeuristicsPlanner extends FirstFitPlanner implements HAPlanner {
    private static final Logger s_logger = LoggerFactory.getLogger(SkipHeuristicsPlanner.class);

    /**
     * This method should remove the clusters crossing capacity threshold
     * to avoid further vm allocation on it.
     * <p>
     * In case of HA, we shouldn't consider this threshold as we have reserved the capacity for such emergencies.
     */
    @Override
    protected void removeClustersCrossingThreshold(final List<Long> clusterListForVmAllocation, final ExcludeList avoid,
                                                   final VirtualMachineProfile vmProfile, final DeploymentPlan plan) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Deploying vm during HA process, so skipping disable threshold check");
        }
        return;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);

        return true;
    }

    @Override
    public boolean canHandle(final VirtualMachineProfile vm, final DeploymentPlan plan, final ExcludeList avoid) {
        return true;
    }
}
