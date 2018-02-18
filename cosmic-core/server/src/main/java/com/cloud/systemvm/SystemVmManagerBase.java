package com.cloud.systemvm;

import static com.cloud.utils.CloudConstants.PROPERTY_LIST_SEPARATOR;

import com.cloud.cluster.ClusterManager;
import com.cloud.context.CallContext;
import com.cloud.db.model.Zone;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.framework.config.ConfigKey;
import com.cloud.host.dao.HostDao;
import com.cloud.info.RunningHostCountInfo;
import com.cloud.info.RunningHostInfoAgregator;
import com.cloud.info.RunningHostInfoAgregator.ZoneHostInfo;
import com.cloud.managementserver.ManagementServerService;
import com.cloud.model.enumeration.NetworkType;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.dao.IPAddressVO;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.NetworkVO;
import com.cloud.network.router.VirtualNetworkApplianceManager;
import com.cloud.network.rules.RulesManager;
import com.cloud.utils.DateUtil;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.AfterScanAction;
import com.cloud.vm.NicProfile;
import com.cloud.vm.SystemVm;
import com.cloud.vm.SystemVmLoadScanHandler;
import com.cloud.vm.VirtualMachineProfile;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SystemVmManagerBase extends ManagerBase implements SystemVmLoadScanHandler<Long> {
    private static final Logger logger = LoggerFactory.getLogger(SystemVmManagerBase.class);

    protected void computeVmIps(final SystemVm vmVO, final Zone zone, final List<NicProfile> nics) {
        for (final NicProfile nic : nics) {
            if ((nic.getTrafficType() == TrafficType.Public && zone.getNetworkType() == NetworkType.Advanced) ||
                    (nic.getTrafficType() == TrafficType.Guest && (zone.getNetworkType() == NetworkType.Basic || zone.isSecurityGroupEnabled()))) {
                vmVO.setPublicIpAddress(nic.getIPv4Address());
                vmVO.setPublicNetmask(nic.getIPv4Netmask());
                vmVO.setPublicMacAddress(nic.getMacAddress());
            } else if (nic.getTrafficType() == TrafficType.Management) {
                vmVO.setPrivateIpAddress(nic.getIPv4Address());
                vmVO.setPrivateMacAddress(nic.getMacAddress());
            }
        }
    }

    protected static void finalizeStop(final VirtualMachineProfile profile, final IPAddressVO ip, final RulesManager _rulesMgr) {
        if (ip != null && ip.getSystem()) {
            final CallContext ctx = CallContext.current();
            try {
                _rulesMgr.disableStaticNat(ip.getId(), ctx.getCallingAccount(), ctx.getCallingUserId(), true);
            } catch (final Exception e) {
                logger.warn("Failed to disable static nat and release system ip " + ip + " as a part of vm " + profile.getVirtualMachine() + " stop due to exception ", e);
            }
        }
    }

    protected static Map<Long, ZoneHostInfo> getLongZoneHostInfoMap(final HostDao _hostDao) {
        final Date cutTime = DateUtil.currentGMTTime();
        final List<RunningHostCountInfo> l = _hostDao.getRunningHostCounts(new Date(cutTime.getTime() - ClusterManager.HeartbeatThreshold.value()));

        final RunningHostInfoAgregator aggregator = new RunningHostInfoAgregator();
        if (l.size() > 0) {
            for (final RunningHostCountInfo countInfo : l) {
                aggregator.aggregate(countInfo);
            }
        }

        return aggregator.getZoneHostInfoMap();
    }

    protected static Long[] getLongs(final DataCenterDao _dcDao) {
        final List<DataCenterVO> zones = _dcDao.listEnabledZones();

        final Long[] dcIdList = new Long[zones.size()];
        int i = 0;
        for (final DataCenterVO dc : zones) {
            dcIdList[i++] = dc.getId();
        }

        return dcIdList;
    }

    protected static NetworkVO getNetworkForAdvancedZone(final Zone zone, final NetworkDao _networkDao) {
        if (zone.getNetworkType() != NetworkType.Advanced) {
            throw new CloudRuntimeException("Zone " + zone + " is not advanced.");
        }

        if (zone.isSecurityGroupEnabled()) {
            final List<NetworkVO> networks = _networkDao.listByZoneSecurityGroup(zone.getId());
            if (CollectionUtils.isEmpty(networks)) {
                throw new CloudRuntimeException("Can not found security enabled network in SG Zone " + zone);
            }

            return networks.get(0);
        } else {
            final TrafficType defaultTrafficType = TrafficType.Public;
            final List<NetworkVO> defaultNetworks = _networkDao.listByZoneAndTrafficType(zone.getId(), defaultTrafficType);
            // api should never allow this situation to happen
            if (defaultNetworks.size() != 1) {
                throw new CloudRuntimeException("Found " + defaultNetworks.size() + " networks of type " + defaultTrafficType + " when expect to find 1");
            }

            return defaultNetworks.get(0);
        }
    }

    protected static NetworkVO getNetworkForBasicZone(final Zone zone, final NetworkDao _networkDao) {
        if (zone.getNetworkType() != NetworkType.Basic) {
            throw new CloudRuntimeException("Zone " + zone + " is not basic.");
        }

        final TrafficType defaultTrafficType = TrafficType.Guest;
        final List<NetworkVO> defaultNetworks = _networkDao.listByZoneAndTrafficType(zone.getId(), defaultTrafficType);
        // api should never allow this situation to happen
        if (defaultNetworks.size() != 1) {
            throw new CloudRuntimeException("Found " + defaultNetworks.size() + " networks of type " + defaultTrafficType + " when expect to find 1");
        }

        return defaultNetworks.get(0);
    }

    protected String computeManagementServerIpList(final ManagementServerService managementServerService) {
        return managementServerService.discoverManagementServerIps()
                                      .collect(Collectors.joining(PROPERTY_LIST_SEPARATOR));
    }

    public void resizePool(final Long pool, final AfterScanAction action, final Object actionArgs) {
        final int count = action.getValue();
        final Stream<Integer> iterations = IntStream.range(0, count).boxed();
        switch (action.getAction()) {
            case EXPAND:
                iterations.forEach(i -> {
                    logger.debug("Expanding pool [iteration {}/{}]", i + 1, count);
                    expandPool(pool, actionArgs);
                });
                break;
            case SHRINK:
                iterations.forEach(i -> {
                    logger.debug("Shrinking pool [iteration {}/{}]", i + 1, count);
                    shrinkPool(pool, actionArgs);
                });
                break;
            case NOP:
                logger.debug("Breaking off resizing pool because no action was selected");
                return;
        }
    }

    protected String retrieveTemplateName(final long datacenterId) {
        final ConfigKey<String> hypervisorConfigKey = VirtualNetworkApplianceManager.RouterTemplateKvm;
        return hypervisorConfigKey.valueIn(datacenterId);
    }

}
