package org.apache.cloudstack.api;

import java.util.Map;

public interface IBaseListTaggedResourcesCmd extends IBaseListProjectAndAccountResourcesCmd {
    Map<String, String> getTags();
}
