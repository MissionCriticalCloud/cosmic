package com.cloud.api.command.admin.swift;

import com.cloud.api.APICommand;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.ImageStoreResponse;
import com.cloud.exception.DiscoveryException;
import com.cloud.storage.ImageStore;
import com.cloud.user.Account;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "addSwift", description = "Adds Swift.", responseObject = ImageStoreResponse.class, since = "3.0.0",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class AddSwiftCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(AddSwiftCmd.class.getName());
    private static final String s_name = "addswiftresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.URL, type = CommandType.STRING, required = true, description = "the URL for swift")
    private String url;

    @Parameter(name = ApiConstants.ACCOUNT, type = CommandType.STRING, description = "the account for swift")
    private String account;

    @Parameter(name = ApiConstants.USERNAME, type = CommandType.STRING, description = "the username for swift")
    private String username;

    @Parameter(name = ApiConstants.KEY, type = CommandType.STRING, description = " key for the user for swift")
    private String key;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final Map<String, String> dm = new HashMap<>();
        dm.put(ApiConstants.ACCOUNT, getAccount());
        dm.put(ApiConstants.USERNAME, getUsername());
        dm.put(ApiConstants.KEY, getKey());

        try {
            final ImageStore result = _storageService.discoverImageStore(null, getUrl(), "Swift", null, dm);
            ImageStoreResponse storeResponse = null;
            if (result != null) {
                storeResponse = _responseGenerator.createImageStoreResponse(result);
                storeResponse.setResponseName(getCommandName());
                storeResponse.setObjectName("secondarystorage");
                setResponseObject(storeResponse);
            } else {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to add Swift secondary storage");
            }
        } catch (final DiscoveryException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, ex.getMessage());
        }
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public String getAccount() {
        return account;
    }

    public String getUsername() {
        return username;
    }

    public String getKey() {
        return key;
    }

    public String getUrl() {
        return url;
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
