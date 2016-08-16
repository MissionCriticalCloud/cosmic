package com.cloud.systemvm;

import static com.cloud.utils.CloudConstants.PROPERTY_LIST_SEPARATOR;

import com.cloud.cluster.ClusterManager;
import com.cloud.dc.DataCenter;
import com.cloud.dc.DataCenter.NetworkType;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.host.dao.HostDao;
import com.cloud.info.RunningHostCountInfo;
import com.cloud.info.RunningHostInfoAgregator;
import com.cloud.info.RunningHostInfoAgregator.ZoneHostInfo;
import com.cloud.managementserver.ManagementServerService;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.dao.IPAddressVO;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.NetworkVO;
import com.cloud.network.rules.RulesManager;
import com.cloud.utils.DateUtil;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.NicProfile;
import com.cloud.vm.SystemVm;
import com.cloud.vm.VirtualMachineProfile;
import org.apache.cloudstack.context.CallContext;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SystemVmManagerBase extends ManagerBase {
    private static final Logger logger = LoggerFactory.getLogger(SystemVmManagerBase.class);

    protected void computeVmIps(final SystemVm vmVO, final DataCenter dc, final List<NicProfile> nics) {
        for (final NicProfile nic : nics) {
            if ((nic.getTrafficType() == TrafficType.Public && dc.getNetworkType() == NetworkType.Advanced) ||
                    (nic.getTrafficType() == TrafficType.Guest && (dc.getNetworkType() == NetworkType.Basic || dc.isSecurityGroupEnabled()))) {
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

    protected static NetworkVO getNetworkForAdvancedZone(final DataCenter dc, final NetworkDao _networkDao) {
        if (dc.getNetworkType() != NetworkType.Advanced) {
            throw new CloudRuntimeException("Zone " + dc + " is not advanced.");
        }

        if (dc.isSecurityGroupEnabled()) {
            final List<NetworkVO> networks = _networkDao.listByZoneSecurityGroup(dc.getId());
            if (CollectionUtils.isEmpty(networks)) {
                throw new CloudRuntimeException("Can not found security enabled network in SG Zone " + dc);
            }

            return networks.get(0);
        } else {
            final TrafficType defaultTrafficType = TrafficType.Public;
            final List<NetworkVO> defaultNetworks = _networkDao.listByZoneAndTrafficType(dc.getId(), defaultTrafficType);
            // api should never allow this situation to happen
            if (defaultNetworks.size() != 1) {
                throw new CloudRuntimeException("Found " + defaultNetworks.size() + " networks of type " + defaultTrafficType + " when expect to find 1");
            }

            return defaultNetworks.get(0);
        }
    }

    protected static NetworkVO getNetworkForBasicZone(final DataCenter dc, final NetworkDao _networkDao) {
        if (dc.getNetworkType() != NetworkType.Basic) {
            throw new CloudRuntimeException("Zone " + dc + "is not basic.");
        }

        final TrafficType defaultTrafficType = TrafficType.Guest;
        final List<NetworkVO> defaultNetworks = _networkDao.listByZoneAndTrafficType(dc.getId(), defaultTrafficType);
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
}
