package org.apache.cloudstack.discovery;

import com.cloud.user.User;
import com.cloud.utils.component.PluggableService;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.response.ListResponse;

public interface ApiDiscoveryService extends PluggableService {
    ListResponse<? extends BaseResponse> listApis(User user, String apiName);
}
