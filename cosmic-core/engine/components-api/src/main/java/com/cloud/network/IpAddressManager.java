package com.cloud.network;

import com.cloud.dc.DataCenter;
import com.cloud.dc.Vlan.VlanType;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientAddressCapacityException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InsufficientVirtualNetworkCapacityException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.addr.PublicIp;
import com.cloud.network.dao.IPAddressVO;
import com.cloud.network.rules.FirewallRule;
import com.cloud.network.rules.StaticNat;
import com.cloud.user.Account;
import com.cloud.utils.db.DB;
import com.cloud.vm.NicProfile;
import com.cloud.vm.VirtualMachineProfile;
import org.apache.cloudstack.framework.config.ConfigKey;

import java.util.List;

public interface IpAddressManager {
    static final String UseSystemPublicIpsCK = "use.system.public.ips";
    static final ConfigKey<Boolean> UseSystemPublicIps = new ConfigKey<>("Advanced", Boolean.class, UseSystemPublicIpsCK, "true",
            "If true, when account has dedicated public ip range(s), once the ips dedicated to the account have been consumed ips will be acquired from the system pool",
            true, ConfigKey.Scope.Account);

    /**
     * Assigns a new public ip address.
     *
     * @param dcId
     * @param podId       TODO
     * @param owner
     * @param type
     * @param networkId
     * @param requestedIp TODO
     * @param allocatedBy TODO
     * @return
     * @throws InsufficientAddressCapacityException
     */
    PublicIp assignPublicIpAddress(long dcId, Long podId, Account owner, VlanType type, Long networkId, String requestedIp, boolean isSystem)
            throws InsufficientAddressCapacityException;

    /**
     * Do all of the work of releasing public ip addresses. Note that if this method fails, there can be side effects.
     *
     * @param userId
     * @param caller    TODO
     * @param IpAddress
     * @return true if it did; false if it didn't
     */
    boolean disassociatePublicIpAddress(long id, long userId, Account caller);

    boolean applyRules(List<? extends FirewallRule> rules, FirewallRule.Purpose purpose, NetworkRuleApplier applier, boolean continueOnError)
            throws ResourceUnavailableException;

    /**
     * @param userId
     * @param accountId
     * @param zoneId
     * @param vlanId
     * @throws ResourceAllocationException          TODO
     * @throws InsufficientCapacityException        Associates an ip address list to an account. The list of ip addresses are all addresses associated
     *                                              with the
     *                                              given vlan id.
     * @throws InsufficientAddressCapacityException
     * @throws
     */
    boolean associateIpAddressListToAccount(long userId, long accountId, long zoneId, Long vlanId, Network guestNetwork) throws InsufficientCapacityException,
            ConcurrentOperationException, ResourceUnavailableException, ResourceAllocationException;

    boolean applyIpAssociations(Network network, boolean continueOnError) throws ResourceUnavailableException;

    boolean applyIpAssociations(Network network, boolean rulesRevoked, boolean continueOnError, List<? extends PublicIpAddress> publicIps)
            throws ResourceUnavailableException;

    IPAddressVO markIpAsUnavailable(long addrId);

    public String acquireGuestIpAddress(Network network, String requestedIp);

    boolean applyStaticNats(List<? extends StaticNat> staticNats, boolean continueOnError, boolean forRevoke) throws ResourceUnavailableException;

    IpAddress assignSystemIp(long networkId, Account owner, boolean forElasticLb, boolean forElasticIp) throws InsufficientAddressCapacityException;

    boolean handleSystemIpRelease(IpAddress ip);

    void allocateDirectIp(NicProfile nic, DataCenter dc, VirtualMachineProfile vm, Network network, String requestedIpv4, String requestedIpv6)
            throws InsufficientVirtualNetworkCapacityException, InsufficientAddressCapacityException;

    /**
     * @param owner
     * @param guestNetwork
     * @return
     * @throws ConcurrentOperationException
     * @throws InsufficientAddressCapacityException
     */
    PublicIp assignSourceNatIpAddressToGuestNetwork(Account owner, Network guestNetwork) throws InsufficientAddressCapacityException, ConcurrentOperationException;

    /**
     * @param ipAddrId
     * @param networkId
     * @param releaseOnFailure TODO
     */
    IPAddressVO associateIPToGuestNetwork(long ipAddrId, long networkId, boolean releaseOnFailure) throws ResourceAllocationException, ResourceUnavailableException,
            InsufficientAddressCapacityException, ConcurrentOperationException;

    IpAddress allocatePortableIp(Account ipOwner, Account caller, long dcId, Long networkId, Long vpcID) throws ConcurrentOperationException,
            ResourceAllocationException, InsufficientAddressCapacityException;

    boolean releasePortableIpAddress(long addrId);

    IPAddressVO associatePortableIPToGuestNetwork(long ipAddrId, long networkId, boolean releaseOnFailure) throws ResourceAllocationException,
            ResourceUnavailableException, InsufficientAddressCapacityException, ConcurrentOperationException;

    IPAddressVO disassociatePortableIPToGuestNetwork(long ipAddrId, long networkId) throws ResourceAllocationException, ResourceUnavailableException,
            InsufficientAddressCapacityException, ConcurrentOperationException;

    boolean isPortableIpTransferableFromNetwork(long ipAddrId, long networkId);

    void transferPortableIP(long ipAddrId, long currentNetworkId, long newNetworkId) throws ResourceAllocationException, ResourceUnavailableException,
            InsufficientAddressCapacityException, ConcurrentOperationException;

    /**
     * @param addr
     */
    void markPublicIpAsAllocated(IPAddressVO addr);

    /**
     * @param owner
     * @param guestNtwkId
     * @param vpcId
     * @param dcId
     * @param isSourceNat
     * @return
     * @throws ConcurrentOperationException
     * @throws InsufficientAddressCapacityException
     */
    PublicIp assignDedicateIpAddress(Account owner, Long guestNtwkId, Long vpcId, long dcId, boolean isSourceNat) throws ConcurrentOperationException,
            InsufficientAddressCapacityException;

    IpAddress allocateIp(Account ipOwner, boolean isSystem, Account caller, long callerId, DataCenter zone, Boolean displayIp) throws ConcurrentOperationException,
            ResourceAllocationException, InsufficientAddressCapacityException;

    PublicIp assignPublicIpAddressFromVlans(long dcId, Long podId, Account owner, VlanType type, List<Long> vlanDbIds, Long networkId, String requestedIp,
                                            boolean isSystem) throws InsufficientAddressCapacityException;

    @DB
    void allocateNicValues(NicProfile nic, DataCenter dc, VirtualMachineProfile vm, Network network, String requestedIpv4,
                           String requestedIpv6) throws InsufficientVirtualNetworkCapacityException, InsufficientAddressCapacityException;

    int getRuleCountForIp(Long addressId, FirewallRule.Purpose purpose, FirewallRule.State state);

    public String allocateGuestIP(Network network, String requestedIp) throws InsufficientAddressCapacityException;

    String allocatePublicIpForGuestNic(Network network, Long podId, Account ipOwner, String requestedIp) throws InsufficientAddressCapacityException;
}
