#!/cygdrive/c/Python27


import os.path
import sys
from xml.dom import minidom
from xml.parsers.expat import ExpatError

REGULAR_USER = 'u'
DOMAIN_ADMIN = 'd'
ROOT_ADMIN = 'r'

user_to_func = {
    REGULAR_USER: 'populateForUser',
    DOMAIN_ADMIN: 'populateForDomainAdmin',
    ROOT_ADMIN: 'populateForRootAdmin',
}

user_to_cns = {
    REGULAR_USER: 'userCommandNames',
    DOMAIN_ADMIN: 'domainAdminCommandNames',
    ROOT_ADMIN: 'rootAdminCommandNames',
}

dirname_to_user = {
    'regular_user': REGULAR_USER,
    'domain_admin': DOMAIN_ADMIN,
    'root_admin': ROOT_ADMIN,
}

dirname_to_dirname = {
    'regular_user': 'user',
    'domain_admin': 'domain_admin',
    'root_admin': 'root_admin',
}

known_categories = {
    'Cisco': 'External Device',
    'VirtualMachine': 'Virtual Machine',
    'VM': 'Virtual Machine',
    'Domain': 'Domain',
    'Template': 'Template',
    'Iso': 'ISO',
    'Volume': 'Volume',
    'Vlan': 'VLAN',
    'IpAddress': 'Address',
    'PortForwarding': 'Firewall',
    'Firewall': 'Firewall',
    'StaticNat': 'NAT',
    'IpForwarding': 'NAT',
    'Host': 'Host',
    'Cluster': 'Cluster',
    'Account': 'Account',
    'Snapshot': 'Snapshot',
    'User': 'User',
    'Os': 'Guest OS',
    'ServiceOffering': 'Service Offering',
    'DiskOffering': 'Disk Offering',
    'LoadBalancer': 'Load Balancer',
    'SslCert': 'Load Balancer',
    'Router': 'Router',
    'SystemVm': 'System VM',
    'Configuration': 'Configuration',
    'Capabilities': 'Configuration',
    'Pod': 'Pod',
    'PublicIpRange': 'Network',
    'Zone': 'Zone',
    'Vmware': 'Zone',
    'NetworkOffering': 'Network Offering',
    'NetworkACL': 'Network ACL',
    'Network': 'Network',
    'CiscoNexus': 'Network',
    'createServiceInstance': 'Network',
    'Vpn': 'VPN',
    'Limit': 'Limit',
    'ResourceCount': 'Limit',
    'CloudIdentifier': 'Cloud Identifier',
    'InstanceGroup': 'VM Group',
    'StorageMaintenance': 'Storage Pool',
    'StoragePool': 'Storage Pool',
    'StorageProvider': 'Storage Pool',
    'SSH': 'SSH',
    'register': 'Registration',
    'AsyncJob': 'Async job',
    'Certificate': 'Certificate',
    'Hypervisor': 'Hypervisor',
    'Alert': 'Alert',
    'Event': 'Event',
    'login': 'Authentication',
    'logout': 'Authentication',
    'saml': 'Authentication',
    'getSPMetadata': 'Authentication',
    'listIdps': 'Authentication',
    'authorizeSamlSso': 'Authentication',
    'listSamlAuthorization': 'Authentication',
    'quota': 'Quota',
    'emailTemplate': 'Quota',
    'Capacity': 'System Capacity',
    'NetworkDevice': 'Network Device',
    'ExternalLoadBalancer': 'Ext Load Balancer',
    'Usage': 'Usage',
    'TrafficMonitor': 'Usage',
    'TrafficType': 'Usage',
    'Product': 'Product',
    'LB': 'Load Balancer',
    'ldap': 'LDAP',
    'SecondaryStorage': 'Host',
    'Project': 'Project',
    'Lun': 'Storage',
    'Pool': 'Pool',
    'VPC': 'VPC',
    'PrivateGateway': 'VPC',
    'StaticRoute': 'VPC',
    'Tags': 'Resource tags',
    'NiciraNvpDevice': 'Nicira NVP',
    'NuageVsp': 'Nuage VSP',
    'Api': 'API Discovery',
    'Region': 'Region',
    'Detail': 'Resource metadata',
    'addIpToNic': 'Nic',
    'removeIpFromNic': 'Nic',
    'updateVmNicIp': 'Nic',
    'listNics': 'Nic',
    'AffinityGroup': 'Affinity Group',
    'addImageStore': 'Image Store',
    'listImageStore': 'Image Store',
    'deleteImageStore': 'Image Store',
    'createSecondaryStagingStore': 'Image Store',
    'deleteSecondaryStagingStore': 'Image Store',
    'listSecondaryStagingStores': 'Image Store',
    'DeploymentPlanners': 'Configuration',
    'ObjectStore': 'Image Store',
    'dedicateHost': 'Dedicate Resources',
    'releaseDedicatedHost': 'Dedicate Resources',
    'CacheStores': 'Cache Stores',
    'CacheStore': 'Cache Store',
    'listHAWorkers': 'CloudOps',
    'listWhoHasThisIp': 'CloudOps',
    'listWhoHasThisMac': 'CloudOps'
}

categories = { }


def choose_category(fn):
    for k, v in list(known_categories.items()):
        if k in fn:
            return v
    raise Exception('Need to add a category for %s to %s:known_categories' %
                    (fn, __file__))
    sys.exit(1)


for f in sys.argv:
    dirname, fn = os.path.split(f)
    if not fn.endswith('.xml'):
        continue
    if fn.endswith('Summary.xml'):
        continue
    if fn.endswith('SummarySorted.xml'):
        continue
    if fn == 'alert_types.xml':
        continue
    if dirname.startswith('./'):
        dirname = dirname[2:]
    try:
        with open(f) as data:
            dom = minidom.parse(data)
        name = dom.getElementsByTagName('name')[0].firstChild.data
        isAsync = dom.getElementsByTagName('isAsync')[0].firstChild.data
        category = choose_category(fn)
        if category not in categories:
            categories[category] = []
        categories[category].append({
            'name': name,
            'dirname': dirname_to_dirname[dirname],
            'async': isAsync == 'true',
            'user': dirname_to_user[dirname],
        })
    except ExpatError as e:
        pass
    except IndexError as e:
        print(fn)


def xml_for(command):
    name = command['name']
    a_sync = command['async'] and ' (A)' or ''
    dirname = command['dirname']
    return '''<xsl:if test="name=\'%(name)s\'">
<li><a href="%(dirname)s/%(name)s.html"><xsl:value-of select="name"/>%(a_sync)s</a></li>
</xsl:if>
''' % locals()


def write_xml(out, user):
    with open(out, 'w') as f:
        cat_strings = []

        for category in list(categories.keys()):
            strings = []
            for command in categories[category]:
                if command['user'] == user:
                    strings.append(xml_for(command))
            if strings:
                all_strings = ''.join(strings)
                cat_strings.append((len(strings), category, all_strings))

        cat_strings.sort(reverse=True)

        i = 0
        for _1, category, all_strings in cat_strings:
            if i == 0:
                f.write('<div class="apismallsections">\n')
            f.write('''<div class="apismallbullet_box">
<h5>%(category)s</h5>
<ul>
<xsl:for-each select="commands/command">
%(all_strings)s
</xsl:for-each>
</ul>
</div>

''' % locals())
            if i == 3:
                f.write('</div>\n')
                i = 0
            else:
                i += 1
        if i != 0:
            f.write('</div>\n')


def java_for(command, user):
    name = command['name']
    cns = user_to_cns[user]
    return '''%(cns)s.add("%(name)s");
''' % locals()


def java_for_user(user):
    strings = []
    for category in list(categories.keys()):
        for command in categories[category]:
            if command['user'] == user:
                strings.append(java_for(command, user))
    func = user_to_func[user]
    all_strings = ''.join(strings)
    return '''
    public void %(func)s() {
        %(all_strings)s
    }
''' % locals()


def write_java(out):
    with open(out, 'w') as f:
        f.write('''/* Generated using gen_toc.py.  Do not edit. */

import java.util.HashSet;
import java.util.Set;

public class XmlToHtmlConverterData {

	Set<String> rootAdminCommandNames = new HashSet<String>();
	Set<String> domainAdminCommandNames = new HashSet<String>();
	Set<String> userCommandNames = new HashSet<String>();

''')
        f.write(java_for_user(REGULAR_USER) + "\n");
        f.write(java_for_user(ROOT_ADMIN) + "\n")
        f.write(java_for_user(DOMAIN_ADMIN) + "\n")

        f.write('''
}

''')


write_xml('generatetocforuser_include.xsl', REGULAR_USER)
write_xml('generatetocforadmin_include.xsl', ROOT_ADMIN)
write_xml('generatetocfordomainadmin_include.xsl', DOMAIN_ADMIN)
write_java('XmlToHtmlConverterData.java')
