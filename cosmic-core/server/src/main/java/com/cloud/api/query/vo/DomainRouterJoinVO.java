package com.cloud.api.query.vo;

import com.cloud.legacymodel.network.VirtualRouter;
import com.cloud.legacymodel.network.VirtualRouter.RedundantState;
import com.cloud.legacymodel.vm.VirtualMachine;
import com.cloud.legacymodel.vm.VirtualMachine.State;
import com.cloud.model.enumeration.GuestType;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.model.enumeration.TrafficType;
import com.cloud.utils.db.GenericDao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.net.URI;
import java.util.Date;

@Entity
@Table(name = "domain_router_view")
public class DomainRouterJoinVO extends BaseViewVO implements ControlledViewEntity {

    @Column(name = "is_redundant_router")
    boolean isRedundantRouter;
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private long id;
    @Column(name = "name", updatable = false, nullable = false, length = 255)
    private String name = null;
    @Column(name = "account_id")
    private long accountId;
    @Column(name = "account_uuid")
    private String accountUuid;
    @Column(name = "account_name")
    private String accountName = null;
    @Column(name = "account_type")
    private short accountType;
    @Column(name = "domain_id")
    private long domainId;
    @Column(name = "domain_uuid")
    private String domainUuid;
    @Column(name = "domain_name")
    private String domainName = null;
    @Column(name = "domain_path")
    private String domainPath = null;
    /**
     * Note that state is intentionally missing the setter.  Any updates to
     * the state machine needs to go through the DAO object because someone
     * else could be updating it as well.
     */
    @Enumerated(value = EnumType.STRING)
    @Column(name = "state", updatable = true, nullable = false, length = 32)
    private State state = null;
    @Column(name = GenericDao.CREATED_COLUMN)
    private Date created;
    @Column(name = GenericDao.REMOVED_COLUMN)
    private Date removed;
    @Column(name = "instance_name", updatable = true, nullable = false)
    private String instanceName;
    @Column(name = "pod_id", updatable = true, nullable = false)
    private Long podId;
    @Column(name = "pod_uuid")
    private String podUuid;
    @Column(name = "data_center_id")
    private long dataCenterId;
    @Column(name = "data_center_uuid")
    private String dataCenterUuid;
    @Column(name = "data_center_name")
    private String dataCenterName = null;
    @Column(name = "cluster_id")
    private long clusterId;
    @Column(name = "dns1")
    private String dns1 = null;
    @Column(name = "dns2")
    private String dns2 = null;
    @Column(name = "ip6_dns1")
    private String ip6Dns1 = null;
    @Column(name = "ip6_dns2")
    private String ip6Dns2 = null;
    @Column(name = "host_id", updatable = true, nullable = true)
    private Long hostId;
    @Column(name = "host_uuid")
    private String hostUuid;
    @Column(name = "host_name", nullable = false)
    private String hostName;
    @Column(name = "hypervisor_type")
    @Enumerated(value = EnumType.STRING)
    private HypervisorType hypervisorType;
    @Column(name = "template_id", updatable = true, nullable = true, length = 17)
    private long templateId;
    @Column(name = "template_uuid")
    private String templateUuid;
    @Column(name = "service_offering_id")
    private long serviceOfferingId;
    @Column(name = "service_offering_uuid")
    private String serviceOfferingUuid;
    @Column(name = "service_offering_name")
    private String serviceOfferingName;
    @Column(name = "vpc_id")
    private long vpcId;
    @Column(name = "vpc_uuid")
    private String vpcUuid;
    @Column(name = "vpc_name")
    private String vpcName;
    @Column(name = "nic_id")
    private long nicId;
    @Column(name = "nic_uuid")
    private String nicUuid;
    @Column(name = "is_default_nic")
    private boolean isDefaultNic;
    @Column(name = "ip_address")
    private String ipAddress;
    @Column(name = "gateway")
    private String gateway;
    @Column(name = "netmask")
    private String netmask;
    @Column(name = "ip6_address")
    private String ip6Address;
    @Column(name = "ip6_gateway")
    private String ip6Gateway;
    @Column(name = "ip6_cidr")
    private String ip6Cidr;
    @Column(name = "mac_address")
    private String macAddress;
    @Column(name = "broadcast_uri")
    private URI broadcastUri;
    @Column(name = "isolation_uri")
    private URI isolationUri;
    @Column(name = "network_id")
    private long networkId;
    @Column(name = "network_uuid")
    private String networkUuid;
    @Column(name = "network_name")
    private String networkName;
    @Column(name = "network_domain")
    private String networkDomain;
    @Column(name = "traffic_type")
    @Enumerated(value = EnumType.STRING)
    private TrafficType trafficType;
    @Column(name = "project_id")
    private long projectId;
    @Column(name = "project_uuid")
    private String projectUuid;
    @Column(name = "project_name")
    private String projectName;
    @Column(name = "job_id")
    private Long jobId;
    @Column(name = "job_uuid")
    private String jobUuid;
    @Column(name = "job_status")
    private int jobStatus;
    @Column(name = "uuid")
    private String uuid;
    @Column(name = "template_version")
    private String templateVersion;
    @Column(name = "scripts_version")
    private String scriptsVersion;
    @Column(name = "redundant_state")
    @Enumerated(EnumType.STRING)
    private RedundantState redundantState;
    @Column(name = "guest_type")
    @Enumerated(value = EnumType.STRING)
    private GuestType guestType;

    @Column(name = "role")
    @Enumerated(value = EnumType.STRING)
    private VirtualRouter.Role role;

    public DomainRouterJoinVO() {
    }

    public void setRedundantRouter(final boolean redundantRouter) {
        isRedundantRouter = redundantRouter;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setAccountId(final long accountId) {
        this.accountId = accountId;
    }

    public void setAccountUuid(final String accountUuid) {
        this.accountUuid = accountUuid;
    }

    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    public void setAccountType(final short accountType) {
        this.accountType = accountType;
    }

    public void setDomainId(final long domainId) {
        this.domainId = domainId;
    }

    public void setDomainUuid(final String domainUuid) {
        this.domainUuid = domainUuid;
    }

    public void setDomainName(final String domainName) {
        this.domainName = domainName;
    }

    public void setDomainPath(final String domainPath) {
        this.domainPath = domainPath;
    }

    public void setState(final State state) {
        this.state = state;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public void setRemoved(final Date removed) {
        this.removed = removed;
    }

    public void setInstanceName(final String instanceName) {
        this.instanceName = instanceName;
    }

    public void setPodId(final Long podId) {
        this.podId = podId;
    }

    public void setPodUuid(final String podUuid) {
        this.podUuid = podUuid;
    }

    public void setDataCenterId(final long dataCenterId) {
        this.dataCenterId = dataCenterId;
    }

    public void setDataCenterUuid(final String dataCenterUuid) {
        this.dataCenterUuid = dataCenterUuid;
    }

    public void setDataCenterName(final String dataCenterName) {
        this.dataCenterName = dataCenterName;
    }

    public void setClusterId(final long clusterId) {
        this.clusterId = clusterId;
    }

    public void setDns1(final String dns1) {
        this.dns1 = dns1;
    }

    public void setDns2(final String dns2) {
        this.dns2 = dns2;
    }

    public void setIp6Dns1(final String ip6Dns1) {
        this.ip6Dns1 = ip6Dns1;
    }

    public void setIp6Dns2(final String ip6Dns2) {
        this.ip6Dns2 = ip6Dns2;
    }

    public void setHostId(final Long hostId) {
        this.hostId = hostId;
    }

    public void setHostUuid(final String hostUuid) {
        this.hostUuid = hostUuid;
    }

    public void setHostName(final String hostName) {
        this.hostName = hostName;
    }

    public void setHypervisorType(final HypervisorType hypervisorType) {
        this.hypervisorType = hypervisorType;
    }

    public void setTemplateId(final long templateId) {
        this.templateId = templateId;
    }

    public void setTemplateUuid(final String templateUuid) {
        this.templateUuid = templateUuid;
    }

    public void setServiceOfferingId(final long serviceOfferingId) {
        this.serviceOfferingId = serviceOfferingId;
    }

    public void setServiceOfferingUuid(final String serviceOfferingUuid) {
        this.serviceOfferingUuid = serviceOfferingUuid;
    }

    public void setServiceOfferingName(final String serviceOfferingName) {
        this.serviceOfferingName = serviceOfferingName;
    }

    public void setVpcId(final long vpcId) {
        this.vpcId = vpcId;
    }

    public void setVpcUuid(final String vpcUuid) {
        this.vpcUuid = vpcUuid;
    }

    public void setVpcName(final String vpcName) {
        this.vpcName = vpcName;
    }

    public void setNicId(final long nicId) {
        this.nicId = nicId;
    }

    public void setNicUuid(final String nicUuid) {
        this.nicUuid = nicUuid;
    }

    public void setDefaultNic(final boolean defaultNic) {
        isDefaultNic = defaultNic;
    }

    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setGateway(final String gateway) {
        this.gateway = gateway;
    }

    public void setNetmask(final String netmask) {
        this.netmask = netmask;
    }

    public void setIp6Address(final String ip6Address) {
        this.ip6Address = ip6Address;
    }

    public void setIp6Gateway(final String ip6Gateway) {
        this.ip6Gateway = ip6Gateway;
    }

    public void setIp6Cidr(final String ip6Cidr) {
        this.ip6Cidr = ip6Cidr;
    }

    public void setMacAddress(final String macAddress) {
        this.macAddress = macAddress;
    }

    public void setBroadcastUri(final URI broadcastUri) {
        this.broadcastUri = broadcastUri;
    }

    public void setIsolationUri(final URI isolationUri) {
        this.isolationUri = isolationUri;
    }

    public void setNetworkId(final long networkId) {
        this.networkId = networkId;
    }

    public void setNetworkUuid(final String networkUuid) {
        this.networkUuid = networkUuid;
    }

    public void setNetworkName(final String networkName) {
        this.networkName = networkName;
    }

    public void setNetworkDomain(final String networkDomain) {
        this.networkDomain = networkDomain;
    }

    public void setTrafficType(final TrafficType trafficType) {
        this.trafficType = trafficType;
    }

    public void setProjectId(final long projectId) {
        this.projectId = projectId;
    }

    public void setProjectUuid(final String projectUuid) {
        this.projectUuid = projectUuid;
    }

    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    public void setJobId(final Long jobId) {
        this.jobId = jobId;
    }

    public void setJobUuid(final String jobUuid) {
        this.jobUuid = jobUuid;
    }

    public void setJobStatus(final int jobStatus) {
        this.jobStatus = jobStatus;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public void setTemplateVersion(final String templateVersion) {
        this.templateVersion = templateVersion;
    }

    public void setScriptsVersion(final String scriptsVersion) {
        this.scriptsVersion = scriptsVersion;
    }

    public void setRedundantState(final RedundantState redundantState) {
        this.redundantState = redundantState;
    }

    public void setGuestType(final GuestType guestType) {
        this.guestType = guestType;
    }

    public void setRole(final VirtualRouter.Role role) {
        this.role = role;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    @Override
    public String getDomainPath() {
        return domainPath;
    }

    @Override
    public short getAccountType() {
        return accountType;
    }

    @Override
    public String getAccountUuid() {
        return accountUuid;
    }

    @Override
    public String getAccountName() {
        return accountName;
    }

    @Override
    public String getDomainUuid() {
        return domainUuid;
    }

    @Override
    public String getDomainName() {
        return domainName;
    }

    @Override
    public String getProjectUuid() {
        return projectUuid;
    }

    @Override
    public String getProjectName() {
        return projectName;
    }

    public State getState() {
        return state;
    }

    public Date getCreated() {
        return created;
    }

    public Date getRemoved() {
        return removed;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public String getPodUuid() {
        return podUuid;
    }

    public String getDataCenterUuid() {
        return dataCenterUuid;
    }

    public String getDataCenterName() {
        return dataCenterName;
    }

    public Long getHostId() {
        return hostId;
    }

    public String getHostUuid() {
        return hostUuid;
    }

    public String getHostName() {
        return hostName;
    }

    public HypervisorType getHypervisorType() {
        return hypervisorType;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public long getTemplateId() {
        return templateId;
    }

    public String getTemplateUuid() {
        return templateUuid;
    }

    public String getServiceOfferingUuid() {
        return serviceOfferingUuid;
    }

    public String getServiceOfferingName() {
        return serviceOfferingName;
    }

    public long getVpcId() {
        return vpcId;
    }

    public String getVpcName() {
        return vpcName;
    }

    public long getNicId() {
        return nicId;
    }

    public boolean isDefaultNic() {
        return isDefaultNic;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getGateway() {
        return gateway;
    }

    public String getNetmask() {
        return netmask;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public URI getBroadcastUri() {
        return broadcastUri;
    }

    public URI getIsolationUri() {
        return isolationUri;
    }

    public long getNetworkId() {
        return networkId;
    }

    public String getNetworkName() {
        return networkName;
    }

    public String getNetworkDomain() {
        return networkDomain;
    }

    public TrafficType getTrafficType() {
        return trafficType;
    }

    public long getServiceOfferingId() {
        return serviceOfferingId;
    }

    public long getProjectId() {
        return projectId;
    }

    public String getVpcUuid() {
        return vpcUuid;
    }

    public String getNicUuid() {
        return nicUuid;
    }

    public String getNetworkUuid() {
        return networkUuid;
    }

    public Long getJobId() {
        return jobId;
    }

    public String getJobUuid() {
        return jobUuid;
    }

    public int getJobStatus() {
        return jobStatus;
    }

    public Long getPodId() {
        return podId;
    }

    public long getDataCenterId() {
        return dataCenterId;
    }

    public String getDns1() {
        return dns1;
    }

    public String getDns2() {
        return dns2;
    }

    public String getTemplateVersion() {
        return templateVersion;
    }

    public String getScriptsVersion() {
        return scriptsVersion;
    }

    public RedundantState getRedundantState() {
        return redundantState;
    }

    public boolean isRedundantRouter() {
        return isRedundantRouter;
    }

    public GuestType getGuestType() {
        return guestType;
    }

    public String getIp6Address() {
        return ip6Address;
    }

    public String getIp6Gateway() {
        return ip6Gateway;
    }

    public String getIp6Cidr() {
        return ip6Cidr;
    }

    public String getIp6Dns1() {
        return ip6Dns1;
    }

    public String getIp6Dns2() {
        return ip6Dns2;
    }

    public VirtualRouter.Role getRole() {
        return role;
    }

    @Override
    public Class<?> getEntityType() {
        return VirtualMachine.class;
    }
}
