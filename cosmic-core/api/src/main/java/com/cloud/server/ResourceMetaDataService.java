package com.cloud.server;

import com.cloud.server.ResourceTag.ResourceObjectType;
import org.apache.cloudstack.api.ResourceDetail;

import java.util.List;
import java.util.Map;

public interface ResourceMetaDataService {

    /**
     * @param resourceId   TODO
     * @param resourceType
     * @param details
     * @param forDisplay   TODO
     * @return
     */
    boolean addResourceMetaData(String resourceId, ResourceObjectType resourceType, Map<String, String> details, boolean forDisplay);

    /**
     * @param resourceId
     * @param resourceType
     * @param key
     * @return
     */
    public boolean deleteResourceMetaData(String resourceId, ResourceObjectType resourceType, String key);

    ResourceDetail getDetail(long resourceId, ResourceObjectType resourceType, String key);

    /**
     * List by key, value pair
     *
     * @param resourceType
     * @param key
     * @param value
     * @param forDisplay
     * @return
     */
    List<? extends ResourceDetail> getDetails(ResourceObjectType resourceType, String key, String value, Boolean forDisplay);

    Map<String, String> getDetailsMap(long resourceId, ResourceObjectType resourceType, Boolean forDisplay);

    List<? extends ResourceDetail> getDetailsList(long resourceId, ResourceObjectType resourceType, Boolean forDisplay);
}
