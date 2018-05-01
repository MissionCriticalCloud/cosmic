package com.cloud.discovery;

import com.cloud.api.BaseResponse;
import com.cloud.api.response.ListResponse;
import com.cloud.legacymodel.user.User;
import com.cloud.utils.component.PluggableService;

public interface ApiDiscoveryService extends PluggableService {
    ListResponse<? extends BaseResponse> listApis(User user, String apiName);
}
