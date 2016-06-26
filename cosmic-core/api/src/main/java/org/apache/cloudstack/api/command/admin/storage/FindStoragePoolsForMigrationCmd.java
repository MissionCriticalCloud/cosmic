package org.apache.cloudstack.api.command.admin.storage;

import com.cloud.storage.StoragePool;
import com.cloud.utils.Pair;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiCommandJobType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.StoragePoolResponse;
import org.apache.cloudstack.api.response.VolumeResponse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "findStoragePoolsForMigration", description = "Lists storage pools available for migration of a volume.", responseObject = StoragePoolResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class FindStoragePoolsForMigrationCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(FindStoragePoolsForMigrationCmd.class.getName());

    private static final String s_name = "findstoragepoolsformigrationresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = VolumeResponse.class, required = true, description = "the ID of the volume")
    private Long id;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.StoragePool;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final Pair<List<? extends StoragePool>, List<? extends StoragePool>> pools = _mgr.listStoragePoolsForMigrationOfVolume(getId());
        final ListResponse<StoragePoolResponse> response = new ListResponse<>();
        final List<StoragePoolResponse> poolResponses = new ArrayList<>();

        final List<? extends StoragePool> allPools = pools.first();
        final List<? extends StoragePool> suitablePoolList = pools.second();
        for (final StoragePool pool : allPools) {
            final StoragePoolResponse poolResponse = _responseGenerator.createStoragePoolForMigrationResponse(pool);
            Boolean suitableForMigration = false;
            for (final StoragePool suitablePool : suitablePoolList) {
                if (suitablePool.getId() == pool.getId()) {
                    suitableForMigration = true;
                    break;
                }
            }
            poolResponse.setSuitableForMigration(suitableForMigration);
            poolResponse.setObjectName("storagepool");
            poolResponses.add(poolResponse);
        }

        response.setResponses(poolResponses);
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    public Long getId() {
        return id;
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
