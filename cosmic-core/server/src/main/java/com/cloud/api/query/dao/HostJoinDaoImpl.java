package com.cloud.api.query.dao;

import com.cloud.affinity.AffinityGroupVO;
import com.cloud.affinity.dao.AffinityGroupDao;
import com.cloud.api.ApiConstants.HostDetails;
import com.cloud.api.ApiDBUtils;
import com.cloud.api.query.vo.HostJoinVO;
import com.cloud.api.response.GpuResponse;
import com.cloud.api.response.HostForMigrationResponse;
import com.cloud.api.response.HostResponse;
import com.cloud.api.response.VgpuResponse;
import com.cloud.capacity.CapacityManager;
import com.cloud.dc.DedicatedResourceVO;
import com.cloud.dc.dao.DedicatedResourceDao;
import com.cloud.domain.DomainVO;
import com.cloud.domain.dao.DomainDao;
import com.cloud.framework.config.dao.ConfigurationDao;
import com.cloud.gpu.HostGpuGroupsVO;
import com.cloud.gpu.VGPUTypesVO;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.legacymodel.dc.Host;
import com.cloud.legacymodel.dc.HostStats;
import com.cloud.legacymodel.storage.StorageStats;
import com.cloud.model.enumeration.Event;
import com.cloud.model.enumeration.HostType;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.user.AccountVO;
import com.cloud.user.dao.AccountDao;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

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
    @Inject
    private DedicatedResourceDao dedicatedResourceDao;
    @Inject
    private DomainDao domainDao;
    @Inject
    private AccountDao accountDao;
    @Inject
    private AffinityGroupDao affinityGroupDao;

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
        hostResponse.setCpuNumber(host.getCpusWithoutHyperThreading());
        hostResponse.setCpuNumberHyperThreading(host.getCpus());
        hostResponse.setZoneId(host.getZoneUuid());
        hostResponse.setDisconnectedOn(host.getDisconnectedOn());
        hostResponse.setHypervisor(host.getHypervisorType());
        hostResponse.setHostType(host.getType());
        hostResponse.setLastPinged(new Date(host.getLastPinged()));
        hostResponse.setManagementServerId(host.getManagementServerId());
        hostResponse.setName(host.getName());
        hostResponse.setPodId(host.getPodUuid());
        hostResponse.setRemoved(host.getRemoved());
        hostResponse.setState(host.getStatus());
        hostResponse.setIpAddress(host.getPrivateIpAddress());
        hostResponse.setVersion(host.getVersion());
        hostResponse.setCreated(host.getCreated());

        final DedicatedResourceVO dedicatedResourceVO = dedicatedResourceDao.findByHostId(host.getId());
        if (dedicatedResourceVO != null) {
            hostResponse.setDedicated(true);

            final DomainVO domainVO = domainDao.findById(dedicatedResourceVO.getDomainId());
            if (domainVO != null) {
                hostResponse.setDomainId(domainVO.getUuid());
                hostResponse.setDomainName(domainVO.getName());
            }

            final AccountVO accountVO = accountDao.findById(dedicatedResourceVO.getAccountId());
            if (accountVO != null) {
                hostResponse.setAccountId(accountVO.getUuid());
                hostResponse.setAccountName(accountVO.getAccountName());
            }

            final AffinityGroupVO affinityGroupVO = affinityGroupDao.findById(dedicatedResourceVO.getAffinityGroupId());
            if (affinityGroupVO != null) {
                hostResponse.setAffinityGroupId(affinityGroupVO.getUuid());
                hostResponse.setAffinityGroupName(affinityGroupVO.getName());
            }
        }

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
        if (host.getType() == HostType.Routing) {
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

                final String cpuAlloc = Float.toString(((float) cpu / (host.getCpus() * CapacityManager.CpuOverprovisioningFactor.valueIn(host.getClusterId()))) * 100f) + "%";
                hostResponse.setCpuAllocated(cpuAlloc);
                final String cpuWithOverprovisioning = Float.toString(host.getCpus() * CapacityManager.CpuOverprovisioningFactor.valueIn(host.getClusterId()));
                hostResponse.setCpuWithOverprovisioning(cpuWithOverprovisioning);
            }

            if (details.contains(HostDetails.all) || details.contains(HostDetails.stats)) {
                // set CPU/RAM/Network stats
                final String cpuUsed;
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

            if (details.contains(HostDetails.all) && host.getHypervisorType() == HypervisorType.KVM) {
                //only kvm has the requirement to return host details
                try {
                    final HostVO h = hostDao.findById(host.getId());
                    hostDao.loadDetails(h);
                    final Map<String, String> hostVoDetails;
                    hostVoDetails = h.getDetails();
                    hostResponse.setDetails(hostVoDetails);
                    hostResponse.setHypervisorVersion(h.getDetail("Host.OS") + " " + h.getDetail("Host.OS.Version") + " with kernel " + h.getDetail("Host.OS.Kernel.Version"));
                } catch (final Exception e) {
                    s_logger.debug("failed to get host details", e);
                }
            }
        } else if (host.getType() == HostType.SecondaryStorage) {
            final StorageStats secStorageStats = ApiDBUtils.getSecondaryStorageStatistics(host.getId());
            if (secStorageStats != null) {
                hostResponse.setDiskSizeTotal(secStorageStats.getCapacityBytes());
                hostResponse.setDiskSizeAllocated(secStorageStats.getByteUsed());
            }
        }

        hostResponse.setLocalStorageActive(ApiDBUtils.isLocalStorageActiveOnHost(host.getId()));

        if (details.contains(HostDetails.all) || details.contains(HostDetails.events)) {
            final Set<Event> possibleEvents = host.getStatus().getPossibleEvents();
            if ((possibleEvents != null) && !possibleEvents.isEmpty()) {
                String events = "";
                final Iterator<Event> iter = possibleEvents.iterator();
                while (iter.hasNext()) {
                    final Event event = iter.next();
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
        hostResponse.setCpuNumber(host.getCpusWithoutHyperThreading());
        hostResponse.setCpuNumberHyperThreading(host.getCpus());
        hostResponse.setZoneId(host.getZoneUuid());
        hostResponse.setDisconnectedOn(host.getDisconnectedOn());
        hostResponse.setHypervisor(host.getHypervisorType());
        hostResponse.setHostType(host.getType());
        hostResponse.setLastPinged(new Date(host.getLastPinged()));
        hostResponse.setManagementServerId(host.getManagementServerId());
        hostResponse.setName(host.getName());
        hostResponse.setPodId(host.getPodUuid());
        hostResponse.setRemoved(host.getRemoved());
        hostResponse.setState(host.getStatus());
        hostResponse.setIpAddress(host.getPrivateIpAddress());
        hostResponse.setVersion(host.getVersion());
        hostResponse.setCreated(host.getCreated());

        final DedicatedResourceVO dedicatedResourceVO = dedicatedResourceDao.findByHostId(host.getId());
        if (dedicatedResourceVO != null) {
            hostResponse.setDedicated(true);

            final DomainVO domainVO = domainDao.findById(dedicatedResourceVO.getDomainId());
            if (domainVO != null) {
                hostResponse.setDomainId(domainVO.getUuid());
                hostResponse.setDomainName(domainVO.getName());
            }

            final AccountVO accountVO = accountDao.findById(dedicatedResourceVO.getAccountId());
            if (accountVO != null) {
                hostResponse.setAccountId(accountVO.getUuid());
                hostResponse.setAccountName(accountVO.getAccountName());
            }

            final AffinityGroupVO affinityGroupVO = affinityGroupDao.findById(dedicatedResourceVO.getAffinityGroupId());
            if (affinityGroupVO != null) {
                hostResponse.setAffinityGroupId(affinityGroupVO.getUuid());
                hostResponse.setAffinityGroupName(affinityGroupVO.getName());
            }
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
        if (host.getType() == HostType.Routing) {
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

                final String cpuAlloc = decimalFormat.format(((float) cpu / (float) host.getCpus()) * 100f) + "%";
                hostResponse.setCpuAllocated(cpuAlloc);
                final String cpuWithOverprovisioning = Float.toString(host.getCpus() * ApiDBUtils.getCpuOverprovisioningFactor(host.getClusterId()));
                hostResponse.setCpuWithOverprovisioning(cpuWithOverprovisioning);
            }

            if (details.contains(HostDetails.all) || details.contains(HostDetails.stats)) {
                // set CPU/RAM/Network stats
                final String cpuUsed;
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
        } else if (host.getType() == HostType.SecondaryStorage) {
            final StorageStats secStorageStats = ApiDBUtils.getSecondaryStorageStatistics(host.getId());
            if (secStorageStats != null) {
                hostResponse.setDiskSizeTotal(secStorageStats.getCapacityBytes());
                hostResponse.setDiskSizeAllocated(secStorageStats.getByteUsed());
            }
        }

        hostResponse.setLocalStorageActive(ApiDBUtils.isLocalStorageActiveOnHost(host.getId()));

        if (details.contains(HostDetails.all) || details.contains(HostDetails.events)) {
            final Set<Event> possibleEvents = host.getStatus().getPossibleEvents();
            if ((possibleEvents != null) && !possibleEvents.isEmpty()) {
                String events = "";
                final Iterator<Event> iter = possibleEvents.iterator();
                while (iter.hasNext()) {
                    final Event event = iter.next();
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
