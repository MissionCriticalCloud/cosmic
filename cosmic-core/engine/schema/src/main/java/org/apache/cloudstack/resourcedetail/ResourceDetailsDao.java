package org.apache.cloudstack.resourcedetail;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.api.ResourceDetail;

import java.util.List;
import java.util.Map;

public interface ResourceDetailsDao<R extends ResourceDetail> extends GenericDao<R, Long> {
    /**
     * Finds detail by resourceId and key
     *
     * @param resourceId
     * @param name
     * @return
     */
    public R findDetail(long resourceId, String name);

    /**
     * Find details by key,value pair
     *
     * @param key
     * @param value
     * @param display
     * @return
     */
    public List<R> findDetails(String key, String value, Boolean display);

    /**
     * Removes all details for the resource specified
     *
     * @param resourceId
     */
    public void removeDetails(long resourceId);

    /**
     * Removes detail having resourceId and key specified (unique combination)
     *
     * @param resourceId
     * @param key
     */
    public void removeDetail(long resourceId, String key);

    /**
     * Lists all details for the resourceId
     *
     * @param resourceId
     * @return list of details each implementing ResourceDetail interface
     */
    public List<R> listDetails(long resourceId);

    /**
     * List details for resourceId having display field = forDisplay value passed in
     *
     * @param resourceId
     * @param forDisplay
     * @return
     */
    public List<R> listDetails(long resourceId, boolean forDisplay);

    public Map<String, String> listDetailsKeyPairs(long resourceId);

    public Map<String, String> listDetailsKeyPairs(long resourceId, boolean forDisplay);

    public void saveDetails(List<R> details);

    public void addDetail(long resourceId, String key, String value, boolean display);
}
