//

//

package com.cloud.agent.resource.virtualnetwork.model;

public class Site2SiteVpn extends ConfigBase {

    private String localPublicIp, localGuestCidr, localPublicGateway, peerGatewayIp, peerGuestCidrList, espPolicy, ikePolicy, ipsecPsk;
    private Long ikeLifetime, espLifetime;
    private boolean create, dpd, passive, encap;

    public Site2SiteVpn() {
        super(ConfigBase.SITE2SITEVPN);
    }

    public Site2SiteVpn(final String localPublicIp, final String localGuestCidr, final String localPublicGateway, final String peerGatewayIp, final String peerGuestCidrList,
                        final String espPolicy,
                        final String ikePolicy,
                        final String ipsecPsk, final Long ikeLifetime, final Long espLifetime, final boolean create, final Boolean dpd, final boolean passive, final boolean
                                encap) {
        super(ConfigBase.SITE2SITEVPN);
        this.localPublicIp = localPublicIp;
        this.localGuestCidr = localGuestCidr;
        this.localPublicGateway = localPublicGateway;
        this.peerGatewayIp = peerGatewayIp;
        this.peerGuestCidrList = peerGuestCidrList;
        this.espPolicy = espPolicy;
        this.ikePolicy = ikePolicy;
        this.ipsecPsk = ipsecPsk;
        this.ikeLifetime = ikeLifetime;
        this.espLifetime = espLifetime;
        this.create = create;
        this.dpd = dpd;
        this.passive = passive;
        this.encap = encap;
    }

    public String getLocalPublicIp() {
        return localPublicIp;
    }

    public void setLocalPublicIp(final String localPublicIp) {
        this.localPublicIp = localPublicIp;
    }

    public String getLocalGuestCidr() {
        return localGuestCidr;
    }

    public void setLocalGuestCidr(final String localGuestCidr) {
        this.localGuestCidr = localGuestCidr;
    }

    public String getLocalPublicGateway() {
        return localPublicGateway;
    }

    public void setLocalPublicGateway(final String localPublicGateway) {
        this.localPublicGateway = localPublicGateway;
    }

    public String getPeerGatewayIp() {
        return peerGatewayIp;
    }

    public void setPeerGatewayIp(final String peerGatewayIp) {
        this.peerGatewayIp = peerGatewayIp;
    }

    public String getPeerGuestCidrList() {
        return peerGuestCidrList;
    }

    public void setPeerGuestCidrList(final String peerGuestCidrList) {
        this.peerGuestCidrList = peerGuestCidrList;
    }

    public String getEspPolicy() {
        return espPolicy;
    }

    public void setEspPolicy(final String espPolicy) {
        this.espPolicy = espPolicy;
    }

    public String getIkePolicy() {
        return ikePolicy;
    }

    public void setIkePolicy(final String ikePolicy) {
        this.ikePolicy = ikePolicy;
    }

    public String getIpsecPsk() {
        return ipsecPsk;
    }

    public void setIpsecPsk(final String ipsecPsk) {
        this.ipsecPsk = ipsecPsk;
    }

    public Long getIkeLifetime() {
        return ikeLifetime;
    }

    public void setIkeLifetime(final Long ikeLifetime) {
        this.ikeLifetime = ikeLifetime;
    }

    public Long getEspLifetime() {
        return espLifetime;
    }

    public void setEspLifetime(final Long espLifetime) {
        this.espLifetime = espLifetime;
    }

    public boolean isCreate() {
        return create;
    }

    public void setCreate(final boolean create) {
        this.create = create;
    }

    public boolean isDpd() {
        return dpd;
    }

    public void setDpd(final boolean dpd) {
        this.dpd = dpd;
    }

    public boolean isPassive() {
        return passive;
    }

    public void setPassive(final boolean passive) {
        this.passive = passive;
    }

    public boolean getEncap() {
        return encap;
    }

    public void setEncap(final boolean encap) {
        this.encap = encap;
    }
}
