package com.cloud.api.command.admin.host;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.ImageStoreResponse;
import com.cloud.api.response.ZoneResponse;
import com.cloud.exception.DiscoveryException;
import com.cloud.legacymodel.user.Account;
import com.cloud.storage.ImageStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "addSecondaryStorage", group = APICommandGroup.HostService, description = "Adds secondary storage.", responseObject = ImageStoreResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class AddSecondaryStorageCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(AddSecondaryStorageCmd.class.getName());
    private static final String s_name = "addsecondarystorageresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.URL, type = CommandType.STRING, required = true, description = "the URL for the secondary storage")
    protected String url;

    @Parameter(name = ApiConstants.ZONE_ID, type = CommandType.UUID, entityType = ZoneResponse.class, description = "the Zone ID for the secondary storage")
    protected Long zoneId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        try {
            final ImageStore result = _storageService.discoverImageStore(null, getUrl(), "NFS", getZoneId(), null);
            ImageStoreResponse storeResponse = null;
            if (result != null) {
                storeResponse = _responseGenerator.createImageStoreResponse(result);
                storeResponse.setResponseName(getCommandName());
                storeResponse.setObjectName("secondarystorage");
                setResponseObject(storeResponse);
            } else {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to add secondary storage");
            }
        } catch (final DiscoveryException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, ex.getMessage());
        }
    }

    public String getUrl() {
        return url;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public Long getZoneId() {
        return zoneId;
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }
}
