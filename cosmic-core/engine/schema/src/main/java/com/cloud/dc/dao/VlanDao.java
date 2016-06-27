package com.cloud.dc.dao;

import com.cloud.dc.Vlan;
import com.cloud.dc.Vlan.VlanType;
import com.cloud.dc.VlanVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface VlanDao extends GenericDao<VlanVO, Long> {

    VlanVO findByZoneAndVlanId(long zoneId, String vlanId);

    List<VlanVO> listByZone(long zoneId);

    List<VlanVO> listByType(Vlan.VlanType vlanType);

    List<VlanVO> listByZoneAndType(long zoneId, Vlan.VlanType vlanType);

    List<VlanVO> listVlansForPod(long podId);

    List<VlanVO> listVlansForPodByType(long podId, Vlan.VlanType vlanType);

    void addToPod(long podId, long vlanDbId);

    List<VlanVO> listVlansForAccountByType(Long zoneId, long accountId, VlanType vlanType);

    boolean zoneHasDirectAttachUntaggedVlans(long zoneId);

    List<VlanVO> listZoneWideVlans(long zoneId, VlanType vlanType, String vlanId);

    List<VlanVO> searchForZoneWideVlans(long dcId, String vlanType, String vlanId);

    List<VlanVO> listVlansByNetworkId(long networkId);

    List<VlanVO> listVlansByPhysicalNetworkId(long physicalNetworkId);

    List<VlanVO> listZoneWideNonDedicatedVlans(long zoneId);

    List<VlanVO> listVlansByNetworkIdAndGateway(long networkid, String gateway);

    List<VlanVO> listDedicatedVlans(long accountId);
}
