package com.cloud.network.guru;

import com.cloud.configuration.ZoneConfig;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.PodVlanMapVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.PodVlanMapDao;
import com.cloud.dc.dao.VlanDao;
import com.cloud.deploy.DeployDestination;
import com.cloud.engine.orchestration.service.NetworkOrchestrationService;
import com.cloud.legacymodel.dc.DataCenter;
import com.cloud.legacymodel.dc.Pod;
import com.cloud.legacymodel.dc.Vlan;
import com.cloud.legacymodel.dc.Vlan.VlanType;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.exceptions.ConcurrentOperationException;
import com.cloud.legacymodel.exceptions.InsufficientAddressCapacityException;
import com.cloud.legacymodel.exceptions.InsufficientVirtualNetworkCapacityException;
import com.cloud.legacymodel.network.Nic;
import com.cloud.legacymodel.network.Nic.ReservationStrategy;
import com.cloud.model.enumeration.BroadcastDomainType;
import com.cloud.model.enumeration.IpAddressFormat;
import com.cloud.model.enumeration.NetworkType;
import com.cloud.model.enumeration.VirtualMachineType;
import com.cloud.network.IpAddressManager;
import com.cloud.network.Network;
import com.cloud.network.Networks.IsolationType;
import com.cloud.network.addr.PublicIp;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.network.dao.IPAddressVO;
import com.cloud.offering.NetworkOffering;
import com.cloud.offerings.dao.NetworkOfferingDao;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.TransactionCallbackNoReturn;
import com.cloud.utils.db.TransactionCallbackWithExceptionNoReturn;
import com.cloud.utils.db.TransactionStatus;
import com.cloud.vm.NicProfile;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.VirtualMachineProfile;

import javax.inject.Inject;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectPodBasedNetworkGuru extends DirectNetworkGuru {
    private static final Logger s_logger = LoggerFactory.getLogger(DirectPodBasedNetworkGuru.class);

    @Inject
    DataCenterDao _dcDao;
    @Inject
    VlanDao _vlanDao;
    @Inject
    NetworkOrchestrationService _networkMgr;
    @Inject
    IPAddressDao _ipAddressDao;
    @Inject
    NetworkOfferingDao _networkOfferingDao;
    @Inject
    PodVlanMapDao _podVlanDao;
    @Inject
    IpAddressManager _ipAddrMgr;

    @Override
    protected boolean canHandle(final NetworkOffering offering, final DataCenter dc) {
        // this guru handles system Direct pod based network
        if (dc.getNetworkType() == NetworkType.Basic && isMyTrafficType(offering.getTrafficType())) {
            return true;
        } else {
            s_logger.trace("We only take care of Guest Direct Pod based networks");
            return false;
        }
    }

    @Override
    public NicProfile allocate(final Network network, NicProfile nic, final VirtualMachineProfile vm) throws InsufficientVirtualNetworkCapacityException,
            InsufficientAddressCapacityException, ConcurrentOperationException {

        final DataCenterVO dc = _dcDao.findById(network.getDataCenterId());
        ReservationStrategy rsStrategy = ReservationStrategy.Start;
        _dcDao.loadDetails(dc);
        final String dhcpStrategy = dc.getDetail(ZoneConfig.DhcpStrategy.key());
        if ("external".equalsIgnoreCase(dhcpStrategy)) {
            rsStrategy = ReservationStrategy.Create;
        }

        if (nic != null && nic.getRequestedIPv4() != null) {
            throw new CloudRuntimeException("Does not support custom ip allocation at this time: " + nic);
        }

        if (nic == null) {
            nic = new NicProfile(rsStrategy, null, null, null, null);
        } else if (nic.getIPv4Address() == null) {
            nic.setReservationStrategy(ReservationStrategy.Start);
        } else {
            nic.setReservationStrategy(ReservationStrategy.Create);
        }

        if (rsStrategy == ReservationStrategy.Create) {
            final String mac = _networkModel.getNextAvailableMacAddressInNetwork(network.getId());
            nic.setMacAddress(mac);
        }
        return nic;
    }

    @Override
    @DB
    public void reserve(final NicProfile nic, final Network network, final VirtualMachineProfile vm, final DeployDestination dest, final ReservationContext context)
            throws InsufficientVirtualNetworkCapacityException, InsufficientAddressCapacityException, ConcurrentOperationException {

        final String oldIp = nic.getIPv4Address();
        boolean getNewIp = false;

        if (oldIp == null) {
            getNewIp = true;
        } else {
            // we need to get a new ip address if we try to deploy a vm in a different pod
            final IPAddressVO ipVO = _ipAddressDao.findByIpAndSourceNetworkId(network.getId(), oldIp);
            if (ipVO != null) {
                final PodVlanMapVO mapVO = _podVlanDao.listPodVlanMapsByVlan(ipVO.getVlanId());
                if (mapVO.getPodId() != dest.getPod().getId()) {
                    Transaction.execute(new TransactionCallbackNoReturn() {
                        @Override
                        public void doInTransactionWithoutResult(final TransactionStatus status) {
                            //release the old ip here
                            _ipAddrMgr.markIpAsUnavailable(ipVO.getId());
                            _ipAddressDao.unassignIpAddress(ipVO.getId());
                        }
                    });

                    nic.setIPv4Address(null);
                    getNewIp = true;
                }
            }
        }

        if (getNewIp) {
            //we don't set reservationStrategy to Create because we need this method to be called again for the case when vm fails to deploy in Pod1, and we try to redeploy it
            // in Pod2
            getIp(nic, dest.getPod(), vm, network);
        }

        final DataCenter dc = _dcDao.findById(network.getDataCenterId());
        nic.setIPv4Dns1(dc.getDns1());
        nic.setIPv4Dns2(dc.getDns2());
    }

    @DB
    protected void getIp(final NicProfile nic, final Pod pod, final VirtualMachineProfile vm, final Network network) throws InsufficientVirtualNetworkCapacityException,
            InsufficientAddressCapacityException, ConcurrentOperationException {
        final DataCenter dc = _dcDao.findById(pod.getDataCenterId());
        if (nic.getIPv4Address() == null) {
            Transaction.execute(new TransactionCallbackWithExceptionNoReturn<InsufficientAddressCapacityException>() {
                @Override
                public void doInTransactionWithoutResult(final TransactionStatus status) throws InsufficientAddressCapacityException {
                    PublicIp ip = null;
                    final List<PodVlanMapVO> podRefs = _podVlanDao.listPodVlanMapsByPod(pod.getId());
                    String podRangeGateway = null;
                    if (!podRefs.isEmpty()) {
                        podRangeGateway = _vlanDao.findById(podRefs.get(0).getVlanDbId()).getVlanGateway();
                    }
                    //Get ip address from the placeholder and don't allocate a new one
                    if (vm.getType() == VirtualMachineType.DomainRouter) {
                        final Nic placeholderNic = _networkModel.getPlaceholderNicForRouter(network, pod.getId());
                        if (placeholderNic != null) {
                            final IPAddressVO userIp = _ipAddressDao.findByIpAndSourceNetworkId(network.getId(), placeholderNic.getIPv4Address());
                            ip = PublicIp.createFromAddrAndVlan(userIp, _vlanDao.findById(userIp.getVlanId()));
                            s_logger.debug("Nic got an ip address " + placeholderNic.getIPv4Address() + " stored in placeholder nic for the network " + network +
                                    " and gateway " + podRangeGateway);
                        }
                    }

                    if (ip == null) {
                        ip = _ipAddrMgr.assignPublicIpAddress(dc.getId(), pod.getId(), vm.getOwner(), VlanType.DirectAttached, network.getId(), null, false);
                    }

                    nic.setIPv4Address(ip.getAddress().toString());
                    nic.setFormat(IpAddressFormat.Ip4);
                    nic.setIPv4Gateway(ip.getGateway());
                    nic.setIPv4Netmask(ip.getNetmask());
                    if (ip.getVlanTag() != null && ip.getVlanTag().equalsIgnoreCase(Vlan.UNTAGGED)) {
                        nic.setIsolationUri(IsolationType.Ec2.toUri(Vlan.UNTAGGED));
                        nic.setBroadcastUri(BroadcastDomainType.Vlan.toUri(Vlan.UNTAGGED));
                        nic.setBroadcastType(BroadcastDomainType.Native);
                    }
                    nic.setReservationId(String.valueOf(ip.getVlanTag()));
                    nic.setMacAddress(ip.getMacAddress());

                    //save the placeholder nic if the vm is the Virtual router
                    if (vm.getType() == VirtualMachineType.DomainRouter) {
                        final Nic placeholderNic = _networkModel.getPlaceholderNicForRouter(network, pod.getId());
                        if (placeholderNic == null) {
                            s_logger.debug("Saving placeholder nic with ip4 address " + nic.getIPv4Address() + " for the network " + network);
                            _networkMgr.savePlaceholderNic(network, nic.getIPv4Address(), null, VirtualMachineType.DomainRouter);
                        }
                    }
                }
            });
        }
        nic.setIPv4Dns1(dc.getDns1());
        nic.setIPv4Dns2(dc.getDns2());
    }
}
