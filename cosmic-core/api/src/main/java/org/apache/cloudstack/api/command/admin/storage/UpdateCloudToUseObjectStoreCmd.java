package org.apache.cloudstack.api.command.admin.storage;

import com.cloud.exception.DiscoveryException;
import com.cloud.storage.ImageStore;
import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.ImageStoreResponse;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateCloudToUseObjectStore", description = "Migrate current NFS secondary storages to use object store.", responseObject = ImageStoreResponse.class, since =
        "4.3.0",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateCloudToUseObjectStoreCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateCloudToUseObjectStoreCmd.class.getName());
    private static final String s_name = "updatecloudtouseobjectstoreresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "the name for the image store")
    private String name;

    @Parameter(name = ApiConstants.URL, type = CommandType.STRING, description = "the URL for the image store")
    private String url;

    @Parameter(name = ApiConstants.PROVIDER, type = CommandType.STRING,
            required = true, description = "the image store provider name")
    private String providerName;

    @Parameter(name = ApiConstants.DETAILS, type = CommandType.MAP, description = "the details for the image store. Example: details[0].key=accesskey&details[0]" +
            ".value=s389ddssaa&details[1].key=secretkey&details[1].value=8dshfsss")
    private Map details;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        try {
            final ImageStore result = _storageService.migrateToObjectStore(getName(), getUrl(), getProviderName(), getDetails());
            ImageStoreResponse storeResponse = null;
            if (result != null) {
                storeResponse = _responseGenerator.createImageStoreResponse(result);
                storeResponse.setResponseName(getCommandName());
                storeResponse.setObjectName("imagestore");
                setResponseObject(storeResponse);
            } else {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to add secondary storage");
            }
        } catch (final DiscoveryException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, ex.getMessage());
        }
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getProviderName() {
        return providerName;
    }

    public Map<String, String> getDetails() {
        Map<String, String> detailsMap = null;
        if (details != null && !details.isEmpty()) {
            detailsMap = new HashMap<>();
            final Collection<?> props = details.values();
            final Iterator<?> iter = props.iterator();
            while (iter.hasNext()) {
                final HashMap<String, String> detail = (HashMap<String, String>) iter.next();
                final String key = detail.get("key");
                final String value = detail.get("value");
                detailsMap.put(key, value);
            }
        }
        return detailsMap;
    }

    public void setDetails(final Map<String, String> details) {
        this.details = details;
    }

    public void setProviderName(final String providerName) {
        this.providerName = providerName;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public void setUrl(final String url) {
        this.url = url;
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
