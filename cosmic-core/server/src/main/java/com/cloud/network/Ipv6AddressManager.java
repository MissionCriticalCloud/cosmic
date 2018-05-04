package com.cloud.network;

import com.cloud.legacymodel.exceptions.InsufficientAddressCapacityException;
import com.cloud.legacymodel.user.Account;
import com.cloud.utils.component.Manager;

public interface Ipv6AddressManager extends Manager {
    public UserIpv6Address assignDirectIp6Address(long dcId, Account owner, Long networkId, String requestedIp6) throws InsufficientAddressCapacityException;

    public void revokeDirectIpv6Address(long networkId, String ip6Address);
}
