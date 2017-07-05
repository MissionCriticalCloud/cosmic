(function ($, cloudStack) {
    cloudStack.sections.cloudOps = {
        title: 'label.menu.cloudOps',
        id: 'cloudOps',
        sectionSelect: {
            label: 'label.select-view'
        },
        sections: {
            haworkers: {
                type: 'select',
                title: 'label.ha.workers.list',
                listView: {
                    id: 'haworkers',
                    label: 'label.ha.workers.list',
                    fields: {
                        virtualmachine: {
                            label: 'label.virtual.machine'
                        },
                        state: {
                            label: 'label.state',
                            indicator: {
                                'Running': 'on',
                                'Stopped': 'off',
                                'Destroyed': 'off',
                                'Error': 'off'
                            }
                        },
                        type: {
                            label: 'label.type'
                        },
                        step: {
                            label: 'label.step'
                        },
                        hypervisor: {
                            label: 'label.hypervisor'
                        },
                        managementserver: {
                            label: 'label.management.server'
                        },
                        domain: {
                            label: 'label.domain'
                        }
                    },
                    dataProvider: function (args) {
                        const data = {};
                        listViewDataProvider(args, data);
                        $.ajax({
                            url: createURL('listHAWorkers'),
                            data: data,
                            async: true,
                            success: function (json) {
                                haworkers = json.listhaworkersresponse.haworker ? json.listhaworkersresponse.haworker : [];
                                args.response.success({
                                    data: $.map(haworkers, function (haworker) {
                                        return {
                                            id: haworker.id,
                                            virtualmachine: haworker.virtualmachinename,
                                            state: haworker.virtualmachinestate,
                                            type: haworker.type,
                                            step: haworker.step,
                                            hypervisor: haworker.hypervisor,
                                            managementserver: haworker.managementservername,
                                            domain: haworker.domainname
                                        };
                                    })
                                });
                            }
                        });
                    },
                    detailView: {
                        name: 'HA Worker details',
                        tabs: {
                            details: {
                                title: 'label.details',
                                fields: {
                                    virtualmachineid: {
                                        label: 'label.virtual.machine.id'
                                    },
                                    virtualmachinename: {
                                        label: 'label.virtual.machine.name'
                                    },
                                    virtualmachinestate: {
                                        label: 'label.virtual.machine.state'
                                    },
                                    type: {
                                        label: 'label.type'
                                    },
                                    created: {
                                        label: 'label.created',
                                        converter: cloudStack.converters.toLocalDate
                                    },
                                    previousstate: {
                                        label: 'label.previous.state'
                                    },
                                    step: {
                                        label: 'label.step'
                                    },
                                    taken: {
                                        label: 'label.step.taken',
                                        converter: cloudStack.converters.toLocalDate
                                    },
                                    hypervisor: {
                                        label: 'label.hypervisor'
                                    },
                                    managementserver: {
                                        label: 'label.management.server'
                                    },
                                    domainid: {
                                        label: 'label.domain.id'
                                    },
                                    domainname: {
                                        label: 'label.domain.name'
                                    }
                                },
                                dataProvider: function (args) {
                                    $.ajax({
                                        url: createURL("listHAWorkers"),
                                        data: {
                                            id: args.context.haworkers[0].id
                                        },
                                        success: function (json) {
                                            haworker = json.listhaworkersresponse.haworker ? json.listhaworkersresponse.haworker[0] : {};
                                            args.response.success({
                                                data: {
                                                    id: haworker.id,
                                                    virtualmachineid: haworker.virtualmachineid,
                                                    virtualmachinename: haworker.virtualmachinename,
                                                    virtualmachinestate: haworker.virtualmachinestate,
                                                    type: haworker.type,
                                                    created: haworker.created,
                                                    previousstate: haworker.state,
                                                    step: haworker.step,
                                                    taken: haworker.taken,
                                                    hypervisor: haworker.hypervisor,
                                                    managementserver: haworker.managementservername,
                                                    domainid: haworker.domainid,
                                                    domainname: haworker.domainname
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            },
            whohasthisip: {
                type: 'select',
                title: 'label.whohasthisip',
                listView: {
                    id: 'whohasthisip',
                    label: 'label.whohasthisip',
                    fields: {
                        ipaddress: {
                            label: 'label.ipaddress'
                        },
                        domain: {
                            label: 'label.domain'
                        },
                        networkname: {
                            label: 'label.network.name'
                        },
                        virtualmachine: {
                            label: 'label.virtual.machine'
                        },
                        associatednetwork: {
                            label: 'label.associated.network.short'
                        },
                        macaddress: {
                            label: 'label.macaddress'
                        },
                        ipstate: {
                            label: 'label.state',
                            indicator: {
                                'Free': 'on',
                                'Allocated': 'off'
                            }
                        }
                    },
                    dataProvider: function (args) {
                        const data = {};
                        listViewDataProvider(args, data);
                        $.extend(data, {
                            ipaddress: args.filterBy.search.value
                        });
                        $.ajax({
                            url: createURL('listWhoHasThisIp'),
                            data: data,
                            async: true,
                            success: function (json) {
                                whohasthisip = json.listwhohasthisipresponse.whohasthisip ? json.listwhohasthisipresponse.whohasthisip : [];
                                args.response.success({
                                    data: $.map(whohasthisip, function (whohasthisip) {
                                        return {
                                            ipaddress: whohasthisip.ipaddress,
                                            uuid: whohasthisip.uuid,
                                            domain: whohasthisip.domainname,
                                            networkname: whohasthisip.networkname,
                                            associatednetwork: whohasthisip.associatednetworkname,
                                            virtualmachine: whohasthisip.virtualmachinename,
                                            macaddress: whohasthisip.macaddress,
                                            mode: whohasthisip.mode,
                                            ipstate: whohasthisip.state,
                                            created: whohasthisip.created
                                        };
                                    })
                                });
                            }
                        });
                    },
                    detailView: {
                        name: 'Who has this IP details',
                        tabs: {
                            details: {
                                title: 'label.details',
                                fields: {
                                    uuid: {
                                        label: 'label.id'
                                    },
                                    ipaddress: {
                                        label: 'label.ipaddress'
                                    },
                                    domainid: {
                                        label: 'label.domain.id'
                                    },
                                    domainname: {
                                        label: 'label.domain.name'
                                    },
                                    networkname: {
                                        label: 'label.network.name'
                                    },
                                    networkid: {
                                        label: 'label.network.id'
                                    },
                                    associatednetworkname: {
                                        label: 'label.associated.network'
                                    },
                                    associatednetworkid: {
                                        label: 'label.associated.network.id'
                                    },
                                    virtualmachineid: {
                                        label: 'label.virtual.machine.id'
                                    },
                                    virtualmachinename: {
                                        label: 'label.virtual.machine.name'
                                    },
                                    type: {
                                        label: 'label.type'
                                    },
                                    macaddress: {
                                        label: 'label.macaddress'
                                    },
                                    netmask: {
                                        label: 'label.ipv4.netmask'
                                    },
                                    broadcasturi: {
                                        label: 'label.broadcasturi'
                                    },
                                    mode: {
                                        label: 'label.mode'
                                    },
                                    ipstate: {
                                        label: 'label.state'
                                    },
                                    created: {
                                        label: 'label.created',
                                        converter: cloudStack.converters.toLocalDate
                                    }
                                },
                                dataProvider: function (args) {
                                    $.ajax({
                                        url: createURL("listWhoHasThisIp"),
                                        data: {
                                            ipaddress: args.context.whohasthisip[0].ipaddress,
                                            uuid: args.context.whohasthisip[0].uuid
                                        },
                                        success: function (json) {
                                            whohasthisip = json.listwhohasthisipresponse.whohasthisip ? json.listwhohasthisipresponse.whohasthisip[0] : {};
                                            args.response.success({
                                                data: {
                                                    ipaddress: whohasthisip.ipaddress,
                                                    uuid: whohasthisip.uuid,
                                                    domainid: whohasthisip.domainuuid,
                                                    domainname: whohasthisip.domainname,
                                                    networkid: whohasthisip.networkuuid,
                                                    networkname: whohasthisip.networkname,
                                                    associatednetworkid: whohasthisip.associatednetworkuuid,
                                                    associatednetworkname: whohasthisip.associatednetworkname,
                                                    virtualmachineid: whohasthisip.virtualmachineuuid,
                                                    virtualmachinename: whohasthisip.virtualmachinename,
                                                    type: whohasthisip.virtualmachinetype,
                                                    macaddress: whohasthisip.macaddress,
                                                    netmask: whohasthisip.netmask,
                                                    broadcasturi: whohasthisip.broadcasturi,
                                                    mode: whohasthisip.mode,
                                                    ipstate: whohasthisip.state,
                                                    created: whohasthisip.created
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            }

        }
    };
})(jQuery, cloudStack);
