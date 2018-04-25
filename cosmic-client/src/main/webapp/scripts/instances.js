(function ($, cloudStack) {
    var vmMigrationHostObjs, ostypeObjs;

    var vmSnapshotAction = function (args) {
        var action = {
            messages: {
                notification: function (args) {
                    return 'label.action.vmsnapshot.create';
                }
            },
            label: 'label.action.vmsnapshot.create',
            addRow: 'false',
            createForm: {
                title: 'label.action.vmsnapshot.create',
                desc: 'message.action.vmsnapshot.create',
                fields: {
                    name: {
                        label: 'label.name',
                        docID: 'helpCreateInstanceSnapshotName',
                        isInput: true
                    },
                    description: {
                        label: 'label.description',
                        docID: 'helpCreateInstanceSnapshotDescription',
                        isTextarea: true
                    },
                    quiescevm: {
                        label: 'label.quiesce.vm',
                        docID: 'helpCreateInstanceSnapshotQuiesce',
                        isBoolean: true,
                        isChecked: false
                    }
                }
            },
            action: function (args) {
                var instances = args.context.instances;

                $(instances).map(function (index, instance) {
                    var array1 = [];
                    array1.push("&quiescevm=" + (args.data.quiescevm == "on"));
                    var displayname = args.data.name;
                    if (displayname != null && displayname.length > 0) {
                        array1.push("&name=" + todb(displayname));
                    }
                    var description = args.data.description;
                    if (description != null && description.length > 0) {
                        array1.push("&description=" + todb(description));
                    }
                    $.ajax({
                        url: createURL("createVMSnapshot&virtualmachineid=" + instance.id + array1.join("")),
                        dataType: "json",
                        async: true,
                        success: function (json) {
                            var jid = json.createvmsnapshotresponse.jobid;
                            args.response.success({
                                _custom: {
                                    jobId: jid,
                                    getUpdatedItem: function (json) {
                                        return json.queryasyncjobresultresponse.jobresult.virtualmachine;
                                    },
                                    getActionFilter: function () {
                                        return vmActionfilter;
                                    }
                                }
                            });
                        },
                        error: function (json) {
                            args.response.error(parseXMLHttpResponse(json));
                        }
                    });
                });

            },
            notification: {
                poll: pollAsyncJobResult
            }
        };

        if (args && args.listView) {
            $.extend(action, {
                isHeader: true,
                isMultiSelectAction: true
            });
        }

        return action;
    };

    cloudStack.sections.instances = {
        title: 'label.instances',
        id: 'instances',
        listView: {
            multiSelect: true,
            section: 'instances',
            filters: {
                all: {
                    label: 'ui.listView.filters.all'
                },
                mine: {
                    label: 'ui.listView.filters.mine'
                },
                running: {
                    label: 'state.Running'
                },
                stopped: {
                    label: 'state.Stopped'
                },
                hostname: {
                    label: 'label.hypervisor'
                },
                destroyed: {
                    preFilter: function (args) {
                        if (isAdmin() || isDomainAdmin())
                            return true;
                        else
                            return false;
                    },
                    label: 'state.Destroyed'
                }
            },
            preFilter: function (args) {
                var hiddenFields = [];
                return hiddenFields;
            },
            fields: {
                name: {
                    label: 'label.name',
                    truncate: true
                },
                instancename: {
                    label: 'label.instance.name'
                },
                displayname: {
                    label: 'label.display.name',
                    truncate: true
                },
                ipaddress: {
                    label: 'label.ip.address'
                },
                hostname: {
                    label: 'label.hypervisor',
                    converter: function (args) {
                        if (args)
                            return args.split('.')[0];
                        else
                            return '';
                    }
                },
                state: {
                    label: 'label.state',
                    indicator: {
                        'Running': 'on',
                        'Stopped': 'off',
                        'Destroyed': 'off',
                        'Error': 'off'
                    }
                }
            },

            advSearchFields: {
                name: {
                    label: 'label.name'
                },
                zoneid: {
                    label: 'label.zone',
                    select: function (args) {
                        $.ajax({
                            url: createURL('listZones'),
                            data: {
                                listAll: true
                            },
                            success: function (json) {
                                var zones = json.listzonesresponse.zone ? json.listzonesresponse.zone : [];

                                args.response.success({
                                    data: $.map(zones, function (zone) {
                                        return {
                                            id: zone.id,
                                            description: zone.name
                                        };
                                    })
                                });
                            }
                        });
                    }
                },

                domainid: {
                    label: 'label.domain',
                    select: function (args) {
                        if (isAdmin() || isDomainAdmin()) {
                            $.ajax({
                                url: createURL('listDomains'),
                                data: {
                                    listAll: true,
                                    details: 'min'
                                },
                                success: function (json) {
                                    var array1 = [{
                                        id: '',
                                        description: ''
                                    }];
                                    var domains = json.listdomainsresponse.domain;
                                    if (domains != null && domains.length > 0) {
                                        for (var i = 0; i < domains.length; i++) {
                                            array1.push({
                                                id: domains[i].id,
                                                description: domains[i].path
                                            });
                                        }
                                    }
                                    array1.sort(function (a, b) {
                                        return a.description.localeCompare(b.description);
                                    });
                                    args.response.success({
                                        data: array1
                                    });
                                }
                            });
                        } else {
                            args.response.success({
                                data: null
                            });
                        }
                    },
                    isHidden: function (args) {
                        if (isAdmin() || isDomainAdmin())
                            return false;
                        else
                            return true;
                    }
                },
                account: {
                    label: 'label.account',
                    isHidden: function (args) {
                        if (isAdmin() || isDomainAdmin())
                            return false;
                        else
                            return true;
                    }
                },

                tagKey: {
                    label: 'label.tag.key'
                },
                tagValue: {
                    label: 'label.tag.value'
                }
            },

            // List view actions
            actions: {
                // Add instance wizard
                add: {
                    label: 'label.vm.add',

                    action: {
                        custom: cloudStack.uiCustom.instanceWizard(cloudStack.instanceWizard)
                    },

                    messages: {
                        notification: function (args) {
                            return 'label.vm.add';
                        }
                    },
                    notification: {
                        poll: pollAsyncJobResult
                    }
                },
                snapshot: vmSnapshotAction({listView: true}),
                viewMetrics: {
                    label: 'label.metrics',
                    isHeader: true,
                    addRow: false,
                    action: {
                        custom: cloudStack.uiCustom.metricsView({resource: 'vms'})
                    },
                    messages: {
                        notification: function (args) {
                            return 'label.metrics';
                        }
                    }
                }
            },

            dataProvider: function (args) {
                var data = {};
                listViewDataProvider(args, data);

                if (args.filterBy != null) { //filter dropdown
                    if (args.filterBy.kind != null) {
                        switch (args.filterBy.kind) {
                            case "all":
                                break;
                            case "mine":
                                if (!args.context.projects) {
                                    $.extend(data, {
                                        domainid: g_domainid,
                                        account: g_account
                                    });
                                }
                                break;
                            case "running":
                                $.extend(data, {
                                    state: 'Running'
                                });
                                break;
                            case "stopped":
                                $.extend(data, {
                                    state: 'Stopped'
                                });
                                break;
                            case "destroyed":
                                $.extend(data, {
                                    state: 'Destroyed'
                                });
                                break;
                        }
                    }
                }

                if ("hosts" in args.context) {
                    g_hostid = args.context.hosts[0].id;
                    $.extend(data, {
                        hostid: args.context.hosts[0].id
                    });
                } else {
                    g_hostid = null;
                }

                if ("affinityGroups" in args.context) {
                    $.extend(data, {
                        affinitygroupid: args.context.affinityGroups[0].id
                    });
                }

                if ("vpc" in args.context &&
                    "networks" in args.context) {
                    $.extend(data, {
                        vpcid: args.context.vpc[0].id,
                        networkid: args.context.networks[0].id
                    });
                }

                if ("templates" in args.context) {
                    $.extend(data, {
                        templateid: args.context.templates[0].id
                    });
                }

                if ("isos" in args.context) {
                    $.extend(data, {
                        isoid: args.context.isos[0].id
                    });
                }

                if ("sshkeypairs" in args.context) {
                    $.extend(data, {
                        domainid: args.context.sshkeypairs[0].domainid,
                        account: args.context.sshkeypairs[0].account,
                        keypair: args.context.sshkeypairs[0].name
                    });
                }

                $.when(
                    $.ajax({
                        url: createURL('listVirtualMachines'),
                        async: true,
                        data: data,
                        error: function (XMLHttpResponse) {
                            cloudStack.dialog.notice({
                                message: parseXMLHttpResponse(XMLHttpResponse)
                            });
                            args.response.error();
                        }
                    }),
                    $.ajax({
                        url: createURL('listVirtualMachines&projectid=-1'),
                        async: true,
                        data: data,
                        error: function (XMLHttpResponse) {
                            cloudStack.dialog.notice({
                                message: parseXMLHttpResponse(XMLHttpResponse)
                            });
                            args.response.error();
                        }
                    })).done(function (jsonvm, jsonvmp) {
                    var items = jsonvm[0].listvirtualmachinesresponse.virtualmachine;
                    if (args.context.projects == null && isAdmin()) {
                        var pitems = jsonvmp[0].listvirtualmachinesresponse.virtualmachine;
                        if (pitems) {
                            if (items) {
                                items.push(pitems[0]);
                            } else {
                                items = pitems;
                            }
                        }
                    }
                    if (items) {
                        $.each(items, function (idx, vm) {
                            if (vm.nic && vm.nic.length > 0 && vm.nic[0].ipaddress) {
                                items[idx].ipaddress = vm.nic[0].ipaddress;
                            }
                        });
                    }
                    args.response.success({
                        data: items
                    });
                });
            },

            detailView: {
                name: 'Instance details',
                viewAll: [{
                    path: 'storage.volumes',
                    label: 'label.volumes'
                }, {
                    path: 'storage.vmsnapshots',
                    label: 'label.snapshots'
                }, {
                    path: 'affinityGroups',
                    label: 'label.affinity.groups'
                }, {
                    path: '_zone.hosts',
                    label: 'label.host',
                    preFilter: function (args) {
                        return isAdmin() && args.context.instances[0].hostid;
                    },
                    updateContext: function (args) {
                        var instance = args.context.instances[0];
                        var zone;

                        $.ajax({
                            url: createURL('listZones'),
                            data: {
                                id: instance.zoneid
                            },
                            async: false,
                            success: function (json) {
                                zone = json.listzonesresponse.zone[0]
                            }
                        });

                        return {
                            zones: [zone]
                        };
                    }
                }],
                actions: {
                    start: {
                        label: 'label.action.start.instance',
                        action: function (args) {
                            $.ajax({
                                url: createURL("startVirtualMachine&id=" + args.context.instances[0].id),
                                dataType: "json",
                                async: true,
                                success: function (json) {
                                    var jid = json.startvirtualmachineresponse.jobid;
                                    args.response.success({
                                        _custom: {
                                            jobId: jid,
                                            getUpdatedItem: function (json) {
                                                return json.queryasyncjobresultresponse.jobresult.virtualmachine;
                                            },
                                            getActionFilter: function () {
                                                return vmActionfilter;
                                            }
                                        }
                                    });
                                }
                            });
                        },
                        messages: {
                            confirm: function (args) {
                                return 'message.action.start.instance';
                            },
                            notification: function (args) {
                                return 'label.action.start.instance';
                            },
                            complete: function (args) {
                                if (args.password != null) {
                                    return 'label.vm.password' + ' ' + args.password;
                                }

                                return false;
                            }
                        },
                        notification: {
                            poll: pollAsyncJobResult
                        }
                    },
                    startByAdmin: {
                        label: 'label.action.start.instance',
                        createForm: {
                            title: 'label.action.start.instance',
                            desc: 'message.action.start.instance',
                            fields: {
                                hostId: {
                                    label: 'label.host',
                                    isHidden: function (args) {
                                        if (isAdmin())
                                            return false;
                                        else
                                            return true;
                                    },
                                    select: function (args) {
                                        if (isAdmin()) {
                                            $.ajax({
                                                url: createURL("listHosts&state=Up&type=Routing&zoneid=" + args.context.instances[0].zoneid),
                                                dataType: "json",
                                                async: true,
                                                success: function (json) {
                                                    if (json.listhostsresponse.host != undefined) {
                                                        hostObjs = json.listhostsresponse.host;
                                                        hostObjs.sort(function (a, b) {
                                                            return a.name.localeCompare(b.name);
                                                        });
                                                        var items = [{
                                                            id: -1,
                                                            description: 'Default'
                                                        }];
                                                        $(hostObjs).each(function () {
                                                            items.push({
                                                                id: this.id,
                                                                description: this.name
                                                            });
                                                        });
                                                        args.response.success({
                                                            data: items
                                                        });
                                                    } else {
                                                        cloudStack.dialog.notice({
                                                            message: _l('No Hosts are avaialble')
                                                        });
                                                    }
                                                }
                                            });
                                        } else {
                                            args.response.success({
                                                data: null
                                            });
                                        }
                                    }
                                }
                            }
                        },
                        action: function (args) {
                            var data = {
                                id: args.context.instances[0].id
                            }
                            if (args.$form.find('.form-item[rel=hostId]').css("display") != "none" && args.data.hostId != -1) {
                                $.extend(data, {
                                    hostid: args.data.hostId
                                });
                            }
                            $.ajax({
                                url: createURL("startVirtualMachine"),
                                data: data,
                                dataType: "json",
                                async: true,
                                success: function (json) {
                                    var jid = json.startvirtualmachineresponse.jobid;
                                    args.response.success({
                                        _custom: {
                                            jobId: jid,
                                            getUpdatedItem: function (json) {
                                                return json.queryasyncjobresultresponse.jobresult.virtualmachine;
                                            },
                                            getActionFilter: function () {
                                                return vmActionfilter;
                                            }
                                        }
                                    });
                                }
                            });
                        },
                        messages: {
                            confirm: function (args) {
                                return 'message.action.start.instance';
                            },
                            notification: function (args) {
                                return 'label.action.start.instance';
                            },
                            complete: function (args) {
                                if (args.password != null) {
                                    return 'label.vm.password' + ' ' + args.password;
                                }

                                return false;
                            }
                        },
                        notification: {
                            poll: pollAsyncJobResult
                        }
                    },
                    stop: {
                        label: 'label.action.stop.instance',
                        compactLabel: 'label.stop',
                        createForm: {
                            title: 'notification.stop.instance',
                            desc: 'message.action.stop.instance',
                            fields: {
                                forced: {
                                    label: 'force.stop',
                                    isBoolean: true,
                                    isChecked: false
                                }
                            }
                        },
                        action: function (args) {
                            var array1 = [];
                            array1.push("&forced=" + (args.data.forced == "on"));
                            $.ajax({
                                url: createURL("stopVirtualMachine&id=" + args.context.instances[0].id + array1.join("")),
                                dataType: "json",
                                async: true,
                                success: function (json) {
                                    var jid = json.stopvirtualmachineresponse.jobid;
                                    args.response.success({
                                        _custom: {
                                            jobId: jid,
                                            getUpdatedItem: function (json) {
                                                return $.extend(json.queryasyncjobresultresponse.jobresult.virtualmachine, {hostid: null});
                                            },
                                            getActionFilter: function () {
                                                return vmActionfilter;
                                            }
                                        }
                                    });
                                }
                            });
                        },
                        messages: {
                            confirm: function (args) {
                                return 'message.action.stop.instance';
                            },
                            notification: function (args) {
                                return 'label.action.stop.instance';
                            }
                        },
                        notification: {
                            poll: pollAsyncJobResult
                        }
                    },
                    restart: {
                        label: 'label.action.reboot.instance',
                        compactLabel: 'label.reboot',
                        action: function (args) {
                            $.ajax({
                                url: createURL("rebootVirtualMachine&id=" + args.context.instances[0].id),
                                dataType: "json",
                                async: true,
                                success: function (json) {
                                    var jid = json.rebootvirtualmachineresponse.jobid;
                                    args.response.success({
                                        _custom: {
                                            jobId: jid,
                                            getUpdatedItem: function (json) {
                                                return json.queryasyncjobresultresponse.jobresult.virtualmachine;
                                            },
                                            getActionFilter: function () {
                                                return vmActionfilter;
                                            }
                                        }
                                    });
                                }
                            });
                        },
                        messages: {
                            confirm: function (args) {
                                return 'message.action.reboot.instance';
                            },
                            notification: function (args) {
                                return 'label.action.reboot.instance';
                            },
                            complete: function (args) {
                                if (args.password != null && args.password.length > 0)
                                    return _l('message.password.has.been.reset.to') + ' ' + args.password;
                                else
                                    return null;
                            }
                        },
                        notification: {
                            poll: pollAsyncJobResult
                        }
                    },
                    snapshot: vmSnapshotAction(),
                    destroy: {
                        label: 'label.action.destroy.instance',
                        compactLabel: 'label.destroy',
                        createForm: {
                            title: 'label.action.destroy.instance',
                            desc: 'label.action.destroy.instance',
                            isWarning: true,
                            preFilter: function (args) {
                                if (!g_allowUserExpungeRecoverVm) {
                                    args.$form.find('.form-item[rel=expunge]').hide();
                                }
                            },
                            fields: {
                                expunge: {
                                    label: 'label.expunge',
                                    isBoolean: true,
                                    isChecked: false
                                }
                            }
                        },
                        messages: {
                            notification: function (args) {
                                return 'label.action.destroy.instance';
                            }
                        },
                        action: function (args) {
                            var data = {
                                id: args.context.instances[0].id
                            };
                            if (args.data.expunge == 'on') {
                                $.extend(data, {
                                    expunge: true
                                });
                            }
                            $.ajax({
                                url: createURL('destroyVirtualMachine'),
                                data: data,
                                success: function (json) {
                                    var jid = json.destroyvirtualmachineresponse.jobid;
                                    args.response.success({
                                        _custom: {
                                            jobId: jid,
                                            getUpdatedItem: function (json) {
                                                if ('virtualmachine' in json.queryasyncjobresultresponse.jobresult) //destroy without expunge
                                                    return json.queryasyncjobresultresponse.jobresult.virtualmachine;
                                                else //destroy with expunge
                                                    return {'toRemove': true};
                                            },
                                            getActionFilter: function () {
                                                return vmActionfilter;
                                            }
                                        }
                                    });
                                }
                            });
                        },
                        notification: {
                            poll: pollAsyncJobResult
                        }
                    },
                    expunge: {
                        label: 'label.action.expunge.instance',
                        compactLabel: 'label.expunge',
                        messages: {
                            confirm: function (args) {
                                return 'message.action.expunge.instance';
                            },
                            isWarning: true,
                            notification: function (args) {
                                return 'label.action.expunge.instance';
                            }
                        },
                        action: function (args) {
                            $.ajax({
                                url: createURL("expungeVirtualMachine&id=" + args.context.instances[0].id),
                                dataType: "json",
                                async: true,
                                success: function (json) {
                                    var jid = json.expungevirtualmachineresponse.jobid;
                                    args.response.success({
                                        _custom: {
                                            jobId: jid,
                                            getActionFilter: function () {
                                                return vmActionfilter;
                                            }
                                        }
                                    });
                                }
                            });
                        },
                        notification: {
                            poll: pollAsyncJobResult
                        }
                    },
                    recover: {
                        label: 'label.recover.vm',
                        compactLabel: 'label.recover.vm',
                        messages: {
                            confirm: function (args) {
                                return 'message.recover.vm';
                            },
                            notification: function (args) {
                                return 'label.recover.vm';
                            }
                        },
                        action: function (args) {
                            $.ajax({
                                url: createURL("recoverVirtualMachine&id=" + args.context.instances[0].id),
                                dataType: "json",
                                async: true,
                                success: function (json) {
                                    var item = json.recovervirtualmachineresponse.virtualmachine;
                                    args.response.success({
                                        data: item
                                    });
                                }
                            });
                        },
                        notification: {
                            poll: function (args) {
                                args.complete({
                                    data: {
                                        state: 'Stopped'
                                    }
                                });
                            }
                        }
                    },
                    reinstall: {
                        label: 'label.reinstall.vm',
                        messages: {
                            confirm: function (args) {
                                return 'message.reinstall.vm';
                            },
                            isWarning: true,
                            notification: function (args) {
                                return 'label.reinstall.vm';
                            },
                            complete: function (args) {
                                if (args.password != null && args.password.length > 0)
                                    return _l('label.password.reset.confirm') + args.password;
                                else
                                    return null;
                            }
                        },
                        createForm: {
                            title: 'label.reinstall.vm',
                            desc: 'message.reinstall.vm',
                            isWarning: true,
                            fields: {
                                template: {
                                    label: 'label.select.a.template',
                                    select: function (args) {
                                        var data = {
                                            templatefilter: 'featured'
                                        };
                                        $.ajax({
                                            url: createURL('listTemplates'),
                                            data: data,
                                            async: false,
                                            success: function (json) {
                                                var templates = json.listtemplatesresponse.template;
                                                var items = [{
                                                    id: -1,
                                                    description: ''
                                                }];
                                                $(templates).each(function () {
                                                    items.push({
                                                        id: this.id,
                                                        description: this.name
                                                    });
                                                });
                                                args.response.success({
                                                    data: items
                                                });
                                            }
                                        });
                                    }
                                }
                            }
                        },

                        action: function (args) {
                            var dataObj = {
                                virtualmachineid: args.context.instances[0].id
                            };
                            if (args.data.template != -1) {
                                $.extend(dataObj, {
                                    templateid: args.data.template
                                });
                            }

                            $.ajax({
                                url: createURL("restoreVirtualMachine"),
                                data: dataObj,
                                dataType: "json",
                                async: true,
                                success: function (json) {
                                    var jid = json.restorevmresponse.jobid;
                                    args.response.success({
                                        _custom: {
                                            jobId: jid,
                                            getUpdatedItem: function (json) {
                                                return json.queryasyncjobresultresponse.jobresult.virtualmachine;
                                            },
                                            getActionFilter: function () {
                                                return vmActionfilter;
                                            }
                                        }
                                    });
                                }
                            });

                        },

                        notification: {
                            poll: pollAsyncJobResult
                        }

                    },

                    changeAffinity: {
                        label: 'label.change.affinity',

                        action: {
                            custom: cloudStack.uiCustom.affinity({
                                tierSelect: function (args) {
                                    if ('vpc' in args.context) { //from VPC section
                                        args.$tierSelect.show(); //show tier dropdown

                                        $.ajax({ //populate tier dropdown
                                            url: createURL("listNetworks"),
                                            async: false,
                                            data: {
                                                vpcid: args.context.vpc[0].id,
                                                //listAll: true,  //do not pass listAll to listNetworks under VPC
                                                domainid: args.context.vpc[0].domainid,
                                                account: args.context.vpc[0].account,
                                                supportedservices: 'StaticNat'
                                            },
                                            success: function (json) {
                                                var networks = json.listnetworksresponse.network;
                                                var items = [{
                                                    id: -1,
                                                    description: 'message.select.tier'
                                                }];
                                                $(networks).each(function () {
                                                    items.push({
                                                        id: this.id,
                                                        description: this.displaytext
                                                    });
                                                });
                                                args.response.success({
                                                    data: items
                                                });
                                            }
                                        });
                                    } else { //from Guest Network section
                                        args.$tierSelect.hide();
                                    }

                                    args.$tierSelect.change(function () {
                                        args.$tierSelect.closest('.list-view').listView('refresh');
                                    });
                                    args.$tierSelect.closest('.list-view').listView('refresh');
                                },

                                listView: {
                                    listView: {
                                        id: 'affinityGroups',
                                        fields: {
                                            name: {
                                                label: 'label.name'
                                            },
                                            type: {
                                                label: 'label.type'
                                            }
                                        },
                                        dataProvider: function (args) {
                                            var data = {
                                                domainid: args.context.instances[0].domainid,
                                                account: args.context.instances[0].account
                                            };
                                            $.ajax({
                                                url: createURL('listAffinityGroups'),
                                                data: data,
                                                async: false, //make it sync to avoid dataProvider() being called twice which produces duplicate data
                                                success: function (json) {
                                                    var items = [];
                                                    var allAffinityGroups = json.listaffinitygroupsresponse.affinitygroup;
                                                    var previouslySelectedAffinityGroups = args.context.instances[0].affinitygroup;
                                                    if (allAffinityGroups != null) {
                                                        for (var i = 0; i < allAffinityGroups.length; i++) {
                                                            var isPreviouslySelected = false;
                                                            if (previouslySelectedAffinityGroups != null) {
                                                                for (var k = 0; k < previouslySelectedAffinityGroups.length; k++) {
                                                                    if (previouslySelectedAffinityGroups[k].id == allAffinityGroups[i].id) {
                                                                        isPreviouslySelected = true;
                                                                        break; //break for loop
                                                                    }
                                                                }
                                                            }
                                                            items.push($.extend(allAffinityGroups[i], {
                                                                _isSelected: isPreviouslySelected
                                                            }));
                                                        }
                                                    }
                                                    args.response.success({
                                                        data: items
                                                    });
                                                }
                                            });
                                        }
                                    }
                                },
                                action: function (args) {
                                    var affinityGroupIdArray = [];
                                    if (args.context.affinityGroups != null) {
                                        for (var i = 0; i < args.context.affinityGroups.length; i++) {
                                            if (args.context.affinityGroups[i]._isSelected == true) {
                                                affinityGroupIdArray.push(args.context.affinityGroups[i].id);
                                            }
                                        }
                                    }
                                    var data = {
                                        id: args.context.instances[0].id,
                                        affinitygroupids: affinityGroupIdArray.join(",")
                                    };
                                    $.ajax({
                                        url: createURL('updateVMAffinityGroup'),
                                        data: data,
                                        success: function (json) {
                                            var jid = json.updatevirtualmachineresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getUpdatedItem: function (json) {
                                                        return json.queryasyncjobresultresponse.jobresult.virtualmachine;
                                                    },
                                                    getActionFilter: function () {
                                                        return vmActionfilter;
                                                    }
                                                }
                                            });
                                        }
                                    });
                                }
                            })
                        },
                        messages: {
                            notification: function (args) {
                                return 'label.change.affinity';
                            }
                        },
                        notification: {
                            poll: pollAsyncJobResult
                        }
                    },

                    edit: {
                        label: 'label.edit',
                        action: function (args) {
                            var data = {
                                id: args.context.instances[0].id,
                                group: args.data.group,
                                isdynamicallyscalable: (args.data.isdynamicallyscalable == "on"),
                                ostypeid: args.data.guestosid
                            };
                            if (args.data.displayname != args.context.instances[0].displayname) {
                                $.extend(data, {
                                    displayName: args.data.displayname
                                });
                            }
                            $.ajax({
                                url: createURL('updateVirtualMachine'),
                                data: data,
                                success: function (json) {
                                    var item = json.updatevirtualmachineresponse.virtualmachine;
                                    args.response.success({
                                        data: item
                                    });
                                }
                            });


                            //***** addResourceDetail *****
                            //XenServer only (starts here)
                            if (args.$detailView.find('form').find('div .detail-group').find('.xenserverToolsVersion61plus').length > 0) {
                                $.ajax({
                                    url: createURL('addResourceDetail'),
                                    data: {
                                        resourceType: 'uservm',
                                        resourceId: args.context.instances[0].id,
                                        'details[0].key': 'hypervisortoolsversion',
                                        'details[0].value': (args.data.xenserverToolsVersion61plus == "on") ? 'xenserver61' : 'xenserver56'
                                    },
                                    success: function (json) {
                                        var jobId = json.addResourceDetailresponse.jobid;
                                        var addResourceDetailIntervalID = setInterval(function () {
                                            $.ajax({
                                                url: createURL("queryAsyncJobResult&jobid=" + jobId),
                                                dataType: "json",
                                                success: function (json) {
                                                    var result = json.queryasyncjobresultresponse;

                                                    if (result.jobstatus == 0) {
                                                        return; //Job has not completed
                                                    } else {
                                                        clearInterval(addResourceDetailIntervalID);

                                                        if (result.jobstatus == 1) {
                                                            //do nothing
                                                        } else if (result.jobstatus == 2) {
                                                            cloudStack.dialog.notice({
                                                                message: _s(result.jobresult.errortext)
                                                            });
                                                        }
                                                    }
                                                },
                                                error: function (XMLHttpResponse) {
                                                    cloudStack.dialog.notice({
                                                        message: parseXMLHttpResponse(XMLHttpResponse)
                                                    });
                                                }
                                            });
                                        }, g_queryAsyncJobResultInterval);
                                    }
                                });
                            }
                            //XenServer only (ends here)

                        }
                    },

                    attachISO: {
                        label: 'label.action.attach.iso',
                        createForm: {
                            title: 'label.action.attach.iso',
                            fields: {
                                iso: {
                                    label: 'label.iso',
                                    select: function (args) {
                                        var items = [];
                                        var map = {};
                                        $.ajax({
                                            url: createURL("listIsos"),
                                            data: {
                                                isofilter: 'featured',
                                                isReady: true,
                                                zoneid: args.context.instances[0].zoneid
                                            },
                                            async: false,
                                            success: function (json) {
                                                var isos = json.listisosresponse.iso;
                                                $(isos).each(function () {
                                                    items.push({
                                                        id: this.id,
                                                        description: this.displaytext
                                                    });
                                                    map[this.id] = 1;
                                                });
                                            }
                                        });
                                        $.ajax({
                                            url: createURL("listIsos"),
                                            data: {
                                                isofilter: 'community',
                                                isReady: true,
                                                zoneid: args.context.instances[0].zoneid
                                            },
                                            async: false,
                                            success: function (json) {
                                                var isos = json.listisosresponse.iso;
                                                $(isos).each(function () {
                                                    if (!(this.id in map)) {
                                                        items.push({
                                                            id: this.id,
                                                            description: this.displaytext
                                                        });
                                                        map[this.id] = 1;
                                                    }
                                                });
                                            }
                                        });
                                        $.ajax({
                                            url: createURL("listIsos"),
                                            data: {
                                                isofilter: 'selfexecutable',
                                                isReady: true,
                                                zoneid: args.context.instances[0].zoneid
                                            },
                                            async: false,
                                            success: function (json) {
                                                var isos = json.listisosresponse.iso;
                                                $(isos).each(function () {
                                                    if (!(this.id in map)) {
                                                        items.push({
                                                            id: this.id,
                                                            description: this.displaytext
                                                        });
                                                        map[this.id] = 1;
                                                    }
                                                });
                                            }
                                        });

                                        args.response.success({
                                            data: items
                                        });
                                    }
                                }
                            }
                        },
                        action: function (args) {
                            $.ajax({
                                url: createURL("attachIso&virtualmachineid=" + args.context.instances[0].id + "&id=" + args.data.iso),
                                dataType: "json",
                                async: true,
                                success: function (json) {
                                    var jid = json.attachisoresponse.jobid;
                                    args.response.success({
                                        _custom: {
                                            jobId: jid,
                                            getUpdatedItem: function (json) {
                                                return json.queryasyncjobresultresponse.jobresult.virtualmachine;
                                            },
                                            getActionFilter: function () {
                                                return vmActionfilter;
                                            }
                                        }
                                    });
                                }
                            });
                        },
                        messages: {
                            notification: function (args) {
                                return 'label.action.attach.iso';
                            }
                        },
                        notification: {
                            poll: pollAsyncJobResult
                        }
                    },

                    detachISO: {
                        label: 'label.action.detach.iso',
                        messages: {
                            confirm: function (args) {
                                return 'message.detach.iso.confirm';
                            },
                            notification: function (args) {
                                return 'label.action.detach.iso';
                            }
                        },
                        action: function (args) {
                            $.ajax({
                                url: createURL("detachIso&virtualmachineid=" + args.context.instances[0].id),
                                dataType: "json",
                                async: true,
                                success: function (json) {
                                    var jid = json.detachisoresponse.jobid;
                                    args.response.success({
                                        _custom: {
                                            jobId: jid,
                                            getUpdatedItem: function (json) {
                                                return json.queryasyncjobresultresponse.jobresult.virtualmachine;
                                            },
                                            getActionFilter: function () {
                                                return vmActionfilter;
                                            }
                                        }
                                    });
                                }
                            });
                        },
                        notification: {
                            poll: pollAsyncJobResult
                        }
                    },

                    resetPassword: {
                        label: 'label.action.reset.password',
                        messages: {
                            confirm: function (args) {
                                return 'message.action.instance.reset.password';
                            },
                            notification: function (args) {
                                return _l('label.action.reset.password');
                            },
                            complete: function (args) {
                                return _l('message.password.has.been.reset.to') + ' ' + args.password;
                            }
                        },

                        preAction: function (args) {
                            var jsonObj = args.context.instances[0];
                            if (jsonObj.passwordenabled == false) {
                                cloudStack.dialog.notice({
                                    message: 'message.reset.password.warning.notPasswordEnabled'
                                });
                                return false;
                            } else if (jsonObj.state != 'Stopped') {
                                cloudStack.dialog.notice({
                                    message: 'message.reset.password.warning.notStopped'
                                });
                                return false;
                            }
                            return true;
                        },

                        action: function (args) {
                            $.ajax({
                                url: createURL("resetPasswordForVirtualMachine&id=" + args.context.instances[0].id),
                                dataType: "json",
                                async: true,
                                success: function (json) {
                                    var jid = json.resetpasswordforvirtualmachineresponse.jobid;
                                    args.response.success({
                                        _custom: {
                                            jobId: jid,
                                            getUpdatedItem: function (json) {
                                                return json.queryasyncjobresultresponse.jobresult.virtualmachine;
                                            },
                                            getActionFilter: function () {
                                                return vmActionfilter;
                                            }
                                        }
                                    });
                                }
                            });
                        },
                        notification: {
                            poll: pollAsyncJobResult
                        }
                    },

                    createTemplate: {
                        label: 'label.create.template',
                        messages: {
                            confirm: function (args) {
                                return 'message.create.template';
                            },
                            notification: function (args) {
                                return 'label.create.template';
                            }
                        },
                        createForm: {
                            title: 'label.create.template',
                            desc: 'label.create.template',
                            preFilter: cloudStack.preFilter.createTemplate,
                            fields: {
                                name: {
                                    label: 'label.name',
                                    validation: {
                                        required: true
                                    }
                                },
                                displayText: {
                                    label: 'label.description',
                                    validation: {
                                        required: true
                                    }
                                },
                                osTypeId: {
                                    label: 'label.os.type',
                                    select: function (args) {
                                        if (ostypeObjs == undefined) {
                                            $.ajax({
                                                url: createURL("listOsTypes"),
                                                dataType: "json",
                                                async: false,
                                                success: function (json) {
                                                    ostypeObjs = json.listostypesresponse.ostype;
                                                }
                                            });
                                        }
                                        var items = [];
                                        $(ostypeObjs).each(function () {
                                            items.push({
                                                id: this.id,
                                                description: this.description
                                            });
                                        });
                                        args.response.success({
                                            data: items
                                        });
                                    }
                                },
                                isPublic: {
                                    label: 'label.public',
                                    isBoolean: true
                                },
                                url: {
                                    label: 'image.directory',
                                    validation: {
                                        required: true
                                    }
                                }
                            }
                        },
                        action: function (args) {
                            var data = {
                                virtualmachineid: args.context.instances[0].id,
                                name: args.data.name,
                                displayText: args.data.displayText,
                                osTypeId: args.data.osTypeId,
                                isPublic: (args.data.isPublic == "on"),
                                url: args.data.url
                            };

                            $.ajax({
                                url: createURL('createTemplate'),
                                data: data,
                                success: function (json) {
                                    var jid = json.createtemplateresponse.jobid;
                                    args.response.success({
                                        _custom: {
                                            jobId: jid,
                                            getUpdatedItem: function (json) {
                                                return {}; //no properties in this VM needs to be updated
                                            },
                                            getActionFilter: function () {
                                                return vmActionfilter;
                                            }
                                        }
                                    });
                                }
                            });
                        },
                        notification: {
                            poll: pollAsyncJobResult
                        }
                    },

                    migrate: {
                        label: 'label.migrate.instance.to.host',
                        compactLabel: 'label.migrate.to.host',
                        messages: {
                            notification: function (args) {
                                return 'label.migrate.instance.to.host';
                            }
                        },
                        action: {
                            custom: cloudStack.uiCustom.migrate({
                                listView: {
                                    label: 'label.migrate.instance.to.host',
                                    listView: {
                                        id: 'availableHosts',
                                        fields: {
                                            availableHostName: {
                                                label: 'label.name'
                                            },
                                            dedicated: {
                                                label: 'label.dedicated'
                                            },
                                            availableHostSuitability: {
                                                label: 'label.suitability',
                                                indicator: {
                                                    'Suitable': 'suitable',
                                                    'Suitable-Storage migration required': 'suitable suitable-storage-migration-required',
                                                    'Not Suitable': 'notsuitable',
                                                    'Not Suitable-Storage migration required': 'notsuitable notsuitable-storage-migration-required'
                                                }
                                            },
                                            cpuallocated: {
                                                label: 'label.cpu.allocated'
                                            },
                                            memoryavailable: {
                                                label: 'label.memory.available'
                                            }
                                        },
                                        dataProvider: function (args) {
                                            var data = {
                                                page: args.page,
                                                pagesize: pageSize
                                            };
                                            if (args.filterBy.search.value) {
                                                data.keyword = args.filterBy.search.value;
                                            }
                                            $.ajax({
                                                url: createURL("findHostsForMigration&VirtualMachineId=" + args.context.instances[0].id),
                                                data: data,
                                                dataType: "json",
                                                async: true,
                                                success: function (json) {
                                                    var items = [];
                                                    if (json.findhostsformigrationresponse.host != undefined) {
                                                        vmMigrationHostObjs = json.findhostsformigrationresponse.host;
                                                        if (vmMigrationHostObjs != null && vmMigrationHostObjs.length > 0) {
                                                            vmMigrationHostObjs.sort(function (a, b) {
                                                                return a.name.localeCompare(b.name);
                                                            });
                                                        }
                                                        $(vmMigrationHostObjs).each(function () {
                                                            var suitability = (this.suitableformigration ? "Suitable" : "Not Suitable");
                                                            if (this.requiresStorageMotion == true) {
                                                                suitability += ("-Storage migration required");
                                                            }
                                                            var dedicated = (this.dedicated ? "Yes, to " + this.domainname : _l('label.no'));

                                                            items.push({
                                                                id: this.id,
                                                                availableHostName: this.name,
                                                                availableHostSuitability: suitability,
                                                                cpuallocated: this.cpuallocated,
                                                                memoryavailable: (parseFloat(this.memorytotal - this.memoryallocated) / (1024.0 * 1024.0 * 1024.0)).toFixed(2) + ' GB',
                                                                dedicated: dedicated,
                                                                requiresStorageMotion: this.requiresStorageMotion
                                                            });
                                                        });
                                                        args.response.success({
                                                            data: items
                                                        });
                                                    } else if (args.page == 1) {
                                                        cloudStack.dialog.notice({
                                                            message: _l('message.no.host.available')
                                                        }); //Only a single host in the set up
                                                        args.response.success({
                                                            data: []
                                                        });
                                                    } else {
                                                        args.response.success({
                                                            data: []
                                                        });
                                                    }
                                                }
                                            });
                                        }
                                    }
                                },
                                action: function (args) {
                                    var selectedHostObj;
                                    if (args.context.selectedHost != null && args.context.selectedHost.length > 0) {
                                        selectedHostObj = args.context.selectedHost[0];
                                        if (selectedHostObj.requiresStorageMotion == true) {
                                            $.ajax({
                                                url: createURL("migrateVirtualMachineWithVolume&hostid=" + selectedHostObj.id + "&virtualmachineid=" + args.context.instances[0].id),
                                                dataType: "json",
                                                async: true,
                                                success: function (json) {
                                                    var jid = json.migratevirtualmachinewithvolumeresponse.jobid;
                                                    args.response.success({
                                                        _custom: {
                                                            jobId: jid,
                                                            getUpdatedItem: function (json) {
                                                                return json.queryasyncjobresultresponse.jobresult.virtualmachine;
                                                            },
                                                            getActionFilter: function () {
                                                                return vmActionfilter;
                                                            }
                                                        }
                                                    });
                                                }
                                            });
                                        } else {
                                            $.ajax({
                                                url: createURL("migrateVirtualMachine&hostid=" + selectedHostObj.id + "&virtualmachineid=" + args.context.instances[0].id),
                                                dataType: "json",
                                                async: true,
                                                success: function (json) {
                                                    var jid = json.migratevirtualmachineresponse.jobid;
                                                    args.response.success({
                                                        _custom: {
                                                            jobId: jid,
                                                            getUpdatedItem: function (json) {
                                                                return json.queryasyncjobresultresponse.jobresult.virtualmachine;
                                                            },
                                                            getActionFilter: function () {
                                                                return vmActionfilter;
                                                            }
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    }
                                }
                            })
                        },
                        notification: {
                            poll: pollAsyncJobResult
                        }
                    },
                    migrateToAnotherStorage: {
                        label: 'label.migrate.instance.to.ps',
                        compactLabel: 'label.migrate.to.storage',
                        messages: {
                            notification: function (args) {
                                return 'label.migrate.instance.to.host';
                            }
                        },
                        action: {
                            custom: cloudStack.uiCustom.migrate({
                                listView: {
                                    label: 'label.migrate.instance.to.ps',
                                    listView: {
                                        id: 'availableHosts',
                                        fields: {
                                            availableStorage: {
                                                label: 'label.primary.storage'
                                            }
                                        },
                                        dataProvider: function (args) {
                                            var data = {
                                                page: args.page,
                                                pagesize: pageSize
                                            };
                                            if (args.filterBy.search.value) {
                                                data.keyword = args.filterBy.search.value;
                                            }
                                            $.ajax({
                                                url: createURL("listStoragePools&zoneid=" + args.context.instances[0].zoneid),
                                                dataType: "json",
                                                async: true,
                                                data: data,
                                                success: function (json) {
                                                    var items = [];
                                                    if ('storagepool' in json.liststoragepoolsresponse) {
                                                        var pools = json.liststoragepoolsresponse.storagepool;
                                                        pools.sort(function (a, b) {
                                                            return a.name.localeCompare(b.name);
                                                        });
                                                        $(pools).each(function () {
                                                            items.push({
                                                                id: this.id,
                                                                availableStorage: this.name
                                                            });
                                                        });
                                                    }
                                                    args.response.success({
                                                        data: items
                                                    });
                                                }
                                            });
                                        }
                                    }
                                },
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("migrateVirtualMachine&storageid=" + args.context.selectedHost[0].id + "&virtualmachineid=" + args.context.instances[0].id),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var jid = json.migratevirtualmachineresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getUpdatedItem: function (json) {
                                                        return json.queryasyncjobresultresponse.jobresult.virtualmachine;
                                                    },
                                                    getActionFilter: function () {
                                                        return vmActionfilter;
                                                    }
                                                }
                                            });
                                        }
                                    });
                                }
                            })
                        },
                        notification: {
                            poll: pollAsyncJobResult
                        }
                    },

                    scaleUp: {
                        label: 'label.change.service.offering',
                        createForm: {
                            title: 'label.change.service.offering',
                            desc: function (args) {
                                var description = '';
                                var vmObj = args.jsonObj;
                                if (vmObj.state == 'Running' && vmObj.hypervisor == 'VMware') {
                                    description = 'message.read.admin.guide.scaling.up';
                                }
                                return description;
                            },
                            fields: {
                                serviceofferingid: {
                                    label: 'label.compute.offering',
                                    select: function (args) {
                                        var serviceofferingObjs;
                                        $.ajax({
                                            url: createURL("listServiceOfferings&VirtualMachineId=" + args.context.instances[0].id),
                                            dataType: "json",
                                            async: true,
                                            success: function (json) {
                                                serviceofferingObjs = json.listserviceofferingsresponse.serviceoffering;
                                                serviceofferingObjs.sort(function (a, b) {
                                                    return a.name.localeCompare(b.name);
                                                });
                                                var items = [];
                                                if (serviceofferingObjs != null) {
                                                    for (var i = 0; i < serviceofferingObjs.length; i++) {
                                                        items.push({
                                                            id: serviceofferingObjs[i].id,
                                                            description: serviceofferingObjs[i].name
                                                        });
                                                    }
                                                }
                                                args.response.success({
                                                    data: items
                                                });
                                            }
                                        });

                                        args.$select.change(function () {
                                            var $form = $(this).closest('form');

                                            var serviceofferingid = $(this).val();
                                            if (serviceofferingid == null || serviceofferingid.length == 0)
                                                return;

                                            var items = [];
                                            var selectedServiceofferingObj;
                                            if (serviceofferingObjs != null) {
                                                for (var i = 0; i < serviceofferingObjs.length; i++) {
                                                    if (serviceofferingObjs[i].id == serviceofferingid) {
                                                        selectedServiceofferingObj = serviceofferingObjs[i];
                                                        break;
                                                    }
                                                }
                                            }
                                            if (selectedServiceofferingObj == undefined)
                                                return;

                                            if (selectedServiceofferingObj.iscustomized == true) {
                                                $form.find('.form-item[rel=cpuNumber]').css('display', 'inline-block');
                                                $form.find('.form-item[rel=memory]').css('display', 'inline-block');
                                            } else {
                                                $form.find('.form-item[rel=cpuNumber]').hide();
                                                $form.find('.form-item[rel=memory]').hide();
                                            }
                                        });
                                    }
                                },
                                cpuNumber: {
                                    label: 'label.num.cpu.cores',
                                    validation: {
                                        required: true,
                                        number: true
                                    },
                                    isHidden: true
                                },
                                memory: {
                                    label: 'label.memory.mb',
                                    validation: {
                                        required: true,
                                        number: true
                                    },
                                    isHidden: true
                                }
                            }
                        },

                        action: function (args) {
                            var data = {
                                id: args.context.instances[0].id,
                                serviceofferingid: args.data.serviceofferingid
                            };

                            if (args.$form.find('.form-item[rel=cpuNumber]').is(':visible')) {
                                $.extend(data, {
                                    'details[0].cpuNumber': args.data.cpuNumber
                                });
                            }
                            if (args.$form.find('.form-item[rel=memory]').is(':visible')) {
                                $.extend(data, {
                                    'details[0].memory': args.data.memory
                                });
                            }

                            $.ajax({
                                url: createURL('scaleVirtualMachine'),
                                data: data,
                                success: function (json) {
                                    var jid = json.scalevirtualmachineresponse.jobid;
                                    args.response.success({
                                        _custom: {
                                            jobId: jid,
                                            getUpdatedItem: function (json) {
                                                return json.queryasyncjobresultresponse.jobresult.virtualmachine;
                                            },
                                            getActionFilter: function () {
                                                return vmActionfilter;
                                            }
                                        }
                                    });

                                },
                                error: function (json) {
                                    args.response.error(parseXMLHttpResponse(json));
                                }

                            });
                        },
                        messages: {
                            notification: function (args) {
                                return 'label.change.service.offering';  //CLOUDSTACK-7744
                            }
                        },
                        notification: {
                            poll: pollAsyncJobResult
                        }
                    },
                    resetSSHKeyForVirtualMachine: {
                        label: 'label.reset.ssh.key.pair',
                        createForm: {
                            title: 'label.reset.ssh.key.pair.on.vm',
                            desc: 'message.desc.reset.ssh.key.pair',
                            fields: {
                                sshkeypair: {
                                    label: 'label.new.ssh.key.pair',
                                    validation: {
                                        required: true
                                    },
                                    select: function (args) {
                                        var data = {
                                            domainid: args.context.instances[0].domainid,
                                            account: args.context.instances[0].account,
                                            listAll: true
                                        };

                                        $.ajax({
                                            url: createURL("listSSHKeyPairs"),
                                            data: data,
                                            async: false,
                                            success: function (json) {
                                                var items = [];
                                                var sshkeypairs = json.listsshkeypairsresponse.sshkeypair;
                                                if (sshkeypairs == null) {
                                                } else {
                                                    for (var i = 0; i < sshkeypairs.length; i++) {
                                                        var sshkeypair = sshkeypairs[i];
                                                        if (sshkeypair.name != args.context.instances[0].keypair) {
                                                            items.push({
                                                                id: sshkeypair.name,
                                                                description: sshkeypair.name
                                                            });
                                                        }
                                                    }
                                                }
                                                args.response.success({
                                                    data: items
                                                });
                                            }
                                        });
                                    }
                                }
                            }
                        },

                        action: function (args) {
                            var data = {
                                domainid: args.context.instances[0].domainid,
                                account: args.context.instances[0].account,
                                id: args.context.instances[0].id,
                                keypair: args.data.sshkeypair
                            };

                            $.ajax({
                                url: createURL("resetSSHKeyForVirtualMachine"),
                                data: data,
                                async: true,
                                success: function (json) {
                                    var jid = json.resetSSHKeyforvirtualmachineresponse.jobid;
                                    args.response.success({
                                        _custom: {
                                            jobId: jid,
                                            getUpdatedItem: function (json) {
                                                return json.queryasyncjobresultresponse.jobresult.virtualmachine;
                                            },
                                            getActionFilter: function () {
                                                return vmActionfilter;
                                            }
                                        }
                                    });
                                }
                            });
                        },
                        messages: {
                            notification: function (args) {
                                return _l('label.reset.ssh.key.pair.on.vm');
                            },
                            complete: function (args) {
                                if (args.password != null) {
                                    return _l('message.password.of.the.vm.has.been.reset.to') + ' ' + args.password;
                                }

                                return false;
                            }
                        },
                        notification: {
                            poll: pollAsyncJobResult
                        }
                    },

                    assignVmToAnotherAccount: {
                        label: 'label.assign.instance.another',
                        createForm: {
                            title: 'label.assign.instance.another',
                            fields: {
                                domainid: {
                                    label: 'label.domain',
                                    validation: {
                                        required: true
                                    },
                                    select: function (args) {
                                        $.ajax({
                                            url: createURL('listDomains'),
                                            data: {
                                                listAll: true,
                                                details: 'min'
                                            },
                                            success: function (json) {
                                                var array1 = [];
                                                var domains = json.listdomainsresponse.domain;
                                                if (domains != null && domains.length > 0) {
                                                    for (var i = 0; i < domains.length; i++) {
                                                        array1.push({
                                                            id: domains[i].id,
                                                            description: domains[i].path
                                                        });
                                                    }
                                                }
                                                array1.sort(function (a, b) {
                                                    return a.description.localeCompare(b.description);
                                                });
                                                args.response.success({
                                                    data: array1
                                                });
                                            }
                                        });
                                    }
                                },
                                account: {
                                    label: 'label.account',
                                    validation: {
                                        required: true
                                    }
                                }
                            }
                        },
                        action: function (args) {
                            $.ajax({
                                url: createURL('assignVirtualMachine'),
                                data: {
                                    virtualmachineid: args.context.instances[0].id,
                                    domainid: args.data.domainid,
                                    account: args.data.account
                                },
                                success: function (json) {
                                    var item = json.assignvirtualmachineresponse.virtualmachine;
                                    args.response.success({
                                        data: item
                                    });
                                }
                            });
                        },
                        messages: {
                            notification: function (args) {
                                return 'label.assign.instance.another';
                            }
                        },
                        notification: {
                            poll: function (args) {
                                args.complete();
                            }
                        }
                    },

                    viewConsole: {
                        label: 'label.view.console',
                        action: {
                            externalLink: {
                                url: function (args) {
                                    return clientConsoleUrl + '?cmd=access&vm=' + args.context.instances[0].id;
                                },
                                title: function (args) {
                                    return args.context.instances[0].id.substr(0, 8); //title in window.open() can't have space nor longer than 8 characters. Otherwise, IE browser will have error.
                                },
                                width: 820,
                                height: 640
                            }
                        }
                    }
                },
                tabs: {
                    // Details tab
                    details: {
                        title: 'label.details',

                        preFilter: function (args) {
                            var hiddenFields;
                            if (isAdmin()) {
                                hiddenFields = [];
                            } else {
                                hiddenFields = ["hypervisor", 'xenserverToolsVersion61plus'];
                            }

                            if ('instances' in args.context && args.context.instances[0].hypervisor != 'XenServer') {
                                hiddenFields.push('xenserverToolsVersion61plus');
                            }

                            if ('instances' in args.context && args.context.instances[0].guestosid != undefined) {
                                if (ostypeObjs == undefined) {
                                    $.ajax({
                                        url: createURL("listOsTypes"),
                                        dataType: "json",
                                        async: false,
                                        success: function (json) {
                                            ostypeObjs = json.listostypesresponse.ostype;
                                        }
                                    });
                                }
                                if (ostypeObjs != undefined) {
                                    var ostypeName;
                                    for (var i = 0; i < ostypeObjs.length; i++) {
                                        if (ostypeObjs[i].id == args.context.instances[0].guestosid) {
                                            ostypeName = ostypeObjs[i].description;
                                            break;
                                        }
                                    }
                                    if (ostypeName == undefined || ostypeName.indexOf("Win") == -1) {
                                        hiddenFields.push('xenserverToolsVersion61plus');
                                    }
                                }
                            }

                            if (!args.context.instances[0].publicip) {
                                hiddenFields.push('publicip');
                            }

                            return hiddenFields;
                        },

                        fields: [{
                            displayname: {
                                label: 'label.display.name',
                                isEditable: true
                            },
                            name: {
                                label: 'label.host.name'
                            },
                            instancename: {
                                label: 'label.instance.name'
                            },
                            id: {
                                label: 'label.id'
                            },
                            state: {
                                label: 'label.state',
                                pollAgainIfValueIsIn: {
                                    'Starting': 1,
                                    'Stopping': 1
                                },
                                pollAgainFn: function (context) {
                                    var toClearInterval = false;
                                    $.ajax({
                                        url: createURL("listVirtualMachines&id=" + context.instances[0].id),
                                        dataType: "json",
                                        async: false,
                                        success: function (json) {
                                            var jsonObj = json.listvirtualmachinesresponse.virtualmachine[0];
                                            if (jsonObj.state != context.instances[0].state) {
                                                toClearInterval = true; //to clear interval
                                            }
                                        }
                                    });
                                    return toClearInterval;
                                }
                            },
                            templatename: {
                                label: 'label.template'
                            },
                            guestosid: {
                                label: 'label.os.type',
                                isEditable: true,
                                select: function (args) {
                                    if (ostypeObjs == undefined) {
                                        $.ajax({
                                            url: createURL("listOsTypes"),
                                            dataType: "json",
                                            async: false,
                                            success: function (json) {
                                                ostypeObjs = json.listostypesresponse.ostype;
                                            }
                                        });
                                    }
                                    var items = [];
                                    $(ostypeObjs).each(function () {
                                        items.push({
                                            id: this.id,
                                            description: this.description
                                        });
                                    });
                                    args.response.success({
                                        data: items
                                    });
                                }
                            },
                            rootdevicecontroller: {
                                label: 'label.root.disk.controller'
                            },
                            hypervisor: {
                                label: 'label.hypervisor'
                            },

                            xenserverToolsVersion61plus: {
                                label: 'label.Xenserver.Tools.Version61plus',
                                isBoolean: true,
                                isEditable: function () {
                                    if (isAdmin())
                                        return true;
                                    else
                                        return false;
                                },
                                converter: cloudStack.converters.toBooleanText
                            },

                            isoname: {
                                label: 'label.attached.iso'
                            },

                            serviceofferingname: {
                                label: 'label.compute.offering'
                            },
                            cpunumber: {
                                label: 'label.num.cpu.cores'
                            },
                            memory: {
                                label: 'label.memory.mb'
                            },
                            haenable: {
                                label: 'label.ha.enabled',
                                converter: cloudStack.converters.toBooleanText
                            },
                            publicip: {
                                label: 'label.public.ip'
                            },
                            group: {
                                label: 'label.group',
                                isEditable: true
                            },
                            zonename: {
                                label: 'label.zone.name',
                                isEditable: false
                            },
                            hostname: {
                                label: 'label.host'
                            },
                            keypair: {
                                label: 'label.ssh.key.pair'
                            },
                            passwordenabled: {
                                label: 'label.password.enabled',
                                converter: cloudStack.converters.toBooleanText
                            },
                            domain: {
                                label: 'label.domain'
                            },
                            account: {
                                label: 'label.account'
                            },
                            username: {
                                label: 'label.created.by'
                            },
                            created: {
                                label: 'label.created',
                                converter: cloudStack.converters.toLocalDate
                            }
                        }],

                        tags: cloudStack.api.tags({
                            resourceType: 'UserVm',
                            contextId: 'instances'
                        }),

                        dataProvider: function (args) {
                            $.ajax({
                                url: createURL("listVirtualMachines&id=" + args.context.instances[0].id),
                                dataType: "json",
                                async: true,
                                success: function (json) {
                                    var jsonObj;
                                    if (json.listvirtualmachinesresponse.virtualmachine != null && json.listvirtualmachinesresponse.virtualmachine.length > 0)
                                        jsonObj = json.listvirtualmachinesresponse.virtualmachine[0];
                                    else if (isAdmin())
                                        jsonObj = $.extend(args.context.instances[0], {
                                            state: "Expunged"
                                        }); //after root/domain admin expunge a VM, listVirtualMachines API will no longer returns this expunged VM to all users.
                                    else
                                        jsonObj = $.extend(args.context.instances[0], {
                                            state: "Destroyed"
                                        }); //after a regular user destroys a VM, listVirtualMachines API will no longer returns this destroyed VM to the regular user.

                                    if ('details' in jsonObj && 'hypervisortoolsversion' in jsonObj.details) {
                                        if (jsonObj.details.hypervisortoolsversion == 'xenserver61')
                                            jsonObj.xenserverToolsVersion61plus = true;
                                        else
                                            jsonObj.xenserverToolsVersion61plus = false;
                                    }

                                    $(window).trigger('cloudStack.module.sharedFunctions.addExtraProperties', {
                                        obj: jsonObj,
                                        objType: "UserVM"
                                    });

                                    args.response.success({
                                        actionFilter: vmActionfilter,
                                        data: jsonObj
                                    });
                                }
                            });
                        }
                    },

                    /**
                     * NICs tab
                     */
                    nics: {
                        title: 'label.nics',
                        multiple: true,
                        actions: {
                            add: {
                                label: 'label.network.addVM',
                                messages: {
                                    confirm: function (args) {
                                        return 'message.network.addVMNIC';
                                    },
                                    notification: function (args) {
                                        return 'label.network.addVM';
                                    }
                                },
                                createForm: {
                                    title: 'label.network.addVM',
                                    desc: 'message.network.addVM.desc',
                                    fields: {
                                        networkid: {
                                            label: 'label.network',
                                            select: function (args) {
                                                var dataUser = {
                                                    zoneid: args.context.instances[0].zoneid
                                                };
                                                if (isAdmin()) {
                                                    $.extend(dataUser, {
                                                        listAll: true
                                                    });
                                                } else {
                                                    $.extend(dataUser, {
                                                        account: args.context.instances[0].account,
                                                        domainid: args.context.instances[0].domainid
                                                    });
                                                }
                                                $.when(
                                                    $.ajax({
                                                        url: createURL('listNetworks'),
                                                        data: dataUser,
                                                        success: function (json) {
                                                            var networkObjs = json.listnetworksresponse.network;
                                                            var nicObjs = args.context.instances[0].nic;

                                                            for (var i = 0; i < networkObjs.length; i++) {
                                                                var networkObj = networkObjs[i];
                                                                var isNetworkExists = false;

                                                                for (var j = 0; j < nicObjs.length; j++) {
                                                                    if (nicObjs[j].networkid == networkObj.id) {
                                                                        isNetworkExists = true;
                                                                        break;
                                                                    }
                                                                }

                                                                if (!isNetworkExists) {
                                                                    var vpcName = "";
                                                                    if (networkObj.vpcname != null) {
                                                                        vpcName = " (VPC " + networkObj.vpcname + ")";
                                                                    }
                                                                    if (typeof(itemsUser) == "undefined") {
                                                                        var itemsUser = [];
                                                                    }
                                                                    itemsUser.push({
                                                                        id: networkObj.id,
                                                                        description: networkObj.name + vpcName
                                                                    });
                                                                }
                                                            }
                                                            if (typeof(itemsUser) != "undefined") {
                                                                args.response.success({
                                                                    data: itemsUser
                                                                });
                                                            }
                                                        }
                                                    }),

                                                    $.ajax({
                                                        url: createURL('listNetworks'),
                                                        data: {
                                                            issystem: true,
                                                            trafficType: "Public"
                                                        },
                                                        success: function (json) {
                                                            var networkObjs = json.listnetworksresponse.network;
                                                            var nicObjs = args.context.instances[0].nic;

                                                            for (var i = 0; i < networkObjs.length; i++) {
                                                                var networkObj = networkObjs[i];
                                                                var isNetworkExists = false;

                                                                for (var j = 0; j < nicObjs.length; j++) {
                                                                    if (nicObjs[j].networkid == networkObj.id) {
                                                                        isNetworkExists = true;
                                                                        break;
                                                                    }
                                                                }

                                                                if (!isNetworkExists) {
                                                                    if (typeof(itemsPub) == "undefined") {
                                                                        var itemsPub = [];
                                                                    }
                                                                    itemsPub.push({
                                                                        id: networkObj.id,
                                                                        description: "Public Network"
                                                                    });
                                                                }
                                                            }

                                                            if (typeof(itemsPub) != "undefined") {
                                                                args.response.success({
                                                                    data: itemsPub
                                                                });
                                                            }
                                                        }
                                                    })).done(function (data1, data2) {
                                                    var selectList = $('#label_network option');
                                                    selectList.sort(function (a, b) {
                                                        return a.text.localeCompare(b.text);
                                                    });

                                                    $('#label_network option').filter(function () {
                                                        return !this.value || $.trim(this.value).length == 0 || $.trim(this.text).length == 0;
                                                    }).remove();

                                                    $('#label_network').html(selectList);

                                                    return data1.concat(data2);
                                                });
                                            }
                                        }
                                    }
                                },
                                action: function (args) {
                                    $.ajax({
                                        url: createURL('addNicToVirtualMachine'),
                                        data: {
                                            virtualmachineid: args.context.instances[0].id,
                                            networkid: args.data.networkid
                                        },
                                        success: function (json) {
                                            args.response.success({
                                                _custom: {
                                                    jobId: json.addnictovirtualmachineresponse.jobid,
                                                    getUpdatedItem: function (json) {
                                                        return json.queryasyncjobresultresponse.jobresult.virtualmachine;
                                                    }
                                                }
                                            });
                                        }
                                    });
                                },
                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            },

                            makeDefault: {
                                label: 'label.set.default.NIC',
                                messages: {
                                    confirm: function () {
                                        return 'message.set.default.NIC';
                                    },
                                    notification: function (args) {
                                        return 'label.set.default.NIC'
                                    }
                                },
                                action: function (args) {
                                    $.ajax({
                                        url: createURL('updateDefaultNicForVirtualMachine'),
                                        data: {
                                            virtualmachineid: args.context.instances[0].id,
                                            nicid: args.context.nics[0].id
                                        },
                                        success: function (json) {
                                            args.response.success({
                                                _custom: {
                                                    jobId: json.updatedefaultnicforvirtualmachineresponse.jobid
                                                }
                                            });
                                            cloudStack.dialog.notice({
                                                message: _l('message.set.default.NIC.manual')
                                            });
                                        }
                                    });
                                },
                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            },

                            updateIpaddr: {
                                label: 'label.change.ipaddress',
                                messages: {
                                    confirm: function () {
                                        return 'message.change.ipaddress';
                                    },
                                    notification: function (args) {
                                        return 'label.change.ipaddress';
                                    }
                                },
                                createForm: {
                                    title: 'label.change.ipaddress',
                                    desc: 'message.change.ipaddress',
                                    preFilter: function (args) {
                                        if (args.context.nics != null && args.context.nics[0].type == 'Isolated') {
                                            args.$form.find('.form-item[rel=ipaddress1]').css('display', 'inline-block'); //shown text
                                            args.$form.find('.form-item[rel=ipaddress2]').hide();
                                        } else if (args.context.nics != null && args.context.nics[0].type == 'Shared') {
                                            args.$form.find('.form-item[rel=ipaddress2]').css('display', 'inline-block'); //shown list
                                            args.$form.find('.form-item[rel=ipaddress1]').hide();
                                        }
                                    },
                                    fields: {
                                        ipaddress1: {
                                            label: 'label.ip.address'
                                        },
                                        ipaddress2: {
                                            label: 'label.ip.address',
                                            select: function (args) {
                                                if (args.context.nics != null && args.context.nics[0].type == 'Shared') {
                                                    $.ajax({
                                                        url: createURL('listPublicIpAddresses'),
                                                        data: {
                                                            allocatedonly: false,
                                                            networkid: args.context.nics[0].networkid,
                                                            forvirtualnetwork: false
                                                        },
                                                        success: function (json) {
                                                            var ips = json.listpublicipaddressesresponse.publicipaddress;
                                                            var items = [{
                                                                id: -1,
                                                                description: ''
                                                            }];
                                                            $(ips).each(function () {
                                                                if (this.state == "Free") {
                                                                    items.push({
                                                                        id: this.ipaddress,
                                                                        description: this.ipaddress
                                                                    });
                                                                }
                                                            });
                                                            args.response.success({
                                                                data: items
                                                            });
                                                        }
                                                    });
                                                } else {
                                                    args.response.success({
                                                        data: null
                                                    });
                                                }
                                            }
                                        }
                                    }
                                },
                                action: function (args) {
                                    var dataObj = {
                                        nicId: args.context.nics[0].id
                                    };

                                    if (args.data.ipaddress1) {
                                        dataObj.ipaddress = args.data.ipaddress1;
                                    } else if (args.data.ipaddress2 != -1) {
                                        dataObj.ipaddress = args.data.ipaddress2;
                                    }

                                    $.ajax({
                                        url: createURL('updateVmNicIp'),
                                        data: dataObj,
                                        success: function (json) {
                                            args.response.success({
                                                _custom: {
                                                    jobId: json.updatevmnicipresponse.jobid,
                                                    getUpdatedItem: function (json) {
                                                        return json.queryasyncjobresultresponse.jobresult.virtualmachine;
                                                    }
                                                }
                                            });
                                        }
                                    });
                                },

                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            },

                            // Remove NIC/Network from VM
                            remove: {
                                label: 'label.action.delete.nic',
                                messages: {
                                    confirm: function (args) {
                                        return 'message.action.delete.nic';
                                    },
                                    notification: function (args) {
                                        return 'label.action.delete.nic';
                                    }
                                },
                                action: function (args) {
                                    $.ajax({
                                        url: createURL('removeNicFromVirtualMachine'),
                                        data: {
                                            virtualmachineid: args.context.instances[0].id,
                                            nicid: args.context.nics[0].id
                                        },
                                        success: function (json) {
                                            args.response.success({
                                                _custom: {
                                                    jobId: json.removenicfromvirtualmachineresponse.jobid,
                                                    getUpdatedItem: function (json) {
                                                        return json.queryasyncjobresultresponse.jobresult.virtualmachine;
                                                    }
                                                }
                                            })
                                        }
                                    });
                                },
                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            }
                        },
                        fields: [{
                            id: {
                                label: 'label.id'
                            },
                            name: {
                                label: 'label.name',
                                header: true
                            },
                            networkname: {
                                label: 'label.network.name'
                            },
                            type: {
                                label: 'label.type'
                            },
                            ipaddress: {
                                label: 'label.ip.address'
                            },
                            macaddress: {
                                label: 'label.macaddress'
                            },
                            secondaryips: {
                                label: 'label.secondary.ips'
                            },
                            gateway: {
                                label: 'label.gateway'
                            },
                            netmask: {
                                label: 'label.netmask'
                            },
                            broadcasturi: {
                                label: 'label.broadcast.uri'
                            },
                            isolationuri: {
                                label: 'label.isolation.uri'
                            },
                            ip6address: {
                                label: 'label.ipv6.address'
                            },
                            ip6gateway: {
                                label: 'label.ipv6.gateway'
                            },
                            ip6cidr: {
                                label: 'label.ipv6.CIDR'
                            },

                            isdefault: {
                                label: 'label.is.default',
                                converter: function (data) {
                                    return data ? _l('label.yes') : _l('label.no');
                                }
                            }
                        }],
                        viewAll: {
                            path: 'network.secondaryNicIps',
                            attachTo: 'secondaryips',
                            label: 'label.edit.secondary.ips',
                            title: function (args) {
                                var title = _l('label.menu.ipaddresses') + ' - ' + args.context.nics[0].name;

                                return title;
                            }
                        },
                        dataProvider: function (args) {
                            $.ajax({
                                url: createURL("listVirtualMachines&details=nics&id=" + args.context.instances[0].id),
                                dataType: "json",
                                async: true,
                                success: function (json) {
                                    // Handling the display of network name for a VM under the NICS tabs
                                    args.response.success({
                                        actionFilter: function (args) {
                                            if (args.context.item.isdefault) {
                                                return ['updateIpaddr'];
                                            } else {
                                                return ['remove', 'makeDefault', 'updateIpaddr'];
                                            }
                                        },
                                        data: $.map(json.listvirtualmachinesresponse.virtualmachine[0].nic, function (nic, index) {
                                            if (nic.secondaryip != null) {
                                                var secondaryips = "";
                                                for (var i = 0; i < nic.secondaryip.length; i++) {
                                                    if (i == 0)
                                                        secondaryips = nic.secondaryip[i].ipaddress;
                                                    else
                                                        secondaryips = secondaryips + " , " + nic.secondaryip[i].ipaddress;
                                                }
                                                $.extend(nic, {
                                                    secondaryips: secondaryips
                                                })
                                            }

                                            var name = 'NIC ' + (index + 1);
                                            if (nic.isdefault) {
                                                name += ' (' + _l('label.default') + ')';
                                            }
                                            return $.extend(nic, {
                                                name: name
                                            });
                                        })
                                    });
                                }
                            });
                        }
                    },

                    /**
                     * Statistics tab
                     */
                    stats: {
                        title: 'label.statistics',
                        fields: {
                            totalCPU: {
                                label: 'label.total.cpu'
                            },
                            cpuused: {
                                label: 'label.cpu.utilized'
                            },
                            networkkbsread: {
                                label: 'label.network.read'
                            },
                            networkkbswrite: {
                                label: 'label.network.write'
                            },
                            diskkbsread: {
                                label: 'label.disk.read.bytes'
                            },
                            diskkbswrite: {
                                label: 'label.disk.write.bytes'
                            },
                            diskioread: {
                                label: 'label.disk.read.io'
                            },
                            diskiowrite: {
                                label: 'label.disk.write.io'
                            }
                        },
                        dataProvider: function (args) {
                            $.ajax({
                                url: createURL("listVirtualMachines&details=stats&id=" + args.context.instances[0].id),
                                dataType: "json",
                                async: true,
                                success: function (json) {
                                    var jsonObj = json.listvirtualmachinesresponse.virtualmachine[0];
                                    args.response.success({
                                        data: {
                                            totalCPU: jsonObj.cpunumber,
                                            cpuused: jsonObj.cpuused,
                                            networkkbsread: (jsonObj.networkkbsread == null) ? "N/A" : cloudStack.converters.convertBytes(jsonObj.networkkbsread * 1024),
                                            networkkbswrite: (jsonObj.networkkbswrite == null) ? "N/A" : cloudStack.converters.convertBytes(jsonObj.networkkbswrite * 1024),
                                            diskkbsread: (jsonObj.diskkbsread == null) ? "N/A" : ((jsonObj.hypervisor == "KVM") ? cloudStack.converters.convertBytes(jsonObj.diskkbsread * 1024) : ((jsonObj.hypervisor == "XenServer") ? cloudStack.converters.convertBytes(jsonObj.diskkbsread * 1024) + "/s" : "N/A")),
                                            diskkbswrite: (jsonObj.diskkbswrite == null) ? "N/A" : ((jsonObj.hypervisor == "KVM") ? cloudStack.converters.convertBytes(jsonObj.diskkbswrite * 1024) : ((jsonObj.hypervisor == "XenServer") ? cloudStack.converters.convertBytes(jsonObj.diskkbswrite * 1024) + "/s" : "N/A")),
                                            diskioread: (jsonObj.diskioread == null) ? "N/A" : ((jsonObj.hypervisor == "KVM") ? jsonObj.diskioread : "N/A"),
                                            diskiowrite: (jsonObj.diskiowrite == null) ? "N/A" : ((jsonObj.hypervisor == "KVM") ? jsonObj.diskiowrite : "N/A")
                                        }
                                    });
                                }
                            });
                        }
                    }
                }
            }
        }
    };

    var vmActionfilter = cloudStack.actionFilter.vmActionFilter = function (args) {
        var jsonObj = args.context.item;
        var allowedActions = [];

        if (jsonObj.state == 'Destroyed') {
            if (g_allowUserExpungeRecoverVm) {
                allowedActions.push("recover");
            }

            if (g_allowUserExpungeRecoverVm) {
                allowedActions.push("expunge");
            }
        } else if (jsonObj.state == 'Running') {
            allowedActions.push("stop");
            allowedActions.push("restart");
            allowedActions.push("snapshot");
            allowedActions.push("destroy");
            allowedActions.push("reinstall");

            // when userVm is running, scaleUp is not supported for KVM
            if (jsonObj.hypervisor != 'KVM') {
                allowedActions.push("scaleUp");
            }

            if (isAdmin()) {
                allowedActions.push("migrate");
            }

            if (jsonObj.isoid == null) {
                allowedActions.push("attachISO");
            } else {
                allowedActions.push("detachISO");
            }

            allowedActions.push("resetPassword");

            allowedActions.push("changeAffinity");

            allowedActions.push("viewConsole");
        } else if (jsonObj.state == 'Paused') {
            allowedActions.push("stop");

        } else if (jsonObj.state == 'Stopped') {
            allowedActions.push("edit");

            if (isAdmin()) {
                allowedActions.push("startByAdmin");
            } else {
                allowedActions.push("start");
            }

            allowedActions.push("destroy");
            allowedActions.push("reinstall");
            allowedActions.push("snapshot");

            // when vm is stopped, scaleUp is supported for all hypervisors
            allowedActions.push("scaleUp");
            allowedActions.push("changeAffinity");

            if (isAdmin())
                allowedActions.push("migrateToAnotherStorage");

            if (jsonObj.isoid == null) {
                allowedActions.push("attachISO");
            } else {
                allowedActions.push("detachISO");
            }
            allowedActions.push("resetPassword");

            if (isAdmin() || isDomainAdmin()) {
                allowedActions.push("assignVmToAnotherAccount");
            }
            allowedActions.push("resetSSHKeyForVirtualMachine");
        } else if (jsonObj.state == 'Starting') {
            //  allowedActions.push("stop");
        } else if (jsonObj.state == 'Error') {
            allowedActions.push("destroy");
        } else if (jsonObj.state == 'Expunging') {
            if (g_allowUserExpungeRecoverVm) {
                allowedActions.push("expunge");
            }
        }
        return allowedActions;
    }

})(jQuery, cloudStack);
