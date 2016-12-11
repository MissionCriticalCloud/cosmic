package com.cloud.api;

import java.util.Map;

public interface IBaseListTaggedResourcesCmd extends IBaseListProjectAndAccountResourcesCmd {
    Map<String, String> getTags();
}
