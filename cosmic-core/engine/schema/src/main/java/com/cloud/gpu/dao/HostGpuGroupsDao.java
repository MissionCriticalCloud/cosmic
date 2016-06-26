package com.cloud.gpu.dao;

import com.cloud.gpu.HostGpuGroupsVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface HostGpuGroupsDao extends GenericDao<HostGpuGroupsVO, Long> {

    /**
     * Find host device by hostId and groupName
     *
     * @param hostId    the host
     * @param groupName GPU group
     * @return HostGpuGroupsVO
     */
    HostGpuGroupsVO findByHostIdGroupName(long hostId, String groupName);

    /**
     * List all the host Ids, that are GPU enabled.
     *
     * @return list of hostIds
     */
    List<Long> listHostIds();

    /**
     * Return a list by hostId.
     *
     * @param hostId the host
     * @return HostGpuGroupsVO
     */
    List<HostGpuGroupsVO> listByHostId(long hostId);

    /**
     * Delete entries by hostId.
     *
     * @param hostId the host
     */
    void deleteGpuEntries(long hostId);

    /**
     * Save the list of GPU groups belonging to a host
     *
     * @param hostId    the host
     * @param gpuGroups the list of GPU groups to save
     */
    void persist(long hostId, List<String> gpuGroups);
}
