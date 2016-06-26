package com.cloud.agent.manager.allocator.impl;

import com.cloud.agent.manager.allocator.HostAllocator;
import com.cloud.capacity.CapacityManager;
import com.cloud.capacity.CapacityVO;
import com.cloud.capacity.dao.CapacityDao;
import com.cloud.configuration.Config;
import com.cloud.dc.ClusterDetailsDao;
import com.cloud.dc.ClusterDetailsVO;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.deploy.DeploymentPlan;
import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.gpu.GPU;
import com.cloud.host.DetailVO;
import com.cloud.host.Host;
import com.cloud.host.Host.Type;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.host.dao.HostDetailsDao;
import com.cloud.offering.ServiceOffering;
import com.cloud.org.Cluster;
import com.cloud.resource.ResourceManager;
import com.cloud.service.ServiceOfferingDetailsVO;
import com.cloud.service.dao.ServiceOfferingDetailsDao;
import com.cloud.storage.GuestOSCategoryVO;
import com.cloud.storage.GuestOSVO;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.dao.GuestOSCategoryDao;
import com.cloud.storage.dao.GuestOSDao;
import com.cloud.user.Account;
import com.cloud.utils.component.AdapterBase;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.vm.dao.VMInstanceDao;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * An allocator that tries to find a fit on a computing host.  This allocator does not care whether or not the host supports routing.
 */
@Component
public class FirstFitAllocator extends AdapterBase implements HostAllocator {
    private static final Logger s_logger = LoggerFactory.getLogger(FirstFitAllocator.class);
    @Inject
    protected HostDao _hostDao = null;
    @Inject
    protected ResourceManager _resourceMgr;
    protected String _allocationAlgorithm = "random";
    @Inject
    HostDetailsDao _hostDetailsDao = null;
    @Inject
    ConfigurationDao _configDao = null;
    @Inject
    GuestOSDao _guestOSDao = null;
    @Inject
    GuestOSCategoryDao _guestOSCategoryDao = null;
    @Inject
    VMInstanceDao _vmInstanceDao = null;
    @Inject
    ClusterDao _clusterDao;
    @Inject
    ClusterDetailsDao _clusterDetailsDao;
    @Inject
    ServiceOfferingDetailsDao _serviceOfferingDetailsDao;
    @Inject
    CapacityManager _capacityMgr;
    @Inject
    CapacityDao _capacityDao;
    boolean _checkHvm = true;

    @Override
    public boolean isVirtualMachineUpgradable(final VirtualMachine vm, final ServiceOffering offering) {
        // currently we do no special checks to rule out a VM being upgradable to an offering, so
        // return true
        return true;
    }

    @Override
    public List<Host> allocateTo(final VirtualMachineProfile vmProfile, final DeploymentPlan plan, final Type type, final ExcludeList avoid, final int returnUpTo) {
        return allocateTo(vmProfile, plan, type, avoid, returnUpTo, true);
    }

    @Override
    public List<Host> allocateTo(final VirtualMachineProfile vmProfile, final DeploymentPlan plan, final Type type, final ExcludeList avoid, final int returnUpTo, final boolean
            considerReservedCapacity) {

        final long dcId = plan.getDataCenterId();
        final Long podId = plan.getPodId();
        final Long clusterId = plan.getClusterId();
        final ServiceOffering offering = vmProfile.getServiceOffering();
        final VMTemplateVO template = (VMTemplateVO) vmProfile.getTemplate();
        final Account account = vmProfile.getOwner();

        if (type == Host.Type.Storage) {
            // FirstFitAllocator should be used for user VMs only since it won't care whether the host is capable of routing or not
            return new ArrayList<>();
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Looking for hosts in dc: " + dcId + "  pod:" + podId + "  cluster:" + clusterId);
        }

        final String hostTagOnOffering = offering.getHostTag();
        final String hostTagOnTemplate = template.getTemplateTag();

        final boolean hasSvcOfferingTag = hostTagOnOffering != null ? true : false;
        final boolean hasTemplateTag = hostTagOnTemplate != null ? true : false;

        List<HostVO> clusterHosts = new ArrayList<>();

        final String haVmTag = (String) vmProfile.getParameter(VirtualMachineProfile.Param.HaTag);
        if (haVmTag != null) {
            clusterHosts = _hostDao.listByHostTag(type, clusterId, podId, dcId, haVmTag);
        } else {
            if (hostTagOnOffering == null && hostTagOnTemplate == null) {
                clusterHosts = _resourceMgr.listAllUpAndEnabledNonHAHosts(type, clusterId, podId, dcId);
            } else {
                List<HostVO> hostsMatchingOfferingTag = new ArrayList<>();
                List<HostVO> hostsMatchingTemplateTag = new ArrayList<>();
                if (hasSvcOfferingTag) {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Looking for hosts having tag specified on SvcOffering:" + hostTagOnOffering);
                    }
                    hostsMatchingOfferingTag = _hostDao.listByHostTag(type, clusterId, podId, dcId, hostTagOnOffering);
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Hosts with tag '" + hostTagOnOffering + "' are:" + hostsMatchingOfferingTag);
                    }
                }
                if (hasTemplateTag) {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Looking for hosts having tag specified on Template:" + hostTagOnTemplate);
                    }
                    hostsMatchingTemplateTag = _hostDao.listByHostTag(type, clusterId, podId, dcId, hostTagOnTemplate);
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Hosts with tag '" + hostTagOnTemplate + "' are:" + hostsMatchingTemplateTag);
                    }
                }

                if (hasSvcOfferingTag && hasTemplateTag) {
                    hostsMatchingOfferingTag.retainAll(hostsMatchingTemplateTag);
                    clusterHosts = _hostDao.listByHostTag(type, clusterId, podId, dcId, hostTagOnTemplate);
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Found " + hostsMatchingOfferingTag.size() + " Hosts satisfying both tags, host ids are:" + hostsMatchingOfferingTag);
                    }

                    clusterHosts = hostsMatchingOfferingTag;
                } else {
                    if (hasSvcOfferingTag) {
                        clusterHosts = hostsMatchingOfferingTag;
                    } else {
                        clusterHosts = hostsMatchingTemplateTag;
                    }
                }
            }
        }

        // add all hosts that we are not considering to the avoid list
        final List<HostVO> allhostsInCluster = _hostDao.listAllUpAndEnabledNonHAHosts(type, clusterId, podId, dcId, null);
        allhostsInCluster.removeAll(clusterHosts);
        for (final HostVO host : allhostsInCluster) {
            avoid.addHost(host.getId());
        }

        return allocateTo(plan, offering, template, avoid, clusterHosts, returnUpTo, considerReservedCapacity, account);
    }

    @Override
    public List<Host> allocateTo(final VirtualMachineProfile vmProfile, final DeploymentPlan plan, final Type type, final ExcludeList avoid, final List<? extends Host> hosts,
                                 final int returnUpTo,
                                 final boolean considerReservedCapacity) {
        final long dcId = plan.getDataCenterId();
        final Long podId = plan.getPodId();
        final Long clusterId = plan.getClusterId();
        final ServiceOffering offering = vmProfile.getServiceOffering();
        final VMTemplateVO template = (VMTemplateVO) vmProfile.getTemplate();
        final Account account = vmProfile.getOwner();
        List<Host> suitableHosts = new ArrayList<>();
        final List<Host> hostsCopy = new ArrayList<>(hosts);

        if (type == Host.Type.Storage) {
            // FirstFitAllocator should be used for user VMs only since it won't care whether the host is capable of
            // routing or not.
            return suitableHosts;
        }

        final String hostTagOnOffering = offering.getHostTag();
        final String hostTagOnTemplate = template.getTemplateTag();
        final boolean hasSvcOfferingTag = hostTagOnOffering != null ? true : false;
        final boolean hasTemplateTag = hostTagOnTemplate != null ? true : false;

        final String haVmTag = (String) vmProfile.getParameter(VirtualMachineProfile.Param.HaTag);
        if (haVmTag != null) {
            hostsCopy.retainAll(_hostDao.listByHostTag(type, clusterId, podId, dcId, haVmTag));
        } else {
            if (hostTagOnOffering == null && hostTagOnTemplate == null) {
                hostsCopy.retainAll(_resourceMgr.listAllUpAndEnabledNonHAHosts(type, clusterId, podId, dcId));
            } else {
                if (hasSvcOfferingTag) {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Looking for hosts having tag specified on SvcOffering:" + hostTagOnOffering);
                    }
                    hostsCopy.retainAll(_hostDao.listByHostTag(type, clusterId, podId, dcId, hostTagOnOffering));

                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Hosts with tag '" + hostTagOnOffering + "' are:" + hostsCopy);
                    }
                }

                if (hasTemplateTag) {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Looking for hosts having tag specified on Template:" + hostTagOnTemplate);
                    }

                    hostsCopy.retainAll(_hostDao.listByHostTag(type, clusterId, podId, dcId, hostTagOnTemplate));

                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Hosts with tag '" + hostTagOnTemplate + "' are:" + hostsCopy);
                    }
                }
            }
        }

        if (!hostsCopy.isEmpty()) {
            suitableHosts = allocateTo(plan, offering, template, avoid, hostsCopy, returnUpTo, considerReservedCapacity, account);
        }

        return suitableHosts;
    }

    protected List<Host> allocateTo(final DeploymentPlan plan, final ServiceOffering offering, final VMTemplateVO template, final ExcludeList avoid, List<? extends Host> hosts,
                                    final int returnUpTo,
                                    final boolean considerReservedCapacity, final Account account) {
        if (_allocationAlgorithm.equals("random") || _allocationAlgorithm.equals("userconcentratedpod_random")) {
            // Shuffle this so that we don't check the hosts in the same order.
            Collections.shuffle(hosts);
        } else if (_allocationAlgorithm.equals("userdispersing")) {
            hosts = reorderHostsByNumberOfVms(plan, hosts, account);
        } else if (_allocationAlgorithm.equals("firstfitleastconsumed")) {
            hosts = reorderHostsByCapacity(plan, hosts);
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("FirstFitAllocator has " + hosts.size() + " hosts to check for allocation: " + hosts);
        }

        // We will try to reorder the host lists such that we give priority to hosts that have
        // the minimums to support a VM's requirements
        hosts = prioritizeHosts(template, offering, hosts);

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Found " + hosts.size() + " hosts for allocation after prioritization: " + hosts);
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Looking for speed=" + (offering.getCpu() * offering.getSpeed()) + "Mhz, Ram=" + offering.getRamSize());
        }

        final long serviceOfferingId = offering.getId();
        final List<Host> suitableHosts = new ArrayList<>();
        ServiceOfferingDetailsVO offeringDetails = null;

        for (final Host host : hosts) {
            if (suitableHosts.size() == returnUpTo) {
                break;
            }
            if (avoid.shouldAvoid(host)) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Host name: " + host.getName() + ", hostId: " + host.getId() + " is in avoid set, skipping this and trying other available hosts");
                }
                continue;
            }

            //find number of guest VMs occupying capacity on this host.
            if (_capacityMgr.checkIfHostReachMaxGuestLimit(host)) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Host name: " + host.getName() + ", hostId: " + host.getId() +
                            " already has max Running VMs(count includes system VMs), skipping this and trying other available hosts");
                }
                avoid.addHost(host.getId());
                continue;
            }

            // Check if GPU device is required by offering and host has the availability
            if ((offeringDetails = _serviceOfferingDetailsDao.findDetail(serviceOfferingId, GPU.Keys.vgpuType.toString())) != null) {
                final ServiceOfferingDetailsVO groupName = _serviceOfferingDetailsDao.findDetail(serviceOfferingId, GPU.Keys.pciDevice.toString());
                if (!_resourceMgr.isGPUDeviceAvailable(host.getId(), groupName.getValue(), offeringDetails.getValue())) {
                    s_logger.info("Host name: " + host.getName() + ", hostId: " + host.getId() + " does not have required GPU devices available");
                    avoid.addHost(host.getId());
                    continue;
                }
            }

            final int cpu_requested = offering.getCpu() * offering.getSpeed();
            final long ram_requested = offering.getRamSize() * 1024L * 1024L;
            final Cluster cluster = _clusterDao.findById(host.getClusterId());
            final ClusterDetailsVO clusterDetailsCpuOvercommit = _clusterDetailsDao.findDetail(cluster.getId(), "cpuOvercommitRatio");
            final ClusterDetailsVO clusterDetailsRamOvercommmt = _clusterDetailsDao.findDetail(cluster.getId(), "memoryOvercommitRatio");
            final Float cpuOvercommitRatio = Float.parseFloat(clusterDetailsCpuOvercommit.getValue());
            final Float memoryOvercommitRatio = Float.parseFloat(clusterDetailsRamOvercommmt.getValue());

            final boolean hostHasCpuCapability = _capacityMgr.checkIfHostHasCpuCapability(host.getId(), offering.getCpu(), offering.getSpeed());
            final boolean hostHasCapacity = _capacityMgr.checkIfHostHasCapacity(host.getId(), cpu_requested, ram_requested, false, cpuOvercommitRatio, memoryOvercommitRatio,
                    considerReservedCapacity);

            if (hostHasCpuCapability && hostHasCapacity) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Found a suitable host, adding to list: " + host.getId());
                }
                suitableHosts.add(host);
            } else {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Not using host " + host.getId() + "; host has cpu capability? " + hostHasCpuCapability + ", host has capacity?" + hostHasCapacity);
                }
                avoid.addHost(host.getId());
            }
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Host Allocator returning " + suitableHosts.size() + " suitable hosts");
        }

        return suitableHosts;
    }

    private List<? extends Host> reorderHostsByNumberOfVms(final DeploymentPlan plan, final List<? extends Host> hosts, final Account account) {
        if (account == null) {
            return hosts;
        }
        final long dcId = plan.getDataCenterId();
        final Long podId = plan.getPodId();
        final Long clusterId = plan.getClusterId();

        final List<Long> hostIdsByVmCount = _vmInstanceDao.listHostIdsByVmCount(dcId, podId, clusterId, account.getAccountId());
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("List of hosts in ascending order of number of VMs: " + hostIdsByVmCount);
        }

        //now filter the given list of Hosts by this ordered list
        final Map<Long, Host> hostMap = new HashMap<>();
        for (final Host host : hosts) {
            hostMap.put(host.getId(), host);
        }
        final List<Long> matchingHostIds = new ArrayList<>(hostMap.keySet());

        hostIdsByVmCount.retainAll(matchingHostIds);

        final List<Host> reorderedHosts = new ArrayList<>();
        for (final Long id : hostIdsByVmCount) {
            reorderedHosts.add(hostMap.get(id));
        }

        return reorderedHosts;
    }

    // Reorder hosts in the decreasing order of free capacity.
    private List<? extends Host> reorderHostsByCapacity(final DeploymentPlan plan, final List<? extends Host> hosts) {
        final Long clusterId = plan.getClusterId();
        //Get capacity by which we should reorder
        final String capacityTypeToOrder = _configDao.getValue(Config.HostCapacityTypeToOrderClusters.key());
        short capacityType = CapacityVO.CAPACITY_TYPE_CPU;
        if ("RAM".equalsIgnoreCase(capacityTypeToOrder)) {
            capacityType = CapacityVO.CAPACITY_TYPE_MEMORY;
        }
        final List<Long> hostIdsByFreeCapacity = _capacityDao.orderHostsByFreeCapacity(clusterId, capacityType);
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("List of hosts in descending order of free capacity in the cluster: " + hostIdsByFreeCapacity);
        }

        //now filter the given list of Hosts by this ordered list
        final Map<Long, Host> hostMap = new HashMap<>();
        for (final Host host : hosts) {
            hostMap.put(host.getId(), host);
        }
        final List<Long> matchingHostIds = new ArrayList<>(hostMap.keySet());

        hostIdsByFreeCapacity.retainAll(matchingHostIds);

        final List<Host> reorderedHosts = new ArrayList<>();
        for (final Long id : hostIdsByFreeCapacity) {
            reorderedHosts.add(hostMap.get(id));
        }

        return reorderedHosts;
    }

    protected List<? extends Host> prioritizeHosts(final VMTemplateVO template, final ServiceOffering offering, final List<? extends Host> hosts) {
        if (template == null) {
            return hosts;
        }

        // Determine the guest OS category of the template
        final String templateGuestOSCategory = getTemplateGuestOSCategory(template);

        final List<Host> prioritizedHosts = new ArrayList<>();
        final List<Host> noHvmHosts = new ArrayList<>();

        // If a template requires HVM and a host doesn't support HVM, remove it from consideration
        final List<Host> hostsToCheck = new ArrayList<>();
        if (template.isRequiresHvm()) {
            for (final Host host : hosts) {
                if (hostSupportsHVM(host)) {
                    hostsToCheck.add(host);
                } else {
                    noHvmHosts.add(host);
                }
            }
        } else {
            hostsToCheck.addAll(hosts);
        }

        if (s_logger.isDebugEnabled()) {
            if (noHvmHosts.size() > 0) {
                s_logger.debug("Not considering hosts: " + noHvmHosts + "  to deploy template: " + template + " as they are not HVM enabled");
            }
        }
        // If a host is tagged with the same guest OS category as the template, move it to a high priority list
        // If a host is tagged with a different guest OS category than the template, move it to a low priority list
        final List<Host> highPriorityHosts = new ArrayList<>();
        final List<Host> lowPriorityHosts = new ArrayList<>();
        for (final Host host : hostsToCheck) {
            final String hostGuestOSCategory = getHostGuestOSCategory(host);
            if (hostGuestOSCategory == null) {
                continue;
            } else if (templateGuestOSCategory.equals(hostGuestOSCategory)) {
                highPriorityHosts.add(host);
            } else {
                lowPriorityHosts.add(host);
            }
        }

        hostsToCheck.removeAll(highPriorityHosts);
        hostsToCheck.removeAll(lowPriorityHosts);

        // Prioritize the remaining hosts by HVM capability
        for (final Host host : hostsToCheck) {
            if (!template.isRequiresHvm() && !hostSupportsHVM(host)) {
                // Host and template both do not support hvm, put it as first consideration
                prioritizedHosts.add(0, host);
            } else {
                // Template doesn't require hvm, but the machine supports it, make it last for consideration
                prioritizedHosts.add(host);
            }
        }

        // Merge the lists
        prioritizedHosts.addAll(0, highPriorityHosts);
        prioritizedHosts.addAll(lowPriorityHosts);

        // if service offering is not GPU enabled then move all the GPU enabled hosts to the end of priority list.
        if (_serviceOfferingDetailsDao.findDetail(offering.getId(), GPU.Keys.vgpuType.toString()) == null) {

            final List<Host> gpuEnabledHosts = new ArrayList<>();
            // Check for GPU enabled hosts.
            for (final Host host : prioritizedHosts) {
                if (_resourceMgr.isHostGpuEnabled(host.getId())) {
                    gpuEnabledHosts.add(host);
                }
            }
            // Move GPU enabled hosts to the end of list
            if (!gpuEnabledHosts.isEmpty()) {
                prioritizedHosts.removeAll(gpuEnabledHosts);
                prioritizedHosts.addAll(gpuEnabledHosts);
            }
        }
        return prioritizedHosts;
    }

    protected String getTemplateGuestOSCategory(final VMTemplateVO template) {
        final long guestOSId = template.getGuestOSId();
        final GuestOSVO guestOS = _guestOSDao.findById(guestOSId);
        final long guestOSCategoryId = guestOS.getCategoryId();
        final GuestOSCategoryVO guestOSCategory = _guestOSCategoryDao.findById(guestOSCategoryId);
        return guestOSCategory.getName();
    }

    protected boolean hostSupportsHVM(final Host host) {
        if (!_checkHvm) {
            return true;
        }
        // Determine host capabilities
        final String caps = host.getCapabilities();

        if (caps != null) {
            final String[] tokens = caps.split(",");
            for (final String token : tokens) {
                if (token.contains("hvm")) {
                    return true;
                }
            }
        }

        return false;
    }

    protected String getHostGuestOSCategory(final Host host) {
        final DetailVO hostDetail = _hostDetailsDao.findDetail(host.getId(), "guest.os.category.id");
        if (hostDetail != null) {
            final String guestOSCategoryIdString = hostDetail.getValue();
            final long guestOSCategoryId;

            try {
                guestOSCategoryId = Long.parseLong(guestOSCategoryIdString);
            } catch (final Exception e) {
                return null;
            }

            final GuestOSCategoryVO guestOSCategory = _guestOSCategoryDao.findById(guestOSCategoryId);

            if (guestOSCategory != null) {
                return guestOSCategory.getName();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        if (_configDao != null) {
            final Map<String, String> configs = _configDao.getConfiguration(params);

            final String allocationAlgorithm = configs.get("vm.allocation.algorithm");
            if (allocationAlgorithm != null) {
                _allocationAlgorithm = allocationAlgorithm;
            }
            final String value = configs.get("xenserver.check.hvm");
            _checkHvm = value == null ? true : Boolean.parseBoolean(value);
        }
        return true;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }
}
