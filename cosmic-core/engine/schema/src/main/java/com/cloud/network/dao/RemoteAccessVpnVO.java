package com.cloud.network.dao;

import com.cloud.network.RemoteAccessVpn;
import com.cloud.utils.db.Encrypt;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = ("remote_access_vpn"))
public class RemoteAccessVpnVO implements RemoteAccessVpn {
    @Column(name = "display", updatable = true, nullable = false)
    protected boolean display = true;
    @Column(name = "account_id")
    private long accountId;
    @Column(name = "network_id")
    private Long networkId;
    @Column(name = "domain_id")
    private long domainId;
    @Column(name = "vpn_server_addr_id")
    private long serverAddressId;
    @Column(name = "local_ip")
    private String localIp;
    @Column(name = "ip_range")
    private String ipRange;
    @Encrypt
    @Column(name = "ipsec_psk")
    private String ipsecPresharedKey;
    @Column(name = "state")
    private State state;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "uuid")
    private String uuid;
    @Column(name = "vpc_id")
    private Long vpcId;

    public RemoteAccessVpnVO() {
        uuid = UUID.randomUUID().toString();
    }

    public RemoteAccessVpnVO(final long accountId, final long domainId, final Long networkId, final long publicIpId, final Long vpcId, final String localIp, final String
            ipRange, final String presharedKey) {
        this.accountId = accountId;
        serverAddressId = publicIpId;
        this.ipRange = ipRange;
        ipsecPresharedKey = presharedKey;
        this.localIp = localIp;
        this.domainId = domainId;
        this.networkId = networkId;
        state = State.Added;
        uuid = UUID.randomUUID().toString();
        this.vpcId = vpcId;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    @Override
    public long getServerAddressId() {
        return serverAddressId;
    }

    @Override
    public String getIpRange() {
        return ipRange;
    }

    public void setIpRange(final String ipRange) {
        this.ipRange = ipRange;
    }

    @Override
    public String getIpsecPresharedKey() {
        return ipsecPresharedKey;
    }

    public void setIpsecPresharedKey(final String ipsecPresharedKey) {
        this.ipsecPresharedKey = ipsecPresharedKey;
    }

    @Override
    public String getLocalIp() {
        return localIp;
    }

    @Override
    public Long getNetworkId() {
        return networkId;
    }

    @Override
    public Long getVpcId() {
        return vpcId;
    }

    @Override
    public State getState() {
        return state;
    }

    public void setState(final State state) {
        this.state = state;
    }

    @Override
    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(final boolean display) {
        this.display = display;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    @Override
    public Class<?> getEntityType() {
        return RemoteAccessVpn.class;
    }
}
