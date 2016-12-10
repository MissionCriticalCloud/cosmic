package com.cloud.discovery;

import com.cloud.api.response.ListResponse;
import com.cloud.user.User;
import com.cloud.utils.component.PluggableService;
import org.apache.cloudstack.api.BaseResponse;

public interface ApiDiscoveryService extends PluggableService {
    ListResponse<? extends BaseResponse> listApis(User user, String apiName);
}
