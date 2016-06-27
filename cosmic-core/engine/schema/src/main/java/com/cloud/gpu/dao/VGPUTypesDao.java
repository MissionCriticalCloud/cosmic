package com.cloud.gpu.dao;

import com.cloud.agent.api.VgpuTypesInfo;
import com.cloud.gpu.VGPUTypesVO;
import com.cloud.utils.db.GenericDao;

import java.util.HashMap;
import java.util.List;

public interface VGPUTypesDao extends GenericDao<VGPUTypesVO, Long> {

    /**
     * List zonewide/podwide/clusterwide GPU card capacities.
     *
     * @param zoneId
     * @param podId
     * @param clusterId
     * @return Custom Query result
     */
    List<VgpuTypesInfo> listGPUCapacities(Long zoneId, Long podId, Long clusterId);

    /**
     * Find VGPU types by group Id
     *
     * @param groupId of the GPU group
     * @return list of VGPUTypesVO
     */
    List<VGPUTypesVO> listByGroupId(long groupId);

    /**
     * Find VGPU type by group Id and VGPU type
     *
     * @param groupId  of the GPU group
     * @param vgpuType name of VGPU type
     * @return VGPUTypesVO
     */
    VGPUTypesVO findByGroupIdVGPUType(long groupId, String vgpuType);

    /**
     * Save the list of enabled VGPU types
     *
     * @param hostId       the host
     * @param groupDetails with enabled VGPU types
     */
    void persist(long hostId, HashMap<String, HashMap<String, VgpuTypesInfo>> groupDetails);
}
