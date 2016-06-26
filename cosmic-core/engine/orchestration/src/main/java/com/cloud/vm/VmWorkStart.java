package com.cloud.vm;

import com.cloud.deploy.DataCenterDeployment;
import com.cloud.deploy.DeploymentPlan;
import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.utils.Journal;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.framework.jobs.impl.JobSerializerHelper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class VmWorkStart extends VmWork {
    private static final long serialVersionUID = 9038937399817468894L;

    private static final Logger s_logger = Logger.getLogger(VmWorkStart.class);

    long dcId;
    Long podId;
    Long clusterId;
    Long hostId;
    Long poolId;
    ExcludeList avoids;
    Long physicalNetworkId;

    String reservationId;
    String journalName;
    String planner;

    // use serialization friendly map
    private Map<String, String> rawParams;

    public VmWorkStart(final long userId, final long accountId, final long vmId, final String handlerName) {
        super(userId, accountId, vmId, handlerName);
    }

    public DeploymentPlan getPlan() {

        if (podId != null || clusterId != null || hostId != null || poolId != null || physicalNetworkId != null || avoids != null) {
            // this is ugly, to work with legacy code, we need to re-construct the DeploymentPlan hard-codely
            // this has to be refactored together with migrating legacy code into the new way
            ReservationContext context = null;
            if (reservationId != null) {
                final Journal journal = new Journal.LogJournal("VmWorkStart", s_logger);
                context = new ReservationContextImpl(reservationId, journal,
                        CallContext.current().getCallingUser(),
                        CallContext.current().getCallingAccount());
            }

            final DeploymentPlan plan = new DataCenterDeployment(
                    dcId, podId, clusterId, hostId, poolId, physicalNetworkId,
                    context);
            plan.setAvoids(avoids);
            return plan;
        }

        return null;
    }

    public void setPlan(final DeploymentPlan plan) {
        if (plan != null) {
            dcId = plan.getDataCenterId();
            podId = plan.getPodId();
            clusterId = plan.getClusterId();
            hostId = plan.getHostId();
            poolId = plan.getPoolId();
            physicalNetworkId = plan.getPhysicalNetworkId();
            avoids = plan.getAvoids();

            if (plan.getReservationContext() != null) {
                reservationId = plan.getReservationContext().getReservationId();
            }
        }
    }

    public String getDeploymentPlanner() {
        return this.planner;
    }

    public void setDeploymentPlanner(final String planner) {
        this.planner = planner;
    }

    public Map<String, String> getRawParams() {
        return rawParams;
    }

    public void setRawParams(final Map<String, String> params) {
        rawParams = params;
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
