package com.cloud.vm;

import com.cloud.hypervisor.Hypervisor.HypervisorType;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.util.Date;

/**
 * ConsoleProxyVO domain object
 */

@Entity
@Table(name = "console_proxy")
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue(value = "ConsoleProxy")
public class ConsoleProxyVO extends VMInstanceVO implements ConsoleProxy {

    @Column(name = "public_ip_address", nullable = false)
    private String publicIpAddress;

    @Column(name = "public_mac_address", nullable = false)
    private String publicMacAddress;

    @Column(name = "public_netmask", nullable = false)
    private String publicNetmask;

    @Column(name = "active_session", updatable = true, nullable = false)
    private int activeSession;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_update", updatable = true, nullable = true)
    private Date lastUpdateTime;

    @Column(name = "session_details", updatable = true, nullable = true)
    private byte[] sessionDetails;

    @Transient
    private boolean sslEnabled = false;

    @Transient
    private int port;

    /**
     * Correct constructor to use.
     */
    public ConsoleProxyVO(final long id, final long serviceOfferingId, final String name, final long templateId, final HypervisorType hypervisorType, final long guestOSId, final
    long dataCenterId, final long domainId,
                          final long accountId, final long userId, final int activeSession, final boolean haEnabled) {
        super(id, serviceOfferingId, name, name, Type.ConsoleProxy, templateId, hypervisorType, guestOSId, domainId, accountId, userId, haEnabled);
        this.activeSession = activeSession;
        this.dataCenterId = dataCenterId;
    }

    protected ConsoleProxyVO() {
        super();
    }

    @Override
    public String getPublicIpAddress() {
        return publicIpAddress;
    }

    public void setPublicIpAddress(final String publicIpAddress) {
        this.publicIpAddress = publicIpAddress;
    }

    @Override
    public String getPublicNetmask() {
        return publicNetmask;
    }

    public void setPublicNetmask(final String publicNetmask) {
        this.publicNetmask = publicNetmask;
    }

    @Override
    public String getPublicMacAddress() {
        return publicMacAddress;
    }

    public void setPublicMacAddress(final String publicMacAddress) {
        this.publicMacAddress = publicMacAddress;
    }

    @Override
    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(final Date time) {
        lastUpdateTime = time;
    }

    @Override
    public int getActiveSession() {
        return activeSession;
    }

    public void setActiveSession(final int activeSession) {
        this.activeSession = activeSession;
    }

    @Override
    public byte[] getSessionDetails() {
        return sessionDetails;
    }

    public void setSessionDetails(final byte[] details) {
        sessionDetails = details;
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }

    public void setSslEnabled(final boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }
}
