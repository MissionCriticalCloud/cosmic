from marvin.cloudstackAPI import (
    listConfigurations,
    listNetworkACLLists,
    listDomains,
    listZones,
    listTemplates,
    listRouters,
    listNetworks,
    listSystemVms,
    listVirtualMachines,
    listLoadBalancerRuleInstances,
    listVlanIpRanges,
    listHosts,
    listPublicIpAddresses,
    listPortForwardingRules,
    listLoadBalancerRules,
    listServiceOfferings,
    listNetworkOfferings,
    listVPCOfferings,
    listVPCs,
    listVpnGateways
)
from marvin.codes import (
    PASS,
    FAILED
)
from utils import validate_list


def get_zone(api_client, zone_name=None, zone_id=None):
    """
    @name : get_zone
    @Desc :Returns the Zone Information for a given zone id or Zone Name		
    @Input : zone_name: Name of the Zone		
             zone_id : Id of the zone		
    @Output : 1. Zone Information for the passed inputs else first zone		
              2. FAILED In case the cmd failed		
   """
    cmd = listZones.listZonesCmd()
    if zone_name is not None:
        cmd.name = zone_name
    if zone_id is not None:
        cmd.id = zone_id

    cmd_out = api_client.listZones(cmd)

    if validate_list(cmd_out)[0] != PASS:
        return FAILED

    '''
    Check if input zone name and zone id is None,
    then return first element of List Zones command		
    '''
    return cmd_out[0]


def get_domain(api_client, domain_id=None, domain_name=None):
    """
    @name : get_domain
    @Desc : Returns the Domain Information for a given domain id or domain name
    @Input : domain id : Id of the Domain
             domain_name : Name of the Domain
    @Output : 1. Domain  Information for the passed inputs else first Domain
              2. FAILED In case the cmd failed
    """
    cmd = listDomains.listDomainsCmd()

    if domain_name is not None:
        cmd.name = domain_name
    if domain_id is not None:
        cmd.id = domain_id
    cmd_out = api_client.listDomains(cmd)
    if validate_list(cmd_out)[0] != PASS:
        return FAILED
    return cmd_out[0]


def get_template(api_client, zone_id=None, template_filter="featured", template_type='BUILTIN', template_id=None,
                 template_name=None, account=None, domain_id=None, project_id=None, hypervisor=None):
    """
    @Name : get_template
    @Desc : Retrieves the template Information based upon inputs provided
            Template is retrieved based upon either of the inputs matched
            condition
    @Input : returns a template"
    @Output : FAILED in case of any failure
              template Information matching the inputs
    """
    cmd = listTemplates.listTemplatesCmd()
    cmd.templatefilter = template_filter
    if domain_id is not None:
        cmd.domainid = domain_id
    if zone_id is not None:
        cmd.zoneid = zone_id
    if template_id is not None:
        cmd.id = template_id
    if template_name is not None:
        cmd.name = template_name
    if hypervisor is not None:
        cmd.hypervisor = hypervisor
    if project_id is not None:
        cmd.projectid = project_id
    if account is not None:
        cmd.account = account

    '''
    Get the Templates pertaining to the inputs provided
    '''
    list_templatesout = api_client.listTemplates(cmd)
    if validate_list(list_templatesout)[0] != PASS:
        return FAILED

    for template in list_templatesout:
        if template.isready and template.templatetype == template_type:
            return template
    '''
    Return default first template, if no template matched
    '''
    return list_templatesout[0]


def list_routers(api_client, **kwargs):
    """List all Routers matching criteria"""

    cmd = listRouters.listRoutersCmd()
    [setattr(cmd, k, v) for k, v in kwargs.items()]
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd.listall = True
    return api_client.listRouters(cmd)


def list_zones(api_client, **kwargs):
    """List all Zones matching criteria"""

    cmd = listZones.listZonesCmd()
    [setattr(cmd, k, v) for k, v in kwargs.items()]
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd.listall = True
    return api_client.listZones(cmd)


def list_networks(api_client, **kwargs):
    """List all Networks matching criteria"""

    cmd = listNetworks.listNetworksCmd()
    [setattr(cmd, k, v) for k, v in kwargs.items()]
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd.listall = True
    return api_client.listNetworks(cmd)


def list_vpcs(api_client, **kwargs):
    """List all VPCs matching criteria"""

    cmd = listVPCs.listVPCsCmd()
    [setattr(cmd, k, v) for k, v in kwargs.items()]
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd.listall = True
    return api_client.listVPCs(cmd)


def list_ssvms(api_client, **kwargs):
    """List all SSVMs matching criteria"""

    cmd = listSystemVms.listSystemVmsCmd()
    [setattr(cmd, k, v) for k, v in kwargs.items()]
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd.listall = True
    return api_client.listSystemVms(cmd)


def list_virtual_machines(api_client, **kwargs):
    """List all VMs matching criteria"""

    cmd = listVirtualMachines.listVirtualMachinesCmd()
    [setattr(cmd, k, v) for k, v in kwargs.items()]
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd.listall = True
    return api_client.listVirtualMachines(cmd)


def list_hosts(api_client, **kwargs):
    """List all Hosts matching criteria"""

    cmd = listHosts.listHostsCmd()
    [setattr(cmd, k, v) for k, v in kwargs.items()]
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd.listall = True
    return api_client.listHosts(cmd)


def list_configurations(api_client, **kwargs):
    """List configuration with specified name"""

    cmd = listConfigurations.listConfigurationsCmd()
    [setattr(cmd, k, v) for k, v in kwargs.items()]
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd.listall = True
    return api_client.listConfigurations(cmd)


def list_public_ip(api_client, **kwargs):
    """List all Public IPs matching criteria"""

    cmd = listPublicIpAddresses.listPublicIpAddressesCmd()
    [setattr(cmd, k, v) for k, v in kwargs.items()]
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd.listall = True
    return api_client.listPublicIpAddresses(cmd)


def list_nat_rules(api_client, **kwargs):
    """List all NAT rules matching criteria"""

    cmd = listPortForwardingRules.listPortForwardingRulesCmd()
    [setattr(cmd, k, v) for k, v in kwargs.items()]
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd.listall = True
    return api_client.listPortForwardingRules(cmd)


def list_lb_rules(api_client, **kwargs):
    """List all Load balancing rules matching criteria"""

    cmd = listLoadBalancerRules.listLoadBalancerRulesCmd()
    [setattr(cmd, k, v) for k, v in kwargs.items()]
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd.listall = True
    return api_client.listLoadBalancerRules(cmd)


def list_lb_instances(api_client, **kwargs):
    """List all Load balancing instances matching criteria"""

    cmd = listLoadBalancerRuleInstances.listLoadBalancerRuleInstancesCmd()
    [setattr(cmd, k, v) for k, v in kwargs.items()]
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd.listall = True
    return api_client.listLoadBalancerRuleInstances(cmd)


def list_service_offering(api_client, **kwargs):
    """Lists all available service offerings."""

    cmd = listServiceOfferings.listServiceOfferingsCmd()
    [setattr(cmd, k, v) for k, v in kwargs.items()]
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd.listall = True
    return api_client.listServiceOfferings(cmd)


def list_vlan_ipranges(api_client, **kwargs):
    """Lists all VLAN IP ranges."""

    cmd = listVlanIpRanges.listVlanIpRangesCmd()
    [setattr(cmd, k, v) for k, v in kwargs.items()]
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd.listall = True
    return api_client.listVlanIpRanges(cmd)


def list_network_offerings(api_client, **kwargs):
    """Lists network offerings"""

    cmd = listNetworkOfferings.listNetworkOfferingsCmd()
    [setattr(cmd, k, v) for k, v in kwargs.items()]
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd.listall = True
    return api_client.listNetworkOfferings(cmd)


def list_vpngateways(api_client, **kwargs):
    """ Lists VPN gateways """

    cmd = listVpnGateways.listVpnGatewaysCmd()
    [setattr(cmd, k, v) for k, v in kwargs.items()]
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd.listall = True
    return api_client.listVpnGateways(cmd)


def list_vpc_offerings(api_client, **kwargs):
    """ Lists VPC offerings """

    cmd = listVPCOfferings.listVPCOfferingsCmd()
    [setattr(cmd, k, v) for k, v in kwargs.items()]
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd.listall = True
    return api_client.listVPCOfferings(cmd)


def list_network_acl_lists(api_client, **kwargs):
    """List Network ACL lists"""

    cmd = listNetworkACLLists.listNetworkACLListsCmd()
    [setattr(cmd, k, v) for k, v in kwargs.items()]
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd.listall = True
    return api_client.listNetworkACLLists(cmd)


def get_hypervisor_type(api_client):
    """Return the hypervisor type of the hosts in setup"""

    cmd = listHosts.listHostsCmd()
    cmd.type = 'Routing'
    cmd.listall = True
    hosts = api_client.listHosts(cmd)
    hosts_list_validation_result = validate_list(hosts)
    assert hosts_list_validation_result[0] == PASS, "host list validation failed"
    return hosts_list_validation_result[1].hypervisor


def get_vpc_offering(api_client, name):
    offerings = list_vpc_offerings(api_client, name=name)
    return find_exact_match_by_name(offerings, name)


def get_default_vpc_offering(api_client):
    return get_vpc_offering(api_client, 'Default VPC offering')


def get_default_redundant_vpc_offering(api_client):
    return get_vpc_offering(api_client, 'Redundant VPC offering')


def get_network_offering(api_client, name):
    offerings = list_network_offerings(api_client, name=name)
    return find_exact_match_by_name(offerings, name)


def get_default_network_offering(api_client):
    return get_network_offering(api_client, 'DefaultIsolatedNetworkOfferingForVpcNetworks')


def get_default_guest_network_offering(api_client):
    return get_network_offering(api_client, 'DefaultIsolatedNetworkOfferingWithSourceNatService')


def get_default_network_offering_no_load_balancer(api_client):
    return get_network_offering(api_client, 'DefaultIsolatedNetworkOfferingForVpcNetworksNoLB')


def get_default_isolated_network_offering(api_client):
    return get_network_offering(api_client, 'DefaultIsolatedNetworkOffering')


def get_default_isolated_network_offering_with_egress(api_client):
    return get_network_offering(api_client, 'DefaultIsolatedNetworkOfferingWithEgress')


def get_default_redundant_isolated_network_offering(api_client):
    return get_network_offering(api_client, 'DefaultRedundantIsolatedNetworkOffering')


def get_default_redundant_isolated_network_offering_with_egress(api_client):
    return get_network_offering(api_client, 'DefaultRedundantIsolatedNetworkOfferingWithEgress')


def get_default_private_network_offering(api_client):
    return get_network_offering(api_client, 'DefaultPrivateGatewayNetworkOffering')


def get_default_virtual_machine_offering(api_client):
    return get_virtual_machine_offering(api_client, 'Small Instance')


def get_virtual_machine_offering(api_client, name):
    offerings = list_service_offering(api_client, name=name)
    return find_exact_match_by_name(offerings, name)


def get_network_acl(api_client, name=None, id=None, vpc=None):
    if vpc:
        acls = list_network_acl_lists(api_client, name=name, id=id, vpcid=vpc.id, listall=True)
    else:
        acls = list_network_acl_lists(api_client, name=name, id=id, listall=True)
    return find_exact_match_by_name(acls, name) if name else acls[0]


def get_default_allow_vpc_acl(api_client, vpc):
    return get_network_acl(api_client, 'default_allow', vpc)


def get_default_deny_vpc_acl(api_client, vpc):
    return get_network_acl(api_client, 'default_deny', vpc)


def get_vpc(api_client, name):
    vpcs = list_vpcs(api_client, name=name, listall=True)
    return find_exact_match_by_name(vpcs, name)


def get_network(api_client, name=None, id=None, vpc=None):
    if vpc:
        networks = list_networks(api_client, name=name, id=id, vpcid=vpc.id)
    else:
        networks = list_networks(api_client, name=name, id=id)
    return find_exact_match_by_name(networks, name) if name else networks[0]


def get_virtual_machine(api_client, name, network=None):
    if network:
        virtual_machines = list_virtual_machines(api_client, name=name, networkid=network.id, listall=True)
    else:
        virtual_machines = list_virtual_machines(api_client, name=name, listall=True)
    return find_exact_match_by_name(virtual_machines, name)


def get_vpngateway(api_client, vpc=None):
    vpngateways = list_vpngateways(api_client, vpcid=vpc.id, listall=True)
    return next(iter(vpngateways or []), None)


def find_exact_match_by_name(items, name):
    items = [item for item in items if item.name == name]
    return next(iter(items or []), None)
