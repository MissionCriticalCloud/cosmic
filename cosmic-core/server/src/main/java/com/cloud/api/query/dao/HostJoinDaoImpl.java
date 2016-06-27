package com.cloud.api.query.dao;

import com.cloud.api.ApiDBUtils;
import com.cloud.api.query.vo.HostJoinVO;
import com.cloud.gpu.HostGpuGroupsVO;
import com.cloud.gpu.VGPUTypesVO;
import com.cloud.host.Host;
import com.cloud.host.HostStats;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor;
import com.cloud.storage.StorageStats;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import org.apache.cloudstack.api.ApiConstants.HostDetails;
import org.apache.cloudstack.api.response.GpuResponse;
import org.apache.cloudstack.api.response.HostForMigrationResponse;
import org.apache.cloudstack.api.response.HostResponse;
import org.apache.cloudstack.api.response.VgpuResponse;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;

import javax.inject.Inject;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HostJoinDaoImpl extends GenericDaoBase<HostJoinVO, Long> implements HostJoinDao {
    public static final Logger s_logger = LoggerFactory.getLogger(HostJoinDaoImpl.class);
    private final SearchBuilder<HostJoinVO> hostSearch;
    private final SearchBuilder<HostJoinVO> hostIdSearch;
    @Inject
    private ConfigurationDao _configDao;
    @Inject
    private HostDao hostDao;

    protected HostJoinDaoImpl() {

        hostSearch = createSearchBuilder();
        hostSearch.and("idIN", hostSearch.entity().getId(), SearchCriteria.Op.IN);
        hostSearch.done();

        hostIdSearch = createSearchBuilder();
        hostIdSearch.and("id", hostIdSearch.entity().getId(), SearchCriteria.Op.EQ);
        hostIdSearch.done();

        this._count = "select count(distinct id) from host_view WHERE ";
    }

    @Override
    public HostResponse newHostResponse(final HostJoinVO host, final EnumSet<HostDetails> details) {
        final HostResponse hostResponse = new HostResponse();
        hostResponse.setId(host.getUuid());
        hostResponse.setCapabilities(host.getCapabilities());
        hostResponse.setClusterId(host.getClusterUuid());
        hostResponse.setCpuSockets(host.getCpuSockets());
        hostResponse.setCpuNumber(host.getCpus());
        hostResponse.setZoneId(host.getZoneUuid());
        hostResponse.setDisconnectedOn(host.getDisconnectedOn());
        hostResponse.setHypervisor(host.getHypervisorType());
        hostResponse.setHostType(host.getType());
        hostResponse.setLastPinged(new Date(host.getLastPinged()));
        hostResponse.setManagementServerId(host.getManagementServerId());
        hostResponse.setName(host.getName());
        hostResponse.setPodId(host.getPodUuid());
        hostResponse.setRemoved(host.getRemoved());
        hostResponse.setCpuSpeed(host.getSpeed());
        hostResponse.setState(host.getStatus());
        hostResponse.setIpAddress(host.getPrivateIpAddress());
        hostResponse.setVersion(host.getVersion());
        hostResponse.setCreated(host.getCreated());

        final List<HostGpuGroupsVO> gpuGroups = ApiDBUtils.getGpuGroups(host.getId());
        if (gpuGroups != null && !gpuGroups.isEmpty()) {
            final List<GpuResponse> gpus = new ArrayList<>();
            for (final HostGpuGroupsVO entry : gpuGroups) {
                final GpuResponse gpuResponse = new GpuResponse();
                gpuResponse.setGpuGroupName(entry.getGroupName());
                final List<VGPUTypesVO> vgpuTypes = ApiDBUtils.getVgpus(entry.getId());
                if (vgpuTypes != null && !vgpuTypes.isEmpty()) {
                    final List<VgpuResponse> vgpus = new ArrayList<>();
                    for (final VGPUTypesVO vgpuType : vgpuTypes) {
                        final VgpuResponse vgpuResponse = new VgpuResponse();
                        vgpuResponse.setName(vgpuType.getVgpuType());
                        vgpuResponse.setVideoRam(vgpuType.getVideoRam());
                        vgpuResponse.setMaxHeads(vgpuType.getMaxHeads());
                        vgpuResponse.setMaxResolutionX(vgpuType.getMaxResolutionX());
                        vgpuResponse.setMaxResolutionY(vgpuType.getMaxResolutionY());
                        vgpuResponse.setMaxVgpuPerPgpu(vgpuType.getMaxVgpuPerPgpu());
                        vgpuResponse.setRemainingCapacity(vgpuType.getRemainingCapacity());
                        vgpuResponse.setmaxCapacity(vgpuType.getMaxCapacity());
                        vgpus.add(vgpuResponse);
                    }
                    gpuResponse.setVgpu(vgpus);
                }
                gpus.add(gpuResponse);
            }
            hostResponse.setGpuGroups(gpus);
        }
        if (details.contains(HostDetails.all) || details.contains(HostDetails.capacity) || details.contains(HostDetails.stats) || details.contains(HostDetails.events)) {

            hostResponse.setOsCategoryId(host.getOsCategoryUuid());
            hostResponse.setOsCategoryName(host.getOsCategoryName());
            hostResponse.setZoneName(host.getZoneName());
            hostResponse.setPodName(host.getPodName());
            if (host.getClusterId() > 0) {
                hostResponse.setClusterName(host.getClusterName());
                hostResponse.setClusterType(host.getClusterType().toString());
            }
        }

        final DecimalFormat decimalFormat = new DecimalFormat("#.##");
        if (host.getType() == Host.Type.Routing) {
            if (details.contains(HostDetails.all) || details.contains(HostDetails.capacity)) {
                // set allocated capacities
                final Long mem = host.getMemReservedCapacity() + host.getMemUsedCapacity();
                final Long cpu = host.getCpuReservedCapacity() + host.getCpuUsedCapacity();

                hostResponse.setMemoryAllocated(mem);
                hostResponse.setMemoryTotal(host.getTotalMemory());

                final String hostTags = host.getTag();
                hostResponse.setHostTags(host.getTag());

                final String haTag = ApiDBUtils.getHaTag();
                if (haTag != null && !haTag.isEmpty() && hostTags != null && !hostTags.isEmpty()) {
                    if (haTag.equalsIgnoreCase(hostTags)) {
                        hostResponse.setHaHost(true);
                    } else {
                        hostResponse.setHaHost(false);
                    }
                } else {
                    hostResponse.setHaHost(false);
                }

                hostResponse.setHypervisorVersion(host.getHypervisorVersion());

                final String cpuAlloc = decimalFormat.format(((float) cpu / (float) (host.getCpus() * host.getSpeed())) * 100f) + "%";
                hostResponse.setCpuAllocated(cpuAlloc);
                final String cpuWithOverprovisioning = new Float(host.getCpus() * host.getSpeed() * ApiDBUtils.getCpuOverprovisioningFactor()).toString();
                hostResponse.setCpuWithOverprovisioning(cpuWithOverprovisioning);
            }

            if (details.contains(HostDetails.all) || details.contains(HostDetails.stats)) {
                // set CPU/RAM/Network stats
                String cpuUsed = null;
                final HostStats hostStats = ApiDBUtils.getHostStatistics(host.getId());
                if (hostStats != null) {
                    final float cpuUtil = (float) hostStats.getCpuUtilization();
                    cpuUsed = decimalFormat.format(cpuUtil) + "%";
                    hostResponse.setCpuUsed(cpuUsed);
                    hostResponse.setMemoryUsed((new Double(hostStats.getUsedMemory())).longValue());
                    hostResponse.setNetworkKbsRead((new Double(hostStats.getNetworkReadKBs())).longValue());
                    hostResponse.setNetworkKbsWrite((new Double(hostStats.getNetworkWriteKBs())).longValue());
                }
            }

            if (details.contains(HostDetails.all) && host.getHypervisorType() == Hypervisor.HypervisorType.KVM) {
                //only kvm has the requirement to return host details
                try {
                    final HostVO h = hostDao.findById(host.getId());
                    hostDao.loadDetails(h);
                    final Map<String, String> hostVoDetails;
                    hostVoDetails = h.getDetails();
                    hostResponse.setDetails(hostVoDetails);
                } catch (final Exception e) {
                    s_logger.debug("failed to get host details", e);
                }
            }
        } else if (host.getType() == Host.Type.SecondaryStorage) {
            final StorageStats secStorageStats = ApiDBUtils.getSecondaryStorageStatistics(host.getId());
            if (secStorageStats != null) {
                hostResponse.setDiskSizeTotal(secStorageStats.getCapacityBytes());
                hostResponse.setDiskSizeAllocated(secStorageStats.getByteUsed());
            }
        }

        hostResponse.setLocalStorageActive(ApiDBUtils.isLocalStorageActiveOnHost(host.getId()));

        if (details.contains(HostDetails.all) || details.contains(HostDetails.events)) {
            final Set<com.cloud.host.Status.Event> possibleEvents = host.getStatus().getPossibleEvents();
            if ((possibleEvents != null) && !possibleEvents.isEmpty()) {
                String events = "";
                final Iterator<com.cloud.host.Status.Event> iter = possibleEvents.iterator();
                while (iter.hasNext()) {
                    final com.cloud.host.Status.Event event = iter.next();
                    events += event.toString();
                    if (iter.hasNext()) {
                        events += "; ";
                    }
                }
                hostResponse.setEvents(events);
            }
        }

        hostResponse.setResourceState(host.getResourceState().toString());

        // set async job
        if (host.getJobId() != null) {
            hostResponse.setJobId(host.getJobUuid());
            hostResponse.setJobStatus(host.getJobStatus());
        }

        hostResponse.setObjectName("host");

        return hostResponse;
    }

    @Override
    public HostResponse setHostResponse(final HostResponse response, final HostJoinVO host) {
        final String tag = host.getTag();
        if (tag != null) {
            if (response.getHostTags() != null && response.getHostTags().length() > 0) {
                response.setHostTags(response.getHostTags() + "," + tag);
            } else {
                response.setHostTags(tag);
            }
        }
        return response;
    }

    @Override
    public HostForMigrationResponse newHostForMigrationResponse(final HostJoinVO host, final EnumSet<HostDetails> details) {
        final HostForMigrationResponse hostResponse = new HostForMigrationResponse();
        hostResponse.setId(host.getUuid());
        hostResponse.setCapabilities(host.getCapabilities());
        hostResponse.setClusterId(host.getClusterUuid());
        hostResponse.setCpuNumber(host.getCpus());
        hostResponse.setZoneId(host.getZoneUuid());
        hostResponse.setDisconnectedOn(host.getDisconnectedOn());
        hostResponse.setHypervisor(host.getHypervisorType());
        hostResponse.setHostType(host.getType());
        hostResponse.setLastPinged(new Date(host.getLastPinged()));
        hostResponse.setManagementServerId(host.getManagementServerId());
        hostResponse.setName(host.getName());
        hostResponse.setPodId(host.getPodUuid());
        hostResponse.setRemoved(host.getRemoved());
        hostResponse.setCpuSpeed(host.getSpeed());
        hostResponse.setState(host.getStatus());
        hostResponse.setIpAddress(host.getPrivateIpAddress());
        hostResponse.setVersion(host.getVersion());
        hostResponse.setCreated(host.getCreated());

        if (details.contains(HostDetails.all) || details.contains(HostDetails.capacity) || details.contains(HostDetails.stats) || details.contains(HostDetails.events)) {

            hostResponse.setOsCategoryId(host.getOsCategoryUuid());
            hostResponse.setOsCategoryName(host.getOsCategoryName());
            hostResponse.setZoneName(host.getZoneName());
            hostResponse.setPodName(host.getPodName());
            if (host.getClusterId() > 0) {
                hostResponse.setClusterName(host.getClusterName());
                hostResponse.setClusterType(host.getClusterType().toString());
            }
        }

        final DecimalFormat decimalFormat = new DecimalFormat("#.##");
        if (host.getType() == Host.Type.Routing) {
            if (details.contains(HostDetails.all) || details.contains(HostDetails.capacity)) {
                // set allocated capacities
                final Long mem = host.getMemReservedCapacity() + host.getMemUsedCapacity();
                final Long cpu = host.getCpuReservedCapacity() + host.getCpuReservedCapacity();

                hostResponse.setMemoryAllocated(mem);
                hostResponse.setMemoryTotal(host.getTotalMemory());

                final String hostTags = host.getTag();
                hostResponse.setHostTags(host.getTag());

                final String haTag = ApiDBUtils.getHaTag();
                if (haTag != null && !haTag.isEmpty() && hostTags != null && !hostTags.isEmpty()) {
                    if (haTag.equalsIgnoreCase(hostTags)) {
                        hostResponse.setHaHost(true);
                    } else {
                        hostResponse.setHaHost(false);
                    }
                } else {
                    hostResponse.setHaHost(false);
                }

                hostResponse.setHypervisorVersion(host.getHypervisorVersion());

                final String cpuAlloc = decimalFormat.format(((float) cpu / (float) (host.getCpus() * host.getSpeed())) * 100f) + "%";
                hostResponse.setCpuAllocated(cpuAlloc);
                final String cpuWithOverprovisioning = new Float(host.getCpus() * host.getSpeed() * ApiDBUtils.getCpuOverprovisioningFactor()).toString();
                hostResponse.setCpuWithOverprovisioning(cpuWithOverprovisioning);
            }

            if (details.contains(HostDetails.all) || details.contains(HostDetails.stats)) {
                // set CPU/RAM/Network stats
                String cpuUsed = null;
                final HostStats hostStats = ApiDBUtils.getHostStatistics(host.getId());
                if (hostStats != null) {
                    final float cpuUtil = (float) hostStats.getCpuUtilization();
                    cpuUsed = decimalFormat.format(cpuUtil) + "%";
                    hostResponse.setCpuUsed(cpuUsed);
                    hostResponse.setMemoryUsed((new Double(hostStats.getUsedMemory())).longValue());
                    hostResponse.setNetworkKbsRead((new Double(hostStats.getNetworkReadKBs())).longValue());
                    hostResponse.setNetworkKbsWrite((new Double(hostStats.getNetworkWriteKBs())).longValue());
                }
            }
        } else if (host.getType() == Host.Type.SecondaryStorage) {
            final StorageStats secStorageStats = ApiDBUtils.getSecondaryStorageStatistics(host.getId());
            if (secStorageStats != null) {
                hostResponse.setDiskSizeTotal(secStorageStats.getCapacityBytes());
                hostResponse.setDiskSizeAllocated(secStorageStats.getByteUsed());
            }
        }

        hostResponse.setLocalStorageActive(ApiDBUtils.isLocalStorageActiveOnHost(host.getId()));

        if (details.contains(HostDetails.all) || details.contains(HostDetails.events)) {
            final Set<com.cloud.host.Status.Event> possibleEvents = host.getStatus().getPossibleEvents();
            if ((possibleEvents != null) && !possibleEvents.isEmpty()) {
                String events = "";
                final Iterator<com.cloud.host.Status.Event> iter = possibleEvents.iterator();
                while (iter.hasNext()) {
                    final com.cloud.host.Status.Event event = iter.next();
                    events += event.toString();
                    if (iter.hasNext()) {
                        events += "; ";
                    }
                }
                hostResponse.setEvents(events);
            }
        }

        hostResponse.setResourceState(host.getResourceState().toString());

        // set async job
        hostResponse.setJobId(host.getJobUuid());
        hostResponse.setJobStatus(host.getJobStatus());

        hostResponse.setObjectName("host");

        return hostResponse;
    }

    @Override
    public HostForMigrationResponse setHostForMigrationResponse(final HostForMigrationResponse response, final HostJoinVO host) {
        final String tag = host.getTag();
        if (tag != null) {
            if (response.getHostTags() != null && response.getHostTags().length() > 0) {
                response.setHostTags(response.getHostTags() + "," + tag);
            } else {
                response.setHostTags(tag);
            }
        }
        return response;
    }

    @Override
    public List<HostJoinVO> newHostView(final Host host) {
        final SearchCriteria<HostJoinVO> sc = hostIdSearch.create();
        sc.setParameters("id", host.getId());
        return searchIncludingRemoved(sc, null, null, false);
    }

    @Override
    public List<HostJoinVO> searchByIds(final Long... hostIds) {
        // set detail batch query size
        int DETAILS_BATCH_SIZE = 2000;
        final String batchCfg = _configDao.getValue("detail.batch.query.size");
        if (batchCfg != null) {
            DETAILS_BATCH_SIZE = Integer.parseInt(batchCfg);
        }
        // query details by batches
        final List<HostJoinVO> uvList = new ArrayList<>();
        // query details by batches
        int curr_index = 0;
        if (hostIds.length > DETAILS_BATCH_SIZE) {
            while ((curr_index + DETAILS_BATCH_SIZE) <= hostIds.length) {
                final Long[] ids = new Long[DETAILS_BATCH_SIZE];
                for (int k = 0, j = curr_index; j < curr_index + DETAILS_BATCH_SIZE; j++, k++) {
                    ids[k] = hostIds[j];
                }
                final SearchCriteria<HostJoinVO> sc = hostSearch.create();
                sc.setParameters("idIN", ids);
                final List<HostJoinVO> vms = searchIncludingRemoved(sc, null, null, false);
                if (vms != null) {
                    uvList.addAll(vms);
                }
                curr_index += DETAILS_BATCH_SIZE;
            }
        }
        if (curr_index < hostIds.length) {
            final int batch_size = (hostIds.length - curr_index);
            // set the ids value
            final Long[] ids = new Long[batch_size];
            for (int k = 0, j = curr_index; j < curr_index + batch_size; j++, k++) {
                ids[k] = hostIds[j];
            }
            final SearchCriteria<HostJoinVO> sc = hostSearch.create();
            sc.setParameters("idIN", ids);
            final List<HostJoinVO> vms = searchIncludingRemoved(sc, null, null, false);
            if (vms != null) {
                uvList.addAll(vms);
            }
        }
        return uvList;
    }
}
