test_data = {
    "scenarios": {
        "scenario_1": {
            "metadata": {
                "name": "Scenario #1",
                "description": "This scenario contains an account with 1 VPC 1 tier and 1 VM, there will be a port forward "
                               "from the public ip address to the VM.",
                "hierarchy": "domain -> account (user) -> vpc (static routes, publicips (acl) -> portforward, "
                             "private gateways) -> tiers (acl) -> vms"
            },
            "data": {
                "domains": [
                    {
                        "metadata": {},
                        "data": {
                            "name": "ROOT",
                            "accounts": [
                                {
                                    "metadata": {},
                                    "data": {
                                        "accounttype": 2,
                                        "email": "john@doe.com",
                                        "firstname": "John",
                                        "lastname": "Doe",
                                        "password": "password",
                                        "username": "john",
                                        "vpcs": [
                                            # vpc01
                                            {
                                                "metadata": {},
                                                "data": {
                                                    "cidr": "10.1.0.0/16",
                                                    "displaytext": "vpc01",
                                                    "name": "vpc01",
                                                    "vpcofferingname": "Redundant VPC offering",
                                                    "networks": [
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "displaytext": "vpc01-tier01",
                                                                "name": "vpc01-tier01",
                                                                "networkofferingname": "DefaultIsolatedNetworkOfferingForVpcNetworks",
                                                                "aclname": "default_allow",
                                                                "cidr": "10.1.1.0/24",
                                                                "gateway": "10.1.1.254",
                                                            }
                                                        },
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "displaytext": "vpc01-tier02",
                                                                "name": "vpc01-tier02",
                                                                "networkofferingname": "DefaultIsolatedNetworkOfferingForVpcNetworks",
                                                                "aclname": "default_allow",
                                                                "cidr": "10.1.2.0/24",
                                                                "gateway": "10.1.2.1",
                                                            }
                                                        }
                                                    ],
                                                    "acls": [
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "name": "acl01",
                                                                "rules": [
                                                                    {
                                                                        "metadata": {},
                                                                        "data": {
                                                                            "protocol": "TCP",
                                                                            "cidrlist": "0.0.0.0/0",
                                                                            "startport": 22,
                                                                            "endport": 22,
                                                                            "traffictype": "Ingress"
                                                                        }
                                                                    }
                                                                ]
                                                            }
                                                        }
                                                    ],
                                                    "publicipaddresses": [
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "portforwards": [
                                                                    {
                                                                        "metadata": {},
                                                                        "data": {
                                                                            "privateport": 22,
                                                                            "publicport": 2201,
                                                                            "protocol": "TCP",
                                                                            "virtualmachinename": "vpc01-tier01-vm01",
                                                                            "nic": "10.1.1.11"
                                                                        }
                                                                    }
                                                                ]
                                                            }
                                                        },
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "portforwards": [
                                                                    {
                                                                        "metadata": {},
                                                                        "data": {
                                                                            "privateport": 22,
                                                                            "publicport": 2201,
                                                                            "protocol": "TCP",
                                                                            "virtualmachinename": "vpc01-tier02-vm01",
                                                                            "nic": "10.1.2.11"
                                                                        }
                                                                    },
                                                                    {
                                                                        "metadata": {},
                                                                        "data": {
                                                                            "privateport": 22,
                                                                            "publicport": 2202,
                                                                            "protocol": "TCP",
                                                                            "virtualmachinename": "vpc01-tier02-vm02",
                                                                            "nic": "10.1.2.12"
                                                                        }
                                                                    }
                                                                ]
                                                            }
                                                        }
                                                    ],
                                                    "privategateways": [
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "ip": "172.16.100.1",
                                                                "aclname": "default_allow",
                                                                "privatenetworkname": "private_gateways_network",
                                                                "staticroutes": [
                                                                    {
                                                                        "metadata": {},
                                                                        "data": {
                                                                            "cidr": "10.3.0.0/16",
                                                                            "nexthop": "172.16.100.3"
                                                                        }
                                                                    }
                                                                ]
                                                            }
                                                        }
                                                    ],
                                                    "vpnconnections": []
                                                }
                                            },
                                            # vpc02
                                            {
                                                "metadata": {},
                                                "data": {
                                                    "cidr": "10.2.0.0/16",
                                                    "displaytext": "vpc02",
                                                    "name": "vpc02",
                                                    "vpcofferingname": "Default VPC offering",
                                                    "networks": [
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "displaytext": "vpc02-tier01",
                                                                "name": "vpc02-tier01",
                                                                "networkofferingname": "DefaultIsolatedNetworkOfferingForVpcNetworks",
                                                                "aclname": "default_allow",
                                                                "cidr": "10.2.1.0/24",
                                                                "gateway": "10.2.1.1",
                                                            }
                                                        }
                                                    ],
                                                    "acls": [],
                                                    "publicipaddresses": [
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "portforwards": [
                                                                    {
                                                                        "metadata": {},
                                                                        "data": {
                                                                            "privateport": 22,
                                                                            "publicport": 2201,
                                                                            "protocol": "TCP",
                                                                            "virtualmachinename": "vpc02-tier01-vm01",
                                                                            "nic": "10.2.1.11"
                                                                        }
                                                                    }
                                                                ]
                                                            }
                                                        }
                                                    ],
                                                    "privategateways": [],
                                                    "vpnconnections": ["vpc04"]
                                                }
                                            },
                                            # vpc03
                                            {
                                                "metadata": {},
                                                "data": {
                                                    "cidr": "10.3.0.0/16",
                                                    "displaytext": "vpc03",
                                                    "name": "vpc03",
                                                    "vpcofferingname": "Default VPC offering",
                                                    "networks": [
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "displaytext": "vpc03-tier01",
                                                                "name": "vpc03-tier01",
                                                                "networkofferingname": "DefaultIsolatedNetworkOfferingForVpcNetworks",
                                                                "aclname": "default_allow",
                                                                "cidr": "10.3.1.0/24",
                                                                "gateway": "10.3.1.1",
                                                            }
                                                        }
                                                    ],
                                                    "acls": [
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "name": "acl03",
                                                                "rules": [
                                                                    {
                                                                        "metadata": {},
                                                                        "data": {
                                                                            "protocol": "TCP",
                                                                            "cidrlist": "0.0.0.0/0",
                                                                            "startport": 22,
                                                                            "endport": 22,
                                                                            "traffictype": "Ingress"
                                                                        }
                                                                    }
                                                                ]
                                                            }
                                                        }
                                                    ],
                                                    "publicipaddresses": [
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "portforwards": [
                                                                    {
                                                                        "metadata": {},
                                                                        "data": {
                                                                            "privateport": 22,
                                                                            "publicport": 2201,
                                                                            "protocol": "TCP",
                                                                            "virtualmachinename": "vpc03-tier01-vm01",
                                                                            "nic": "10.3.1.11"
                                                                        }
                                                                    }
                                                                ]
                                                            }
                                                        }
                                                    ],
                                                    "privategateways": [
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "ip": "172.16.100.3",
                                                                "aclname": "default_allow",
                                                                "privatenetworkname": "private_gateways_network",
                                                                "staticroutes": [
                                                                    {
                                                                        "metadata": {},
                                                                        "data": {
                                                                            "cidr": "10.1.0.0/16",
                                                                            "nexthop": "172.16.100.1"
                                                                        }
                                                                    }
                                                                ]
                                                            }
                                                        }
                                                    ],
                                                    "vpnconnections": []
                                                }
                                            },
                                            # vpc04
                                            {
                                                "metadata": {},
                                                "data": {
                                                    "cidr": "10.4.0.0/16",
                                                    "displaytext": "vpc04",
                                                    "name": "vpc04",
                                                    "vpcofferingname": "Redundant VPC offering",
                                                    "networks": [
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "displaytext": "vpc04-tier01",
                                                                "name": "vpc04-tier01",
                                                                "networkofferingname": "DefaultIsolatedNetworkOfferingForVpcNetworks",
                                                                "aclname": "default_allow",
                                                                "cidr": "10.4.1.0/24",
                                                                "gateway": "10.4.1.1",
                                                            }
                                                        }
                                                    ],
                                                    "acls": [
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "name": "acl04",
                                                                "rules": [
                                                                    {
                                                                        "metadata": {},
                                                                        "data": {
                                                                            "protocol": "TCP",
                                                                            "cidrlist": "0.0.0.0/0",
                                                                            "startport": 22,
                                                                            "endport": 22,
                                                                            "traffictype": "Ingress"
                                                                        }
                                                                    }
                                                                ]
                                                            }
                                                        }
                                                    ],
                                                    "publicipaddresses": [
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "portforwards": [
                                                                    {
                                                                        "metadata": {},
                                                                        "data": {
                                                                            "privateport": 22,
                                                                            "publicport": 2201,
                                                                            "protocol": "TCP",
                                                                            "virtualmachinename": "vpc04-tier01-vm01",
                                                                            "nic": "10.4.1.11"
                                                                        }
                                                                    }
                                                                ]
                                                            }
                                                        }
                                                    ],
                                                    "privategateways": [],
                                                    "vpnconnections": ["vpc02"]
                                                }
                                            }
                                        ],
                                        "isolatednetworks": [
                                            # isonet01
                                            {
                                                "metadata": {},
                                                "data": {
                                                    "name": "isonet01",
                                                    "displaytext": "isonet01",
                                                    "networkofferingname": "DefaultRedundantIsolatedNetworkOffering",
                                                    "gateway": "172.16.1.1",
                                                    "netmask": "255.255.255.0",
                                                    "egressrules": [
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "protocol": "TCP",
                                                                "cidrlist": "0.0.0.0/0",
                                                                "startport": 2200,
                                                                "endport": 2299
                                                            }
                                                        }
                                                    ],
                                                    "publicipaddresses": [
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "firewallrules": [
                                                                    {
                                                                        "metadata": {},
                                                                        "data": {
                                                                            "protocol": "TCP",
                                                                            "cidrlist": "0.0.0.0/0",
                                                                            "startport": 2200,
                                                                            "endport": 2299
                                                                        }
                                                                    }
                                                                ],
                                                                "portforwards": [
                                                                    {
                                                                        "metadata": {},
                                                                        "data": {
                                                                            "protocol": "TCP",
                                                                            "privateport": 22,
                                                                            "publicport": 2201,
                                                                            "openfirewall": False,
                                                                            "virtualmachinename": "isonet01-vm01",
                                                                            "nic": "172.16.1.11"
                                                                        }
                                                                    },
                                                                    {
                                                                        "metadata": {},
                                                                        "data": {
                                                                            "protocol": "TCP",
                                                                            "privateport": 22,
                                                                            "publicport": 2202,
                                                                            "openfirewall": False,
                                                                            "virtualmachinename": "isonet01-vm02",
                                                                            "nic": "172.16.1.12"
                                                                        }
                                                                    }
                                                                ]
                                                            }
                                                        }
                                                    ]
                                                }
                                            },
                                            # isonet02
                                            {
                                                "metadata": {},
                                                "data": {
                                                    "name": "isonet02",
                                                    "displaytext": "isonet02",
                                                    "networkofferingname": "DefaultIsolatedNetworkOffering",
                                                    "gateway": "172.16.2.1",
                                                    "netmask": "255.255.255.0",
                                                    "egressrules": [
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "protocol": "TCP",
                                                                "cidrlist": "0.0.0.0/0",
                                                                "startport": 2200,
                                                                "endport": 2299
                                                            }
                                                        }
                                                    ],
                                                    "publicipaddresses": [
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "firewallrules": [
                                                                    {
                                                                        "metadata": {},
                                                                        "data": {
                                                                            "protocol": "TCP",
                                                                            "cidrlist": "0.0.0.0/0",
                                                                            "startport": 2200,
                                                                            "endport": 2299
                                                                        }
                                                                    }
                                                                ],
                                                                "portforwards": [
                                                                    {
                                                                        "metadata": {},
                                                                        "data": {
                                                                            "protocol": "TCP",
                                                                            "privateport": 22,
                                                                            "publicport": 2201,
                                                                            "openfirewall": False,
                                                                            "virtualmachinename": "isonet02-vm01",
                                                                            "nic": "172.16.2.11"
                                                                        }
                                                                    }
                                                                ]
                                                            }
                                                        }
                                                    ]
                                                }
                                            }
                                        ],
                                        "privatenetworks": [
                                            {
                                                "metadata": {},
                                                "data": {
                                                    "name": "private_gateways_network",
                                                    "displaytext": "private_gateways_network",
                                                    "cidr": "172.16.100.0/24",
                                                    "networkofferingname": "DefaultPrivateGatewayNetworkOffering",
                                                    "aclname": "default_allow"
                                                }
                                            }
                                        ],
                                        "virtualmachines": [
                                            # vpc01-tier01-vm01
                                            {
                                                "metadata": {},
                                                "data": {
                                                    "name": "vpc01-tier01-vm011",
                                                    "displayname": "vpc01-tier01-vm01",
                                                    "templatename": "tiny linux kvm",
                                                    "serviceofferingname": "Small Instance",
                                                    "nics": [
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "guestip": "10.1.1.11",
                                                                "networkname": "vpc01-tier01"
                                                            }
                                                        }
                                                    ]
                                                }
                                            },
                                            # vpc01-tier02-vm01
                                            {
                                                "metadata": {},
                                                "data": {
                                                    "name": "vpc01-tier02-vm01",
                                                    "displayname": "vpc01-tier02-vm011",
                                                    "templatename": "tiny linux kvm",
                                                    "serviceofferingname": "Small Instance",
                                                    "nics": [
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "guestip": "10.1.2.11",
                                                                "networkname": "vpc01-tier02"
                                                            }
                                                        }
                                                    ]
                                                }
                                            },
                                            # vpc01-tier02-vm02
                                            {
                                                "metadata": {},
                                                "data": {
                                                    "name": "vpc01-tier02-vm02",
                                                    "displayname": "vpc01-tier02-vm02",
                                                    "templatename": "tiny linux kvm",
                                                    "serviceofferingname": "Small Instance",
                                                    "nics": [
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "guestip": "10.1.2.12",
                                                                "networkname": "vpc01-tier02"
                                                            }
                                                        }
                                                    ]
                                                }
                                            },
                                            # vpc02-tier01-vm01
                                            {
                                                "metadata": {},
                                                "data": {
                                                    "name": "vpc02-tier01-vm01",
                                                    "displayname": "vpc02-tier01-vm01",
                                                    "templatename": "tiny linux kvm",
                                                    "serviceofferingname": "Small Instance",
                                                    "nics": [
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "guestip": "10.2.1.11",
                                                                "networkname": "vpc02-tier01"
                                                            }
                                                        }
                                                    ]
                                                }
                                            },
                                            # vpc03-tier01-vm01
                                            {
                                                "metadata": {},
                                                "data": {
                                                    "name": "vpc03-tier01-vm01",
                                                    "displayname": "vpc03-tier01-vm01",
                                                    "templatename": "tiny linux kvm",
                                                    "serviceofferingname": "Small Instance",
                                                    "nics": [
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "guestip": "10.3.1.11",
                                                                "networkname": "vpc03-tier01"
                                                            }
                                                        }
                                                    ]
                                                }
                                            },
                                            # vpc04-tier01-vm01
                                            {
                                                "metadata": {},
                                                "data": {
                                                    "name": "vpc04-tier01-vm01",
                                                    "displayname": "vpc04-tier01-vm01",
                                                    "templatename": "tiny linux kvm",
                                                    "serviceofferingname": "Small Instance",
                                                    "nics": [
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "guestip": "10.4.1.11",
                                                                "networkname": "vpc04-tier01"
                                                            }
                                                        }
                                                    ]
                                                }
                                            },
                                            # isonet01-vm01
                                            {
                                                "metadata": {},
                                                "data": {
                                                    "name": "isonet01-vm01",
                                                    "displayname": "isonet01-vm01",
                                                    "templatename": "tiny linux kvm",
                                                    "serviceofferingname": "Small Instance",
                                                    "nics": [
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "guestip": "172.16.1.11",
                                                                "networkname": "isonet01"
                                                            }
                                                        }
                                                    ]
                                                }
                                            },
                                            # isonet01-vm02
                                            {
                                                "metadata": {},
                                                "data": {
                                                    "name": "isonet01-vm02",
                                                    "displayname": "isonet01-vm02",
                                                    "templatename": "tiny linux kvm",
                                                    "serviceofferingname": "Small Instance",
                                                    "nics": [
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "guestip": "172.16.1.12",
                                                                "networkname": "isonet01"
                                                            }
                                                        }
                                                    ]
                                                }
                                            },
                                            # isonet02-vm01
                                            {
                                                "metadata": {},
                                                "data": {
                                                    "name": "isonet02-vm01",
                                                    "displayname": "isonet02-vm01",
                                                    "templatename": "tiny linux kvm",
                                                    "serviceofferingname": "Small Instance",
                                                    "nics": [
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "guestip": "172.16.2.11",
                                                                "networkname": "isonet02"
                                                            }
                                                        }
                                                    ]
                                                }
                                            }
                                        ]
                                    }
                                }
                            ]
                        }
                    }
                ]
            }
        },
        "scenario_2": {
            "metadata": {
                "name": "Scenario #2",
                "description": "This scenario contains an account with 2 VPC (each) 1 tier and 1 VM,"
                "there will be a port forward from the public ip address to the VM.",
                "hierarchy": "domain -> account (user) -> vpc (static routes, publicips (acl) -> portforward, "
                             "private gateways) -> tiers (acl) -> vms"
            },
            "data": {
                "domains": [
                    {
                        "metadata": {},
                        "data": {
                            "name": "ROOT",
                            "accounts": [
                                {
                                    "metadata": {},
                                    "data": {
                                        "accounttype": 2,
                                        "email": "jane@doe.com",
                                        "firstname": "Jane",
                                        "lastname": "Doe",
                                        "password": "password",
                                        "username": "jane",
                                        "vpcs": [
                                            # vpc01
                                            {
                                                "metadata": {},
                                                "data": {
                                                    "cidr": "10.1.0.0/16",
                                                    "displaytext": "vpc01",
                                                    "name": "vpc01",
                                                    "vpcofferingname": "Redundant VPC offering",
                                                    "networks": [
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "displaytext": "vpc01-tier01",
                                                                "name": "vpc01-tier01",
                                                                "networkofferingname": "DefaultIsolatedNetworkOfferingForVpcNetworks",
                                                                "aclname": "default_allow",
                                                                "cidr": "10.1.1.0/24",
                                                                "gateway": "10.1.1.254",
                                                            }
                                                        }
                                                    ],
                                                    "acls": [
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "name": "acl01",
                                                                "rules": [
                                                                    {
                                                                        "metadata": {},
                                                                        "data": {
                                                                            "protocol": "TCP",
                                                                            "cidrlist": "0.0.0.0/0",
                                                                            "startport": 22,
                                                                            "endport": 22,
                                                                            "traffictype": "Ingress"
                                                                        }
                                                                    }
                                                                ]
                                                            }
                                                        }
                                                    ],
                                                    "publicipaddresses": [
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "portforwards": [
                                                                    {
                                                                        "metadata": {},
                                                                        "data": {
                                                                            "privateport": 22,
                                                                            "publicport": 2201,
                                                                            "protocol": "TCP",
                                                                            "virtualmachinename": "vpc01-tier01-vm01",
                                                                            "nic": "10.1.1.11"
                                                                        }
                                                                    }
                                                                ]
                                                            }
                                                        }
                                                    ],
                                                    "privategateways": [],
                                                    "vpnconnections": []
                                                }
                                            },
                                            # vpc02
                                            {
                                                "metadata": {},
                                                "data": {
                                                    "cidr": "10.2.0.0/16",
                                                    "displaytext": "vpc02",
                                                    "name": "vpc02",
                                                    "vpcofferingname": "Default VPC offering",
                                                    "networks": [
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "displaytext": "vpc02-tier01",
                                                                "name": "vpc02-tier01",
                                                                "networkofferingname": "DefaultIsolatedNetworkOfferingForVpcNetworks",
                                                                "aclname": "default_allow",
                                                                "cidr": "10.2.1.0/24",
                                                                "gateway": "10.2.1.1",
                                                            }
                                                        }
                                                    ],
                                                    "acls": [],
                                                    "publicipaddresses": [
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "portforwards": [
                                                                    {
                                                                        "metadata": {},
                                                                        "data": {
                                                                            "privateport": 22,
                                                                            "publicport": 2201,
                                                                            "protocol": "TCP",
                                                                            "virtualmachinename": "vpc02-tier01-vm01",
                                                                            "nic": "10.2.1.11"
                                                                        }
                                                                    }
                                                                ]
                                                            }
                                                        }
                                                    ],
                                                    "privategateways": [],
                                                    "vpnconnections": []
                                                }
                                            }
                                        ],
                                        "isolatednetworks": [],
                                        "privatenetworks": [],
                                        "virtualmachines": [
                                            # vpc01-tier01-vm01
                                            {
                                                "metadata": {},
                                                "data": {
                                                    "name": "vpc01-tier01-vm01",
                                                    "displayname": "vpc01-tier01-vm01",
                                                    "templatename": "tiny linux kvm",
                                                    "serviceofferingname": "Small Instance",
                                                    "nics": [
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "guestip": "10.1.1.11",
                                                                "networkname": "vpc01-tier01"
                                                            }
                                                        }
                                                    ]
                                                }
                                            },
                                            # vpc02-tier01-vm01
                                            {
                                                "metadata": {},
                                                "data": {
                                                    "name": "vpc02-tier01-vm01",
                                                    "displayname": "vpc02-tier01-vm01",
                                                    "templatename": "tiny linux kvm",
                                                    "serviceofferingname": "Small Instance",
                                                    "nics": [
                                                        {
                                                            "metadata": {},
                                                            "data": {
                                                                "guestip": "10.2.1.11",
                                                                "networkname": "vpc02-tier01"
                                                            }
                                                        }
                                                    ]
                                                }
                                            }
                                        ]
                                    }
                                }
                            ]
                        }
                    }
                ]
            }
        }
    },
    "test_data": {
        "TestPublicIpAcl": {
            "acls": {
                "acl1": {
                    "name": "acl1",
                    "description": "acl1",
                    "entries": {
                        "entry1": {
                            "protocol": "TCP",
                            "action": "Allow",
                            "traffictype": "Ingress",
                            "startport": 22,
                            "endport": 22
                        }
                    }
                },
                "acl2": {
                    "name": "acl2",
                    "description": "acl2",
                    "entries": {
                        "entry1": {
                            "protocol": "TCP",
                            "action": "Deny",
                            "traffictype": "Ingress",
                            "startport": 22,
                            "endport": 22
                        }
                    }
                }
            }
        }
    },
    "region": {
        "regionid": "2",
        "regionname": "Region2",
        "regionendpoint": "http://region2:8080/client"
    },
    "zone": "NA",
    "hypervisor": "XenServer",
    "deleteDC": True,
    "vdomain": {
        "name": "domain"
    },
    "domain": {"name": "domain"},
    "email": "test@test.com",
    "gateway": "172.1.1.1",
    "netmask": "255.255.255.0",
    "startip": "172.1.1.10",
    "endip": "172.1.1.20",
    "regionid": "1",

    "project": {
        "name": "Project",
        "displaytext": "Test project"
    },
    "publiciprange": {
        "gateway": "",
        "netmask": "",
        "startip": "",
        "endip": "",
        "forvirtualnetwork": "true",
        "vlan": "",
        "zoneid": ""
    },
    "private_gateway": {
        "ipaddress": "172.16.1.2",
        "gateway": "172.16.1.1",
        "netmask": "255.255.255.0",
        "vlan": "10",
        "name": "test_private_gateway"
    },
    "account": {
        "email": "test-account@test.com",
        "firstname": "test",
        "lastname": "test",
        "username": "test-account",
        "password": "password"
    },
    "account2": {
        "email": "test-account2@test.com",
        "firstname": "test2",
        "lastname": "test2",
        "username": "test-account2",
        "password": "password"
    },
    "small": {
        "displayname": "testserver",
        "username": "root",
        "password": "password",
        "ssh_port": 22,
        "hypervisor": "XenServer",
        "privateport": 22,
        "publicport": 22,
        "protocol": "TCP",
    },
    "service_offering": {
        "name": "Tiny Instance",
        "displaytext": "Tiny Instance",
        "cpunumber": 1,
        "cpuspeed": 256,  # in MHz
        "memory": 256,  # In MBs
    },
    "service_offerings": {
        "tiny": {
            "name": "Tiny Instance",
            "displaytext": "Tiny Instance",
            "cpunumber": 1,
            "cpuspeed": 100,
            "memory": 128,
        },
        "small": {
            "name": "Small Instance",
            "displaytext": "Small Instance",
            "cpunumber": 1,
            "cpuspeed": 100,
            "memory": 256
        },
        "medium": {
            "name": "Medium Instance",
            "displaytext": "Medium Instance",
            "cpunumber": 1,
            "cpuspeed": 100,
            "memory": 256,
        },
        "big": {
            "name": "BigInstance",
            "displaytext": "BigInstance",
            "cpunumber": 1,
            "cpuspeed": 100,
            "memory": 512,
        },
        "large": {
            "name": "LargeInstance",
            "displaytext": "LargeInstance",
            "cpunumber": 1,
            "cpuspeed": 500,
            "memory": 2048,
        },
        "hasmall": {
            "name": "HA Small Instance",
            "displaytext": "HA Small Instance",
            "cpunumber": 1,
            "cpuspeed": 100,
            "memory": 256,
            "hosttags": "ha",
            "offerha": True,
        },
        "taggedsmall": {
            "name": "Tagged Small Instance",
            "displaytext": "Tagged Small Instance",
            "cpunumber": 1,
            "cpuspeed": 100,
            "memory": 256,
            "hosttags": "vmsync",
        },
    },
    "service_offering_h1": {
        "name": "Tagged h1 Small Instance",
        "displaytext": "Tagged h1 Small Instance",
        "cpunumber": 1,
        "cpuspeed": 100,
        "memory": 256,
        "hosttags": "h1"
    },
    "service_offering_h2": {
        "name": "Tagged h2 Small Instance",
        "displaytext": "Tagged h2 Small Instance",
        "cpunumber": 1,
        "cpuspeed": 100,
        "memory": 256,
        "hosttags": "h2"
    },
    "disk_offering": {
        "name": "Disk offering",
        "displaytext": "Disk offering",
        "disksize": 1
    },
    "resized_disk_offering": {
        "displaytext": "Resized",
        "name": "Resized",
        "disksize": 3
    },
    "disk_offering_shared_5GB": {
        "displaytext": "disk_offering_shared_5GB",
        "name": "disk_offering_shared_5GB",
        "disksize": 5
    },
    "disk_offering_shared_15GB": {
        "displaytext": "disk_offering_shared_5GB",
        "name": "disk_offering_shared_5GB",
        "disksize": 15
    },
    "network2": {
        "name": "Test Network Shared",
        "displaytext": "Test Network Shared",
        "vlan": 1201,
        "gateway": "172.16.15.1",
        "netmask": "255.255.255.0",
        "startip": "172.16.15.21",
        "endip": "172.16.15.41",
        "acltype": "Account",
    },
    "network_offering": {
        "name": "Test Network offering",
        "displaytext": "Test Network offering",
        "guestiptype": "Isolated",
        "supportedservices": "Dhcp,Dns,SourceNat,PortForwarding",
        "traffictype": "GUEST",
        "availability": "Optional",
        "serviceProviderList": {
            "Dhcp": "VirtualRouter",
            "Dns": "VirtualRouter",
            "SourceNat": "VirtualRouter",
            "PortForwarding": "VirtualRouter",
        },
    },
    "nw_off_isolated_persistent": {
        "name": "Test Nw off isolated persistent",
        "displaytext": "Test Nw off isolated persistent",
        "guestiptype": "Isolated",
        "supportedservices": "Dhcp,Dns,SourceNat,PortForwarding",
        "traffictype": "GUEST",
        "ispersistent": "True",
        "availability": "Optional",
        "serviceProviderList": {
            "Dhcp": "VirtualRouter",
            "Dns": "VirtualRouter",
            "SourceNat": "VirtualRouter",
            "PortForwarding": "VirtualRouter",
        },
    },
    "nw_off_isolated_persistent_lb": {
        "name": "Test Nw off isolated persistent",
        "displaytext": "Test Nw off isolated persistent",
        "guestiptype": "Isolated",
        "supportedservices": "Dhcp,Dns,SourceNat,PortForwarding,Lb",
        "traffictype": "GUEST",
        "ispersistent": "True",
        "availability": "Optional",
        "serviceProviderList": {
            "Dhcp": "VirtualRouter",
            "Dns": "VirtualRouter",
            "SourceNat": "VirtualRouter",
            "PortForwarding": "VirtualRouter",
            "Lb": "VirtualRouter"
        },
    },
    "isolated_network_offering": {
        "name": "Network offering-DA services",
        "displaytext": "Network offering-DA services",
        "guestiptype": "Isolated",
        "supportedservices":
            "Dhcp,Dns,SourceNat,PortForwarding,Vpn,Firewall,Lb,UserData,StaticNat",
        "traffictype": "GUEST",
        "availability": "Optional",
        "serviceProviderList": {
            "Dhcp": "VirtualRouter",
            "Dns": "VirtualRouter",
            "SourceNat": "VirtualRouter",
            "PortForwarding": "VirtualRouter",
            "Vpn": "VirtualRouter",
            "Firewall": "VirtualRouter",
            "Lb": "VirtualRouter",
            "UserData": "VirtualRouter",
            "StaticNat": "VirtualRouter"
        }
    },
    "network_offering_vlan": {
        "name": "Test Network offering",
        "displaytext": "Test Network offering",
        "guestiptype": "Isolated",
        "supportedservices": "Dhcp,Dns,SourceNat,PortForwarding",
        "traffictype": "GUEST",
        "specifyvlan": "False",
        "availability": "Optional",
        "serviceProviderList": {
            "Dhcp": "VirtualRouter",
            "Dns": "VirtualRouter",
            "SourceNat": "VirtualRouter",
            "PortForwarding": "VirtualRouter",
        },
    },
    "network_offering_without_sourcenat": {
        "name": "Test Network offering",
        "displaytext": "Test Network offering",
        "guestiptype": "Isolated",
        "supportedservices": "Dhcp,Dns,UserData",
        "traffictype": "GUEST",
        "availability": "Optional",
        "serviceProviderList": {
            "Dhcp": "VirtualRouter",
            "Dns": "VirtualRouter",
            "UserData": "VirtualRouter",
        },
    },
    "isolated_network": {
        "name": "Isolated Network",
        "displaytext": "Isolated Network"
    },
    "network_without_acl": {
        "name": "TestNetwork",
        "displaytext": "TestNetwork",
    },
    "virtual_machine_userdata": {
        "displayname": "Test VM",
        "username": "root",
        "password": "password",
        "ssh_port": 22,
        "privateport": 22,
        "publicport": 22,
        "protocol": "TCP",
        "affinity": {
            "name": "webvms",
            "type": "host anti-affinity",
        },
        "userdata": "This is sample data"
    },
    "virtual_machine2": {
        "name": "testvm2",
        "displayname": "Test VM2",
    },
    "virtual_machine3": {
        "name": "testvm3",
        "displayname": "Test VM3",
    },
    "shared_network": {
        "name": "MySharedNetwork - Test",
        "displaytext": "MySharedNetwork",
        "vlan": "",
        "gateway": "",
        "netmask": "",
        "startip": "",
        "endip": "",
        "acltype": "Domain",
        "scope": "all"
    },
    "shared_network_offering": {
        "name": "MySharedOffering-shared",
        "displaytext": "MySharedOffering",
        "guestiptype": "Shared",
        "supportedservices": "Dhcp,Dns,UserData",
        "specifyVlan": "False",
        "specifyIpRanges": "False",
        "traffictype": "GUEST",
        "serviceProviderList": {
            "Dhcp": "VirtualRouter",
            "Dns": "VirtualRouter",
            "UserData": "VirtualRouter"
        }
    },
    "shared_network_offering_all_services": {
        "name": "shared network offering with services enabled",
        "displaytext": "Shared network offering",
        "guestiptype": "Shared",
        "supportedservices": "Dhcp,Dns,SourceNat,PortForwarding,Vpn,Firewall,Lb,UserData,StaticNat",
        "specifyVlan": "False",
        "specifyIpRanges": "False",
        "traffictype": "GUEST",
        "serviceProviderList": {
            "Dhcp": "VirtualRouter",
            "Dns": "VirtualRouter",
            "UserData": "VirtualRouter",
            "SourceNat": "VirtualRouter",
            "PortForwarding": "VirtualRouter",
            "Vpn": "VirtualRouter",
            "Firewall": "VirtualRouter",
            "Lb": "VirtualRouter",
            "UserData": "VirtualRouter",
            "StaticNat": "VirtualRouter"
        }
    },
    "shared_network_offering_sg": {
        "name": "MySharedOffering-sg",
        "displaytext": "MySharedOffering-sg",
        "guestiptype": "Shared",
        "supportedservices": "Dhcp,Dns,UserData,SecurityGroup",
        "specifyVlan": "False",
        "specifyIpRanges": "False",
        "traffictype": "GUEST",
        "serviceProviderList": {
            "Dhcp": "VirtualRouter",
            "Dns": "VirtualRouter",
            "UserData": "VirtualRouter",
            "SecurityGroup": "SecurityGroupProvider"
        }
    },
    "shared_network_sg": {
        "name": "Shared-Network-SG-Test",
        "displaytext": "Shared-Network_SG-Test",
        "networkofferingid": "1",
        "vlan": "",
        "gateway": "",
        "netmask": "255.255.255.0",
        "startip": "",
        "endip": "",
        "acltype": "Domain",
        "scope": "all"
    },
    "vpc_offering": {
        "name": "VPC off",
        "displaytext": "VPC off",
        "supportedservices":
            "Dhcp,Dns,SourceNat,PortForwarding,Vpn,Lb,UserData,StaticNat,NetworkACL"
    },
    "vpc_network_domain": {
        "name": "TestVPC",
        "displaytext": "TestVPC",
        "cidr": "10.0.0.1/24",
        "network_domain": "TestVPC"
    },
    "clusters": {
        0: {
            "clustername": "Xen Cluster",
            "clustertype": "CloudManaged",
            "hypervisor": "XenServer",
        },
        1: {
            "clustername": "KVM Cluster",
            "clustertype": "CloudManaged",
            "hypervisor": "KVM",
        }
    },
    "hosts": {
        "xenserver": {
            "hypervisor": "XenServer",
            "clustertype": "CloudManaged",
            "url": "http://192.168.100.211",
            "username": "root",
            "password": "fr3sca",
        },
        "kvm": {
            "hypervisor": "KVM",
            "clustertype": "CloudManaged",
            "url": "http://192.168.100.212",
            "username": "root",
            "password": "fr3sca",
        }
    },
    "network_offering_shared": {
        "name": "Test Network offering shared",
        "displaytext": "Test Network offering Shared",
        "guestiptype": "Shared",
        "supportedservices": "Dhcp,Dns,UserData",
        "traffictype": "GUEST",
        "specifyVlan": "True",
        "specifyIpRanges": "True",
        "serviceProviderList": {
            "Dhcp": "VirtualRouter",
            "Dns": "VirtualRouter",
            "UserData": "VirtualRouter",
        },
    },
    "nw_off_isolated_RVR": {
        "name": "Network offering-RVR services",
        "displaytext": "Network off-RVR services",
        "guestiptype": "Isolated",
        "supportedservices": "Vpn,Dhcp,Dns,SourceNat,PortForwarding,Firewall,Lb,UserData,StaticNat",
        "traffictype": "GUEST",
        "availability": "Optional",
        "ispersistent": "False",
        "serviceProviderList": {
            "Vpn": "VirtualRouter",
            "Dhcp": "VirtualRouter",
            "Dns": "VirtualRouter",
            "SourceNat": "VirtualRouter",
            "PortForwarding": "VirtualRouter",
            "Firewall": "VirtualRouter",
            "Lb": "VirtualRouter",
            "UserData": "VirtualRouter",
            "StaticNat": "VirtualRouter"
        },
        "serviceCapabilityList": {
            "SourceNat": {
                "SupportedSourceNatTypes": "peraccount",
                "RedundantRouter": "true"
            },
            "lb": {
                "SupportedLbIsolation": "dedicated"
            }
        }
    },
    "nw_off_persistent_RVR": {
        "name": "Network offering-RVR services",
        "displaytext": "Network off-RVR services",
        "guestiptype": "Isolated",
        "supportedservices":
            "Vpn,Dhcp,Dns,SourceNat,PortForwarding,Firewall,Lb,UserData,StaticNat",
        "traffictype": "GUEST",
        "ispersistent": "True",
        "availability": "Optional",
        "serviceProviderList": {
            "Vpn": "VirtualRouter",
            "Dhcp": "VirtualRouter",
            "Dns": "VirtualRouter",
            "SourceNat": "VirtualRouter",
            "PortForwarding": "VirtualRouter",
            "Firewall": "VirtualRouter",
            "Lb": "VirtualRouter",
            "UserData": "VirtualRouter",
            "StaticNat": "VirtualRouter",
        },
        "serviceCapabilityList": {
            "SourceNat": {
                "SupportedSourceNatTypes": "peraccount",
                "RedundantRouter": "true",
            },
            "lb": {
                "SupportedLbIsolation": "dedicated"
            },
        },
    },
    "nw_offering_isolated_vpc": {
        "name": "Isolated Network for VPC",
        "displaytext": "Isolated Network for VPC",
        "guestiptype": "Isolated",
        "supportedservices": "Dhcp,Dns,SourceNat,PortForwarding,Vpn,UserData,StaticNat,NetworkACL,Lb",
        "traffictype": "GUEST",
        "availability": "Optional",
        "ispersistent": "False",
        "useVpc": "on",
        "serviceProviderList": {
            "Dhcp": "VpcVirtualRouter",
            "Dns": "VpcVirtualRouter",
            "SourceNat": "VpcVirtualRouter",
            "PortForwarding": "VpcVirtualRouter",
            "Vpn": "VpcVirtualRouter",
            "UserData": "VpcVirtualRouter",
            "StaticNat": "VpcVirtualRouter",
            "NetworkACL": "VpcVirtualRouter",
            "Lb": "VpcVirtualRouter"
        }
    },
    "nw_off_persistent_VPCVR_LB": {
        "name": "Persistent Network VPC with LB",
        "displaytext": "Persistent Network VPC No LB",
        "guestiptype": "Isolated",
        "supportedservices": "Dhcp,Dns,SourceNat,PortForwarding,Vpn,Lb,UserData,StaticNat,NetworkACL",
        "traffictype": "GUEST",
        "availability": "Optional",
        "ispersistent": "True",
        "useVpc": "on",
        "serviceProviderList": {
            "Dhcp": "VpcVirtualRouter",
            "Dns": "VpcVirtualRouter",
            "SourceNat": "VpcVirtualRouter",
            "PortForwarding": "VpcVirtualRouter",
            "Vpn": "VpcVirtualRouter",
            "Lb": "VpcVirtualRouter",
            "UserData": "VpcVirtualRouter",
            "StaticNat": "VpcVirtualRouter",
            "NetworkACL": "VpcVirtualRouter"
        }
    },
    "nw_off_persistent_VPCVR_NoLB": {
        "name": "Persistent Network VPC No LB",
        "displaytext": "Persistent Network VPC No LB",
        "guestiptype": "Isolated",
        "supportedservices": "Dhcp,Dns,SourceNat,PortForwarding,Vpn,UserData,StaticNat,NetworkACL",
        "traffictype": "GUEST",
        "availability": "Optional",
        "ispersistent": "True",
        "useVpc": "on",
        "serviceProviderList": {
            "Dhcp": "VpcVirtualRouter",
            "Dns": "VpcVirtualRouter",
            "SourceNat": "VpcVirtualRouter",
            "PortForwarding": "VpcVirtualRouter",
            "Vpn": "VpcVirtualRouter",
            "UserData": "VpcVirtualRouter",
            "StaticNat": "VpcVirtualRouter",
            "NetworkACL": "VpcVirtualRouter"
        }
    },
    "nw_offering_shared_persistent": {
        "name": "Network offering for Shared Persistent Network",
        "displaytext": "Network offering-DA services",
        "guestiptype": "Shared",
        "supportedservices": "Dhcp,Dns,SourceNat,PortForwarding,Vpn,Firewall,Lb,UserData,StaticNat",
        "traffictype": "GUEST",
        "availability": "Optional",
        "ispersistent": "True",
        "serviceProviderList": {
            "Dhcp": "VirtualRouter",
            "Dns": "VirtualRouter",
            "SourceNat": "VirtualRouter",
            "PortForwarding": "VirtualRouter",
            "Vpn": "VirtualRouter",
            "Firewall": "VirtualRouter",
            "Lb": "VirtualRouter",
            "UserData": "VirtualRouter",
            "StaticNat": "VirtualRouter"
        }
    },
    "fwrule": {
        "startport": 22,
        "endport": 22,
        "cidr": "0.0.0.0/0",
        "protocol": "TCP"
    },
    "network_acl_rule": {
        "protocol": "TCP",
        "traffictype": "ingress",
        "cidrlist": "0.0.0.0/0",
        "startport": "1",
        "endport": "1"
    },
    "network_offering_internal_lb": {
        "name": "Network offering for internal lb service",
        "displaytext": "Network offering for internal lb service",
        "guestiptype": "Isolated",
        "traffictype": "Guest",
        "supportedservices":
            "Vpn,Dhcp,Dns,Lb,UserData,SourceNat,StaticNat,PortForwarding,NetworkACL",
        "serviceProviderList": {
            "Dhcp": "VpcVirtualRouter",
            "Dns": "VpcVirtualRouter",
            "Vpn": "VpcVirtualRouter",
            "UserData": "VpcVirtualRouter",
            "SourceNat": "VpcVirtualRouter",
            "StaticNat": "VpcVirtualRouter",
            "PortForwarding": "VpcVirtualRouter",
            "NetworkACL": "VpcVirtualRouter",
        },
        "serviceCapabilityList": {
            "SourceNat": {"SupportedSourceNatTypes": "peraccount"},
            "Lb": {"lbSchemes": "internal", "SupportedLbIsolation": "dedicated"}
        }
    },
    "egress_80": {
        "startport": 80,
        "endport": 80,
        "protocol": "TCP",
        "cidrlist": ["0.0.0.0/0"]
    },
    "vpclbrule": {
        "name": "SSH",
        "alg": "roundrobin",
        "privateport": 22,
        "publicport": 22,
        "protocol": "TCP"
    },
    "icmprule": {
        "icmptype": -1,
        "icmpcode": -1,
        "cidrlist": "0.0.0.0/0",
        "protocol": "ICMP"
    },
    "iso": {
        "displaytext": "Test ISO",
        "name": "ISO",
        "url": "http://people.apache.org/~tsp/dummy.iso",
        "bootable": False,
        "ispublic": False,
        "ostype": "Other (64-bit)",
    },
    "iso1": {
        "displaytext": "Test ISO 1",
        "name": "ISO 1",
        "url": "http://people.apache.org/~tsp/dummy.iso",
        "isextractable": True,
        "isfeatured": True,
        "ispublic": True,
        "ostype": "CentOS 5.6 (64-bit)",
    },
    "iso2": {
        "displaytext": "Test ISO 2",
        "name": "ISO 2",
        "url": "http://people.apache.org/~tsp/dummy.iso",
        "isextractable": True,
        "isfeatured": True,
        "ispublic": True,
        "ostype": "CentOS 5.6 (64-bit)",
        "mode": "HTTP_DOWNLOAD",
    },
    "isfeatured": True,
    "ispublic": True,
    "isextractable": True,
    "bootable": True,
    "passwordenabled": True,

    "template": {
        "displaytext": "xs",
        "name": "xs",
        "passwordenabled": False,
        "ostype": "CentOS 5.6 (64-bit)"

    },
    "CentOS6.3template": {
        "displaytext": "Centos",
        "name": "Centos",
        "passwordenabled": False,
        "ostype": "CentOS 6.3 (64-bit)",
        "url": "http://people.apache.org/~sanjeev/centos63.ova",
        "format": "OVA",
        "ispublic": "true"
    },
    "template_2": {
        "displaytext": "Public Template",
        "name": "Public template",
        "ostype": "CentOS 5.6 (64-bit)",
        "isfeatured": True,
        "ispublic": True,
        "isextractable": True,
        "mode": "HTTP_DOWNLOAD",
        "templatefilter": "self"
    },
    "Windows 7 (64-bit)": {
        "displaytext": "Windows 7 (64-bit)",
        "name": "Windows 7 (64-bit)",
        "passwordenabled": False,
        "url": "http://people.apache.org/~sanjeev/windows7.vhd",
        "format": "VHD",
        "ostype": "Windows 7 (64-bit)",
        "ispublic": "true",
        "hypervisor": "XenServer"
    },
    "privatetemplate": {
        "displaytext": "Public Template",
        "name": "Public template",
        "ostype": "CentOS 5.6 (64-bit)",
        "isfeatured": True,
        "ispublic": False,
        "isextractable": True,
        "mode": "HTTP_DOWNLOAD",
        "templatefilter": "self"
    },
    "volume_from_snapshot": {
        "diskname": "Volume from snapshot",
        "size": "1",
        "zoneid": ""
    },
    "templatefilter": "self",
    "templates": {
        "displaytext": "Template",
        "name": "Template",
        "ostype": "CentOS 5.3 (64-bit)",
        "templatefilter": "self",
    },
    "win2012template": {
        "displaytext": "win2012",
        "name": "win2012",
        "passwordenabled": False,
        "url": "http://people.apache.org/~sanjeev/new-test-win.ova",
        "format": "OVA",
        "ostype": "Windows 8 (64-bit)",
    },
    "rhel60template": {
        "displaytext": "Rhel60",
        "name": "Rhel60",
        "passwordenabled": False,
        "url": "http://people.apache.org/~sanjeev/Rhel6-64bit.ova",
        "format": "OVA",
        "ostype": "Red Hat Enterprise Linux 6.0 (64-bit)"
    },
    "templateregister": {
        "displaytext": "xs",
        "name": "xs",
        "passwordenabled": False,
        "url": "http://people.apache.org/~sanjeev/ttylinux_pv.vhd.bz2",
        "format": "VHD"
    },
    "security_group": {"name": "custom_Sec_Grp"},
    "ingress_rule": {
        "protocol": "TCP",
        "startport": "22",
        "endport": "22",
        "cidrlist": "0.0.0.0/0"
    },
    "ingress_rule_ICMP": {
        "name": "ICMP",
        "protocol": "ICMP",
        "startport": -1,
        "endport": -1,
        "cidrlist": "0.0.0.0/0",
    },
    "vlan_ip_range": {
        "startip": "",
        "endip": "",
        "netmask": "",
        "gateway": "",
        "forvirtualnetwork": "false",
        "vlan": "untagged",
    },
    "sleep": 90,
    "page": 1,
    "pagesize": 2,
    "listall": "true",
    "advanced_sg": {
        "zone": {
            "name": "",
            "dns1": "8.8.8.8",
            "internaldns1": "192.168.100.1",
            "networktype": "Advanced",
            "securitygroupenabled": "true"
        },
        "securitygroupenabled": "true"
    },
    "vlan": "10",
    "nfs": {
        "url": "nfs://nfs/export/automation/1/testprimary",
        "name": "Primary XEN"
    },
    "iscsi": {
        "url":
            "iscsi://192.168.100.21/iqn.2012-01.localdomain.clo-cstack-cos6:iser/1",
        "name": "Primary iSCSI"
    },
    "volume": {"diskname": "Test Volume",
               "size": 1
               },
    "volume_write_path": {
        "diskname": "APP Data Volume",
        "size": 1,  # in GBs
        "xenserver": {"rootdiskdevice": "/dev/xvda",
                      "datadiskdevice_1": "/dev/xvdb",
                      "datadiskdevice_2": "/dev/xvdc",  # Data Disk
                      },
        "kvm": {"rootdiskdevice": "/dev/vda",
                "datadiskdevice_1": "/dev/vdb",
                "datadiskdevice_2": "/dev/vdc"
                }
    },
    "data_write_paths": {
        "mount_dir": "/mnt/tmp",
        "sub_dir": "test",
        "sub_lvl_dir1": "test1",
        "sub_lvl_dir2": "test2",
        "random_data": "random.data",
    },
    "custom_volume": {
        "customdisksize": 1,
        "diskname": "Custom disk",
    },
    "volume_offerings": {
        0: {"diskname": "TestDiskServ"},
    },
    "diskdevice": ["/dev/vdc", "/dev/vdb", "/dev/hdb", "/dev/hdc",
                   "/dev/xvdd", "/dev/cdrom", "/dev/sr0", "/dev/cdrom1"],

    # test_vpc_vpn.py
    "vpn_user": {
        "username": "test",
        "password": "password",
    },
    "ntwk": {
        "name": "tier1",
        "displaytext": "vpc-tier1",
        "gateway": "10.1.1.1",
        "netmask": "255.255.255.192"
    },
    "vpc2": {
        "name": "vpc2_vpn",
        "displaytext": "vpc2-vpn",
        "cidr": "10.2.1.0/24"
    },
    "ntwk2": {
        "name": "tier2",
        "displaytext": "vpc-tier2",
        "gateway": "10.2.1.1",
        "netmask": "255.255.255.192"
    },
    "privateport": 22,
    "publicport": 22,
    "protocol": "TCP",
    "forvirtualnetwork": "true",
    "customdisksize": 1,
    "diskname": "Test Volume",
    "sparse": {
        "name": "Sparse Type Disk offering",
        "displaytext":
            "Sparse Type Disk offering",
        "disksize": 1,  # in GB
        "provisioningtype": "sparse"
    },
    "fat": {
        "name": "Fat Type Disk offering",
        "displaytext":
            "Fat Type Disk offering",
        "disksize": 1,  # in GB
        "provisioningtype": "fat"
    },
    "sparse_disk_offering": {
        "displaytext": "Sparse",
        "name": "Sparse",
        "provisioningtype": "sparse",
        "disksize": 1
    },
    "host_anti_affinity": {
        "name": "hostantiaffinity",
        "type": "host anti-affinity",
    },
    "vgpu": {
        "disk_offering": {
            "displaytext": "Small",
            "name": "Small",
            "disksize": 1
        },
        "templateregister1": {
            "displaytext": "win8withpv",
            "name": "win8withpv",
            "passwordenabled": False,
            "url": "http://pleaseupdateURL/dummy.vhd",
            "format": "VHD",
            "ostype": "Windows 8 (64-bit)",
            "ispublic": "true",
            "hypervisor": "XenServer"
        },
        "Windows 8 (64-bit)": {
            "displaytext": "Windows 8 (64-bit)",
            "name": "win8withpv",
            "passwordenabled": False,
            "url": "http://pleaseupdateURL/dummy.vhd",
            "format": "VHD",
            "ostype": "Windows 8 (64-bit)",
            "ispublic": "true",
            "hypervisor": "XenServer"
        },
        "Windows Server 2012 (64-bit)": {
            "displaytext": "Windows Server 2012 (64-bit)",
            "name": "Windows Server 2012 (64-bit)",
            "passwordenabled": False,
            "url": "http://pleaseupdateURL/dummy.vhd",
            "format": "VHD",
            "ostype": "Windows Server 2012 (64-bit)",
            "ispublic": "true",
            "hypervisor": "XenServer"
        },

        "Windows 7 (64-bit)": {
            "displaytext": "Windows 7 (64-bit)",
            "name": "Windows 7 (64-bit)",
            "passwordenabled": False,
            "url": "http://pleaseupdateURL/dummy.vhd",
            "format": "VHD",
            "ostype": "Windows 7 (64-bit)",
            "ispublic": "true",
            "hypervisor": "XenServer"
        },
        "RHEL 7 (64-bit)": {
            "displaytext": "RHEL7 (64-bit)",
            "name": "RHEL 7 Insta1",
            "passwordenabled": False,
            "url": "http://people.apache.org/~sanjeev/RHEL764bitwithtools.vhd",
            "format": "VHD",
            "ostype": "RHEL 7 (64-bit)",
            "ispublic": "true",
            "hypervisor": "XenServer"
        },
        "clusters": {
            "clustername": "Xen Cluster Vgpu",
            "clustertype": "CloudManaged",
            "hypervisor": "XenServer"
        },
        "hosts": {
            "nonvgpuxenserver": {
                "hypervisor": "XenServer",
                "clustertype": "CloudManaged",
                "url": "http://10.102.192.57",
                "username": "root",
                "password": "freebsd",
            },
        },
        "account": {
            "email": "test@test.com",
            "firstname": "Test",
            "lastname": "User",
            "username": "test",
            # Random characters are appended in create account to
            # ensure unique username generated each time
            "password": "password",
        },
        "service_offerings":
            {
                "GRID K260Q":
                    {
                        "name": "vGPU260Q",
                        "displaytext": "vGPU260Q",
                        "cpunumber": 2,
                        "cpuspeed": 1600,  # in MHz
                        "memory": 3072,  # In MBs
                    },
                "GRID K240Q":
                    {
                        "name": "vGPU240Q",
                        "displaytext": "vGPU240Q",
                        "cpunumber": 2,
                        "cpuspeed": 1600,  # in MHz
                        "memory": 3072,  # In MBs
                    },
                "GRID K220Q":
                    {
                        "name": "vGPU220Q",
                        "displaytext": "vGPU220Q",
                        "cpunumber": 2,
                        "cpuspeed": 1600,  # in MHz
                        "memory": 3072,  # In MBs
                    },
                "GRID K200":
                    {
                        "name": "vGPU200",
                        "displaytext": "vGPU200",
                        "cpunumber": 2,
                        "cpuspeed": 1600,  # in MHz
                        "memory": 3072,  # In MBs
                    },
                "passthrough":
                    {
                        "name": "vGPU passthrough",
                        "displaytext": "vGPU passthrough",
                        "cpunumber": 2,
                        "cpuspeed": 1600,  # in MHz
                        "memory": 3072,  # In MBs
                    },
                "GRID K140Q":
                    {
                        # Small service offering ID to for change VM
                        # service offering from medium to small
                        "name": "vGPU140Q",
                        "displaytext": "vGPU140Q",
                        "cpunumber": 2,
                        "cpuspeed": 1600,
                        "memory": 3072,
                    },
                "GRID K120Q":
                    {
                        "name": "vGPU120Q",
                        "displaytext": "vGPU120Q",
                        "cpunumber": 2,
                        "cpuspeed": 1600,
                        "memory": 3072,
                    },
                "GRID K100":
                    {
                        "name": "vGPU100",
                        "displaytext": "vGPU100",
                        "cpunumber": 2,
                        "cpuspeed": 1600,
                        "memory": 3072,
                    },
                "nonvgpuoffering":
                    {
                        "name": "nonvgpuoffering",
                        "displaytext": "nonvgpuoffering",
                        "cpunumber": 2,
                        "cpuspeed": 1600,
                        "memory": 3072,
                    }

            },
        "diskdevice": ["/dev/vdc", "/dev/vdb", "/dev/hdb", "/dev/hdc", "/dev/xvdd", "/dev/cdrom", "/dev/sr0",
                       "/dev/cdrom1"],
        # Disk device where ISO is attached to instance
        "mount_dir": "/mnt/tmp",
        "sleep": 180,
        "timeout": 60,
        "ostype": "Windows 8 (64-bit)",
        "nongpu_host_ip": "10.102.192.57"
    },
    "acl": {
        # data for domains and accounts
        "domain1": {
            "name": "D1",
        },
        "accountD1": {
            "email": "testD1@test.com",
            "firstname": "testD1",
            "lastname": "Admin",
            "username": "testD1",
            "password": "password",
            "accounttype": "1",
        },
        "accountD1A": {
            "email": "testD1A@test.com",
            "firstname": "testD1A",
            "lastname": "User",
            "username": "testD1A",
            "password": "password",
        },
        "accountD1B": {
            "email": "testD1B@test.com",
            "firstname": "testD1B",
            "lastname": "User",
            "username": "testD1B",
            "password": "password",
        },
        "domain11": {
            "name": "D11",
        },
        "accountD11": {
            "email": "testD11@test.com",
            "firstname": "testD11",
            "lastname": "Admin",
            "username": "testD11",
            "password": "password",
            "accounttype": "1",
        },
        "accountD11A": {
            "email": "testD11A@test.com",
            "firstname": "testD11A",
            "lastname": "User",
            "username": "testD11A",
            "password": "password",
        },
        "accountD11B": {
            "email": "test11B@test.com",
            "firstname": "testD11B",
            "lastname": "User",
            "username": "testD11B",
            "password": "password",
        },
        "domain111": {
            "name": "D111",
        },
        "accountD111": {
            "email": "testD111@test.com",
            "firstname": "testD111",
            "lastname": "Admin",
            "username": "testD111",
            "password": "password",
        },
        "accountD111A": {
            "email": "testD111A@test.com",
            "firstname": "testD111A",
            "lastname": "User",
            "username": "testD111A",
            "password": "password",
        },
        "accountD111B": {
            "email": "testD111B@test.com",
            "firstname": "testD111B",
            "lastname": "User",
            "username": "testD111B",
            "password": "password",
        },
        "domain12": {
            "name": "D12",
        },
        "accountD12A": {
            "email": "testD12A@test.com",
            "firstname": "testD12A",
            "lastname": "User",
            "username": "testD12A",
            "password": "password",
        },
        "accountD12B": {
            "email": "testD12B@test.com",
            "firstname": "testD12B",
            "lastname": "User",
            "username": "testD12B",
            "password": "password",
        },
        "domain2": {
            "name": "D2",
        },
        "accountD2": {
            "email": "testD2@test.com",
            "firstname": "testD2",
            "lastname": "User",
            "username": "testD2",
            "password": "password",
            "accounttype": "1",
        },
        "accountD2A": {
            "email": "testD2A@test.com",
            "firstname": "testD2A",
            "lastname": "User",
            "username": "testD2A",
            "password": "password",
        },
        "accountROOTA": {
            "email": "testROOTA@test.com",
            "firstname": "testROOTA",
            "lastname": "User",
            "username": "testROOTA",
            "password": "password",
        },

        "accountROOT": {
            "email": "testROOTA@test.com",
            "firstname": "testROOT",
            "lastname": "admin",
            "username": "testROOT",
            "password": "password",
        },
        # data reqd for virtual machine creation
        "vmD1": {
            "name": "d1",
            "displayname": "d1",
        },
        "vmD1A": {
            "name": "d1a",
            "displayname": "d1a",
        },
        "vmD1B": {
            "name": "d1b",
            "displayname": "d1b",
        },
        "vmD11": {
            "name": "d11",
            "displayname": "d11",
        },
        "vmD11A": {
            "name": "d11a",
            "displayname": "d11a",
        },
        "vmD11B": {
            "name": "d11b",
            "displayname": "d11b",
        },
        "vmD111": {
            "name": "d111",
            "displayname": "d111",
        },
        "vmD111A": {
            "name": "d111a",
            "displayname": "d111a",
        },
        "vmD111B": {
            "name": "d111b",
            "displayname": "d111b",
        },
        "vmD12A": {
            "name": "d12a",
            "displayname": "d12a",
        },
        "vmD12B": {
            "name": "d12b",
            "displayname": "d12b",
        },
        "vmD2A": {
            "name": "d2a",
            "displayname": "d2a",
        },

        "vmROOTA": {
            "name": "roota",
            "displayname": "roota",
        },
        "vmROOT": {
            "name": "root",
            "displayname": "root",
        },

        # data reqd for Network creation
        "network_all": {
            "name": "SharedNetwork-All",
            "displaytext": "SharedNetwork-All",
            "vlan": "4001",
            "gateway": "10.223.1.1",
            "netmask": "255.255.255.0",
            "startip": "10.223.1.2",
            "endip": "10.223.1.100",
            "acltype": "Domain"
        },
        "network_domain_with_no_subdomain_access": {
            "name": "SharedNetwork-Domain-nosubdomain",
            "displaytext": "SharedNetwork-Domain-nosubdomain",
            "vlan": "4002",
            "gateway": "10.223.1.1",
            "netmask": "255.255.255.0",
            "startip": "10.223.1.2",
            "endip": "10.223.1.100",
            "acltype": "Domain",
            "subdomainaccess": "false"
        },
        "network_domain_with_subdomain_access": {
            "name": "SharedNetwork-Domain-withsubdomain",
            "displaytext": "SharedNetwork-Domain-withsubdomain",
            "vlan": "4003",
            "gateway": "10.223.1.1",
            "netmask": "255.255.255.0",
            "startip": "10.223.1.2",
            "endip": "10.223.1.100",
            "acltype": "Domain",
            "subdomainaccess": "true"
        },
        "network_account": {
            "name": "SharedNetwork-Account",
            "displaytext": "SharedNetwork-Account",
            "vlan": "4004",
            "gateway": "10.223.1.1",
            "netmask": "255.255.255.0",
            "startip": "10.223.1.2",
            "endip": "10.223.1.100",
            "acltype": "Account"
        },

        "network": {
            "name": "Network-",
            "displaytext": "Network-",
            "gateway": "10.223.1.1",
            "netmask": "255.255.255.0",
            "startip": "10.223.59.200",
            "endip": "10.223.59.240",
            "vlan": "1000"
        },
        "iscsi": {
            "url": "",
            "name": "Primary iSCSI"
        },
        "host": {
            "publicport": 22,
            "username": "root",
            "password": "password"
        },
        "ldap_account": {
            "email": "",
            "firstname": "",
            "lastname": "",
            "username": "",
            "password": "",
        },
        "ldap_configuration": {
            "basedn": "",
            "emailAttribute": "",
            "userObject": "",
            "usernameAttribute": "",
            "hostname": "",
            "port": "",
            "ldapUsername": "",
            "ldapPassword": ""
        },
        "systemVmDelay": 120,
        "setUsageConfigurationThroughTestCase": False,
        # small service offering
        "service_offering": {
            "small": {
                "name": "Small Instance",
                "displaytext": "Small Instance",
                "cpunumber": 1,
                "cpuspeed": 100,
                "memory": 128,
            },
        },
        "ostype": "CentOS 5.6 (64-bit)",
    },
    "test_34_DeployVM_in_SecondSGNetwork": {
        "zone": "advsg",
        "config": "D:\ACS-Repo\setup\dev\\advancedsg.cfg",  # Absolute path to cfg file
        # For sample configuration please refer to <ACS repo>/setup/dev/advancedsg.cfg
        "template": "CentOS 5.3(64-bit) no GUI (Simulator)",
        "dbSvr": {
            "dbSvr": "10.146.0.133",
            "passwd": "cloud",
            "db": "cloud",
            "port": 3306,
            "user": "cloud"
        },
        "mgtSvr": [
            {
                "mgtSvrIp": "10.146.0.133",
                "passwd": "password",
                "user": "root",
                "port": 8096
            }
        ],
        "ipranges": [
            {
                "startip": "10.147.32.150",
                "endip": "10.147.32.153",
                "netmask": "255.255.255.0",
                "vlan": "32",
                "gateway": "10.147.32.1"
            }
        ]
    },
    "browser_upload_volume": {
        "VHD": {
            "diskname": "XenUploadVol",
            "url": "http://people.apache.org/~sanjeev/rajani-thin-volume.vhd",
            "checksum": "09b08b6abb1b903fca7711d3ac8d6598",
        },
        "QCOW2": {
            "diskname": "KVMUploadVol",
            "url": "http://people.apache.org/~sanjeev/rajani-thin-volume.qcow2",
            "checksum": "02de0576dd3a61ab59c03fd795fc86ac",
        },
        "browser_resized_disk_offering": {
            "displaytext": "Resizeddisk",
            "name": "Resizeddisk",
            "disksize": 3,
        }
    },
    "browser_upload_template": {
        "VHD": {
            "templatename": "XenUploadtemplate",
            "displaytext": "XenUploadtemplate",
            "url": "http://people.apache.org/~sanjeev/centos56-x86_64.vhd.bz2",
            "hypervisor": "XenServer",
            "checksum": "09b08b6abb1b903fca7711d3ac8d6598",
            "ostypeid": "74affaea-c658-11e4-ad38-a6d1374244b4"
        },
        "QCOW2": {
            "templatename": "KVMUploadtemplate",
            "displaytext": "KVMUploadtemplate",
            "url": "http://people.apache.org/~sanjeev/eec2209b-9875-3c8d-92be-c001bd8a0faf.qcow2.bz2",
            "checksum": "da997b697feaa2f1f6e0d4785b0cece2",
            "hypervisor": "KVM",
            "ostypeid": "2e02e376-cdf3-11e4-beb3-8aa6272b57ef"
        },
    },
    "configurableData":
        {
            "iscsi": {
                "url": "",
                "name": "Primary iSCSI"
            },
            "host": {
                "publicport": 22,
                "username": "root",
                "password": "password",
                "port": 22
            },
            "ldap_account": {
                "email": "",
                "firstname": "",
                "lastname": "",
                "username": "",
                "password": "",
            },
            "ldap_configuration": {
                "basedn": "",
                "emailAttribute": "",
                "userObject": "",
                "usernameAttribute": "",
                "hostname": "",
                "port": "",
                "ldapUsername": "",
                "ldapPassword": ""
            },
            "systemVmDelay": 120,
            "setUsageConfigurationThroughTestCase": True,
            "upload_volume": {
                "diskname": "UploadVol",
                "format": "VHD",
                "url": "http://download.cloud.com/releases/2.0.0/UbuntuServer-10-04-64bit.vhd.bz2",
                "checksum": "",
            },
            "bootableIso":
                {
                    "displaytext": "Test Bootable ISO",
                    "name": "testISO",
                    "bootable": True,
                    "ispublic": False,
                    "url": "http://people.apache.org/~sanjeev/CentOS-6.3-x86_64-bin-DVD1.iso",
                    "ostype": "CentOS 6.3 (64-bit)",
                    "mode": "HTTP_DOWNLOAD"
                },
            "setHostConfigurationForIngressRule": False,
            "restartManagementServerThroughTestCase": False,
            "input": "INPUT",
            "forward": "FORWARD"
        },
    "networks": {
        "network1": {
            "name": "network1",
            "displaytext": "network1",
            "gateway": "10.1.1.1",
            "netmask": "255.255.255.0"
        },
        "network2": {
            "name": "network2",
            "displaytext": "network2",
            "gateway": "10.1.2.1",
            "netmask": "255.255.255.248"
        },
        "network3": {
            "name": "network3",
            "displaytext": "network3",
            "gateway": "10.2.1.1",
            "netmask": "255.255.255.0"
        },
        "private_gateways_network": {
            "name": "private_gateways_network",
            "displaytext": "private_gateways_network",
            "cidr": "172.16.1.0/24"
        }
    },
    "vms": {
        "vm1": {
            "name": "vm1",
            "displayname": "vm1"
        },
        "vm2": {
            "name": "vm2",
            "displayname": "vm2",
            "privateport": 22,
            "publicport": 22,
            "protocol": "TCP"
        },
        "vm3": {
            "name": "vm3",
            "displayname": "vm3",
            "privateport": 22,
            "publicport": 22,
            "protocol": "TCP"
        }
    },
    "nat_rule": {
        "protocol": "TCP",
        "publicport": 22,
        "privateport": 22
    },
    "network_1": {
        "name": "Test Network 1",
        "displaytext": "Test Network 1",
        "netmask": "255.255.255.0",
        "gateway": "10.0.0.2"
    },
    "vpcN": {
        "name": "TestVPC{N}",
        "displaytext": "VPC{N}",
        "cidr": "10.{N}.0.0/16"
    },
    "network_N": {
        "name": "Test Network {N}",
        "displaytext": "Test Network {N}",
        "netmask": "255.255.255.0",
        "gateway": "10.{N}.1.1"
    },
    "vpn": {
        "vpn_user": "root",
        "vpn_pass": "Md1s#dc",
        "vpn_pass_fail": "abc!123",  # too short
        "iprange": "10.2.2.1-10.2.2.10",
        "fordisplay": "true"
    },
    "vpncustomergateway": {
        "esppolicy": "3des-md5;modp1536",
        "ikepolicy": "3des-md5;modp1536",
        "ipsecpsk": "ipsecpsk"
    },
    "natrule": {
        "privateport": 80,
        "publicport": 80,
        "startport": 80,
        "endport": 80,
        "protocol": "TCP",
        "cidrlist": "0.0.0.0/0",
    },
    "http_rule": {
        "privateport": 80,
        "publicport": 80,
        "startport": 80,
        "endport": 80,
        "cidrlist": "0.0.0.0/0",
        "protocol": "TCP"
    },
    "natrule_ssh": {
        "privateport": 22,
        "publicport": 22,
        "startport": 22,
        "endport": 22,
        "protocol": "TCP",
        "cidrlist": "0.0.0.0/0",
    },
    "vpc": {
        "name": "TestVPC",
        "displaytext": "TestVPC",
        "cidr": "10.0.0.1/24"
    },
    "network": {
        "name": "Test Network",
        "displaytext": "Test Network",
        "netmask": "255.255.255.0"
    },
    "lbrule": {
        "name": "SSH",
        "alg": "leastconn",
        # Algorithm used for load balancing
        "privateport": 22,
        "publicport": 2222,
        "openfirewall": False,
        "startport": 22,
        "endport": 2222,
        "protocol": "TCP",
        "cidrlist": "0.0.0.0/0",
    },
    "lbrule_http": {
        "name": "HTTP",
        "alg": "leastconn",
        # Algorithm used for load balancing
        "privateport": 80,
        "publicport": 8888,
        "openfirewall": False,
        "startport": 80,
        "endport": 8888,
        "protocol": "TCP",
        "cidrlist": "0.0.0.0/0",
    },

    "virtual_machine": {
        "displayname": "Test VM",
        "username": "root",
        "password": "password",
        "ssh_port": 22,
        "privateport": 22,
        "publicport": 22,
        "protocol": "TCP",
    },
    "ostype": "CentOS 5.3 (64-bit)",
    "timeout": 10,
    "acls": {
        "acl1": {
            "name": "acl1",
            "description": "acl1",
            "entries": {
                "entry1": {
                    "protocol": "TCP",
                    "action": "Allow",
                    "traffictype": "Ingress",
                    "startport": 22,
                    "endport": 22
                }
            }
        },
        "acl2": {
            "name": "acl2",
            "description": "acl2",
            "entries": {
                "entry2": {
                    "protocol": "TCP",
                    "action": "Deny",
                    "traffictype": "Ingress",
                    "startport": 22,
                    "endport": 22
                }
            }
        }
    },
    "vpcs": {
        "vpc1": {
            "name": "vpc1",
            "displaytext": "vpc1",
            "cidr": "10.1.0.0/16"
        },
        "vpc2": {
            "name": "vpc2",
            "displaytext": "vpc2",
            "cidr": "10.2.0.0/16"
        }
    },
    "private_gateways": {
        "private_gateway1": "172.16.1.1",
        "private_gateway2": "172.16.1.2"
    },
    "static_routes": {
        "static_route1": {
            "cidr": "10.2.0.0/16",
            "nexthop": "172.16.1.2"
        },
        "static_route2": {
            "cidr": "10.1.0.0/16",
            "nexthop": "172.16.1.1"
        }
    }

}
