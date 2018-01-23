package com.cloud.api.command.admin.storage;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.ImageStoreResponse;
import com.cloud.api.response.ZoneResponse;
import com.cloud.storage.ImageStore;
import com.cloud.user.Account;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createSecondaryStagingStore", group = APICommandGroup.ImageStoreService, description = "create secondary staging store.", responseObject = ImageStoreResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CreateSecondaryStagingStoreCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(AddImageStoreCmd.class.getName());
    private static final String s_name = "createsecondarystagingstoreresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.URL, type = CommandType.STRING, required = true, length = 2048, description = "the URL for the staging store")
    private String url;

    @Parameter(name = ApiConstants.ZONE_ID, type = CommandType.UUID, entityType = ZoneResponse.class, description = "the Zone ID for the staging store")
    private Long zoneId;

    @Parameter(name = ApiConstants.DETAILS, type = CommandType.MAP, description = "the details for the staging store")
    private Map<String, ? extends Map<String, String>> details;

    @Parameter(name = ApiConstants.SCOPE, type = CommandType.STRING, required = false, description = "the scope of the staging store: zone only for now")
    private String scope;

    @Parameter(name = ApiConstants.PROVIDER, type = CommandType.STRING, required = false, description = "the staging store provider name")
    private String providerName;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getUrl() {
        return url;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public Map<String, String> getDetails() {
        Map<String, String> detailsMap = null;
        if (details != null && !details.isEmpty()) {
            detailsMap = new HashMap<>();
            for (final Map<String, String> detail : details.values()) {
                detailsMap.put(detail.get("key"), detail.get("value"));
            }
        }
        return detailsMap;
    }

    public String getScope() {
        return this.scope;
    }

    public String getProviderName() {
        return this.providerName;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        try {
            final ImageStore result = _storageService.createSecondaryStagingStore(this);
            ImageStoreResponse storeResponse = null;
            if (result != null) {
                storeResponse = _responseGenerator.createImageStoreResponse(result);
                storeResponse.setResponseName(getCommandName());
                storeResponse.setObjectName("secondarystorage");
                this.setResponseObject(storeResponse);
            } else {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to add secondary storage");
            }
        } catch (final Exception ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, ex.getMessage());
        }
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
