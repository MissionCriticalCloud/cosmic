from .codes import (
    PASS,
    FAILED
)
from .utils import (
    validate_list
)


class Response:
    def __init__(self, items):
        if isinstance(items, dict):
            self.__dict__.update(items)

    def __getitem__(self, item):
        return self.__dict__[item]

    def __setitem__(self, key, value):
        self.__dict__[key] = value

    @staticmethod
    def list(items):
        if items is None:
            return items
        new_items = []
        for item in items:
            new_items.append(Response(item))
        return new_items


def get_zone(api_client, zone_name=None, zone_id=None):
    """
    @name : get_zone
    @Desc :Returns the Zone Information for a given zone id or Zone Name
    @Input : zone_name: Name of the Zone
             zone_id : Id of the zone
    @Output : 1. Zone Information for the passed inputs else first zone
              2. FAILED In case the cmd failed
   """
    cmd = {}
    response = "zone"
    if zone_name is not None:
        cmd['name'] = zone_name
    if zone_id is not None:
        cmd['id'] = zone_id

    cmd_out = api_client.listZones(**cmd)

    if validate_list(cmd_out, response)[0] != PASS:
        return FAILED

    '''
    Check if input zone name and zone id is None,
    then return first element of List Zones command		
    '''
    return Response(cmd_out[response][0])


def get_domain(api_client, domain_id=None, domain_name=None):
    """
    @name : get_domain
    @Desc : Returns the Domain Information for a given domain id or domain name
    @Input : domain id : Id of the Domain
             domain_name : Name of the Domain
    @Output : 1. Domain  Information for the passed inputs else first Domain
              2. FAILED In case the cmd failed
    """
    cmd = {}
    response = "domain"
    if domain_name is not None:
        cmd['name'] = domain_name
    if domain_id is not None:
        cmd['id'] = domain_id
    cmd_out = api_client.listDomains(**cmd)
    if validate_list(cmd_out, response)[0] != PASS:
        return FAILED
    return Response(cmd_out[response][0])


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
    cmd = {}
    response = "template"
    cmd['templatefilter'] = template_filter
    if domain_id is not None:
        cmd['domainid'] = domain_id
    if zone_id is not None:
        cmd['zoneid'] = zone_id
    if template_id is not None:
        cmd['id'] = template_id
    if template_name is not None:
        cmd['name'] = template_name
    if hypervisor is not None:
        cmd['hypervisor'] = hypervisor
    if project_id is not None:
        cmd['projectid'] = project_id
    if account is not None:
        cmd['account'] = account

    '''
    Get the Templates pertaining to the inputs provided
    '''
    list_templatesout = api_client.listTemplates(**cmd)
    if validate_list(list_templatesout, response)[0] != PASS:
        return FAILED

    for template in list_templatesout[response]:
        if template['isready'] and template['templatetype'] == template_type:
            return Response(template)
    '''
    Return default first template, if no template matched
    '''
    return Response(list_templatesout[response][0])


def list_routers(api_client, **kwargs):
    """List all Routers matching criteria"""

    cmd = {}
    cmd.update(kwargs)
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd['listall'] = True
    return Response.list(api_client.listRouters(**cmd).get('router', []))


def list_zones(api_client, **kwargs):
    """List all Zones matching criteria"""

    cmd = {}
    cmd.update(kwargs)
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd['listall'] = True
    return Response.list(api_client.listZones(**cmd).get('zone', []))


def list_networks(api_client, **kwargs):
    """List all Networks matching criteria"""

    cmd = {}
    cmd.update(kwargs)
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd['listall'] = True
    return api_client.listNetworks(**cmd)


def list_vpcs(api_client, **kwargs):
    """List all VPCs matching criteria"""

    cmd = {}
    cmd.update(kwargs)
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd['listall'] = True
    return api_client.listVPCs(**cmd)


def list_ssvms(api_client, **kwargs):
    """List all SSVMs matching criteria"""

    cmd = {}
    cmd.update(kwargs)
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd['listall'] = True
    return Response.list(api_client.listSystemVms(**cmd).get('systemvm', []))


def list_virtual_machines(api_client, **kwargs):
    """List all VMs matching criteria"""

    cmd = {}
    cmd.update(kwargs)
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd['listall'] = True
    return api_client.listVirtualMachines(**cmd)


def list_hosts(api_client, **kwargs):
    """List all Hosts matching criteria"""

    cmd = {}
    cmd.update(kwargs)
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd['listall'] = True
    return Response.list(api_client.listHosts(**cmd).get('host', []))


def list_configurations(api_client, **kwargs):
    """List configuration with specified name"""

    cmd = {}
    cmd.update(kwargs)
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd['listall'] = True
    return Response.list(api_client.listConfigurations(**cmd)['configuration'])


def list_public_ip(api_client, **kwargs):
    """List all Public IPs matching criteria"""

    cmd = {}
    cmd.update(kwargs)
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd['listall'] = True
    return Response.list(api_client.listPublicIpAddresses(**cmd).get('publicipaddress', []))


def list_nat_rules(api_client, **kwargs):
    """List all NAT rules matching criteria"""

    cmd = {}
    cmd.update(kwargs)
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd['listall'] = True
    return api_client.listPortForwardingRules(**cmd)


def list_lb_rules(api_client, **kwargs):
    """List all Load balancing rules matching criteria"""

    cmd = {}
    cmd.update(kwargs)
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd['listall'] = True
    return Response.list(api_client.listLoadBalancerRules(**cmd)['loadbalancerrule'])


def list_lb_instances(api_client, **kwargs):
    """List all Load balancing instances matching criteria"""

    cmd = {}
    cmd.update(kwargs)
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd['listall'] = True
    return Response.list(api_client.listLoadBalancerRuleInstances(**cmd).get('loadbalancerruleinstance', []))


def list_service_offering(api_client, **kwargs):
    """Lists all available service offerings."""

    cmd = {}
    cmd.update(kwargs)
    # [setattr(cmd, k, v) for k, v in kwargs.items()]
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd['listall'] = True
    return Response.list(api_client.listServiceOfferings(**cmd)['serviceoffering'])


def list_vlan_ipranges(api_client, **kwargs):
    """Lists all VLAN IP ranges."""

    cmd = {}
    cmd.update(kwargs)
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd['listall'] = True
    return Response.list(api_client.listVlanIpRanges(**cmd).get('vlaniprange', []))


def list_network_offerings(api_client, **kwargs):
    """Lists network offerings"""

    cmd = {}
    cmd.update(kwargs)
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd['listall'] = True
    return Response.list(api_client.listNetworkOfferings(**cmd)['networkoffering'])


def list_vpngateways(api_client, **kwargs):
    """ Lists VPN gateways """

    cmd = {}
    cmd.update(kwargs)
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd['listall'] = True
    return api_client.listVpnGateways(**cmd)


def list_vpc_offerings(api_client, **kwargs):
    """ Lists VPC offerings """

    cmd = {}
    cmd.update(kwargs)
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd['listall'] = True
    return Response.list(api_client.listVPCOfferings(**cmd)['vpcoffering'])


def list_network_acl_lists(api_client, **kwargs):
    """List Network ACL lists"""

    cmd = {}
    cmd.update(kwargs)
    if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
        cmd['listall'] = True
    return Response.list(api_client.listNetworkACLLists(**cmd)['networkacllist'])


def get_hypervisor_type(api_client):
    """Return the hypervisor type of the hosts in setup"""

    cmd = {'type': 'Routing', 'listall': True}
    hosts = api_client.listHosts(**cmd)['host']
    hosts_list_validation_result = validate_list(hosts, 'host')
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


def get_network_acl(api_client, name=None, acl_id=None, vpc=None):
    if vpc:
        acls = list_network_acl_lists(api_client, name=name, id=acl_id, vpcid=vpc.id, listall=True)
    else:
        acls = list_network_acl_lists(api_client, name=name, id=acl_id, listall=True)
    return find_exact_match_by_name(acls, name if name else acls['networkacllist'][0])


def get_default_allow_vpc_acl(api_client, vpc):
    return get_network_acl(api_client, 'default_allow', vpc)


def get_default_deny_vpc_acl(api_client, vpc):
    return get_network_acl(api_client, 'default_deny', vpc)


def get_vpc(api_client, name):
    vpcs = list_vpcs(api_client, name=name, listall=True)
    return find_exact_match_by_name(vpcs, name)


def get_network(api_client, name=None, nw_id=None, vpc=None):
    if vpc:
        networks = list_networks(api_client, name=name, id=nw_id, vpcid=vpc.id)
    else:
        networks = list_networks(api_client, name=name, id=nw_id)
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
