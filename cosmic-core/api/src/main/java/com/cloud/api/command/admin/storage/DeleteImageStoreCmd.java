package com.cloud.api.command.admin.storage;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.ImageStoreResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.user.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deleteImageStore", group = APICommandGroup.ImageStoreService, description = "Deletes an image store or Secondary Storage.", responseObject = SuccessResponse.class, since = "4.2.0",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DeleteImageStoreCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeleteImageStoreCmd.class.getName());

    private static final String s_name = "deleteimagestoreresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = ImageStoreResponse.class, required = true, description = "The image store ID or Secondary Storage ID.")
    private Long id;

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    @Override
    public void execute() {
        final boolean result = _storageService.deleteImageStore(this);
        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to delete image store");
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
