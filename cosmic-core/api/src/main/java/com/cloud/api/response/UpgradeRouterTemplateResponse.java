package com.cloud.api.response;

import com.cloud.api.BaseResponse;
import com.cloud.api.EntityReference;
import com.cloud.jobs.JobInfo;

@EntityReference(value = JobInfo.class)
public class UpgradeRouterTemplateResponse extends BaseResponse {
}
