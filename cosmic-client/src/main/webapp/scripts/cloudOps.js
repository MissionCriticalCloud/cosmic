(function ($, cloudStack) {
    cloudStack.sections.cloudOps = {
        title: 'label.menu.cloudOps',
        id: 'cloudOps',
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
                        $.ajax({
                            url: createURL('listHAWorkers'),
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
                                    console.log(args.context.haworkers);
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
            }
        }
    };
})(jQuery, cloudStack);
