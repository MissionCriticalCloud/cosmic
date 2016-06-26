package org.apache.cloudstack.region;

import com.cloud.user.AccountVO;

public class RegionAccount extends AccountVO {
    String accountUuid;
    String domainUuid;
    String domain;
    String receivedbytes;
    String sentbytes;
    String vmlimit;
    String vmtotal;
    String vmavailable;
    String iplimit;
    String iptotal;
    String ipavailable;
    String volumelimit;
    String volumetotal;
    String volumeavailable;
    String snapshotlimit;
    String snapshottotal;
    String snapshotavailable;
    String templatelimit;
    String templatetotal;
    String templateavailable;
    String vmstopped;
    String vmrunning;
    String projectlimit;
    String projecttotal;
    String projectavailable;
    String networklimit;
    String networktotal;
    String networkavailable;
    RegionUser user;

    public RegionAccount() {
    }

    public String getAccountuuid() {
        return accountUuid;
    }

    public void setAccountuuid(final String accountUuid) {
        this.accountUuid = accountUuid;
    }

    public String getDomainUuid() {
        return domainUuid;
    }

    public void setDomainUuid(final String domainUuid) {
        this.domainUuid = domainUuid;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(final String domain) {
        this.domain = domain;
    }

    public String getReceivedbytes() {
        return receivedbytes;
    }

    public void setReceivedbytes(final String receivedbytes) {
        this.receivedbytes = receivedbytes;
    }

    public String getSentbytes() {
        return sentbytes;
    }

    public void setSentbytes(final String sentbytes) {
        this.sentbytes = sentbytes;
    }

    public String getVmlimit() {
        return vmlimit;
    }

    public void setVmlimit(final String vmlimit) {
        this.vmlimit = vmlimit;
    }

    public String getVmtotal() {
        return vmtotal;
    }

    public void setVmtotal(final String vmtotal) {
        this.vmtotal = vmtotal;
    }

    public String getVmavailable() {
        return vmavailable;
    }

    public void setVmavailable(final String vmavailable) {
        this.vmavailable = vmavailable;
    }

    public String getIplimit() {
        return iplimit;
    }

    public void setIplimit(final String iplimit) {
        this.iplimit = iplimit;
    }

    public String getIptotal() {
        return iptotal;
    }

    public void setIptotal(final String iptotal) {
        this.iptotal = iptotal;
    }

    public String getIpavailable() {
        return ipavailable;
    }

    public void setIpavailable(final String ipavailable) {
        this.ipavailable = ipavailable;
    }

    public String getVolumelimit() {
        return volumelimit;
    }

    public void setVolumelimit(final String volumelimit) {
        this.volumelimit = volumelimit;
    }

    public String getVolumetotal() {
        return volumetotal;
    }

    public void setVolumetotal(final String volumetotal) {
        this.volumetotal = volumetotal;
    }

    public String getVolumeavailable() {
        return volumeavailable;
    }

    public void setVolumeavailable(final String volumeavailable) {
        this.volumeavailable = volumeavailable;
    }

    public String getSnapshotlimit() {
        return snapshotlimit;
    }

    public void setSnapshotlimit(final String snapshotlimit) {
        this.snapshotlimit = snapshotlimit;
    }

    public String getSnapshottotal() {
        return snapshottotal;
    }

    public void setSnapshottotal(final String snapshottotal) {
        this.snapshottotal = snapshottotal;
    }

    public String getSnapshotavailable() {
        return snapshotavailable;
    }

    public void setSnapshotavailable(final String snapshotavailable) {
        this.snapshotavailable = snapshotavailable;
    }

    public String getTemplatelimit() {
        return templatelimit;
    }

    public void setTemplatelimit(final String templatelimit) {
        this.templatelimit = templatelimit;
    }

    public String getTemplatetotal() {
        return templatetotal;
    }

    public void setTemplatetotal(final String templatetotal) {
        this.templatetotal = templatetotal;
    }

    public String getTemplateavailable() {
        return templateavailable;
    }

    public void setTemplateavailable(final String templateavailable) {
        this.templateavailable = templateavailable;
    }

    public String getVmstopped() {
        return vmstopped;
    }

    public void setVmstopped(final String vmstopped) {
        this.vmstopped = vmstopped;
    }

    public String getVmrunning() {
        return vmrunning;
    }

    public void setVmrunning(final String vmrunning) {
        this.vmrunning = vmrunning;
    }

    public String getProjectlimit() {
        return projectlimit;
    }

    public void setProjectlimit(final String projectlimit) {
        this.projectlimit = projectlimit;
    }

    public String getProjecttotal() {
        return projecttotal;
    }

    public void setProjecttotal(final String projecttotal) {
        this.projecttotal = projecttotal;
    }

    public String getProjectavailable() {
        return projectavailable;
    }

    public void setProjectavailable(final String projectavailable) {
        this.projectavailable = projectavailable;
    }

    public String getNetworklimit() {
        return networklimit;
    }

    public void setNetworklimit(final String networklimit) {
        this.networklimit = networklimit;
    }

    public String getNetworktotal() {
        return networktotal;
    }

    public void setNetworktotal(final String networktotal) {
        this.networktotal = networktotal;
    }

    public String getNetworkavailable() {
        return networkavailable;
    }

    public void setNetworkavailable(final String networkavailable) {
        this.networkavailable = networkavailable;
    }

    public RegionUser getUser() {
        return user;
    }

    public void setUser(final RegionUser user) {
        this.user = user;
    }
}
