package com.cloud.api.command.user.volume;

import com.cloud.api.APICommand;
import com.cloud.api.AbstractGetUploadParamsCmd;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.DiskOfferingResponse;
import com.cloud.api.response.GetUploadParamsResponse;
import com.cloud.context.CallContext;
import com.cloud.exception.ResourceAllocationException;

import java.net.MalformedURLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "getUploadParamsForVolume", description = "Upload a data disk to the cloudstack cloud.", responseObject = GetUploadParamsResponse.class, since = "4.6.0",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class GetUploadParamsForVolumeCmd extends AbstractGetUploadParamsCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(GetUploadParamsForVolumeCmd.class.getName());

    private static final String s_name = "postuploadvolumeresponse";

    @Parameter(name = ApiConstants.IMAGE_STORE_UUID, type = CommandType.STRING, description = "Image store uuid")
    private String imageStoreUuid;

    @Parameter(name = ApiConstants.DISK_OFFERING_ID, required = false, type = CommandType.UUID, entityType = DiskOfferingResponse.class, description = "the ID of the disk "
            + "offering. This must be a custom sized offering since during upload of volume/template size is unknown.")
    private Long diskOfferingId;

    public String getImageStoreUuid() {
        return imageStoreUuid;
    }

    public Long getDiskOfferingId() {
        return diskOfferingId;
    }

    @Override
    public void execute() throws ServerApiException {

        try {
            final GetUploadParamsResponse response = _volumeService.uploadVolume(this);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } catch (MalformedURLException | ResourceAllocationException e) {
            s_logger.error("exception while uploading volume", e);
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "exception while uploading a volume: " + e.getMessage());
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final Long accountId = _accountService.finalyzeAccountId(getAccountName(), getDomainId(), getProjectId(), true);
        if (accountId == null) {
            return CallContext.current().getCallingAccount().getId();
        }
        return accountId;
    }
}
