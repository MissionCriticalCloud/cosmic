package com.cloud.gpu.dao;

import com.cloud.agent.api.VgpuTypesInfo;
import com.cloud.gpu.HostGpuGroupsVO;
import com.cloud.gpu.VGPUTypesVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.exception.CloudRuntimeException;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VGPUTypesDaoImpl extends GenericDaoBase<VGPUTypesVO, Long> implements VGPUTypesDao {
    private static final Logger s_logger = LoggerFactory.getLogger(VGPUTypesDaoImpl.class);
    private static final String LIST_ZONE_POD_CLUSTER_WIDE_GPU_CAPACITIES =
            "SELECT host_gpu_groups.group_name, vgpu_type, max_vgpu_per_pgpu, SUM(remaining_capacity) AS remaining_capacity, SUM(max_capacity) AS total_capacity FROM" +
                    " `cloud`.`vgpu_types` INNER JOIN `cloud`.`host_gpu_groups` ON vgpu_types.gpu_group_id = host_gpu_groups.id INNER JOIN `cloud`.`host`" +
                    " ON host_gpu_groups.host_id = host.id WHERE host.type =  'Routing' AND host.data_center_id = ?";
    private final SearchBuilder<VGPUTypesVO> _searchByGroupId;
    private final SearchBuilder<VGPUTypesVO> _searchByGroupIdVGPUType;
    @Inject
    protected HostGpuGroupsDao _hostGpuGroupsDao;

    public VGPUTypesDaoImpl() {

        _searchByGroupId = createSearchBuilder();
        _searchByGroupId.and("groupId", _searchByGroupId.entity().getGpuGroupId(), SearchCriteria.Op.EQ);
        _searchByGroupId.done();

        _searchByGroupIdVGPUType = createSearchBuilder();
        _searchByGroupIdVGPUType.and("groupId", _searchByGroupIdVGPUType.entity().getGpuGroupId(), SearchCriteria.Op.EQ);
        _searchByGroupIdVGPUType.and("vgpuType", _searchByGroupIdVGPUType.entity().getVgpuType(), SearchCriteria.Op.EQ);
        _searchByGroupIdVGPUType.done();
    }

    @Override
    public List<VgpuTypesInfo> listGPUCapacities(final Long dcId, final Long podId, final Long clusterId) {
        final StringBuilder finalQuery = new StringBuilder();
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        PreparedStatement pstmt = null;
        final List<Long> resourceIdList = new ArrayList<>();
        final ArrayList<VgpuTypesInfo> result = new ArrayList<>();

        resourceIdList.add(dcId);
        finalQuery.append(LIST_ZONE_POD_CLUSTER_WIDE_GPU_CAPACITIES);

        if (podId != null) {
            finalQuery.append(" AND host.pod_id = ?");
            resourceIdList.add(podId);
        }

        if (clusterId != null) {
            finalQuery.append(" AND host.cluster_id = ?");
            resourceIdList.add(clusterId);
        }
        finalQuery.append(" GROUP BY host_gpu_groups.group_name, vgpu_type");

        try {
            pstmt = txn.prepareAutoCloseStatement(finalQuery.toString());
            for (int i = 0; i < resourceIdList.size(); i++) {
                pstmt.setLong(1 + i, resourceIdList.get(i));
            }
            final ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {

                final VgpuTypesInfo gpuCapacity = new VgpuTypesInfo(rs.getString(1), rs.getString(2), null, null, null, null, rs.getLong(3), rs.getLong(4), rs.getLong(5));
                result.add(gpuCapacity);
            }
            return result;
        } catch (final SQLException e) {
            throw new CloudRuntimeException("DB Exception on: " + finalQuery, e);
        } catch (final Throwable e) {
            throw new CloudRuntimeException("Caught: " + finalQuery, e);
        }
    }

    @Override
    public List<VGPUTypesVO> listByGroupId(final long groupId) {
        final SearchCriteria<VGPUTypesVO> sc = _searchByGroupId.create();
        sc.setParameters("groupId", groupId);
        return listBy(sc);
    }

    @Override
    public VGPUTypesVO findByGroupIdVGPUType(final long groupId, final String vgpuType) {
        final SearchCriteria<VGPUTypesVO> sc = _searchByGroupIdVGPUType.create();
        sc.setParameters("groupId", groupId);
        sc.setParameters("vgpuType", vgpuType);
        return findOneBy(sc);
    }

    @Override
    public void persist(final long hostId, final HashMap<String, HashMap<String, VgpuTypesInfo>> groupDetails) {
        final Iterator<Entry<String, HashMap<String, VgpuTypesInfo>>> it1 = groupDetails.entrySet().iterator();
        while (it1.hasNext()) {
            final Entry<String, HashMap<String, VgpuTypesInfo>> entry = it1.next();
            final HostGpuGroupsVO gpuGroup = _hostGpuGroupsDao.findByHostIdGroupName(hostId, entry.getKey());
            final HashMap<String, VgpuTypesInfo> values = entry.getValue();
            final Iterator<Entry<String, VgpuTypesInfo>> it2 = values.entrySet().iterator();
            while (it2.hasNext()) {
                final Entry<String, VgpuTypesInfo> record = it2.next();
                final VgpuTypesInfo details = record.getValue();
                VGPUTypesVO vgpuType = null;
                if ((vgpuType = findByGroupIdVGPUType(gpuGroup.getId(), record.getKey())) == null) {
                    persist(new VGPUTypesVO(gpuGroup.getId(), record.getKey(), details.getVideoRam(), details.getMaxHeads(), details.getMaxResolutionX(),
                            details.getMaxResolutionY(), details.getMaxVpuPerGpu(), details.getRemainingCapacity(), details.getMaxCapacity()));
                } else {
                    vgpuType.setRemainingCapacity(details.getRemainingCapacity());
                    vgpuType.setMaxCapacity(details.getMaxCapacity());
                    update(vgpuType.getId(), vgpuType);
                }
            }
        }
    }
}
