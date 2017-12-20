package com.cloud.network.rules;

import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.dc.dao.VlanDao;
import com.cloud.network.IpAddressManager;
import com.cloud.network.NetworkModel;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.network.dao.LoadBalancerDao;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.lb.LoadBalancingRulesManager;
import com.cloud.network.router.NetworkHelper;
import com.cloud.network.router.NicProfileHelper;
import com.cloud.network.vpc.NetworkACLManager;
import com.cloud.network.vpc.VpcManager;
import com.cloud.network.vpc.dao.PrivateIpDao;
import com.cloud.network.vpc.dao.VpcDao;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.user.dao.UserStatisticsDao;
import com.cloud.vm.VirtualMachineManager;
import com.cloud.vm.dao.NicDao;
import com.cloud.vm.dao.UserVmDao;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Qualifier;

public class VirtualNetworkApplianceFactory {

    @Inject
    private NetworkModel _networkModel;
    @Inject
    private LoadBalancingRulesManager _lbMgr;
    @Inject
    private LoadBalancerDao _loadBalancerDao;
    @Inject
    private NicDao _nicDao;
    @Inject
    private VirtualMachineManager _itMgr;
    @Inject
    private DataCenterDao _dcDao;
    @Inject
    private UserVmDao _userVmDao;
    @Inject
    private UserStatisticsDao _userStatsDao;
    @Inject
    private VpcDao _vpcDao;
    @Inject
    private VpcManager _vpcMgr;
    @Inject
    private VMTemplateDao _templateDao;
    @Inject
    private NetworkDao _networkDao;
    @Inject
    private HostPodDao _podDao;
    @Inject
    private VlanDao _vlanDao;
    @Inject
    private IPAddressDao _ipAddressDao;
    @Inject
    private PrivateIpDao _privateIpDao;
    @Inject
    private IpAddressManager _ipAddrMgr;
    @Inject
    private NetworkACLManager _networkACLMgr;

    @Inject
    @Qualifier("networkHelper")
    private NetworkHelper _networkHelper;

    @Inject
    private NicProfileHelper _nicProfileHelper;

    public NetworkModel getNetworkModel() {
        return _networkModel;
    }

    public LoadBalancingRulesManager getLbMgr() {
        return _lbMgr;
    }

    public LoadBalancerDao getLoadBalancerDao() {
        return _loadBalancerDao;
    }

    public NicDao getNicDao() {
        return _nicDao;
    }

    public VirtualMachineManager getItMgr() {
        return _itMgr;
    }

    public DataCenterDao getDcDao() {
        return _dcDao;
    }

    public UserVmDao getUserVmDao() {
        return _userVmDao;
    }

    public UserStatisticsDao getUserStatsDao() {
        return _userStatsDao;
    }

    public VpcDao getVpcDao() {
        return _vpcDao;
    }

    public VpcManager getVpcMgr() {
        return _vpcMgr;
    }

    public VMTemplateDao getTemplateDao() {
        return _templateDao;
    }

    public NetworkDao getNetworkDao() {
        return _networkDao;
    }

    public HostPodDao getPodDao() {
        return _podDao;
    }

    public VlanDao getVlanDao() {
        return _vlanDao;
    }

    public IPAddressDao getIpAddressDao() {
        return _ipAddressDao;
    }

    public PrivateIpDao getPrivateIpDao() {
        return _privateIpDao;
    }

    public IpAddressManager getIpAddrMgr() {
        return _ipAddrMgr;
    }

    public NetworkACLManager getNetworkACLMgr() {
        return _networkACLMgr;
    }

    public NetworkHelper getNetworkHelper() {
        return _networkHelper;
    }

    public NicProfileHelper getNicProfileHelper() {
        return _nicProfileHelper;
    }
}
