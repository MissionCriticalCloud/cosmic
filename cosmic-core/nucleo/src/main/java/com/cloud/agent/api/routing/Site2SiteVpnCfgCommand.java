//

//

package com.cloud.agent.api.routing;

public class Site2SiteVpnCfgCommand extends NetworkElementCommand {

    private boolean create;
    private String localPublicIp;
    private String localGuestCidr;
    private String localPublicGateway;
    private String peerGatewayIp;
    private String peerGuestCidrList;
    private String ipsecPsk;
    private String ikePolicy;
    private String espPolicy;
    private long ikeLifetime;
    private long espLifetime;
    private boolean dpd;
    private boolean passive;
    private boolean encap;

    public Site2SiteVpnCfgCommand() {
        this.create = false;
    }

    public Site2SiteVpnCfgCommand(final boolean create, final String localPublicIp, final String localPublicGateway, final String localGuestCidr, final String peerGatewayIp,
                                  final String peerGuestCidrList,
                                  final String ikePolicy, final String espPolicy, final String ipsecPsk, final Long ikeLifetime, final Long espLifetime, final Boolean dpd, final
                                  boolean passive, final boolean encap) {
        this.create = create;
        this.setLocalPublicIp(localPublicIp);
        this.setLocalPublicGateway(localPublicGateway);
        this.setLocalGuestCidr(localGuestCidr);
        this.setPeerGatewayIp(peerGatewayIp);
        this.setPeerGuestCidrList(peerGuestCidrList);
        this.ipsecPsk = ipsecPsk;
        this.ikePolicy = ikePolicy;
        this.espPolicy = espPolicy;
        this.ikeLifetime = ikeLifetime;
        this.espLifetime = espLifetime;
        this.dpd = dpd;
        this.passive = passive;
        this.encap = encap;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }

    public boolean isCreate() {
        return create;
    }

    public void setCreate(final boolean create) {
        this.create = create;
    }

    public String getIpsecPsk() {
        return ipsecPsk;
    }

    public void setIpsecPsk(final String ipsecPsk) {
        this.ipsecPsk = ipsecPsk;
    }

    public String getIkePolicy() {
        return ikePolicy;
    }

    public void setIkePolicy(final String ikePolicy) {
        this.ikePolicy = ikePolicy;
    }

    public String getEspPolicy() {
        return espPolicy;
    }

    public void setEspPolicy(final String espPolicy) {
        this.espPolicy = espPolicy;
    }

    public long getIkeLifetime() {
        return ikeLifetime;
    }

    public void setikeLifetime(final long ikeLifetime) {
        this.ikeLifetime = ikeLifetime;
    }

    public long getEspLifetime() {
        return espLifetime;
    }

    public void setEspLifetime(final long espLifetime) {
        this.espLifetime = espLifetime;
    }

    public Boolean getDpd() {
        return dpd;
    }

    public void setDpd(final Boolean dpd) {
        this.dpd = dpd;
    }

    public Boolean getEncap() {
        return encap;
    }

    public void setEncap(final Boolean encap) {
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

    public boolean isPassive() {
        return passive;
    }

    public void setPassive(final boolean passive) {
        this.passive = passive;
    }
}
