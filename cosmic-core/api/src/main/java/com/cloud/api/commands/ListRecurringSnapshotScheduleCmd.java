package com.cloud.api.commands;

import com.cloud.storage.snapshot.SnapshotSchedule;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.SnapshotScheduleResponse;

import java.util.ArrayList;
import java.util.List;

//@APICommand(description="Lists recurring snapshot schedule", responseObject=SnapshotScheduleResponse.class)
public class ListRecurringSnapshotScheduleCmd extends BaseListCmd {
    private static final String s_name = "listrecurringsnapshotscheduleresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.SNAPSHOT_POLICY_ID, type = CommandType.LONG, description = "lists recurring snapshots by snapshot policy ID")
    private Long snapshotPolicyId;

    @Parameter(name = ApiConstants.VOLUME_ID, type = CommandType.LONG, required = true, description = "list recurring snapshots by volume ID")
    private Long volumeId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getSnapshotPolicyId() {
        return snapshotPolicyId;
    }

    public Long getVolumeId() {
        return volumeId;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final List<? extends SnapshotSchedule> snapshotSchedules = _snapshotService.findRecurringSnapshotSchedule(this);
        final ListResponse<SnapshotScheduleResponse> response = new ListResponse<>();
        final List<SnapshotScheduleResponse> snapshotScheduleResponses = new ArrayList<>();
        for (final SnapshotSchedule snapshotSchedule : snapshotSchedules) {
            final SnapshotScheduleResponse snapSchedResponse = _responseGenerator.createSnapshotScheduleResponse(snapshotSchedule);
            snapshotScheduleResponses.add(snapSchedResponse);
        }

        response.setResponses(snapshotScheduleResponses);
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
