package com.cloud.api.command.admin.storage;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseListCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.StorageProviderResponse;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listStorageProviders", group = APICommandGroup.StoragePoolService, description = "Lists storage providers.", responseObject = StorageProviderResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListStorageProvidersCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListStorageProvidersCmd.class.getName());
    private static final String s_name = "liststorageprovidersresponse";

    @Parameter(name = ApiConstants.TYPE, type = CommandType.STRING, description = "the type of storage provider: either primary or image", required = true)
    private String type;

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException, ConcurrentOperationException,
            ResourceAllocationException, NetworkRuleConflictException {
        if (getType() == null) {
            throw new ServerApiException(ApiErrorCode.MALFORMED_PARAMETER_ERROR, "need to specify type: either primary or image");
        }

        final List<StorageProviderResponse> providers = this.dataStoreProviderApiService.getDataStoreProviders(getType());
        final ListResponse<StorageProviderResponse> responses = new ListResponse<>();
        for (final StorageProviderResponse provider : providers) {
            provider.setObjectName("dataStoreProvider");
        }
        responses.setResponses(providers);
        responses.setResponseName(this.getCommandName());
        this.setResponseObject(responses);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    public String getType() {
        return this.type;
    }
}
