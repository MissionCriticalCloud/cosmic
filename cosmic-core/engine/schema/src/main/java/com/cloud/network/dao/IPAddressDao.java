package com.cloud.network.dao;

import com.cloud.dc.Vlan.VlanType;
import com.cloud.utils.db.GenericDao;
import com.cloud.utils.net.Ip;

import java.util.List;

public interface IPAddressDao extends GenericDao<IPAddressVO, Long> {

    IPAddressVO markAsUnavailable(long ipAddressId);

    void unassignIpAddress(long ipAddressId);

    List<IPAddressVO> listByAccount(long accountId);

    List<IPAddressVO> listByVlanId(long vlanId);

    List<IPAddressVO> listByIpAddress(String ipAddress);

    List<IPAddressVO> listByDcIdIpAddress(long dcId, String ipAddress);

    List<IPAddressVO> listByDcId(long dcId);

    List<IPAddressVO> listByAssociatedNetwork(long networkId, Boolean isSourceNat);

    List<IPAddressVO> listStaticNatPublicIps(long networkId);

    List<IPAddressVO> listByAclId(long aclId);

    int countIPs(long dcId, boolean onlyCountAllocated);

    int countIPs(long dcId, long vlanDbId, boolean onlyCountAllocated);

    int countIPs(long dcId, Long accountId, String vlanId, String vlanGateway, String vlanNetmask);

    long countAllocatedIPsForAccount(long accountId);

    boolean mark(long dcId, Ip ip);

    int countIPsForNetwork(long dcId, boolean onlyCountAllocated, VlanType vlanType);

    IPAddressVO findByAssociatedVmId(long vmId);

    // for vm secondary ips case mapping is  IP1--> vmIp1, IP2-->vmIp2, etc
    // This method is used when one vm is mapped to muliple to public ips
    List<IPAddressVO> findAllByAssociatedVmId(long vmId);

    IPAddressVO findByIpAndSourceNetworkId(long networkId, String ipAddress);

    List<IPAddressVO> listByVpcAndSourceNetwork(long vpcId, long networkId);

    List<IPAddressVO> listByVpc(long vpcId, Boolean isSourceNat);

    List<IPAddressVO> listByVpcWithAssociatedNetwork(long vpcId);

    long countFreePublicIPs();

    long countFreeIPsInNetwork(long networkId);

    IPAddressVO findByAssociatedVmIdAndVmIp(long vmId, String vmIp);

    IPAddressVO findByIpAndNetworkId(long networkId, String ipAddress);

    long countFreeIpsInVlan(long vlanDbId);

    boolean deletePublicIPRange(long vlanDbId);

    void lockRange(long vlandbId);
}
