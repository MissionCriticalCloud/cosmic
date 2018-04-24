package com.cloud.api.command.user.vm;

import com.cloud.acl.RoleType;
import com.cloud.affinity.AffinityGroupResponse;
import com.cloud.api.ACL;
import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCreateCustomIdCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.DiskOfferingResponse;
import com.cloud.api.response.DomainResponse;
import com.cloud.api.response.HostResponse;
import com.cloud.api.response.NetworkResponse;
import com.cloud.api.response.ProjectResponse;
import com.cloud.api.response.ServiceOfferingResponse;
import com.cloud.api.response.TemplateResponse;
import com.cloud.api.response.UserVmResponse;
import com.cloud.api.response.ZoneResponse;
import com.cloud.context.CallContext;
import com.cloud.db.model.Zone;
import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InsufficientServerCapacityException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.model.enumeration.DiskControllerType;
import com.cloud.network.Network;
import com.cloud.network.Network.IpAddresses;
import com.cloud.offering.DiskOffering;
import com.cloud.offering.ServiceOffering;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.user.Account;
import com.cloud.uservm.UserVm;
import com.cloud.utils.exception.InvalidParameterValueException;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.VirtualMachine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deployVirtualMachine", group = APICommandGroup.VirtualMachineService, description = "Creates and automatically starts a virtual machine based on a service offering, disk " +
        "offering, and template.",
        responseObject = UserVmResponse.class, responseView = ResponseView.Restricted, entityType = {VirtualMachine.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = true)
public class DeployVMCmd extends BaseAsyncCreateCustomIdCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeployVMCmd.class.getName());

    private static final String s_name = "deployvirtualmachineresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ZONE_ID, type = CommandType.UUID, entityType = ZoneResponse.class, required = true, description = "availability zone for the virtual machine")
    private Long zoneId;

    @ACL
    @Parameter(name = ApiConstants.SERVICE_OFFERING_ID, type = CommandType.UUID, entityType = ServiceOfferingResponse.class, required = true, description = "the ID of the " +
            "service offering for the virtual machine")
    private Long serviceOfferingId;

    @ACL
    @Parameter(name = ApiConstants.TEMPLATE_ID, type = CommandType.UUID, entityType = TemplateResponse.class, required = true, description = "the ID of the template for the " +
            "virtual machine")
    private Long templateId;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "host name for the virtual machine")
    private String name;

    @Parameter(name = ApiConstants.DISPLAY_NAME, type = CommandType.STRING, description = "an optional user generated name for the virtual machine")
    private String displayName;

    //Owner information
    @Parameter(name = ApiConstants.ACCOUNT, type = CommandType.STRING, description = "an optional account for the virtual machine. Must be used with domainId.")
    private String accountName;

    @Parameter(name = ApiConstants.DOMAIN_ID, type = CommandType.UUID, entityType = DomainResponse.class, description = "an optional domainId for the virtual machine. If the " +
            "account parameter is used, domainId must also be used.")
    private Long domainId;

    //Network information
    //@ACL(accessType = AccessType.UseEntry)
    @Parameter(name = ApiConstants.NETWORK_IDS, type = CommandType.LIST, collectionType = CommandType.UUID, entityType = NetworkResponse.class, description = "list of network " +
            "ids used by virtual machine. Can't be specified with ipToNetworkList parameter")
    private List<Long> networkIds;

    //DataDisk information
    @ACL
    @Parameter(name = ApiConstants.DISK_OFFERING_ID, type = CommandType.UUID, entityType = DiskOfferingResponse.class, description = "the ID of the disk offering for the virtual" +
            " machine. If the template is of ISO format,"
            + " the diskOfferingId is for the root disk volume. Otherwise this parameter is used to indicate the "
            + "offering for the data disk volume. If the templateId parameter passed is from a Template object,"
            + " the diskOfferingId refers to a DATA Disk Volume created. If the templateId parameter passed is "
            + "from an ISO object, the diskOfferingId refers to a ROOT Disk Volume created.")
    private Long diskOfferingId;

    @Parameter(name = ApiConstants.SIZE, type = CommandType.LONG, description = "the arbitrary size for the DATADISK volume. Mutually exclusive with diskOfferingId")
    private Long size;

    @Parameter(name = ApiConstants.ROOT_DISK_SIZE,
            type = CommandType.LONG,
            description = "Optional field to resize root disk on deploy. Value is in GB. Only applies to template-based deployments. Analogous to details[0].rootdisksize, which " +
                    "takes precedence over this parameter if both are provided",
            since = "4.4")
    private Long rootdisksize;

    @Parameter(name = ApiConstants.DISK_CONTROLLER,
            required = false,
            type = CommandType.STRING,
            description = "the disk controller to use. Either 'IDE', 'VIRTIO' or 'SCSI'")
    private String diskController;

    @Parameter(name = ApiConstants.GROUP, type = CommandType.STRING, description = "an optional group for the virtual machine")
    private String group;

    @Parameter(name = ApiConstants.HYPERVISOR, type = CommandType.STRING, description = "the hypervisor on which to deploy the virtual machine. "
            + "The parameter is required and respected only when hypervisor info is not set on the ISO/Template passed to the call")
    private String hypervisor;

    @Parameter(name = ApiConstants.USER_DATA, type = CommandType.STRING, description = "an optional binary data that can be sent to the virtual machine upon a successful " +
            "deployment. This binary data must be base64 encoded before adding it to the request. Using HTTP GET (via querystring), you can send up to 2KB of data after base64 " +
            "encoding. Using HTTP POST(via POST body), you can send up to 32K of data after base64 encoding.", length = 32768)
    private String userData;

    @Parameter(name = ApiConstants.SSH_KEYPAIR, type = CommandType.STRING, description = "name of the ssh key pair used to login to the virtual machine")
    private String sshKeyPairName;

    @Parameter(name = ApiConstants.HOST_ID, type = CommandType.UUID, entityType = HostResponse.class, description = "destination Host ID to deploy the VM to - parameter " +
            "available for root admin only")
    private Long hostId;

    @Parameter(name = ApiConstants.IP_NETWORK_LIST, type = CommandType.MAP, description = "ip to network mapping. Can't be specified with networkIds parameter."
            + " Example: iptonetworklist[0].ip=10.10.10.11&iptonetworklist[0].ipv6=fc00:1234:5678::abcd&iptonetworklist[0].networkid=uuid - requests to use ip 10.10.10.11 in " +
            "network id=uuid")
    private Map ipToNetworkList;

    @Parameter(name = ApiConstants.IP_ADDRESS, type = CommandType.STRING, description = "the ip address for default vm's network")
    private String ipAddress;

    @Parameter(name = ApiConstants.IP6_ADDRESS, type = CommandType.STRING, description = "the ipv6 address for default vm's network")
    private String ip6Address;

    @Parameter(name = ApiConstants.MAC_ADDRESS, type = CommandType.STRING, description = "the MAC-Address for default vm's network")
    private String macAddress;

    @Parameter(name = ApiConstants.KEYBOARD, type = CommandType.STRING, description = "an optional keyboard device type for the virtual machine. valid value can be one of de," +
            "de-ch,es,fi,fr,fr-be,fr-ch,is,it,jp,nl-be,no,pt,uk,us")
    private String keyboard;

    @Parameter(name = ApiConstants.PROJECT_ID, type = CommandType.UUID, entityType = ProjectResponse.class, description = "Deploy vm for the project")
    private Long projectId;

    @Parameter(name = ApiConstants.START_VM, type = CommandType.BOOLEAN, description = "true if start vm after creating; defaulted to true if not specified")
    private Boolean startVm;

    @ACL
    @Parameter(name = ApiConstants.AFFINITY_GROUP_IDS, type = CommandType.LIST, collectionType = CommandType.UUID, entityType = AffinityGroupResponse.class, description = "comma" +
            " separated list of affinity groups id that are going to be applied to the virtual machine."
            + " Mutually exclusive with affinitygroupnames parameter")
    private List<Long> affinityGroupIdList;

    @ACL
    @Parameter(name = ApiConstants.AFFINITY_GROUP_NAMES, type = CommandType.LIST, collectionType = CommandType.STRING, entityType = AffinityGroupResponse.class, description =
            "comma separated list of affinity groups names that are going to be applied to the virtual machine."
                    + "Mutually exclusive with affinitygroupids parameter")
    private List<String> affinityGroupNameList;

    @Parameter(name = ApiConstants.DISPLAY_VM, type = CommandType.BOOLEAN, since = "4.2", description = "an optional field, whether to the display the vm to the end user or not" +
            ".", authorized = {RoleType.Admin})
    private Boolean displayVm;

    @Parameter(name = ApiConstants.DETAILS, type = CommandType.MAP, since = "4.3", description = "used to specify the custom parameters.")
    private Map details;

    @Parameter(name = ApiConstants.DEPLOYMENT_PLANNER, type = CommandType.STRING, description = "Deployment planner to use for vm allocation. Available to ROOT admin only",
            since = "4.4", authorized = {RoleType.Admin})
    private String deploymentPlanner;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public static String getResultObjectName() {
        return "virtualmachine";
    }

    public String getAccountName() {
        if (accountName == null) {
            return CallContext.current().getCallingAccount().getAccountName();
        }
        return accountName;
    }

    public Long getDiskOfferingId() {
        return diskOfferingId;
    }

    public String getDeploymentPlanner() {
        return deploymentPlanner;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Long getDomainId() {
        if (domainId == null) {
            return CallContext.current().getCallingAccount().getDomainId();
        }
        return domainId;
    }

    public Map<String, String> getDetails() {
        final Map<String, String> customparameterMap = new HashMap<>();
        if (details != null && details.size() != 0) {
            final Collection parameterCollection = details.values();
            final Iterator iter = parameterCollection.iterator();
            while (iter.hasNext()) {
                final HashMap<String, String> value = (HashMap<String, String>) iter.next();
                for (final Map.Entry<String, String> entry : value.entrySet()) {
                    customparameterMap.put(entry.getKey(), entry.getValue());
                }
            }
        }
        if (rootdisksize != null && !customparameterMap.containsKey("rootdisksize")) {
            customparameterMap.put("rootdisksize", rootdisksize.toString());
        }
        return customparameterMap;
    }

    public String getGroup() {
        return group;
    }

    public HypervisorType getHypervisor() {
        return HypervisorType.getType(hypervisor);
    }

    public Boolean getDisplayVm() {
        return displayVm;
    }

    public Long getServiceOfferingId() {
        return serviceOfferingId;
    }

    public Long getSize() {
        return size;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public String getUserData() {
        return userData;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public DiskControllerType getDiskController() {
        if (diskController != null) {
            return DiskControllerType.valueOf(diskController);
        } else {
            return null;
        }
    }

    public List<Long> getNetworkIds() {
        if (ipToNetworkList != null && !ipToNetworkList.isEmpty()) {
            if ((networkIds != null && !networkIds.isEmpty()) || ipAddress != null || getIp6Address() != null) {
                throw new InvalidParameterValueException("ipToNetworkMap can't be specified along with networkIds or ipAddress");
            } else {
                final List<Long> networks = new ArrayList<>();
                networks.addAll(getIpToNetworkMap().keySet());
                return networks;
            }
        }
        return networkIds;
    }

    public String getName() {
        return name;
    }

    public String getSSHKeyPairName() {
        return sshKeyPairName;
    }

    public Long getHostId() {
        return hostId;
    }

    private Map<Long, IpAddresses> getIpToNetworkMap() {
        if ((networkIds != null || ipAddress != null || getIp6Address() != null) && ipToNetworkList != null) {
            throw new InvalidParameterValueException("NetworkIds and ipAddress can't be specified along with ipToNetworkMap parameter");
        }
        LinkedHashMap<Long, IpAddresses> ipToNetworkMap = null;
        if (ipToNetworkList != null && !ipToNetworkList.isEmpty()) {
            ipToNetworkMap = new LinkedHashMap<>();
            final Collection ipsCollection = ipToNetworkList.values();
            final Iterator iter = ipsCollection.iterator();
            while (iter.hasNext()) {
                final HashMap<String, String> ips = (HashMap<String, String>) iter.next();
                final Long networkId;
                final Network network = _networkService.getNetwork(ips.get("networkid"));
                if (Network.GuestType.Private.equals(network.getGuestType())) {
                    throw new InvalidParameterValueException("Deploying VMs in a network of type " + Network.GuestType.Private + " is not possible.");
                }
                if (network != null) {
                    networkId = network.getId();
                } else {
                    try {
                        networkId = Long.parseLong(ips.get("networkid"));
                    } catch (final NumberFormatException e) {
                        throw new InvalidParameterValueException("Unable to translate and find entity with networkId: " + ips.get("networkid"));
                    }
                }
                final String requestedIp = ips.get("ip");
                String requestedIpv6 = ips.get("ipv6");
                String requestedMac = ips.get("mac");
                if (requestedIpv6 != null) {
                    requestedIpv6 = NetUtils.standardizeIp6Address(requestedIpv6);
                }
                if (requestedMac != null) {
                    if (!NetUtils.isValidMac(requestedMac)) {
                        throw new InvalidParameterValueException("MAC-Address is not valid: " + requestedMac);
                    } else if (!NetUtils.isUnicastMac(requestedMac)) {
                        throw new InvalidParameterValueException("MAC-Address is not unicast: " + requestedMac);
                    }
                    requestedMac = NetUtils.standardizeMacAddress(requestedMac);
                }
                IpAddresses addrs = new IpAddresses(requestedIp, requestedIpv6, requestedMac);
                ipToNetworkMap.put(networkId, addrs);
            }
        }

        return ipToNetworkMap;
    }

    public String getIp6Address() {
        if (ip6Address == null) {
            return null;
        }
        return NetUtils.standardizeIp6Address(ip6Address);
    }

    public String getMacAddress() {
        if (macAddress == null) {
            return null;
        }
        if (!NetUtils.isValidMac(macAddress)) {
            throw new InvalidParameterValueException("MAC-Address is not valid: " + macAddress);
        } else if (!NetUtils.isUnicastMac(macAddress)) {
            throw new InvalidParameterValueException("MAC-Address is not unicast: " + macAddress);
        }
        return NetUtils.standardizeMacAddress(macAddress);
    }

    public List<Long> getAffinityGroupIdList() {
        if (affinityGroupNameList != null && affinityGroupIdList != null) {
            throw new InvalidParameterValueException("affinitygroupids parameter is mutually exclusive with affinitygroupnames parameter");
        }

        // transform group names to ids here
        if (affinityGroupNameList != null) {
            final List<Long> affinityGroupIds = new ArrayList<>();
            for (final String groupName : affinityGroupNameList) {
                final Long groupId = _responseGenerator.getAffinityGroupId(groupName, getEntityOwnerId());
                if (groupId == null) {
                    throw new InvalidParameterValueException("Unable to find affinity group by name " + groupName);
                } else {
                    affinityGroupIds.add(groupId);
                }
            }
            return affinityGroupIds;
        } else {
            return affinityGroupIdList;
        }
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_VM_CREATE;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventDescription() {
        return "starting Vm. Vm Id: " + getEntityId();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.VirtualMachine;
    }

    @Override
    public void execute() {
        final UserVm result;

        if (getStartVm()) {
            try {
                CallContext.current().setEventDetails("Vm Id: " + getEntityId());
                result = _userVmService.startVirtualMachine(this);
            } catch (final ResourceUnavailableException ex) {
                s_logger.warn("Exception: ", ex);
                throw new ServerApiException(ApiErrorCode.RESOURCE_UNAVAILABLE_ERROR, ex.getMessage());
            } catch (final ConcurrentOperationException ex) {
                s_logger.warn("Exception: ", ex);
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, ex.getMessage());
            } catch (final InsufficientCapacityException ex) {
                final StringBuilder message = new StringBuilder(ex.getMessage());
                if (ex instanceof InsufficientServerCapacityException) {
                    if (((InsufficientServerCapacityException) ex).isAffinityApplied()) {
                        message.append(", Please check the affinity groups provided, there may not be sufficient capacity to follow them");
                    }
                }
                s_logger.info(ex.toString());
                s_logger.info(message.toString(), ex);
                throw new ServerApiException(ApiErrorCode.INSUFFICIENT_CAPACITY_ERROR, message.toString());
            }
        } else {
            result = _userVmService.getUserVm(getEntityId());
        }

        if (result != null) {
            final UserVmResponse response = _responseGenerator.createUserVmResponse(ResponseView.Restricted, "virtualmachine", result).get(0);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to deploy vm uuid:" + getEntityUuid());
        }
    }

    public boolean getStartVm() {
        return startVm == null ? true : startVm;
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final Long accountId = _accountService.finalyzeAccountId(accountName, domainId, projectId, true);
        if (accountId == null) {
            return CallContext.current().getCallingAccount().getId();
        }

        return accountId;
    }

    @Override
    public boolean isDisplay() {
        if (displayVm == null) {
            return true;
        } else {
            return displayVm;
        }
    }

    // this is an opportunity to verify that parameters that came in via the Details Map are OK
    // for example, minIops and maxIops should either both be specified or neither be specified and,
    // if specified, minIops should be <= maxIops
    private void verifyDetails() {
        final Map<String, String> map = getDetails();

        if (map != null) {
            String minIops = (String) map.get("minIops");
            String maxIops = (String) map.get("maxIops");

            verifyMinAndMaxIops(minIops, maxIops);

            minIops = (String) map.get("minIopsDo");
            maxIops = (String) map.get("maxIopsDo");

            verifyMinAndMaxIops(minIops, maxIops);
        }
    }

    private void verifyMinAndMaxIops(final String minIops, final String maxIops) {
        if ((minIops != null && maxIops == null) || (minIops == null && maxIops != null)) {
            throw new InvalidParameterValueException("Either 'Min IOPS' and 'Max IOPS' must both be specified or neither be specified.");
        }

        final long lMinIops;

        try {
            if (minIops != null) {
                lMinIops = Long.parseLong(minIops);
            } else {
                lMinIops = 0;
            }
        } catch (final NumberFormatException ex) {
            throw new InvalidParameterValueException("'Min IOPS' must be a whole number.");
        }

        final long lMaxIops;

        try {
            if (maxIops != null) {
                lMaxIops = Long.parseLong(maxIops);
            } else {
                lMaxIops = 0;
            }
        } catch (final NumberFormatException ex) {
            throw new InvalidParameterValueException("'Max IOPS' must be a whole number.");
        }

        if (lMinIops > lMaxIops) {
            throw new InvalidParameterValueException("'Min IOPS' must be less than or equal to 'Max IOPS'.");
        }
    }

    @Override
    public void create() {
        try {
            //Verify that all objects exist before passing them to the service
            final Account owner = _accountService.getActiveAccountById(getEntityOwnerId());

            verifyDetails();

            final Zone zone = zoneRepository.findOne(zoneId);

            if (zone == null) {
                throw new InvalidParameterValueException("Unable to find zone by id=" + zoneId);
            }

            final ServiceOffering serviceOffering = _entityMgr.findById(ServiceOffering.class, serviceOfferingId);
            if (serviceOffering == null) {
                throw new InvalidParameterValueException("Unable to find service offering: " + serviceOfferingId);
            }

            final VirtualMachineTemplate template = _entityMgr.findById(VirtualMachineTemplate.class, templateId);
            // Make sure a valid template ID was specified
            if (template == null) {
                throw new InvalidParameterValueException("Unable to find the template " + templateId);
            }

            DiskOffering diskOffering = null;
            if (diskOfferingId != null) {
                diskOffering = _entityMgr.findById(DiskOffering.class, diskOfferingId);
                if (diskOffering == null) {
                    throw new InvalidParameterValueException("Unable to find disk offering " + diskOfferingId);
                }
            }

            if (!zone.isLocalStorageEnabled()) {
                if (serviceOffering.getUseLocalStorage()) {
                    throw new InvalidParameterValueException("Zone is not configured to use local storage but service offering " + serviceOffering.getName() + " uses it");
                }
                if (diskOffering != null && diskOffering.getUseLocalStorage()) {
                    throw new InvalidParameterValueException("Zone is not configured to use local storage but disk offering " + diskOffering.getName() + " uses it");
                }
            }

            final IpAddresses addrs = new IpAddresses(ipAddress, ip6Address, getMacAddress());
            final UserVm vm = _userVmService.createAdvancedVirtualMachine(zone, serviceOffering, template, getNetworkIds(), owner, name, displayName, diskOfferingId, size, group,
                    getHypervisor(), getHttpMethod(), userData, sshKeyPairName, getIpToNetworkMap(), addrs, displayVm, keyboard, getAffinityGroupIdList(), getDetails(),
                    getCustomId(), getDiskController());

            if (vm != null) {
                setEntityId(vm.getId());
                setEntityUuid(vm.getUuid());
            } else {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to deploy vm");
            }
        } catch (final InsufficientCapacityException ex) {
            s_logger.info(ex.toString());
            s_logger.trace(ex.getMessage(), ex);
            throw new ServerApiException(ApiErrorCode.INSUFFICIENT_CAPACITY_ERROR, ex.getMessage());
        } catch (final ResourceUnavailableException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(ApiErrorCode.RESOURCE_UNAVAILABLE_ERROR, ex.getMessage());
        } catch (final ConcurrentOperationException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, ex.getMessage());
        } catch (final ResourceAllocationException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(ApiErrorCode.RESOURCE_ALLOCATION_ERROR, ex.getMessage());
        }
    }

    @Override
    public String getCreateEventType() {
        return EventTypes.EVENT_VM_CREATE;
    }

    @Override
    public String getCreateEventDescription() {
        return "creating Vm";
    }
}
