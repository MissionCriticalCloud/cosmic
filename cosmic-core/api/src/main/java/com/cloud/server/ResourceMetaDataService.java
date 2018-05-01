package com.cloud.server;

import com.cloud.api.ResourceDetail;
import com.cloud.server.ResourceTag.ResourceObjectType;

import java.util.List;
import java.util.Map;

public interface ResourceMetaDataService {

    boolean addResourceMetaData(String resourceId, ResourceObjectType resourceType, Map<String, String> details, boolean forDisplay);

    boolean deleteResourceMetaData(String resourceId, ResourceObjectType resourceType, String key);

    ResourceDetail getDetail(long resourceId, ResourceObjectType resourceType, String key);

    List<? extends ResourceDetail> getDetails(ResourceObjectType resourceType, String key, String value, Boolean forDisplay);

    Map<String, String> getDetailsMap(long resourceId, ResourceObjectType resourceType, Boolean forDisplay);

    List<? extends ResourceDetail> getDetailsList(long resourceId, ResourceObjectType resourceType, Boolean forDisplay);
}
