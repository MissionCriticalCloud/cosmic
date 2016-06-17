// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

(function ($, cloudStack) {

    var zoneObjs, podObjs, clusterObjs, domainObjs, networkOfferingObjs, physicalNetworkObjs;
    var selectedClusterObj, selectedZoneObj, selectedPublicNetworkObj, selectedManagementNetworkObj, selectedPhysicalNetworkObj, selectedGuestNetworkObj;
    var nspMap = {};
    //from listNetworkServiceProviders API
    var nspHardcodingArray = []; //for service providers listView (hardcoding, not from listNetworkServiceProviders API)

    // Add router type to virtual router
    // -- can be either Project, VPC, or System (standard)
    var mapRouterType = function (index, router) {
        var routerType = _l('label.menu.system');

        if (router.projectid) routerType = _l('label.project');
        if (router.vpcid) routerType = _l('label.vpc');

        return $.extend(router, {
            routerType: routerType
        });
    };

    cloudStack.publicIpRangeAccount = {
        dialog: function (args) {
            return function (args) {
                var data = args.data ? args.data : {};
                var fields = {
                    account: {
                        label: 'label.account',
                        defaultValue: data.account
                    },
                    domainid: {
                        label: 'label.domain',
                        defaultValue: data.domainid,
                        select: function (args) {
                            $.ajax({
                                url: createURL('listDomains'),
                                data: {
                                    listAll: true
                                },
                                success: function (json) {
                                    args.response.success({
                                        data: $.map(json.listdomainsresponse.domain, function (domain) {
                                            return {
                                                id: domain.id,
                                                description: domain.path
                                            };
                                        })
                                    });
                                }
                            });
                        }
                    }
                };
                var success = args.response.success;

                if (args.$item) {
                    // Account data is read-only after creation
                    $.ajax({
                        url: createURL('listDomains'),
                        data: {
                            id: data.domainid,
                            listAll: true
                        },
                        success: function (json) {
                            var domain = json.listdomainsresponse.domain[0];

                            if (data.account != null)
                                cloudStack.dialog.notice({
                                    message: '<ul><li>' + _l('label.account') + ': ' + data.account + '</li>' + '<li>' + _l('label.domain') + ': ' + domain.path + '</li></ul>'
                                });
                            else
                                cloudStack.dialog.notice({
                                    message: '<ul><li>' + _l('label.domain') + ': ' + domain.path + '</li></ul>'
                                });
                        }
                    });
                } else {
                    cloudStack.dialog.createForm({
                        form: {
                            title: 'label.add.account',
                            desc: '(optional) Please specify an account to be associated with this IP range.',
                            fields: fields
                        },
                        after: function (args) {
                            var data = cloudStack.serializeForm(args.$form);

                            success({
                                data: data
                            });
                        }
                    });
                }
            };
        }
    };

    var getTrafficType = function (physicalNetwork, typeID) {
        var trafficType = {};

        $.ajax({
            url: createURL('listTrafficTypes'),
            data: {
                physicalnetworkid: physicalNetwork.id
            },
            async: false,
            success: function (json) {
                trafficType = $.grep(
                    json.listtraffictypesresponse.traffictype,
                    function (trafficType) {
                        return trafficType.traffictype == typeID;
                    })[0];
            }
        });

        if (trafficType.xennetworklabel == null || trafficType.xennetworklabel == 0)
            trafficType.xennetworklabel = _l('label.network.label.display.for.blank.value');
        if (trafficType.kvmnetworklabel == null || trafficType.kvmnetworklabel == 0)
            trafficType.kvmnetworklabel = _l('label.network.label.display.for.blank.value');
        if (trafficType.ovm3networklabel == null || trafficType.ovm3networklabel == 0)
            trafficType.ovm3networklabel = _l('label.network.label.display.for.blank.value');

        return trafficType;
    };

    var updateTrafficLabels = function (trafficType, labels, complete) {
        var array1 = [];
        if (labels.xennetworklabel != _l('label.network.label.display.for.blank.value'))
            array1.push("&xennetworklabel=" + labels.xennetworklabel);
        if (labels.kvmnetworklabel != _l('label.network.label.display.for.blank.value'))
            array1.push("&kvmnetworklabel=" + labels.kvmnetworklabel);
        if (labels.ovm3networklabel != _l('label.network.label.display.for.blank.value'))
            array1.push("&ovm3networklabel=" + labels.ovm3networklabel);

        $.ajax({
            url: createURL('updateTrafficType' + array1.join("")),
            data: {
                id: trafficType.id
            },
            success: function (json) {
                var jobID = json.updatetraffictyperesponse.jobid;

                cloudStack.ui.notifications.add({
                        desc: 'Update traffic labels',
                        poll: pollAsyncJobResult,
                        section: 'System',
                        _custom: {
                            jobId: jobID
                        }
                    },
                    complete ? complete : function () {
                    },
                    {},
                    function (data) {
                        // Error
                        cloudStack.dialog.notice({
                            message: parseXMLHttpResponse(data)
                        });
                    },
                    {});
            }
        })
    };

    function virtualRouterProviderActionFilter(args) {
        var allowedActions = [];
        var jsonObj = args.context.item; //args.context.item == nspMap["virtualRouter"]
        if (jsonObj.state == "Enabled")
            allowedActions.push("disable"); else if (jsonObj.state == "Disabled")
            allowedActions.push("enable");
        return allowedActions;
    };

    function ovsProviderActionFilter(args) {
        var allowedActions = [];
        var jsonObj = args.context.item; //args.context.item == nspMap["virtualRouter"]
        if (jsonObj.state == "Enabled")
            allowedActions.push("disable");
        else if (jsonObj.state == "Disabled")
            allowedActions.push("enable");
        return allowedActions;
    };

    cloudStack.sections.system = {
        title: 'label.menu.infrastructure',
        id: 'system',

        // System dashboard
        dashboard: {
            dataProvider: function (args) {
                var dataFns = {
                    zoneCount: function (data) {
                        $.ajax({
                            url: createURL('listZones'),
                            data: {
                                listAll: true,
                                page: 1,
                                pagesize: 1 //specifying pagesize as 1 because we don't need any embedded objects to be returned here. The only thing we need from API response is "count" property.
                            },
                            success: function (json) {
                                args.response.success({
                                    data: {
                                        zoneCount: json.listzonesresponse.count ? json.listzonesresponse.count : 0,
                                        zones: json.listzonesresponse.zone
                                    }
                                });
                            }
                        });
                        dataFns.podCount();
                    },

                    podCount: function (data) {
                        $.ajax({
                            url: createURL('listPods'),
                            data: {
                                listAll: true,
                                page: 1,
                                pagesize: 1 //specifying pagesize as 1 because we don't need any embedded objects to be returned here. The only thing we need from API response is "count" property.
                            },
                            success: function (json) {
                                args.response.success({
                                    data: {
                                        podCount: json.listpodsresponse.count ? json.listpodsresponse.count : 0
                                    }
                                });
                            }
                        });
                        dataFns.clusterCount();
                    },

                    clusterCount: function (data) {
                        $.ajax({
                            url: createURL('listClusters'),
                            data: {
                                listAll: true,
                                page: 1,
                                pagesize: 1 //specifying pagesize as 1 because we don't need any embedded objects to be returned here. The only thing we need from API response is "count" property.
                            },
                            success: function (json) {
                                args.response.success({
                                    data: {
                                        clusterCount: json.listclustersresponse.count ? json.listclustersresponse.count : 0
                                    }
                                });
                            }
                        });
                        dataFns.hostCount();
                    },

                    hostCount: function (data) {
                        var data2 = {
                            type: 'routing',
                            listAll: true,
                            page: 1,
                            pagesize: 1 //specifying pagesize as 1 because we don't need any embedded objects to be returned here. The only thing we need from API response is "count" property.
                        };
                        $.ajax({
                            url: createURL('listHosts'),
                            data: data2,
                            success: function (json) {
                                args.response.success({
                                    data: {
                                        hostCount: json.listhostsresponse.count ? json.listhostsresponse.count : 0
                                    }
                                });
                            }
                        });
                        dataFns.primaryStorageCount();
                    },

                    primaryStorageCount: function (data) {
                        var data2 = {
                            listAll: true,
                            page: 1,
                            pagesize: 1 //specifying pagesize as 1 because we don't need any embedded objects to be returned here. The only thing we need from API response is "count" property.
                        };
                        $.ajax({
                            url: createURL('listStoragePools'),
                            data: data2,
                            success: function (json) {
                                args.response.success({
                                    data: {
                                        primaryStorageCount: json.liststoragepoolsresponse.count ? json.liststoragepoolsresponse.count : 0
                                    }
                                });
                            }
                        });
                        dataFns.secondaryStorageCount();
                    },

                    secondaryStorageCount: function (data) {
                        var data2 = {
                            type: 'SecondaryStorage',
                            listAll: true,
                            page: 1,
                            pagesize: 1 //specifying pagesize as 1 because we don't need any embedded objects to be returned here. The only thing we need from API response is "count" property.
                        };
                        $.ajax({
                            url: createURL('listImageStores'),
                            data: data2,
                            success: function (json) {
                                args.response.success({
                                    data: {
                                        secondaryStorageCount: json.listimagestoresresponse.imagestore ? json.listimagestoresresponse.count : 0
                                    }
                                });
                            }
                        });
                        dataFns.systemVmCount();
                    },

                    systemVmCount: function (data) {
                        $.ajax({
                            url: createURL('listSystemVms'),
                            data: {
                                listAll: true,
                                page: 1,
                                pagesize: 1 //specifying pagesize as 1 because we don't need any embedded objects to be returned here. The only thing we need from API response is "count" property.
                            },
                            success: function (json) {
                                args.response.success({
                                    data: {
                                        systemVmCount: json.listsystemvmsresponse.count ? json.listsystemvmsresponse.count : 0
                                    }
                                });
                            }
                        });
                        dataFns.virtualRouterCount();
                    },

                    virtualRouterCount: function (data) {
                        var data2 = {
                            listAll: true,
                            page: 1,
                            pagesize: 1 //specifying pagesize as 1 because we don't need any embedded objects to be returned here. The only thing we need from API response is "count" property.
                        };
                        $.ajax({
                            url: createURL('listRouters'),
                            data: data2,
                            success: function (json) {
                                var total1 = json.listroutersresponse.count ? json.listroutersresponse.count : 0;
                                var total2 = 0; //reset

                                /*
                                 * In project view, the first listRotuers API(without projectid=-1) will return the same objects as the second listRouters API(with projectid=-1),
                                 * because in project view, all API calls are appended with projectid=[projectID].
                                 * Therefore, we only call the second listRouters API(with projectid=-1) in non-project view.
                                 */
                                if (cloudStack.context && cloudStack.context.projects == null) { //non-project view
                                    var data3 = {
                                        listAll: true,
                                        projectid: -1,
                                        page: 1,
                                        pagesize: 1 //specifying pagesize as 1 because we don't need any embedded objects to be returned here. The only thing we need from API response is "count" property.
                                    };
                                    $.ajax({
                                        url: createURL('listRouters'),
                                        data: data3,
                                        async: false,
                                        success: function (json) {
                                            total2 = json.listroutersresponse.count ? json.listroutersresponse.count : 0;
                                        }
                                    });
                                }

                                args.response.success({
                                    data: {
                                        virtualRouterCount: (total1 + total2)
                                    }
                                });
                            }
                        });
                        dataFns.capacity();
                    },

                    capacity: function (data) {
                        $.ajax({
                            url: createURL('listCapacity'),
                            success: function (json) {
                                var capacities = json.listcapacityresponse.capacity;
                                if (capacities) {
                                    var capacityTotal = function (id, converter) {
                                        var capacity = $.grep(capacities, function (capacity) {
                                            return capacity.type == id;
                                        })[0];

                                        var total = capacity ? capacity.capacitytotal : 0;

                                        if (converter) {
                                            return converter(total);
                                        }

                                        return total;
                                    };

                                    args.response.success({
                                        data: {
                                            cpuCapacityTotal: capacityTotal(1, cloudStack.converters.convertHz),
                                            memCapacityTotal: capacityTotal(0, cloudStack.converters.convertBytes),
                                            storageCapacityTotal: capacityTotal(2, cloudStack.converters.convertBytes)
                                        }
                                    });

                                } else {

                                    args.response.success({
                                        data: {
                                            cpuCapacityTotal: cloudStack.converters.convertHz(0),
                                            memCapacityTotal: cloudStack.converters.convertBytes(0),
                                            storageCapacityTotal: cloudStack.converters.convertBytes(0)
                                        }
                                    });

                                }
                            }
                        });

                        dataFns.socketInfo();
                    },

                    socketInfo: function (data) {
                        var socketCount = 0;

                        function listHostFunction(hypervisor, pageSizeValue) {
                            var deferred = $.Deferred();
                            var totalHostCount = 0;
                            var returnedHostCount = 0;
                            var returnedHostCpusocketsSum = 0;

                            var callListHostsWithPage = function (page) {
                                $.ajax({
                                    url: createURL('listHosts'),
                                    data: {
                                        type: 'routing',
                                        hypervisor: hypervisor,
                                        page: page,
                                        details: 'min',
                                        pagesize: pageSizeValue
                                    },
                                    success: function (json) {
                                        if (json.listhostsresponse.count == undefined) {
                                            deferred.resolve();
                                            return;
                                        }

                                        totalHostCount = json.listhostsresponse.count;
                                        returnedHostCount += json.listhostsresponse.host.length;

                                        var items = json.listhostsresponse.host;
                                        for (var i = 0; i < items.length; i++) {
                                            if (items[i].cpusockets != undefined && isNaN(items[i].cpusockets) == false) {
                                                returnedHostCpusocketsSum += items[i].cpusockets;
                                            }
                                        }

                                        if (returnedHostCount < totalHostCount) {
                                            callListHostsWithPage(++page);
                                        } else {
                                            socketCount += returnedHostCpusocketsSum;
                                            deferred.resolve();
                                        }
                                    }
                                });
                            }

                            callListHostsWithPage(1);

                            return deferred;

                        }

                        $.ajax({
                            url: createURL('listConfigurations'),
                            data: {
                                name: 'default.page.size'
                            },
                            success: function (json) {
                                pageSizeValue = json.listconfigurationsresponse.configuration[0].value;
                                if (!pageSizeValue) {
                                    return;
                                }
                                $.ajax({
                                    url: createURL('listHypervisors'),
                                    success: function (json) {
                                        var deferredArray = [];

                                        $(json.listhypervisorsresponse.hypervisor).map(function (index, hypervisor) {
                                            deferredArray.push(listHostFunction(hypervisor.name, pageSizeValue));
                                        });

                                        $.when.apply(null, deferredArray).then(function () {
                                            args.response.success({
                                                data: {
                                                    socketCount: socketCount
                                                }
                                            });
                                        });
                                    }
                                });
                            }
                        });

                    }
                };

                dataFns.zoneCount();
            }
        },

        zoneDashboard: function (args) {
            $.ajax({
                url: createURL('listCapacity'),
                data: {
                    zoneid: args.context.zones[0].id
                },
                success: function (json) {
                    var capacities = json.listcapacityresponse.capacity;
                    var data = {};

                    $(capacities).each(function () {
                        var capacity = this;

                        data[capacity.type] = {
                            used: cloudStack.converters.convertByType(capacity.type, capacity.capacityused),
                            total: cloudStack.converters.convertByType(capacity.type, capacity.capacitytotal),
                            percent: parseInt(capacity.percentused)
                        };
                    });

                    args.response.success({
                        data: data
                    });
                }
            });
        },

        // Network-as-a-service configuration
        naas: {
            providerListView: {
                id: 'networkProviders',
                fields: {
                    name: {
                        label: 'label.name'
                    },
                    state: {
                        label: 'label.state',
                        converter: function (str) {
                            // For localization
                            return str;
                        },
                        indicator: {
                            'Enabled': 'on',
                            'Disabled': 'off'
                        }
                    }
                },
                disableInfiniteScrolling: true,
                dataProvider: function (args) {
                    refreshNspData();
                    args.response.success({
                        data: nspHardcodingArray
                    })
                },

                detailView: function (args) {
                    return cloudStack.sections.system.naas.networkProviders.types[
                        args.context.networkProviders[0].id];
                }
            },
            mainNetworks: {
                'public': {
                    detailView: {
                        actions: {
                            edit: {
                                label: 'label.edit',
                                action: function (args) {
                                    var trafficType = getTrafficType(selectedPhysicalNetworkObj, 'Public');

                                    updateTrafficLabels(trafficType, args.data, function () {
                                        args.response.success();
                                    });
                                }
                            }
                        },
                        tabs: {
                            details: {
                                title: 'label.details',
                                fields: [{
                                    traffictype: {
                                        label: 'label.traffic.type'
                                    },
                                    broadcastdomaintype: {
                                        label: 'label.broadcast.domain.type'
                                    }
                                },
                                    {
                                        xennetworklabel: {
                                            label: 'label.xenserver.traffic.label',
                                            isEditable: true
                                        },
                                        kvmnetworklabel: {
                                            label: 'label.kvm.traffic.label',
                                            isEditable: true
                                        },
                                        ovm3networklabel: {
                                            label: 'label.ovm3.traffic.label',
                                            isEditable: true
                                        }
                                    }],

                                dataProvider: function (args) {
                                    $.ajax({
                                        url: createURL("listNetworks&listAll=true&trafficType=Public&isSystem=true&zoneId=" + selectedZoneObj.id, {
                                            ignoreProject: true
                                        }),
                                        dataType: "json",
                                        async: false,
                                        success: function (json) {
                                            var trafficType = getTrafficType(selectedPhysicalNetworkObj, 'Public');
                                            var items = json.listnetworksresponse.network;

                                            selectedPublicNetworkObj = items[0];

                                            // Include traffic labels
                                            selectedPublicNetworkObj.xennetworklabel = trafficType.xennetworklabel;
                                            selectedPublicNetworkObj.kvmnetworklabel = trafficType.kvmnetworklabel;
                                            selectedPublicNetworkObj.ovm3networklabel = trafficType.ovm3networklabel;
                                            args.response.success({
                                                data: selectedPublicNetworkObj
                                            });
                                        }
                                    });
                                }
                            },

                            ipAddresses: {
                                title: 'label.ip.ranges',
                                custom: function (args) {
                                    return $('<div></div>').multiEdit({
                                        context: args.context,
                                        noSelect: true,
                                        fields: {
                                            'gateway': {
                                                edit: true,
                                                label: 'label.gateway'
                                            },
                                            'netmask': {
                                                edit: true,
                                                label: 'label.netmask'
                                            },
                                            'vlan': {
                                                edit: true,
                                                label: 'label.vlan',
                                                isOptional: true
                                            },
                                            'startip': {
                                                edit: true,
                                                label: 'label.start.IP'
                                            },
                                            'endip': {
                                                edit: true,
                                                label: 'label.end.IP'
                                            },
                                            'account': {
                                                label: 'label.account',
                                                custom: {
                                                    buttonLabel: 'label.add.account',
                                                    action: cloudStack.publicIpRangeAccount.dialog()
                                                }
                                            },
                                            'add-rule': {
                                                label: 'label.add',
                                                addButton: true
                                            }
                                        },
                                        add: {
                                            label: 'label.add',
                                            action: function (args) {
                                                var array1 = [];
                                                array1.push("&zoneId=" + args.context.zones[0].id);

                                                if (args.data.vlan != null && args.data.vlan.length > 0)
                                                    array1.push("&vlan=" + todb(args.data.vlan)); else
                                                    array1.push("&vlan=untagged");

                                                array1.push("&gateway=" + args.data.gateway);
                                                array1.push("&netmask=" + args.data.netmask);
                                                array1.push("&startip=" + args.data.startip);
                                                if (args.data.endip != null && args.data.endip.length > 0)
                                                    array1.push("&endip=" + args.data.endip);

                                                if (args.data.account) {
                                                    if (args.data.account.account)
                                                        array1.push("&account=" + args.data.account.account);
                                                    array1.push("&domainid=" + args.data.account.domainid);
                                                }

                                                array1.push("&forVirtualNetwork=true");
                                                //indicates this new IP range is for public network, not guest network

                                                $.ajax({
                                                    url: createURL("createVlanIpRange" + array1.join("")),
                                                    dataType: "json",
                                                    success: function (json) {
                                                        var item = json.createvlaniprangeresponse.vlan;
                                                        args.response.success({
                                                            data: item,
                                                            notification: {
                                                                label: 'label.add.ip.range',
                                                                poll: function (args) {
                                                                    args.complete();
                                                                }
                                                            }
                                                        });
                                                    },
                                                    error: function (XMLHttpResponse) {
                                                        var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
                                                        args.response.error(errorMsg);
                                                    }
                                                });
                                            }
                                        },
                                        actionPreFilter: function (args) {
                                            var actionsToShow = ['destroy'];
                                            if (args.context.multiRule[0].domain == 'ROOT' && args.context.multiRule[0].account != null && args.context.multiRule[0].account.account == 'system')
                                                actionsToShow.push('addAccount'); else
                                                actionsToShow.push('releaseFromAccount');
                                            return actionsToShow;
                                        },
                                        actions: {
                                            destroy: {
                                                label: 'label.remove.ip.range',
                                                action: function (args) {
                                                    $.ajax({
                                                        url: createURL('deleteVlanIpRange&id=' + args.context.multiRule[0].id),
                                                        dataType: 'json',
                                                        async: true,
                                                        success: function (json) {
                                                            args.response.success({
                                                                notification: {
                                                                    label: 'label.remove.ip.range',
                                                                    poll: function (args) {
                                                                        args.complete();
                                                                    }
                                                                }
                                                            });
                                                        },
                                                        error: function (json) {
                                                            args.response.error(parseXMLHttpResponse(json));
                                                        }
                                                    });
                                                }
                                            },

                                            releaseFromAccount: {
                                                label: 'label.release.account',
                                                action: function (args) {
                                                    $.ajax({
                                                        url: createURL('releasePublicIpRange'),
                                                        data: {
                                                            id: args.context.multiRule[0].id
                                                        },
                                                        success: function (json) {
                                                            args.response.success({
                                                                notification: {
                                                                    label: 'label.release.account.lowercase',
                                                                    poll: function (args) {
                                                                        args.complete();
                                                                    }
                                                                }
                                                            });
                                                        },
                                                        error: function (json) {
                                                            args.response.error(parseXMLHttpResponse(json));
                                                        }
                                                    });
                                                }
                                            },

                                            addAccount: {
                                                label: 'label.add.account',
                                                createForm: {
                                                    title: 'label.add.account',
                                                    fields: {
                                                        account: {
                                                            label: 'label.account'
                                                        },
                                                        domainid: {
                                                            label: 'label.domain',
                                                            select: function (args) {
                                                                $.ajax({
                                                                    url: createURL('listDomains'),
                                                                    data: {
                                                                        listAll: true
                                                                    },
                                                                    success: function (json) {
                                                                        args.response.success({
                                                                            data: $.map(json.listdomainsresponse.domain, function (domain) {
                                                                                return {
                                                                                    id: domain.id,
                                                                                    description: domain.path
                                                                                };
                                                                            })
                                                                        });
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    }
                                                },
                                                action: function (args) {
                                                    var data = {
                                                        id: args.context.multiRule[0].id,
                                                        zoneid: args.context.multiRule[0].zoneid,
                                                        domainid: args.data.domainid
                                                    };
                                                    if (args.data.account) {
                                                        $.extend(data, {
                                                            account: args.data.account
                                                        });
                                                    }
                                                    $.ajax({
                                                        url: createURL('dedicatePublicIpRange'),
                                                        data: data,
                                                        success: function (json) {
                                                            args.response.success({
                                                                notification: {
                                                                    label: 'label.add.account',
                                                                    poll: function (args) {
                                                                        args.complete();
                                                                    }
                                                                }
                                                            });
                                                        },
                                                        error: function (json) {
                                                            args.response.error(parseXMLHttpResponse(json));
                                                        }
                                                    });
                                                }
                                            }
                                        },
                                        dataProvider: function (args) {
                                            $.ajax({
                                                url: createURL("listVlanIpRanges&zoneid=" + args.context.zones[0].id + "&networkId=" + selectedPublicNetworkObj.id),
                                                dataType: "json",
                                                success: function (json) {
                                                    var items = json.listvlaniprangesresponse.vlaniprange;

                                                    args.response.success({
                                                        data: $.map(items, function (item) {
                                                            return $.extend(item, {
                                                                account: {
                                                                    _buttonLabel: item.account ? '[' + item.domain + '] ' + item.account : item.domain,
                                                                    account: item.account,
                                                                    domainid: item.domainid
                                                                }
                                                            });
                                                        })
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        }
                    }
                },

                'storage': {
                    detailView: {
                        actions: {
                            edit: {
                                label: 'label.edit',
                                action: function (args) {
                                    var trafficType = getTrafficType(selectedPhysicalNetworkObj, 'Storage');

                                    updateTrafficLabels(trafficType, args.data, function () {
                                        args.response.success();
                                    });
                                }
                            }
                        },
                        tabs: {
                            details: {
                                title: 'label.details',
                                fields: [{
                                    traffictype: {
                                        label: 'label.traffic.type'
                                    },
                                    broadcastdomaintype: {
                                        label: 'label.broadcast.domain.type'
                                    }
                                },
                                    {
                                        xennetworklabel: {
                                            label: 'label.xenserver.traffic.label',
                                            isEditable: true
                                        },
                                        kvmnetworklabel: {
                                            label: 'label.kvm.traffic.label',
                                            isEditable: true
                                        },
                                        ovm3networklabel: {
                                            label: 'label.ovm3.traffic.label',
                                            isEditable: true
                                        }
                                    }],

                                dataProvider: function (args) {
                                    $.ajax({
                                        url: createURL("listNetworks&listAll=true&trafficType=Storage&isSystem=true&zoneId=" + selectedZoneObj.id),
                                        dataType: "json",
                                        async: false,
                                        success: function (json) {
                                            var items = json.listnetworksresponse.network;
                                            var trafficType = getTrafficType(selectedPhysicalNetworkObj, 'Storage');
                                            selectedPublicNetworkObj = items[0];

                                            selectedPublicNetworkObj.xennetworklabel = trafficType.xennetworklabel;
                                            selectedPublicNetworkObj.kvmnetworklabel = trafficType.kvmnetworklabel;
                                            selectedPublicNetworkObj.ovm3networklabel = trafficType.ovm3networklabel;
                                            args.response.success({
                                                data: selectedPublicNetworkObj
                                            });
                                        }
                                    });
                                }
                            },

                            ipAddresses: {
                                title: 'label.ip.ranges',
                                custom: function (args) {
                                    return $('<div></div>').multiEdit({
                                        context: args.context,
                                        noSelect: true,
                                        fields: {
                                            'podid': {
                                                label: 'label.pod',
                                                select: function (args) {
                                                    $.ajax({
                                                        url: createURL("listPods&zoneid=" + selectedZoneObj.id),
                                                        dataType: "json",
                                                        success: function (json) {
                                                            var items = [];
                                                            var pods = json.listpodsresponse.pod;
                                                            $(pods).each(function () {
                                                                items.push({
                                                                    name: this.id,
                                                                    description: this.name
                                                                });
                                                                //should be "{id: this.id, description: this.name}" (to be consistent with dropdown in createFrom and edit mode) (Brian will fix widget later)
                                                            });
                                                            args.response.success({
                                                                data: items
                                                            });
                                                        }
                                                    });
                                                }
                                            },
                                            'gateway': {
                                                edit: true,
                                                label: 'label.gateway'
                                            },
                                            'netmask': {
                                                edit: true,
                                                label: 'label.netmask'
                                            },
                                            'vlan': {
                                                edit: true,
                                                label: 'label.vlan',
                                                isOptional: true
                                            },
                                            'startip': {
                                                edit: true,
                                                label: 'label.start.IP'
                                            },
                                            'endip': {
                                                edit: true,
                                                label: 'label.end.IP'
                                            },
                                            'add-rule': {
                                                label: 'label.add',
                                                addButton: true
                                            }
                                        },
                                        add: {
                                            label: 'label.add',
                                            action: function (args) {
                                                var array1 = [];
                                                array1.push("&zoneId=" + args.context.zones[0].id);
                                                array1.push("&podid=" + args.data.podid);

                                                array1.push("&gateway=" + args.data.gateway);

                                                if (args.data.vlan != null && args.data.vlan.length > 0)
                                                    array1.push("&vlan=" + todb(args.data.vlan));

                                                array1.push("&netmask=" + args.data.netmask);
                                                array1.push("&startip=" + args.data.startip);
                                                if (args.data.endip != null && args.data.endip.length > 0)
                                                    array1.push("&endip=" + args.data.endip);

                                                $.ajax({
                                                    url: createURL("createStorageNetworkIpRange" + array1.join("")),
                                                    dataType: "json",
                                                    success: function (json) {
                                                        args.response.success({
                                                            _custom: {
                                                                jobId: json.createstoragenetworkiprangeresponse.jobid
                                                            },
                                                            notification: {
                                                                label: 'label.add.ip.range',
                                                                poll: pollAsyncJobResult
                                                            }
                                                        });
                                                    },
                                                    error: function (XMLHttpResponse) {
                                                        var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
                                                        args.response.error(errorMsg);
                                                    }
                                                });
                                            }
                                        },
                                        actions: {
                                            destroy: {
                                                label: 'label.delete',
                                                action: function (args) {
                                                    $.ajax({
                                                        url: createURL('deleteStorageNetworkIpRange&id=' + args.context.multiRule[0].id),
                                                        dataType: 'json',
                                                        async: true,
                                                        success: function (json) {
                                                            args.response.success({
                                                                notification: {
                                                                    label: 'label.remove.ip.range',
                                                                    poll: function (args) {
                                                                        args.complete();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            }
                                        },
                                        dataProvider: function (args) {
                                            $.ajax({
                                                url: createURL("listStorageNetworkIpRange&zoneid=" + args.context.zones[0].id),
                                                dataType: "json",
                                                success: function (json) {
                                                    var items = json.liststoragenetworkiprangeresponse.storagenetworkiprange;
                                                    args.response.success({
                                                        data: items
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        }
                    }
                },

                'management': {
                    detailView: {
                        actions: {
                            edit: {
                                label: 'label.edit',
                                action: function (args) {
                                    var trafficType = getTrafficType(selectedPhysicalNetworkObj, 'Management');

                                    updateTrafficLabels(trafficType, args.data, function () {
                                        args.response.success();
                                    });
                                }
                            }
                        },
                        tabs: {
                            details: {
                                title: 'label.details',
                                fields: [{
                                    traffictype: {
                                        label: 'label.traffic.type'
                                    },
                                    broadcastdomaintype: {
                                        label: 'label.broadcast.domain.type'
                                    }
                                },
                                    {
                                        xennetworklabel: {
                                            label: 'label.xenserver.traffic.label',
                                            isEditable: true
                                        },
                                        kvmnetworklabel: {
                                            label: 'label.kvm.traffic.label',
                                            isEditable: true
                                        },
                                        ovm3networklabel: {
                                            label: 'label.ovm3.traffic.label',
                                            isEditable: true
                                        }
                                    }],
                                dataProvider: function (args) {
                                    $.ajax({
                                        url: createURL("listNetworks&listAll=true&issystem=true&trafficType=Management&zoneId=" + selectedZoneObj.id),
                                        dataType: "json",
                                        success: function (json) {
                                            selectedManagementNetworkObj = json.listnetworksresponse.network[0];

                                            var trafficType = getTrafficType(selectedPhysicalNetworkObj, 'Management');

                                            selectedManagementNetworkObj.xennetworklabel = trafficType.xennetworklabel;
                                            selectedManagementNetworkObj.kvmnetworklabel = trafficType.kvmnetworklabel;
                                            selectedManagementNetworkObj.ovm3networklabel = trafficType.ovm3networklabel;
                                            args.response.success({
                                                data: selectedManagementNetworkObj
                                            });
                                        }
                                    });
                                }
                            },
                            ipAddresses: {
                                //read-only listView (no actions) filled with pod info (not VlanIpRange info)
                                title: 'label.ip.ranges',
                                listView: {
                                    fields: {
                                        name: {
                                            label: 'label.pod'
                                        },
                                        //pod name
                                        gateway: {
                                            label: 'label.gateway'
                                        },
                                        //'Reserved system gateway' is too long and causes a visual format bug (2 lines overlay)
                                        netmask: {
                                            label: 'label.netmask'
                                        },
                                        //'Reserved system netmask' is too long and causes a visual format bug (2 lines overlay)
                                        startip: {
                                            label: 'label.start.IP'
                                        },
                                        //'Reserved system start IP' is too long and causes a visual format bug (2 lines overlay)
                                        endip: {
                                            label: 'label.end.IP'
                                        }
                                        //'Reserved system end IP' is too long and causes a visual format bug (2 lines overlay)
                                    },
                                    dataProvider: function (args) {
                                        var array1 = [];
                                        if (args.filterBy != null) {
                                            if (args.filterBy.search != null && args.filterBy.search.by != null && args.filterBy.search.value != null) {
                                                switch (args.filterBy.search.by) {
                                                    case "name":
                                                        if (args.filterBy.search.value.length > 0)
                                                            array1.push("&keyword=" + args.filterBy.search.value);
                                                        break;
                                                }
                                            }
                                        }
                                        $.ajax({
                                            url: createURL("listPods&zoneid=" + selectedZoneObj.id + "&page=" + args.page + "&pagesize=" + pageSize + array1.join("")),
                                            dataType: "json",
                                            async: true,
                                            success: function (json) {
                                                var items = json.listpodsresponse.pod;
                                                args.response.success({
                                                    data: items
                                                });
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    }
                },

                'guest': {
                    //physical network + Guest traffic type
                    detailView: {
                        actions: {
                            edit: {
                                label: 'label.edit',
                                action: function (args) {
                                    var data = {
                                        id: selectedPhysicalNetworkObj.id
                                    };

                                    $.extend(data, {
                                        vlan: args.data.vlan
                                    });

                                    $.extend(data, {
                                        tags: args.data.tags
                                    });

                                    $.ajax({
                                        url: createURL('updatePhysicalNetwork'),
                                        data: data,
                                        success: function (json) {
                                            var jobId = json.updatephysicalnetworkresponse.jobid;

                                            var trafficType = getTrafficType(selectedPhysicalNetworkObj, 'Guest');

                                            updateTrafficLabels(trafficType, args.data, function () {
                                                args.response.success({
                                                    _custom: {
                                                        jobId: jobId
                                                    }
                                                });
                                            });
                                        }
                                    });
                                },
                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            }
                        },

                        tabFilter: function (args) {
                            var hiddenTabs = [];
                            if (selectedZoneObj.networktype == 'Basic') {
                                hiddenTabs.push("network");
                                hiddenTabs.push("dedicatedGuestVlanRanges");
                            } else {
                                //selectedZoneObj.networktype == 'Advanced'
                                hiddenTabs.push("ipAddresses");
                            }
                            return hiddenTabs;
                        },

                        tabs: {
                            details: {
                                title: 'label.details',
                                preFilter: function (args) {
                                    var hiddenFields = [];
                                    if (selectedZoneObj.networktype == "Basic") {
                                        hiddenFields.push("vlan");
                                        // hiddenFields.push("endVlan");
                                    }
                                    return hiddenFields;
                                },
                                fields: [{
                                    //updatePhysicalNetwork API
                                    state: {
                                        label: 'label.state'
                                    },
                                    vlan: {
                                        label: 'label.vlan.vni.ranges',
                                        isEditable: true
                                    },
                                    tags: {
                                        label: 'label.tags',
                                        isEditable: true
                                    },
                                    broadcastdomainrange: {
                                        label: 'label.broadcast.domain.range'
                                    }
                                },
                                    {
                                        //updateTrafficType API
                                        xennetworklabel: {
                                            label: 'label.xenserver.traffic.label',
                                            isEditable: true
                                        },
                                        kvmnetworklabel: {
                                            label: 'label.kvm.traffic.label',
                                            isEditable: true
                                        },
                                        ovm3networklabel: {
                                            label: 'label.ovm3.traffic.label',
                                            isEditable: true
                                        }
                                    }],
                                dataProvider: function (args) {
                                    //physical network + Guest traffic type
                                    //refresh physical network
                                    $.ajax({
                                        url: createURL('listPhysicalNetworks'),
                                        data: {
                                            id: args.context.physicalNetworks[0].id
                                        },
                                        async: true,
                                        success: function (json) {
                                            selectedPhysicalNetworkObj = json.listphysicalnetworksresponse.physicalnetwork[0];

                                            //    var startVlan, endVlan;
                                            var vlan = selectedPhysicalNetworkObj.vlan;
                                            /*    if(vlan != null && vlan.length > 0) {
                                             if(vlan.indexOf("-") != -1) {
                                             var vlanArray = vlan.split("-");
                                             startVlan = vlanArray[0];
                                             endVlan = vlanArray[1];
                                             }
                                             else {
                                             startVlan = vlan;
                                             }
                                             selectedPhysicalNetworkObj["startVlan"] = startVlan;
                                             selectedPhysicalNetworkObj["endVlan"] = endVlan;
                                             }*/

                                            //traffic type
                                            var xenservertrafficlabel, kvmtrafficlabel;
                                            var trafficType = getTrafficType(selectedPhysicalNetworkObj, 'Guest');
                                            //refresh Guest traffic type
                                            selectedPhysicalNetworkObj["xennetworklabel"] = trafficType.xennetworklabel;
                                            selectedPhysicalNetworkObj["kvmnetworklabel"] = trafficType.kvmnetworklabel;
                                            selectedPhysicalNetworkObj["ovm3networklabel"] = trafficType.ovm3networklabel;
                                            args.response.success({
                                                actionFilter: function () {
                                                    var allowedActions = ['edit', 'addVlanRange', 'removeVlanRange'];
                                                    return allowedActions;
                                                },
                                                data: selectedPhysicalNetworkObj
                                            });
                                        }
                                    });
                                }
                            },

                            ipAddresses: {
                                title: 'label.ip.ranges',
                                custom: function (args) {
                                    return $('<div></div>').multiEdit({
                                        context: args.context,
                                        noSelect: true,
                                        fields: {
                                            'podid': {
                                                label: 'label.pod',
                                                select: function (args) {
                                                    $.ajax({
                                                        url: createURL("listPods&zoneid=" + selectedZoneObj.id),
                                                        dataType: "json",
                                                        success: function (json) {
                                                            var items = [];
                                                            var pods = json.listpodsresponse.pod;
                                                            $(pods).each(function () {
                                                                items.push({
                                                                    name: this.id,
                                                                    description: this.name
                                                                });
                                                                //should be "{id: this.id, description: this.name}" (to be consistent with dropdown in createFrom and edit mode) (Brian will fix widget later)
                                                            });
                                                            args.response.success({
                                                                data: items
                                                            });
                                                        }
                                                    });
                                                }
                                            },
                                            'gateway': {
                                                edit: true,
                                                label: 'label.gateway'
                                            },
                                            'netmask': {
                                                edit: true,
                                                label: 'label.netmask'
                                            },
                                            'startip': {
                                                edit: true,
                                                label: 'label.start.IP'
                                            },
                                            'endip': {
                                                edit: true,
                                                label: 'label.end.IP'
                                            },
                                            'add-rule': {
                                                label: 'label.add',
                                                addButton: true
                                            }
                                        },
                                        add: {
                                            label: 'label.add',
                                            action: function (args) {
                                                var array1 = [];
                                                array1.push("&podid=" + args.data.podid);
                                                array1.push("&networkid=" + selectedGuestNetworkObj.id);
                                                array1.push("&gateway=" + args.data.gateway);
                                                array1.push("&netmask=" + args.data.netmask);
                                                array1.push("&startip=" + args.data.startip);
                                                if (args.data.endip != null && args.data.endip.length > 0)
                                                    array1.push("&endip=" + args.data.endip);
                                                array1.push("&forVirtualNetwork=false");
                                                //indicates this new IP range is for guest network, not public network

                                                $.ajax({
                                                    url: createURL("createVlanIpRange" + array1.join("")),
                                                    dataType: "json",
                                                    success: function (json) {
                                                        var item = json.createvlaniprangeresponse.vlan;
                                                        args.response.success({
                                                            data: item,
                                                            notification: {
                                                                label: 'label.add.ip.range',
                                                                poll: function (args) {
                                                                    args.complete();
                                                                }
                                                            }
                                                        });
                                                    },
                                                    error: function (XMLHttpResponse) {
                                                        var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
                                                        args.response.error(errorMsg);
                                                    }
                                                });
                                            }
                                        },
                                        actions: {
                                            destroy: {
                                                label: 'label.remove.ip.range',
                                                action: function (args) {
                                                    $.ajax({
                                                        url: createURL('deleteVlanIpRange&id=' + args.context.multiRule[0].id),
                                                        dataType: 'json',
                                                        async: true,
                                                        success: function (json) {
                                                            args.response.success({
                                                                notification: {
                                                                    label: 'label.remove.ip.range',
                                                                    poll: function (args) {
                                                                        args.complete();
                                                                    }
                                                                }
                                                            });
                                                        },
                                                        error: function (json) {
                                                            args.response.error(parseXMLHttpResponse(json));
                                                        }
                                                    });
                                                }
                                            }
                                        },
                                        dataProvider: function (args) {
                                            //only basic zone has IP Range tab
                                            selectedGuestNetworkObj = null;
                                            $.ajax({
                                                url: createURL("listNetworks&listAll=true&trafficType=Guest&zoneid=" + selectedZoneObj.id),
                                                dataType: "json",
                                                async: false,
                                                success: function (json) {
                                                    var items = json.listnetworksresponse.network;
                                                    if (items != null && items.length > 0)
                                                        selectedGuestNetworkObj = json.listnetworksresponse.network[0];
                                                }
                                            });
                                            if (selectedGuestNetworkObj == null)
                                                return;

                                            $.ajax({
                                                url: createURL("listVlanIpRanges&zoneid=" + selectedZoneObj.id + "&networkId=" + selectedGuestNetworkObj.id),
                                                dataType: "json",
                                                success: function (json) {
                                                    var items = json.listvlaniprangesresponse.vlaniprange;
                                                    args.response.success({
                                                        data: items
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            },

                            network: {
                                title: 'label.network',
                                listView: {
                                    section: 'networks',
                                    id: 'networks',
                                    fields: {
                                        name: {
                                            label: 'label.name'
                                        },
                                        type: {
                                            label: 'label.type'
                                        },
                                        vlan: {
                                            label: 'label.vnet.id'
                                        },
                                        broadcasturi: {
                                            label: 'label.broadcat.uri'
                                        },
                                        cidr: {
                                            label: 'label.ipv4.cidr'
                                        },
                                        ip6cidr: {
                                            label: 'label.ipv6.CIDR'
                                        }
                                        //scope: { label: 'label.scope' }
                                    },
                                    actions: {
                                        add: addGuestNetworkDialog.def
                                    },

                                    dataProvider: function (args) {
                                        var array1 = [];
                                        if (args.filterBy != null) {
                                            if (args.filterBy.search != null && args.filterBy.search.by != null && args.filterBy.search.value != null) {
                                                switch (args.filterBy.search.by) {
                                                    case "name":
                                                        if (args.filterBy.search.value.length > 0)
                                                            array1.push("&keyword=" + args.filterBy.search.value);
                                                        break;
                                                }
                                            }
                                        }

                                        //need to make 2 listNetworks API call to get all guest networks from one physical network in Advanced zone
                                        var items = [];
                                        //"listNetworks&projectid=-1": list guest networks under all projects (no matter who the owner is)
                                        $.ajax({
                                            url: createURL("listNetworks&projectid=-1&trafficType=Guest&zoneId=" + selectedZoneObj.id + "&physicalnetworkid=" + selectedPhysicalNetworkObj.id + "&page=" + args.page + "&pagesize=" + pageSize + array1.join("")),
                                            dataType: "json",
                                            async: false,
                                            success: function (json) {
                                                if (json.listnetworksresponse.network != null && json.listnetworksresponse.network.length > 0)
                                                    items = json.listnetworksresponse.network;
                                            }
                                        });

                                        var networkCollectionMap = {};
                                        $(items).each(function () {
                                            networkCollectionMap[this.id] = this.name;
                                        });

                                        //"listNetworks&listAll=true: list guest networks that are not under any project (no matter who the owner is)
                                        $.ajax({
                                            url: createURL("listNetworks&listAll=true&trafficType=Guest&zoneId=" + selectedZoneObj.id + "&physicalnetworkid=" + selectedPhysicalNetworkObj.id + "&page=" + args.page + "&pagesize=" + pageSize + array1.join("")),
                                            dataType: "json",
                                            async: false,
                                            success: function (json) {
                                                $(json.listnetworksresponse.network).each(function () {
                                                    if ((this.id in networkCollectionMap) == false)
                                                        items.push(this);
                                                });
                                            }
                                        });

                                        $(items).each(function () {
                                            addExtraPropertiesToGuestNetworkObject(this);
                                        });

                                        args.response.success({
                                            data: items
                                        });
                                    },

                                    detailView: {
                                        name: 'label.guest.network.details',
                                        noCompact: true,
                                        viewAll: {
                                            path: '_zone.guestIpRanges',
                                            label: 'label.ip.ranges',
                                            preFilter: function (args) {
                                                if (selectedGuestNetworkObj.type == "Isolated") {
                                                    var services = selectedGuestNetworkObj.service;
                                                    if (services != null) {
                                                        for (var i = 0; i < services.length; i++) {
                                                            var service = services[i];
                                                            if (service.name == "SourceNat")
                                                                return false;
                                                        }
                                                    }
                                                }
                                                return true;
                                            }
                                        },
                                        actions: {
                                            edit: {
                                                label: 'label.edit',
                                                action: function (args) {
                                                    var array1 = [];
                                                    array1.push("&name=" + todb(args.data.name));
                                                    array1.push("&displaytext=" + todb(args.data.displaytext));

                                                    //args.data.networkdomain is null when networkdomain field is hidden
                                                    if (args.data.networkdomain != null && args.data.networkdomain != selectedGuestNetworkObj.networkdomain)
                                                        array1.push("&networkdomain=" + todb(args.data.networkdomain));

                                                    //args.data.networkofferingid is null when networkofferingid field is hidden
                                                    if (args.data.networkofferingid != null && args.data.networkofferingid != args.context.networks[0].networkofferingid) {
                                                        array1.push("&networkofferingid=" + todb(args.data.networkofferingid));

                                                        if (args.context.networks[0].type == "Isolated") {
                                                            //Isolated network
                                                            cloudStack.dialog.confirm({
                                                                message: 'message.confirm.current.guest.CIDR.unchanged',
                                                                action: function () {
                                                                    //"Yes"    button is clicked
                                                                    array1.push("&changecidr=false");
                                                                    $.ajax({
                                                                        url: createURL("updateNetwork&id=" + args.context.networks[0].id + array1.join("")),
                                                                        dataType: "json",
                                                                        success: function (json) {
                                                                            var jid = json.updatenetworkresponse.jobid;
                                                                            args.response.success({
                                                                                _custom: {
                                                                                    jobId: jid,
                                                                                    getUpdatedItem: function (json) {
                                                                                        var item = json.queryasyncjobresultresponse.jobresult.network;
                                                                                        return {
                                                                                            data: item
                                                                                        };
                                                                                    }
                                                                                }
                                                                            });
                                                                        }
                                                                    });
                                                                },
                                                                cancelAction: function () {
                                                                    //"Cancel" button is clicked
                                                                    array1.push("&changecidr=true");
                                                                    $.ajax({
                                                                        url: createURL("updateNetwork&id=" + args.context.networks[0].id + array1.join("")),
                                                                        dataType: "json",
                                                                        success: function (json) {
                                                                            var jid = json.updatenetworkresponse.jobid;
                                                                            args.response.success({
                                                                                _custom: {
                                                                                    jobId: jid,
                                                                                    getUpdatedItem: function (json) {
                                                                                        var item = json.queryasyncjobresultresponse.jobresult.network;
                                                                                        return {
                                                                                            data: item
                                                                                        };
                                                                                    }
                                                                                }
                                                                            });
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                            return;
                                                        }
                                                    }

                                                    $.ajax({
                                                        url: createURL("updateNetwork&id=" + args.context.networks[0].id + array1.join("")),
                                                        dataType: "json",
                                                        success: function (json) {
                                                            var jid = json.updatenetworkresponse.jobid;
                                                            args.response.success({
                                                                _custom: {
                                                                    jobId: jid,
                                                                    getUpdatedItem: function (json) {
                                                                        var item = json.queryasyncjobresultresponse.jobresult.network;
                                                                        return {
                                                                            data: item
                                                                        };
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

                                            'restart': {
                                                label: 'label.restart.network',
                                                createForm: {
                                                    title: 'label.restart.network',
                                                    desc: 'message.restart.network',
                                                    preFilter: function (args) {
                                                        if (selectedZoneObj.networktype == "Basic") {
                                                            args.$form.find('.form-item[rel=cleanup]').find('input').removeAttr('checked');
                                                            //unchecked
                                                            args.$form.find('.form-item[rel=cleanup]').hide();
                                                            //hidden
                                                        } else {
                                                            args.$form.find('.form-item[rel=cleanup]').find('input').attr('checked', 'checked');
                                                            //checked
                                                            args.$form.find('.form-item[rel=cleanup]').css('display', 'inline-block');
                                                            //shown
                                                        }
                                                    },
                                                    fields: {
                                                        cleanup: {
                                                            label: 'label.clean.up',
                                                            isBoolean: true
                                                        }
                                                    }
                                                },
                                                action: function (args) {
                                                    var array1 = [];
                                                    array1.push("&cleanup=" + (args.data.cleanup == "on"));
                                                    $.ajax({
                                                        url: createURL("restartNetwork&cleanup=true&id=" + args.context.networks[0].id + array1.join("")),
                                                        dataType: "json",
                                                        async: true,
                                                        success: function (json) {
                                                            var jid = json.restartnetworkresponse.jobid;
                                                            args.response.success({
                                                                _custom: {
                                                                    jobId: jid,
                                                                    getUpdatedItem: function (json) {
                                                                        return json.queryasyncjobresultresponse.jobresult.network;
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    });
                                                },
                                                messages: {
                                                    notification: function (args) {
                                                        return 'label.restart.network';
                                                    }
                                                },
                                                notification: {
                                                    poll: pollAsyncJobResult
                                                }
                                            },

                                            'remove': {
                                                label: 'label.action.delete.network',
                                                messages: {
                                                    confirm: function (args) {
                                                        return 'message.action.delete.network';
                                                    },
                                                    notification: function (args) {
                                                        return 'label.action.delete.network';
                                                    }
                                                },
                                                action: function (args) {
                                                    $.ajax({
                                                        url: createURL("deleteNetwork&id=" + args.context.networks[0].id),
                                                        dataType: "json",
                                                        async: true,
                                                        success: function (json) {
                                                            var jid = json.deletenetworkresponse.jobid;
                                                            args.response.success({
                                                                _custom: {
                                                                    jobId: jid,
                                                                    getUpdatedItem: function (json) {
                                                                        return {};
                                                                        //nothing in this network needs to be updated, in fact, this whole template has being deleted
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    });
                                                },
                                                notification: {
                                                    poll: pollAsyncJobResult
                                                }
                                            }
                                        },
                                        tabs: {
                                            details: {
                                                title: 'label.details',
                                                preFilter: function (args) {
                                                    var hiddenFields = [];
                                                    if (selectedZoneObj.networktype == "Basic") {
                                                        hiddenFields.push("account");
                                                        hiddenFields.push("gateway");
                                                        //hiddenFields.push("netmask");
                                                    }

                                                    if (selectedGuestNetworkObj.type == "Isolated") {
                                                        hiddenFields.push("networkofferingdisplaytext");
                                                        hiddenFields.push("networkdomaintext");
                                                        hiddenFields.push("gateway");
                                                        //hiddenFields.push("netmask");
                                                    } else {
                                                        //selectedGuestNetworkObj.type == "Shared"
                                                        hiddenFields.push("networkofferingid");
                                                        hiddenFields.push("networkdomain");
                                                    }
                                                    return hiddenFields;
                                                },
                                                fields: [{
                                                    name: {
                                                        label: 'label.name',
                                                        isEditable: true
                                                    }
                                                },
                                                    {
                                                        id: {
                                                            label: 'label.id'
                                                        },
                                                        displaytext: {
                                                            label: 'label.description',
                                                            isEditable: true
                                                        },
                                                        type: {
                                                            label: 'label.type'
                                                        },
                                                        state: {
                                                            label: 'label.state'
                                                        },
                                                        restartrequired: {
                                                            label: 'label.restart.required',
                                                            converter: function (booleanValue) {
                                                                if (booleanValue == true)
                                                                    return "<font color='red'>Yes</font>"; else if (booleanValue == false)
                                                                    return "No";
                                                            }
                                                        },
                                                        vlan: {
                                                            label: 'label.vlan.id'
                                                        },
                                                        broadcasturi: {
                                                            label: 'label.broadcat.uri'
                                                        },
                                                        scope: {
                                                            label: 'label.scope'
                                                        },
                                                        networkofferingdisplaytext: {
                                                            label: 'label.network.offering'
                                                        },
                                                        networkofferingid: {
                                                            label: 'label.network.offering',
                                                            isEditable: true,
                                                            select: function (args) {
                                                                var items = [];
                                                                $.ajax({
                                                                    url: createURL("listNetworkOfferings&state=Enabled&networkid=" + selectedGuestNetworkObj.id + "&zoneid=" + selectedGuestNetworkObj.zoneid),
                                                                    dataType: "json",
                                                                    async: false,
                                                                    success: function (json) {
                                                                        var networkOfferingObjs = json.listnetworkofferingsresponse.networkoffering;
                                                                        $(networkOfferingObjs).each(function () {
                                                                            items.push({
                                                                                id: this.id,
                                                                                description: this.displaytext
                                                                            });
                                                                        });
                                                                    }
                                                                });

                                                                //include currently selected network offeirng to dropdown
                                                                items.push({
                                                                    id: selectedGuestNetworkObj.networkofferingid,
                                                                    description: selectedGuestNetworkObj.networkofferingdisplaytext
                                                                });

                                                                args.response.success({
                                                                    data: items
                                                                });
                                                            }
                                                        },

                                                        networkofferingidText: {
                                                            label: 'label.network.offering.id'
                                                        },

                                                        gateway: {
                                                            label: 'label.ipv4.gateway'
                                                        },
                                                        //netmask: { label: 'label.netmask' },
                                                        cidr: {
                                                            label: 'label.ipv4.cidr'
                                                        },

                                                        ip6gateway: {
                                                            label: 'label.ipv6.gateway'
                                                        },
                                                        ip6cidr: {
                                                            label: 'label.ipv6.CIDR'
                                                        },

                                                        networkdomaintext: {
                                                            label: 'label.network.domain'
                                                        },
                                                        networkdomain: {
                                                            label: 'label.network.domain',
                                                            isEditable: true
                                                        },

                                                        domain: {
                                                            label: 'label.domain'
                                                        },
                                                        subdomainaccess: {
                                                            label: 'label.subdomain.access',
                                                            converter: function (data) {
                                                                return data ? 'Yes' : 'No';
                                                            }
                                                        },
                                                        account: {
                                                            label: 'label.account'
                                                        },
                                                        project: {
                                                            label: 'label.project'
                                                        }
                                                    }],
                                                dataProvider: function (args) {
                                                    var data = {
                                                        id: args.context.networks[0].id
                                                    };
                                                    if (args.context.networks[0].projectid != null) {
                                                        $.extend(data, {
                                                            projectid: -1
                                                        });
                                                    } else {
                                                        $.extend(data, {
                                                            listAll: true //pass "&listAll=true" to "listNetworks&id=xxxxxxxx" for now before API gets fixed.
                                                        });
                                                    }

                                                    $.ajax({
                                                        url: createURL("listNetworks"),
                                                        data: data,
                                                        async: false,
                                                        success: function (json) {
                                                            selectedGuestNetworkObj = json.listnetworksresponse.network[0];
                                                            addExtraPropertiesToGuestNetworkObject(selectedGuestNetworkObj);

                                                            $(window).trigger('cloudStack.module.sharedFunctions.addExtraProperties', {
                                                                obj: selectedGuestNetworkObj,
                                                                objType: "Network"
                                                            });

                                                            args.response.success({
                                                                actionFilter: cloudStack.actionFilter.guestNetwork,
                                                                data: selectedGuestNetworkObj
                                                            });
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    }
                                }
                            },

                            dedicatedGuestVlanRanges: {
                                title: 'label.dedicated.vlan.vni.ranges',
                                listView: {
                                    section: 'dedicatedGuestVlanRanges',
                                    id: 'dedicatedGuestVlanRanges',
                                    fields: {
                                        guestvlanrange: {
                                            label: 'label.vlan.vni.ranges'
                                        },
                                        domain: {
                                            label: 'label.domain'
                                        },
                                        account: {
                                            label: 'label.account'
                                        }
                                    },
                                    dataProvider: function (args) {
                                        $.ajax({
                                            url: createURL('listDedicatedGuestVlanRanges'),
                                            data: {
                                                physicalnetworkid: args.context.physicalNetworks[0].id
                                            },
                                            success: function (json) {
                                                var items = json.listdedicatedguestvlanrangesresponse.dedicatedguestvlanrange;
                                                args.response.success({
                                                    data: items
                                                })
                                            }
                                        });
                                    },
                                    actions: {
                                        add: {
                                            label: 'label.dedicate.vlan.vni.range',
                                            messages: {
                                                notification: function (args) {
                                                    return 'label.dedicate.vlan.vni.range';
                                                }
                                            },
                                            createForm: {
                                                title: 'label.dedicate.vlan.vni.range',
                                                fields: {
                                                    vlanrange: {
                                                        label: 'label.vlan.vni.range',
                                                        /*  select: function(args) {
                                                         var items = [];
                                                         if(args.context.physicalNetworks[0].vlan != null && args.context.physicalNetworks[0].vlan.length > 0) {
                                                         var vlanranges = args.context.physicalNetworks[0].vlan.split(";");
                                                         for(var i = 0; i < vlanranges.length ; i++) {
                                                         items.push({id: vlanranges[i], description: vlanranges[i]});
                                                         }
                                                         }
                                                         args.response.success({data: items});
                                                         },*/
                                                        validation: {
                                                            required: true
                                                        }
                                                    },
                                                    account: {
                                                        label: 'label.account',
                                                        validation: {
                                                            required: true
                                                        }
                                                    },
                                                    domainid: {
                                                        label: 'label.domain',
                                                        validation: {
                                                            required: true
                                                        },
                                                        select: function (args) {
                                                            $.ajax({
                                                                url: createURL('listDomains'),
                                                                data: {
                                                                    listAll: true
                                                                },
                                                                success: function (json) {
                                                                    args.response.success({
                                                                        data: $.map(json.listdomainsresponse.domain, function (domain) {
                                                                            return {
                                                                                id: domain.id,
                                                                                description: domain.path
                                                                            };
                                                                        })
                                                                    });
                                                                }
                                                            });
                                                        }
                                                    }
                                                }
                                            },
                                            action: function (args) {
                                                var data = {
                                                    physicalnetworkid: args.context.physicalNetworks[0].id,
                                                    vlanrange: args.data.vlanrange,
                                                    domainid: args.data.domainid,
                                                    account: args.data.account
                                                };
                                                $.ajax({
                                                    url: createURL('dedicateGuestVlanRange'),
                                                    data: data,
                                                    success: function (json) {
                                                        var item = json.dedicateguestvlanrangeresponse.dedicatedguestvlanrange;
                                                        args.response.success({
                                                            data: item
                                                        });
                                                    }
                                                });
                                            },
                                            notification: {
                                                poll: function (args) {
                                                    args.complete();
                                                }
                                            }
                                        }
                                    },

                                    detailView: {
                                        name: 'label.vlan.range.details',
                                        actions: {
                                            remove: {
                                                label: 'label.release.dedicated.vlan.range',
                                                messages: {
                                                    confirm: function (args) {
                                                        return 'message.confirm.release.dedicate.vlan.range';
                                                    },
                                                    notification: function (args) {
                                                        return 'label.release.dedicated.vlan.range';
                                                    }
                                                },
                                                action: function (args) {
                                                    var data = {
                                                        id: args.context.dedicatedGuestVlanRanges[0].id
                                                    };
                                                    $.ajax({
                                                        url: createURL('releaseDedicatedGuestVlanRange'),
                                                        data: data,
                                                        async: true,
                                                        success: function (json) {
                                                            var jid = json.releasededicatedguestvlanrangeresponse.jobid;
                                                            args.response.success({
                                                                _custom: {
                                                                    jobId: jid
                                                                }
                                                            });
                                                        }
                                                    });
                                                },
                                                notification: {
                                                    poll: pollAsyncJobResult
                                                }
                                            }
                                        },

                                        tabs: {
                                            details: {
                                                title: 'label.details',
                                                fields: [{
                                                    guestvlanrange: {
                                                        label: 'label.vlan.ranges'
                                                    }
                                                },
                                                    {
                                                        domain: {
                                                            label: 'label.domain'
                                                        },
                                                        account: {
                                                            label: 'label.account'
                                                        },
                                                        id: {
                                                            label: 'label.id'
                                                        }
                                                    }],
                                                dataProvider: function (args) {
                                                    $.ajax({
                                                        url: createURL('listDedicatedGuestVlanRanges'),
                                                        data: {
                                                            id: args.context.dedicatedGuestVlanRanges[0].id
                                                        },
                                                        success: function (json) {
                                                            var item = json.listdedicatedguestvlanrangesresponse.dedicatedguestvlanrange[0];
                                                            args.response.success({
                                                                data: item
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
                    }
                }
            },

            networks: {
                listView: {
                    id: 'physicalNetworks',
                    hideToolbar: true,
                    fields: {
                        name: {
                            label: 'label.name'
                        },
                        state: {
                            converter: function (str) {
                                // For localization
                                return str;
                            },
                            label: 'label.state',
                            indicator: {
                                'Enabled': 'on',
                                'Disabled': 'off'
                            }
                        },
                        isolationmethods: {
                            label: 'label.isolation.method'
                        }
                    },

                    actions: {
                        remove: {
                            label: 'label.action.delete.physical.network',
                            messages: {
                                confirm: function (args) {
                                    return 'message.action.delete.physical.network';
                                },
                                notification: function (args) {
                                    return 'label.action.delete.physical.network';
                                }
                            },
                            action: function (args) {
                                $.ajax({
                                    url: createURL("deletePhysicalNetwork&id=" + args.context.physicalNetworks[0].id),
                                    dataType: "json",
                                    async: true,
                                    success: function (json) {
                                        var jid = json.deletephysicalnetworkresponse.jobid;
                                        args.response.success({
                                            _custom: {
                                                jobId: jid
                                            }
                                        });
                                    }
                                });
                            },
                            notification: {
                                poll: pollAsyncJobResult
                            }
                        }
                    }
                },
                dataProvider: function (args) {
                    $.ajax({
                        url: createURL('listPhysicalNetworks'),
                        data: {
                            zoneid: args.context.zones[0].id
                        },
                        success: function (json) {
                            physicalNetworkObjs = json.listphysicalnetworksresponse.physicalnetwork;
                            args.response.success({
                                actionFilter: cloudStack.actionFilter.physicalNetwork,
                                data: json.listphysicalnetworksresponse.physicalnetwork
                            });
                        }
                    });
                }
            },

            trafficTypes: {
                dataProvider: function (args) {
                    selectedPhysicalNetworkObj = args.context.physicalNetworks[0];

                    $.ajax({
                        url: createURL('listTrafficTypes'),
                        data: {
                            physicalnetworkid: selectedPhysicalNetworkObj.id
                        },
                        success: function (json) {
                            args.response.success({
                                data: $.map(json.listtraffictypesresponse.traffictype, function (trafficType) {
                                    return {
                                        id: trafficType.id,
                                        name: trafficType.traffictype
                                    };
                                })
                            });
                        }
                    });
                }
            },

            networkProviders: {
                statusLabels: {
                    enabled: 'Enabled', //having device, network service provider is enabled
                    'not-configured': 'Not setup', //no device
                    disabled: 'Disabled' //having device, network service provider is disabled
                },

                // Actions performed on entire net. provider type
                actions: {
                    enable: function (args) {
                        args.response.success();
                    },

                    disable: function (args) {
                        args.response.success();
                    }
                },

                types: {
                    virtualRouter: {
                        id: 'virtualRouterProviders',
                        label: 'label.virtual.router',
                        isMaximized: true,
                        type: 'detailView',
                        fields: {
                            name: {
                                label: 'label.name'
                            },
                            ipaddress: {
                                label: 'label.ip.address'
                            },
                            state: {
                                label: 'label.status',
                                indicator: {
                                    'Enabled': 'on'
                                }
                            }
                        },
                        tabs: {
                            network: {
                                title: 'label.network',
                                fields: [{
                                    name: {
                                        label: 'label.name'
                                    }
                                },
                                    {
                                        id: {
                                            label: 'label.id'
                                        },
                                        state: {
                                            label: 'label.state'
                                        },
                                        physicalnetworkid: {
                                            label: 'label.physical.network.ID'
                                        },
                                        destinationphysicalnetworkid: {
                                            label: 'label.destination.physical.network.id'
                                        },
                                        supportedServices: {
                                            label: 'label.supported.services'
                                        }
                                    }],
                                dataProvider: function (args) {
                                    refreshNspData("VirtualRouter");
                                    args.response.success({
                                        actionFilter: virtualRouterProviderActionFilter,
                                        data: $.extend(nspMap["virtualRouter"], {
                                            supportedServices: nspMap["virtualRouter"].servicelist.join(', ')
                                        })
                                    });
                                }
                            },

                            instances: {
                                title: 'label.instances',
                                listView: {
                                    label: 'label.virtual.appliances',
                                    id: 'routers',
                                    fields: {
                                        name: {
                                            label: 'label.name'
                                        },
                                        zonename: {
                                            label: 'label.zone'
                                        },
                                        routerType: {
                                            label: 'label.type'
                                        },
                                        state: {
                                            converter: function (str) {
                                                // For localization
                                                return str;
                                            },
                                            label: 'label.status',
                                            indicator: {
                                                'Running': 'on',
                                                'Stopped': 'off',
                                                'Error': 'off'
                                            }
                                        }
                                    },
                                    dataProvider: function (args) {
                                        var array1 = [];
                                        if (args.filterBy != null) {
                                            if (args.filterBy.search != null && args.filterBy.search.by != null && args.filterBy.search.value != null) {
                                                switch (args.filterBy.search.by) {
                                                    case "name":
                                                        if (args.filterBy.search.value.length > 0)
                                                            array1.push("&keyword=" + args.filterBy.search.value);
                                                        break;
                                                }
                                            }
                                        }

                                        var data2 = {
                                            forvpc: false
                                        };
                                        var routers = [];
                                        $.ajax({
                                            url: createURL("listRouters&zoneid=" + selectedZoneObj.id + "&listAll=true&page=" + args.page + "&pagesize=" + pageSize + array1.join("")),
                                            data: data2,
                                            success: function (json) {
                                                var items = json.listroutersresponse.router ?
                                                    json.listroutersresponse.router : [];

                                                $(items).map(function (index, item) {
                                                    routers.push(item);
                                                });

                                                /*
                                                 * In project view, the first listRotuers API(without projectid=-1) will return the same objects as the second listRouters API(with projectid=-1),
                                                 * because in project view, all API calls are appended with projectid=[projectID].
                                                 * Therefore, we only call the second listRouters API(with projectid=-1) in non-project view.
                                                 */
                                                if (cloudStack.context && cloudStack.context.projects == null) { //non-project view
                                                    $.ajax({
                                                        url: createURL("listRouters&zoneid=" + selectedZoneObj.id + "&listAll=true&page=" + args.page + "&pagesize=" + pageSize + array1.join("") + "&projectid=-1"),
                                                        data: data2,
                                                        async: false,
                                                        success: function (json) {
                                                            var items = json.listroutersresponse.router ?
                                                                json.listroutersresponse.router : [];

                                                            $(items).map(function (index, item) {
                                                                routers.push(item);
                                                            });
                                                        }
                                                    });
                                                }

                                                args.response.success({
                                                    actionFilter: routerActionfilter,
                                                    data: $(routers).map(mapRouterType)
                                                });
                                            }
                                        });
                                    },
                                    detailView: {
                                        name: 'label.virtual.appliance.details',
                                        actions: {
                                            start: {
                                                label: 'label.action.start.router',
                                                messages: {
                                                    confirm: function (args) {
                                                        return 'message.action.start.router';
                                                    },
                                                    notification: function (args) {
                                                        return 'label.action.start.router';
                                                    }
                                                },
                                                action: function (args) {
                                                    $.ajax({
                                                        url: createURL('startRouter&id=' + args.context.routers[0].id),
                                                        dataType: 'json',
                                                        async: true,
                                                        success: function (json) {
                                                            var jid = json.startrouterresponse.jobid;
                                                            args.response.success({
                                                                _custom: {
                                                                    jobId: jid,
                                                                    getUpdatedItem: function (json) {
                                                                        return json.queryasyncjobresultresponse.jobresult.router;
                                                                    },
                                                                    getActionFilter: function () {
                                                                        return routerActionfilter;
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

                                            stop: {
                                                label: 'label.action.stop.router',
                                                createForm: {
                                                    title: 'label.action.stop.router',
                                                    desc: 'message.action.stop.router',
                                                    fields: {
                                                        forced: {
                                                            label: 'force.stop',
                                                            isBoolean: true,
                                                            isChecked: false
                                                        }
                                                    }
                                                },
                                                messages: {
                                                    notification: function (args) {
                                                        return 'label.action.stop.router';
                                                    }
                                                },
                                                action: function (args) {
                                                    var array1 = [];
                                                    array1.push("&forced=" + (args.data.forced == "on"));
                                                    $.ajax({
                                                        url: createURL('stopRouter&id=' + args.context.routers[0].id + array1.join("")),
                                                        dataType: 'json',
                                                        async: true,
                                                        success: function (json) {
                                                            var jid = json.stoprouterresponse.jobid;
                                                            args.response.success({
                                                                _custom: {
                                                                    jobId: jid,
                                                                    getUpdatedItem: function (json) {
                                                                        return json.queryasyncjobresultresponse.jobresult.router;
                                                                    },
                                                                    getActionFilter: function () {
                                                                        return routerActionfilter;
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

                                            'remove': {
                                                label: 'label.destroy.router',
                                                messages: {
                                                    confirm: function (args) {
                                                        return 'message.confirm.destroy.router';
                                                    },
                                                    notification: function (args) {
                                                        return 'label.destroy.router';
                                                    }
                                                },
                                                action: function (args) {
                                                    $.ajax({
                                                        url: createURL("destroyRouter&id=" + args.context.routers[0].id),
                                                        dataType: "json",
                                                        async: true,
                                                        success: function (json) {
                                                            var jid = json.destroyrouterresponse.jobid;
                                                            args.response.success({
                                                                _custom: {
                                                                    jobId: jid
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
                                                label: 'label.action.migrate.router',
                                                createForm: {
                                                    title: 'label.action.migrate.router',
                                                    desc: '',
                                                    fields: {
                                                        hostId: {
                                                            label: 'label.host',
                                                            validation: {
                                                                required: true
                                                            },
                                                            select: function (args) {
                                                                $.ajax({
                                                                    url: createURL("findHostsForMigration&VirtualMachineId=" + args.context.routers[0].id),
                                                                    dataType: "json",
                                                                    async: true,
                                                                    success: function (json) {
                                                                        var hostObjs = json.findhostsformigrationresponse.host;
                                                                        var items = [];
                                                                        $(hostObjs).each(function () {
                                                                            items.push({
                                                                                id: this.id,
                                                                                description: (this.name + " (" + (this.suitableformigration ? "Suitable" : "Not Suitable") + ")")
                                                                            });
                                                                        });
                                                                        args.response.success({
                                                                            data: items
                                                                        });
                                                                    }
                                                                });
                                                            },
                                                            error: function (XMLHttpResponse) {
                                                                var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
                                                                args.response.error(errorMsg);
                                                            }
                                                        }
                                                    }
                                                },
                                                messages: {
                                                    notification: function (args) {
                                                        return 'label.action.migrate.router';
                                                    }
                                                },
                                                action: function (args) {
                                                    $.ajax({
                                                        url: createURL("migrateSystemVm&hostid=" + args.data.hostId + "&virtualmachineid=" + args.context.routers[0].id),
                                                        dataType: "json",
                                                        async: true,
                                                        success: function (json) {
                                                            var jid = json.migratesystemvmresponse.jobid;
                                                            args.response.success({
                                                                _custom: {
                                                                    jobId: jid,
                                                                    getUpdatedItem: function (json) {
                                                                        //return json.queryasyncjobresultresponse.jobresult.systemvminstance;    //not all properties returned in systemvminstance
                                                                        $.ajax({
                                                                            url: createURL("listRouters&id=" + json.queryasyncjobresultresponse.jobresult.systemvm.id),
                                                                            dataType: "json",
                                                                            async: false,
                                                                            success: function (json) {
                                                                                var items = json.listroutersresponse.router;
                                                                                if (items != null && items.length > 0) {
                                                                                    return items[0];
                                                                                }
                                                                            }
                                                                        });
                                                                    },
                                                                    getActionFilter: function () {
                                                                        return routerActionfilter;
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

                                            viewConsole: {
                                                label: 'label.view.console',
                                                action: {
                                                    externalLink: {
                                                        url: function (args) {
                                                            return clientConsoleUrl + '?cmd=access&vm=' + args.context.routers[0].id;
                                                        },
                                                        title: function (args) {
                                                            return args.context.routers[0].id.substr(0, 8);
                                                            //title in window.open() can't have space nor longer than 8 characters. Otherwise, IE browser will have error.
                                                        },
                                                        width: 820,
                                                        height: 640
                                                    }
                                                }
                                            }
                                        },
                                        tabs: {
                                            details: {
                                                title: 'label.details',
                                                preFilter: function (args) {
                                                    var hiddenFields = [];
                                                    if (!args.context.routers[0].project) {
                                                        hiddenFields.push('project');
                                                        hiddenFields.push('projectid');
                                                    }
                                                    if (selectedZoneObj.networktype == 'Basic') {
                                                        hiddenFields.push('publicip');
                                                        //In Basic zone, guest IP is public IP. So, publicip is not returned by listRouters API. Only guestipaddress is returned by listRouters API.
                                                    }

                                                    if ('routers' in args.context && args.context.routers[0].vpcid != undefined) {
                                                        hiddenFields.push('guestnetworkid');
                                                        hiddenFields.push('guestnetworkname');
                                                    } else if ('routers' in args.context && args.context.routers[0].guestnetworkid != undefined) {
                                                        hiddenFields.push('vpcid');
                                                        hiddenFields.push('vpcname');
                                                    }

                                                    return hiddenFields;
                                                },
                                                fields: [{
                                                    name: {
                                                        label: 'label.name'
                                                    },
                                                    project: {
                                                        label: 'label.project'
                                                    }
                                                },
                                                    {
                                                        id: {
                                                            label: 'label.id'
                                                        },
                                                        projectid: {
                                                            label: 'label.project.id'
                                                        },
                                                        state: {
                                                            label: 'label.state'
                                                        },
                                                        guestnetworkid: {
                                                            label: 'label.network.id'
                                                        },
                                                        guestnetworkname: {
                                                            label: 'label.network.name'
                                                        },
                                                        vpcid: {
                                                            label: 'label.vpc.id'
                                                        },
                                                        vpcname: {
                                                            label: 'label.vpc'
                                                        },
                                                        publicip: {
                                                            label: 'label.public.ip'
                                                        },
                                                        guestipaddress: {
                                                            label: 'label.guest.ip'
                                                        },
                                                        linklocalip: {
                                                            label: 'label.linklocal.ip'
                                                        },
                                                        hostname: {
                                                            label: 'label.host'
                                                        },
                                                        serviceofferingname: {
                                                            label: 'label.compute.offering'
                                                        },
                                                        networkdomain: {
                                                            label: 'label.network.domain'
                                                        },
                                                        domain: {
                                                            label: 'label.domain'
                                                        },
                                                        account: {
                                                            label: 'label.account'
                                                        },
                                                        created: {
                                                            label: 'label.created',
                                                            converter: cloudStack.converters.toLocalDate
                                                        },
                                                        isredundantrouter: {
                                                            label: 'label.redundant.router',
                                                            converter: cloudStack.converters.toBooleanText
                                                        },
                                                        redundantRouterState: {
                                                            label: 'label.redundant.state'
                                                        }
                                                    }],
                                                dataProvider: function (args) {
                                                    $.ajax({
                                                        url: createURL("listRouters&id=" + args.context.routers[0].id),
                                                        dataType: 'json',
                                                        async: true,
                                                        success: function (json) {
                                                            var jsonObj = json.listroutersresponse.router[0];
                                                            addExtraPropertiesToRouterInstanceObject(jsonObj);
                                                            args.response.success({
                                                                actionFilter: routerActionfilter,
                                                                data: jsonObj
                                                            });
                                                        }
                                                    });
                                                }
                                            },
                                            nics: {
                                                title: 'label.nics',
                                                multiple: true,
                                                fields: [{
                                                    name: {
                                                        label: 'label.name',
                                                        header: true
                                                    },
                                                    type: {
                                                        label: 'label.type'
                                                    },
                                                    traffictype: {
                                                        label: 'label.traffic.type'
                                                    },
                                                    networkname: {
                                                        label: 'label.network.name'
                                                    },
                                                    netmask: {
                                                        label: 'label.netmask'
                                                    },
                                                    ipaddress: {
                                                        label: 'label.ip.address'
                                                    },
                                                    id: {
                                                        label: 'label.id'
                                                    },
                                                    networkid: {
                                                        label: 'label.network.id'
                                                    },
                                                    isolationuri: {
                                                        label: 'label.isolation.uri'
                                                    },
                                                    broadcasturi: {
                                                        label: 'label.broadcast.uri'
                                                    }
                                                }],
                                                dataProvider: function (args) {
                                                    $.ajax({
                                                        url: createURL("listRouters&id=" + args.context.routers[0].id),
                                                        dataType: 'json',
                                                        async: true,
                                                        success: function (json) {
                                                            var jsonObj = json.listroutersresponse.router[0].nic;

                                                            args.response.success({
                                                                actionFilter: routerActionfilter,
                                                                data: $.map(jsonObj, function (nic, index) {
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
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        actions: {
                            enable: {
                                label: 'label.enable.provider',
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("updateNetworkServiceProvider&id=" + nspMap["virtualRouter"].id + "&state=Enabled"),
                                        dataType: "json",
                                        success: function (json) {
                                            var jid = json.updatenetworkserviceproviderresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getUpdatedItem: function (json) {
                                                        $(window).trigger('cloudStack.fullRefresh');
                                                    }
                                                }
                                            });
                                        }
                                    });
                                },
                                messages: {
                                    confirm: function (args) {
                                        return 'message.confirm.enable.provider';
                                    },
                                    notification: function () {
                                        return 'label.enable.provider';
                                    }
                                },
                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            },
                            disable: {
                                label: 'label.disable.provider',
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("updateNetworkServiceProvider&id=" + nspMap["virtualRouter"].id + "&state=Disabled"),
                                        dataType: "json",
                                        success: function (json) {
                                            var jid = json.updatenetworkserviceproviderresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getUpdatedItem: function (json) {
                                                        $(window).trigger('cloudStack.fullRefresh');
                                                    }
                                                }
                                            });
                                        }
                                    });
                                },
                                messages: {
                                    confirm: function (args) {
                                        return 'message.confirm.disable.provider';
                                    },
                                    notification: function () {
                                        return 'label.disable.provider';
                                    }
                                },
                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            }
                        }
                    },

                    InternalLbVm: {
                        id: 'InternalLbVm',
                        label: 'label.internallbvm',
                        isMaximized: true,
                        type: 'detailView',
                        fields: {
                            name: {
                                label: 'label.name'
                            },
                            ipaddress: {
                                label: 'label.ip.address'
                            },
                            state: {
                                label: 'label.status',
                                indicator: {
                                    'Enabled': 'on'
                                }
                            }
                        },
                        tabs: {
                            network: {
                                title: 'label.network',
                                fields: [{
                                    name: {
                                        label: 'label.name'
                                    }
                                },
                                    {
                                        id: {
                                            label: 'label.id'
                                        },
                                        state: {
                                            label: 'label.state'
                                        },
                                        physicalnetworkid: {
                                            label: 'label.physical.network.ID'
                                        },
                                        destinationphysicalnetworkid: {
                                            label: 'label.destination.physical.network.id'
                                        },
                                        supportedServices: {
                                            label: 'label.supported.services'
                                        }
                                    }],
                                dataProvider: function (args) {
                                    refreshNspData("InternalLbVm");
                                    args.response.success({
                                        actionFilter: virtualRouterProviderActionFilter,
                                        data: $.extend(nspMap["InternalLbVm"], {
                                            supportedServices: nspMap["InternalLbVm"].servicelist.join(', ')
                                        })
                                    });
                                }
                            },

                            instances: {
                                title: 'label.instances',
                                listView: {
                                    label: 'label.virtual.appliances',
                                    id: 'internallbinstances',
                                    fields: {
                                        name: {
                                            label: 'label.name'
                                        },
                                        zonename: {
                                            label: 'label.zone'
                                        },
                                        routerType: {
                                            label: 'label.type'
                                        },
                                        state: {
                                            converter: function (str) {
                                                // For localization
                                                return str;
                                            },
                                            label: 'label.status',
                                            indicator: {
                                                'Running': 'on',
                                                'Stopped': 'off',
                                                'Error': 'off'
                                            }
                                        }
                                    },
                                    dataProvider: function (args) {
                                        var array1 = [];
                                        if (args.filterBy != null) {
                                            if (args.filterBy.search != null && args.filterBy.search.by != null && args.filterBy.search.value != null) {
                                                switch (args.filterBy.search.by) {
                                                    case "name":
                                                        if (args.filterBy.search.value.length > 0)
                                                            array1.push("&keyword=" + args.filterBy.search.value);
                                                        break;
                                                }
                                            }
                                        }

                                        var routers = [];
                                        $.ajax({
                                            url: createURL("listInternalLoadBalancerVMs&zoneid=" + selectedZoneObj.id + "&listAll=true&page=" + args.page + "&pagesize=" + pageSize + array1.join("")),
                                            success: function (json) {
                                                var items = json.listinternallbvmssresponse.internalloadbalancervm ?
                                                    json.listinternallbvmssresponse.internalloadbalancervm : [];

                                                $(items).map(function (index, item) {
                                                    routers.push(item);
                                                });

                                                // Get project routers
                                                $.ajax({
                                                    url: createURL("listInternalLoadBalancerVMs&zoneid=" + selectedZoneObj.id + "&listAll=true&page=" + args.page + "&pagesize=" + pageSize + array1.join("") + "&projectid=-1"),
                                                    success: function (json) {
                                                        var items = json.listinternallbvmssresponse.internalloadbalancervm ?
                                                            json.listinternallbvmssresponse.internalloadbalancervm : [];

                                                        $(items).map(function (index, item) {
                                                            routers.push(item);
                                                        });
                                                        args.response.success({
                                                            actionFilter: internallbinstanceActionfilter,
                                                            data: $(routers).map(mapRouterType)
                                                        });
                                                    }
                                                });
                                            }
                                        });
                                    },
                                    detailView: {
                                        name: 'label.virtual.appliance.details',
                                        actions: {
                                            start: {
                                                label: 'label.start.lb.vm',
                                                messages: {
                                                    confirm: function (args) {
                                                        return 'message.confirm.start.lb.vm';
                                                    },
                                                    notification: function (args) {
                                                        return 'label.start.lb.vm';
                                                    }
                                                },
                                                action: function (args) {
                                                    $.ajax({
                                                        url: createURL('startInternalLoadBalancerVM&id=' + args.context.internallbinstances[0].id),
                                                        dataType: 'json',
                                                        async: true,
                                                        success: function (json) {
                                                            var jid = json.startinternallbvmresponse.jobid;
                                                            args.response.success({
                                                                _custom: {
                                                                    jobId: jid,
                                                                    getUpdatedItem: function (json) {
                                                                        return json.queryasyncjobresultresponse.jobresult.internalloadbalancervm;
                                                                    },
                                                                    getActionFilter: function () {
                                                                        return internallbinstanceActionfilter;
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

                                            stop: {
                                                label: 'label.stop.lb.vm',
                                                createForm: {
                                                    title: 'message.confirm.stop.lb.vm',
                                                    desc: 'label.stop.lb.vm',
                                                    fields: {
                                                        forced: {
                                                            label: 'force.stop',
                                                            isBoolean: true,
                                                            isChecked: false
                                                        }
                                                    }
                                                },
                                                messages: {
                                                    notification: function (args) {
                                                        return 'label.stop.lb.vm';
                                                    }
                                                },
                                                action: function (args) {
                                                    var array1 = [];
                                                    array1.push("&forced=" + (args.data.forced == "on"));
                                                    $.ajax({
                                                        url: createURL('stopInternalLoadBalancerVM&id=' + args.context.internallbinstances[0].id + array1.join("")),
                                                        dataType: 'json',
                                                        async: true,
                                                        success: function (json) {
                                                            var jid = json.stopinternallbvmresponse.jobid;
                                                            args.response.success({
                                                                _custom: {
                                                                    jobId: jid,
                                                                    getUpdatedItem: function (json) {
                                                                        return json.queryasyncjobresultresponse.jobresult.internalloadbalancervm;
                                                                    },
                                                                    getActionFilter: function () {
                                                                        return internallbinstanceActionfilter;
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
                                                label: 'label.migrate.lb.vm',
                                                createForm: {
                                                    title: 'label.migrate.lb.vm',
                                                    fields: {
                                                        hostId: {
                                                            label: 'label.host',
                                                            validation: {
                                                                required: true
                                                            },
                                                            select: function (args) {
                                                                $.ajax({
                                                                    url: createURL("findHostsForMigration&VirtualMachineId=" + args.context.internallbinstances[0].id),
                                                                    dataType: "json",
                                                                    async: true,
                                                                    success: function (json) {
                                                                        var hostObjs = json.findhostsformigrationresponse.host;
                                                                        var items = [];
                                                                        $(hostObjs).each(function () {
                                                                            items.push({
                                                                                id: this.id,
                                                                                description: (this.name + " (" + (this.suitableformigration ? "Suitable" : "Not Suitable") + ")")
                                                                            });
                                                                        });
                                                                        args.response.success({
                                                                            data: items
                                                                        });
                                                                    }
                                                                });
                                                            },
                                                            error: function (XMLHttpResponse) {
                                                                var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
                                                                args.response.error(errorMsg);
                                                            }
                                                        }
                                                    }
                                                },
                                                messages: {
                                                    notification: function (args) {
                                                        return 'label.migrate.lb.vm';
                                                    }
                                                },
                                                action: function (args) {
                                                    $.ajax({
                                                        url: createURL("migrateSystemVm&hostid=" + args.data.hostId + "&virtualmachineid=" + args.context.internallbinstances[0].id),
                                                        dataType: "json",
                                                        async: true,
                                                        success: function (json) {
                                                            var jid = json.migratesystemvmresponse.jobid;
                                                            args.response.success({
                                                                _custom: {
                                                                    jobId: jid,
                                                                    getUpdatedItem: function (json) {
                                                                        //return json.queryasyncjobresultresponse.jobresult.systemvminstance;    //not all properties returned in systemvminstance
                                                                        $.ajax({
                                                                            url: createURL("listInternalLoadBalancerVMs&id=" + json.queryasyncjobresultresponse.jobresult.systemvm.id),
                                                                            dataType: "json",
                                                                            async: false,
                                                                            success: function (json) {
                                                                                var items = json.listinternallbvmssresponse.internalloadbalancervm;
                                                                                if (items != null && items.length > 0) {
                                                                                    return items[0];
                                                                                }
                                                                            }
                                                                        });
                                                                    },
                                                                    getActionFilter: function () {
                                                                        return internallbinstanceActionfilter;
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

                                            viewConsole: {
                                                label: 'label.view.console',
                                                action: {
                                                    externalLink: {
                                                        url: function (args) {
                                                            return clientConsoleUrl + '?cmd=access&vm=' + args.context.internallbinstances[0].id;
                                                        },
                                                        title: function (args) {
                                                            return args.context.internallbinstances[0].id.substr(0, 8);
                                                            //title in window.open() can't have space nor longer than 8 characters. Otherwise, IE browser will have error.
                                                        },
                                                        width: 820,
                                                        height: 640
                                                    }
                                                }
                                            }
                                        },
                                        tabs: {
                                            details: {
                                                title: 'label.details',
                                                preFilter: function (args) {
                                                    var hiddenFields = [];
                                                    if (!args.context.internallbinstances[0].project) {
                                                        hiddenFields.push('project');
                                                        hiddenFields.push('projectid');
                                                    }
                                                    if (selectedZoneObj.networktype == 'Basic') {
                                                        hiddenFields.push('publicip');
                                                        //In Basic zone, guest IP is public IP. So, publicip is not returned by listRouters API. Only guestipaddress is returned by listRouters API.
                                                    }

                                                    if ('routers' in args.context && args.context.routers[0].vpcid != undefined) {
                                                        hiddenFields.push('guestnetworkid');
                                                        hiddenFields.push('guestnetworkname');
                                                    } else if ('routers' in args.context && args.context.routers[0].guestnetworkid != undefined) {
                                                        hiddenFields.push('vpcid');
                                                        hiddenFields.push('vpcname');
                                                    }

                                                    return hiddenFields;
                                                },
                                                fields: [{
                                                    name: {
                                                        label: 'label.name'
                                                    },
                                                    project: {
                                                        label: 'label.project'
                                                    }
                                                },
                                                    {
                                                        id: {
                                                            label: 'label.id'
                                                        },
                                                        projectid: {
                                                            label: 'label.project.id'
                                                        },
                                                        state: {
                                                            label: 'label.state'
                                                        },
                                                        guestnetworkid: {
                                                            label: 'label.network.id'
                                                        },
                                                        guestnetworkname: {
                                                            label: 'label.network.name'
                                                        },
                                                        vpcid: {
                                                            label: 'label.vpc.id'
                                                        },
                                                        vpcname: {
                                                            label: 'label.vpc'
                                                        },
                                                        publicip: {
                                                            label: 'label.public.ip'
                                                        },
                                                        guestipaddress: {
                                                            label: 'label.guest.ip'
                                                        },
                                                        linklocalip: {
                                                            label: 'label.linklocal.ip'
                                                        },
                                                        hostname: {
                                                            label: 'label.host'
                                                        },
                                                        serviceofferingname: {
                                                            label: 'label.compute.offering'
                                                        },
                                                        networkdomain: {
                                                            label: 'label.network.domain'
                                                        },
                                                        domain: {
                                                            label: 'label.domain'
                                                        },
                                                        account: {
                                                            label: 'label.account'
                                                        },
                                                        created: {
                                                            label: 'label.created',
                                                            converter: cloudStack.converters.toLocalDate
                                                        },
                                                        isredundantrouter: {
                                                            label: 'label.redundant.router',
                                                            converter: cloudStack.converters.toBooleanText
                                                        },
                                                        redundantRouterState: {
                                                            label: 'label.redundant.state'
                                                        }
                                                    }],
                                                dataProvider: function (args) {
                                                    $.ajax({
                                                        url: createURL("listInternalLoadBalancerVMs&id=" + args.context.internallbinstances[0].id),
                                                        dataType: 'json',
                                                        async: true,
                                                        success: function (json) {
                                                            var jsonObj = json.listinternallbvmssresponse.internalloadbalancervm[0];
                                                            addExtraPropertiesToRouterInstanceObject(jsonObj);
                                                            args.response.success({
                                                                actionFilter: internallbinstanceActionfilter,
                                                                data: jsonObj
                                                            });
                                                        }
                                                    });
                                                }
                                            },
                                            nics: {
                                                title: 'label.nics',
                                                multiple: true,
                                                fields: [{
                                                    name: {
                                                        label: 'label.name',
                                                        header: true
                                                    },
                                                    type: {
                                                        label: 'label.type'
                                                    },
                                                    traffictype: {
                                                        label: 'label.traffic.type'
                                                    },
                                                    networkname: {
                                                        label: 'label.network.name'
                                                    },
                                                    netmask: {
                                                        label: 'label.netmask'
                                                    },
                                                    ipaddress: {
                                                        label: 'label.ip.address'
                                                    },
                                                    id: {
                                                        label: 'label.id'
                                                    },
                                                    networkid: {
                                                        label: 'label.network.id'
                                                    },
                                                    isolationuri: {
                                                        label: 'label.isolation.uri'
                                                    },
                                                    broadcasturi: {
                                                        label: 'label.broadcast.uri'
                                                    }
                                                }],
                                                dataProvider: function (args) {
                                                    $.ajax({
                                                        url: createURL("listInternalLoadBalancerVMs&id=" + args.context.internallbinstances[0].id),
                                                        dataType: 'json',
                                                        async: true,
                                                        success: function (json) {
                                                            var jsonObj = json.listinternallbvmssresponse.internalloadbalancervm[0].nic;

                                                            args.response.success({
                                                                actionFilter: internallbinstanceActionfilter,
                                                                data: $.map(jsonObj, function (nic, index) {
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
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        actions: {
                            enable: {
                                label: 'label.enable.provider',
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("updateNetworkServiceProvider&id=" + nspMap["InternalLbVm"].id + "&state=Enabled"),
                                        dataType: "json",
                                        success: function (json) {
                                            var jid = json.updatenetworkserviceproviderresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getUpdatedItem: function (json) {
                                                        $(window).trigger('cloudStack.fullRefresh');
                                                    }
                                                }
                                            });
                                        }
                                    });
                                },
                                messages: {
                                    confirm: function (args) {
                                        return 'message.confirm.enable.provider';
                                    },
                                    notification: function () {
                                        return 'label.enable.provider';
                                    }
                                },
                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            },
                            disable: {
                                label: 'label.disable.provider',
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("updateNetworkServiceProvider&id=" + nspMap["InternalLbVm"].id + "&state=Disabled"),
                                        dataType: "json",
                                        success: function (json) {
                                            var jid = json.updatenetworkserviceproviderresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getUpdatedItem: function (json) {
                                                        $(window).trigger('cloudStack.fullRefresh');
                                                    }
                                                }
                                            });
                                        }
                                    });
                                },
                                messages: {
                                    confirm: function (args) {
                                        return 'message.confirm.disable.provider';
                                    },
                                    notification: function () {
                                        return 'label.disable.provider';
                                    }
                                },
                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            }
                        }
                    },

                    vpcVirtualRouter: {
                        id: 'vpcVirtualRouterProviders',
                        label: 'label.vpc.virtual.router',
                        isMaximized: true,
                        type: 'detailView',
                        fields: {
                            name: {
                                label: 'label.name'
                            },
                            ipaddress: {
                                label: 'label.ip.address'
                            },
                            state: {
                                label: 'label.status',
                                indicator: {
                                    'Enabled': 'on'
                                }
                            }
                        },
                        tabs: {
                            network: {
                                title: 'label.network',
                                fields: [{
                                    name: {
                                        label: 'label.name'
                                    }
                                },
                                    {
                                        id: {
                                            label: 'label.id'
                                        },
                                        state: {
                                            label: 'label.state'
                                        },
                                        physicalnetworkid: {
                                            label: 'label.physical.network.ID'
                                        },
                                        destinationphysicalnetworkid: {
                                            label: 'label.destination.physical.network.id'
                                        },
                                        supportedServices: {
                                            label: 'label.supported.services'
                                        }
                                    }],
                                dataProvider: function (args) {
                                    refreshNspData("VpcVirtualRouter");
                                    args.response.success({
                                        actionFilter: virtualRouterProviderActionFilter,
                                        data: $.extend(nspMap["vpcVirtualRouter"], {
                                            supportedServices: nspMap["vpcVirtualRouter"].servicelist.join(', ')
                                        })
                                    });
                                }
                            },

                            instances: {
                                title: 'label.instances',
                                listView: {
                                    label: 'label.virtual.appliances',
                                    id: 'routers',
                                    fields: {
                                        name: {
                                            label: 'label.name'
                                        },
                                        zonename: {
                                            label: 'label.zone'
                                        },
                                        routerType: {
                                            label: 'label.type'
                                        },
                                        state: {
                                            converter: function (str) {
                                                // For localization
                                                return str;
                                            },
                                            label: 'label.status',
                                            indicator: {
                                                'Running': 'on',
                                                'Stopped': 'off',
                                                'Error': 'off'
                                            }
                                        }
                                    },
                                    dataProvider: function (args) {
                                        var array1 = [];
                                        if (args.filterBy != null) {
                                            if (args.filterBy.search != null && args.filterBy.search.by != null && args.filterBy.search.value != null) {
                                                switch (args.filterBy.search.by) {
                                                    case "name":
                                                        if (args.filterBy.search.value.length > 0)
                                                            array1.push("&keyword=" + args.filterBy.search.value);
                                                        break;
                                                }
                                            }
                                        }

                                        var data2 = {
                                            forvpc: true
                                        };
                                        var routers = [];
                                        $.ajax({
                                            url: createURL("listRouters&zoneid=" + selectedZoneObj.id + "&listAll=true&page=" + args.page + "&pagesize=" + pageSize + array1.join("")),
                                            dataType: 'json',
                                            data: data2,
                                            async: true,
                                            success: function (json) {
                                                var items = json.listroutersresponse.router;
                                                $(items).map(function (index, item) {
                                                    routers.push(item);
                                                });

                                                /*
                                                 * In project view, the first listRotuers API(without projectid=-1) will return the same objects as the second listRouters API(with projectid=-1),
                                                 * because in project view, all API calls are appended with projectid=[projectID].
                                                 * Therefore, we only call the second listRouters API(with projectid=-1) in non-project view.
                                                 */
                                                if (cloudStack.context && cloudStack.context.projects == null) { //non-project view
                                                    $.ajax({
                                                        url: createURL("listRouters&zoneid=" + selectedZoneObj.id + "&listAll=true&page=" + args.page + "&pagesize=" + pageSize + array1.join("") + "&projectid=-1"),
                                                        dataType: 'json',
                                                        data: data2,
                                                        async: false,
                                                        success: function (json) {
                                                            var items = json.listroutersresponse.router;
                                                            $(items).map(function (index, item) {
                                                                routers.push(item);
                                                            });
                                                        }
                                                    });
                                                }

                                                args.response.success({
                                                    actionFilter: routerActionfilter,
                                                    data: $(routers).map(mapRouterType)
                                                });
                                            }
                                        });
                                    },
                                    detailView: {
                                        name: 'label.virtual.appliance.details',
                                        actions: {
                                            start: {
                                                label: 'label.action.start.router',
                                                messages: {
                                                    confirm: function (args) {
                                                        return 'message.action.start.router';
                                                    },
                                                    notification: function (args) {
                                                        return 'label.action.start.router';
                                                    }
                                                },
                                                action: function (args) {
                                                    $.ajax({
                                                        url: createURL('startRouter&id=' + args.context.routers[0].id),
                                                        dataType: 'json',
                                                        async: true,
                                                        success: function (json) {
                                                            var jid = json.startrouterresponse.jobid;
                                                            args.response.success({
                                                                _custom: {
                                                                    jobId: jid,
                                                                    getUpdatedItem: function (json) {
                                                                        return json.queryasyncjobresultresponse.jobresult.router;
                                                                    },
                                                                    getActionFilter: function () {
                                                                        return routerActionfilter;
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

                                            stop: {
                                                label: 'label.action.stop.router',
                                                createForm: {
                                                    title: 'label.action.stop.router',
                                                    desc: 'message.action.stop.router',
                                                    fields: {
                                                        forced: {
                                                            label: 'force.stop',
                                                            isBoolean: true,
                                                            isChecked: false
                                                        }
                                                    }
                                                },
                                                messages: {
                                                    notification: function (args) {
                                                        return 'label.action.stop.router';
                                                    }
                                                },
                                                action: function (args) {
                                                    var array1 = [];
                                                    array1.push("&forced=" + (args.data.forced == "on"));
                                                    $.ajax({
                                                        url: createURL('stopRouter&id=' + args.context.routers[0].id + array1.join("")),
                                                        dataType: 'json',
                                                        async: true,
                                                        success: function (json) {
                                                            var jid = json.stoprouterresponse.jobid;
                                                            args.response.success({
                                                                _custom: {
                                                                    jobId: jid,
                                                                    getUpdatedItem: function (json) {
                                                                        return json.queryasyncjobresultresponse.jobresult.router;
                                                                    },
                                                                    getActionFilter: function () {
                                                                        return routerActionfilter;
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

                                            restart: {
                                                label: 'label.action.reboot.router',
                                                messages: {
                                                    confirm: function (args) {
                                                        return 'message.action.reboot.router';
                                                    },
                                                    notification: function (args) {
                                                        return 'label.action.reboot.router';
                                                    }
                                                },
                                                action: function (args) {
                                                    $.ajax({
                                                        url: createURL('rebootRouter&id=' + args.context.routers[0].id),
                                                        dataType: 'json',
                                                        async: true,
                                                        success: function (json) {
                                                            var jid = json.rebootrouterresponse.jobid;
                                                            args.response.success({
                                                                _custom: {
                                                                    jobId: jid,
                                                                    getUpdatedItem: function (json) {
                                                                        return json.queryasyncjobresultresponse.jobresult.router;
                                                                    },
                                                                    getActionFilter: function () {
                                                                        return routerActionfilter;
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

                                            'remove': {
                                                label: 'label.destroy.router',
                                                messages: {
                                                    confirm: function (args) {
                                                        return 'message.confirm.destroy.router';
                                                    },
                                                    notification: function (args) {
                                                        return 'label.destroy.router';
                                                    }
                                                },
                                                action: function (args) {
                                                    $.ajax({
                                                        url: createURL("destroyRouter&id=" + args.context.routers[0].id),
                                                        dataType: "json",
                                                        async: true,
                                                        success: function (json) {
                                                            var jid = json.destroyrouterresponse.jobid;
                                                            args.response.success({
                                                                _custom: {
                                                                    jobId: jid
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
                                                label: 'label.action.migrate.router',
                                                createForm: {
                                                    title: 'label.action.migrate.router',
                                                    desc: '',
                                                    fields: {
                                                        hostId: {
                                                            label: 'label.host',
                                                            validation: {
                                                                required: true
                                                            },
                                                            select: function (args) {
                                                                $.ajax({
                                                                    url: createURL("findHostsForMigration&VirtualMachineId=" + args.context.routers[0].id),
                                                                    dataType: "json",
                                                                    async: true,
                                                                    success: function (json) {
                                                                        var hostObjs = json.findhostsformigrationresponse.host;
                                                                        var items = [];
                                                                        $(hostObjs).each(function () {
                                                                            items.push({
                                                                                id: this.id,
                                                                                description: (this.name + " (" + (this.suitableformigration ? "Suitable" : "Not Suitable") + ")")
                                                                            });
                                                                        });
                                                                        args.response.success({
                                                                            data: items
                                                                        });
                                                                    }
                                                                });
                                                            },
                                                            error: function (XMLHttpResponse) {
                                                                var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
                                                                args.response.error(errorMsg);
                                                            }
                                                        }
                                                    }
                                                },
                                                messages: {
                                                    notification: function (args) {
                                                        return 'label.action.migrate.router';
                                                    }
                                                },
                                                action: function (args) {
                                                    $.ajax({
                                                        url: createURL("migrateSystemVm&hostid=" + args.data.hostId + "&virtualmachineid=" + args.context.routers[0].id),
                                                        dataType: "json",
                                                        async: true,
                                                        success: function (json) {
                                                            var jid = json.migratesystemvmresponse.jobid;
                                                            args.response.success({
                                                                _custom: {
                                                                    jobId: jid,
                                                                    getUpdatedItem: function (json) {
                                                                        //return json.queryasyncjobresultresponse.jobresult.systemvminstance;    //not all properties returned in systemvminstance
                                                                        $.ajax({
                                                                            url: createURL("listRouters&id=" + json.queryasyncjobresultresponse.jobresult.systemvm.id),
                                                                            dataType: "json",
                                                                            async: false,
                                                                            success: function (json) {
                                                                                var items = json.listroutersresponse.router;
                                                                                if (items != null && items.length > 0) {
                                                                                    return items[0];
                                                                                }
                                                                            }
                                                                        });
                                                                    },
                                                                    getActionFilter: function () {
                                                                        return routerActionfilter;
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

                                            viewConsole: {
                                                label: 'label.view.console',
                                                action: {
                                                    externalLink: {
                                                        url: function (args) {
                                                            return clientConsoleUrl + '?cmd=access&vm=' + args.context.routers[0].id;
                                                        },
                                                        title: function (args) {
                                                            return args.context.routers[0].id.substr(0, 8);
                                                            //title in window.open() can't have space nor longer than 8 characters. Otherwise, IE browser will have error.
                                                        },
                                                        width: 820,
                                                        height: 640
                                                    }
                                                }
                                            }
                                        },
                                        tabs: {
                                            details: {
                                                title: 'label.details',
                                                preFilter: function (args) {
                                                    var hiddenFields = [];
                                                    if (!args.context.routers[0].project) {
                                                        hiddenFields.push('project');
                                                        hiddenFields.push('projectid');
                                                    }
                                                    if (selectedZoneObj.networktype == 'Basic') {
                                                        hiddenFields.push('publicip');
                                                        //In Basic zone, guest IP is public IP. So, publicip is not returned by listRouters API. Only guestipaddress is returned by listRouters API.
                                                    }
                                                    return hiddenFields;
                                                },
                                                fields: [{
                                                    name: {
                                                        label: 'label.name'
                                                    },
                                                    project: {
                                                        label: 'label.project'
                                                    }
                                                },
                                                    {
                                                        id: {
                                                            label: 'label.id'
                                                        },
                                                        projectid: {
                                                            label: 'label.project.id'
                                                        },
                                                        state: {
                                                            label: 'label.state'
                                                        },
                                                        publicip: {
                                                            label: 'label.public.ip'
                                                        },
                                                        guestipaddress: {
                                                            label: 'label.guest.ip'
                                                        },
                                                        linklocalip: {
                                                            label: 'label.linklocal.ip'
                                                        },
                                                        hostname: {
                                                            label: 'label.host'
                                                        },
                                                        serviceofferingname: {
                                                            label: 'label.compute.offering'
                                                        },
                                                        networkdomain: {
                                                            label: 'label.network.domain'
                                                        },
                                                        domain: {
                                                            label: 'label.domain'
                                                        },
                                                        account: {
                                                            label: 'label.account'
                                                        },
                                                        created: {
                                                            label: 'label.created',
                                                            converter: cloudStack.converters.toLocalDate
                                                        },
                                                        isredundantrouter: {
                                                            label: 'label.redundant.router',
                                                            converter: cloudStack.converters.toBooleanText
                                                        },
                                                        redundantRouterState: {
                                                            label: 'label.redundant.state'
                                                        },
                                                        vpcid: {
                                                            label: 'label.vpc.id'
                                                        }
                                                    }],
                                                dataProvider: function (args) {
                                                    $.ajax({
                                                        url: createURL("listRouters&id=" + args.context.routers[0].id),
                                                        dataType: 'json',
                                                        async: true,
                                                        success: function (json) {
                                                            var jsonObj = json.listroutersresponse.router[0];
                                                            addExtraPropertiesToRouterInstanceObject(jsonObj);
                                                            args.response.success({
                                                                actionFilter: routerActionfilter,
                                                                data: jsonObj
                                                            });
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        actions: {
                            enable: {
                                label: 'label.enable.provider',
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("updateNetworkServiceProvider&id=" + nspMap["vpcVirtualRouter"].id + "&state=Enabled"),
                                        dataType: "json",
                                        success: function (json) {
                                            var jid = json.updatenetworkserviceproviderresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getUpdatedItem: function (json) {
                                                        $(window).trigger('cloudStack.fullRefresh');
                                                    }
                                                }
                                            });
                                        }
                                    });
                                },
                                messages: {
                                    confirm: function (args) {
                                        return 'message.confirm.enable.provider';
                                    },
                                    notification: function () {
                                        return 'label.enable.provider';
                                    }
                                },
                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            },
                            disable: {
                                label: 'label.disable.provider',
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("updateNetworkServiceProvider&id=" + nspMap["vpcVirtualRouter"].id + "&state=Disabled"),
                                        dataType: "json",
                                        success: function (json) {
                                            var jid = json.updatenetworkserviceproviderresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getUpdatedItem: function (json) {
                                                        $(window).trigger('cloudStack.fullRefresh');
                                                    }
                                                }
                                            });
                                        }
                                    });
                                },
                                messages: {
                                    confirm: function (args) {
                                        return 'message.confirm.disable.provider';
                                    },
                                    notification: function () {
                                        return 'label.disable.provider';
                                    }
                                },
                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            }
                        }
                    },

                    // SRX provider detailView
                    srx: {
                        type: 'detailView',
                        id: 'srxProvider',
                        label: 'label.srx',
                        viewAll: {
                            label: 'label.devices',
                            path: '_zone.srxDevices'
                        },
                        tabs: {
                            details: {
                                title: 'label.details',
                                fields: [{
                                    name: {
                                        label: 'label.name'
                                    }
                                },
                                    {
                                        state: {
                                            label: 'label.state'
                                        }
                                    }],
                                dataProvider: function (args) {
                                    refreshNspData("JuniperSRX");
                                    var providerObj;
                                    $(nspHardcodingArray).each(function () {
                                        if (this.id == "srx") {
                                            providerObj = this;
                                            return false; //break each loop
                                        }
                                    });
                                    args.response.success({
                                        data: providerObj,
                                        actionFilter: networkProviderActionFilter('srx')
                                    });
                                }
                            }
                        },
                        actions: {
                            add: {
                                label: 'label.add.SRX.device',
                                createForm: {
                                    title: 'label.add.SRX.device',
                                    fields: {
                                        ip: {
                                            label: 'label.ip.address',
                                            docID: 'helpSRXIPAddress'
                                        },
                                        username: {
                                            label: 'label.username',
                                            docID: 'helpSRXUsername'
                                        },
                                        password: {
                                            label: 'label.password',
                                            isPassword: true,
                                            docID: 'helpSRXPassword'
                                        },
                                        networkdevicetype: {
                                            label: 'label.type',
                                            docID: 'helpSRXType',
                                            select: function (args) {
                                                var items = [];
                                                items.push({
                                                    id: "JuniperSRXFirewall",
                                                    description: "Juniper SRX Firewall"
                                                });
                                                args.response.success({
                                                    data: items
                                                });
                                            }
                                        },
                                        publicinterface: {
                                            label: 'label.public.interface',
                                            docID: 'helpSRXPublicInterface'
                                        },
                                        privateinterface: {
                                            label: 'label.private.interface',
                                            docID: 'helpSRXPrivateInterface'
                                        },
                                        usageinterface: {
                                            label: 'label.usage.interface',
                                            docID: 'helpSRXUsageInterface'
                                        },
                                        numretries: {
                                            label: 'label.numretries',
                                            defaultValue: '2',
                                            docID: 'helpSRXRetries'
                                        },
                                        timeout: {
                                            label: 'label.timeout',
                                            defaultValue: '300',
                                            docID: 'helpSRXTimeout'
                                        },
                                        // inline: {
                                        //   label: 'Mode',
                                        //   docID: 'helpSRXMode',
                                        //   select: function(args) {
                                        //     var items = [];
                                        //     items.push({id: "false", description: "side by side"});
                                        //     items.push({id: "true", description: "inline"});
                                        //     args.response.success({data: items});
                                        //   }
                                        // },
                                        publicnetwork: {
                                            label: 'label.public.network',
                                            defaultValue: 'untrusted',
                                            docID: 'helpSRXPublicNetwork',
                                            isDisabled: true
                                        },
                                        privatenetwork: {
                                            label: 'label.private.network',
                                            defaultValue: 'trusted',
                                            docID: 'helpSRXPrivateNetwork',
                                            isDisabled: true
                                        },
                                        capacity: {
                                            label: 'label.capacity',
                                            validation: {
                                                required: false,
                                                number: true
                                            },
                                            docID: 'helpSRXCapacity'
                                        },
                                        dedicated: {
                                            label: 'label.dedicated',
                                            isBoolean: true,
                                            isChecked: false,
                                            docID: 'helpSRXDedicated'
                                        }
                                    }
                                },
                                action: function (args) {
                                    if (nspMap["srx"] == null) {
                                        $.ajax({
                                            url: createURL("addNetworkServiceProvider&name=JuniperSRX&physicalnetworkid=" + selectedPhysicalNetworkObj.id),
                                            dataType: "json",
                                            async: true,
                                            success: function (json) {
                                                var jobId = json.addnetworkserviceproviderresponse.jobid;
                                                var addJuniperSRXProviderIntervalID = setInterval(function () {
                                                        $.ajax({
                                                            url: createURL("queryAsyncJobResult&jobId=" + jobId),
                                                            dataType: "json",
                                                            success: function (json) {
                                                                var result = json.queryasyncjobresultresponse;
                                                                if (result.jobstatus == 0) {
                                                                    return; //Job has not completed
                                                                } else {
                                                                    clearInterval(addJuniperSRXProviderIntervalID);
                                                                    if (result.jobstatus == 1) {
                                                                        nspMap["srx"] = json.queryasyncjobresultresponse.jobresult.networkserviceprovider;
                                                                        addExternalFirewall(args, selectedPhysicalNetworkObj, "addSrxFirewall", "addsrxfirewallresponse", "srxfirewall");
                                                                    } else if (result.jobstatus == 2) {
                                                                        alert("addNetworkServiceProvider&name=JuniperSRX failed. Error: " + _s(result.jobresult.errortext));
                                                                    }
                                                                }
                                                            },
                                                            error: function (XMLHttpResponse) {
                                                                var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
                                                                alert("addNetworkServiceProvider&name=JuniperSRX failed. Error: " + errorMsg);
                                                            }
                                                        });
                                                    },
                                                    g_queryAsyncJobResultInterval);
                                            }
                                        });
                                    } else {
                                        addExternalFirewall(args, selectedPhysicalNetworkObj, "addSrxFirewall", "addsrxfirewallresponse", "srxfirewall");
                                    }
                                },
                                messages: {
                                    notification: function (args) {
                                        return 'label.add.SRX.device';
                                    }
                                },
                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            },
                            enable: {
                                label: 'label.enable.provider',
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("updateNetworkServiceProvider&id=" + nspMap["srx"].id + "&state=Enabled"),
                                        dataType: "json",
                                        success: function (json) {
                                            var jid = json.updatenetworkserviceproviderresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getUpdatedItem: function (json) {
                                                        $(window).trigger('cloudStack.fullRefresh');
                                                    }
                                                }
                                            });
                                        }
                                    });
                                },
                                messages: {
                                    confirm: function (args) {
                                        return 'message.confirm.enable.provider';
                                    },
                                    notification: function () {
                                        return 'label.enable.provider';
                                    }
                                },
                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            },
                            disable: {
                                label: 'label.disable.provider',
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("updateNetworkServiceProvider&id=" + nspMap["srx"].id + "&state=Disabled"),
                                        dataType: "json",
                                        success: function (json) {
                                            var jid = json.updatenetworkserviceproviderresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getUpdatedItem: function (json) {
                                                        $(window).trigger('cloudStack.fullRefresh');
                                                    }
                                                }
                                            });
                                        }
                                    });
                                },
                                messages: {
                                    confirm: function (args) {
                                        return 'message.confirm.disable.provider';
                                    },
                                    notification: function () {
                                        return 'label.disable.provider';
                                    }
                                },
                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            },
                            destroy: {
                                label: 'label.shutdown.provider',
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("deleteNetworkServiceProvider&id=" + nspMap["srx"].id),
                                        dataType: "json",
                                        success: function (json) {
                                            var jid = json.deletenetworkserviceproviderresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid
                                                }
                                            });

                                            $(window).trigger('cloudStack.fullRefresh');
                                        }
                                    });
                                },
                                messages: {
                                    confirm: function (args) {
                                        return 'message.confirm.shutdown.provider';
                                    },
                                    notification: function (args) {
                                        return 'label.shutdown.provider';
                                    }
                                },
                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            }
                        }
                    },

                    // Security groups detail view
                    securityGroups: {
                        id: 'securityGroup-providers',
                        label: 'label.menu.security.groups',
                        type: 'detailView',
                        viewAll: {
                            label: 'label.rules',
                            path: 'network.securityGroups'
                        },
                        tabs: {
                            details: {
                                title: 'label.details',
                                fields: [{
                                    name: {
                                        label: 'label.name'
                                    }
                                },
                                    {
                                        state: {
                                            label: 'label.state'
                                        }
                                    }],
                                dataProvider: function (args) {
                                    refreshNspData("SecurityGroupProvider");
                                    var providerObj;
                                    $(nspHardcodingArray).each(function () {
                                        if (this.id == "securityGroups") {
                                            providerObj = this;
                                            return false; //break each loop
                                        }
                                    });
                                    args.response.success({
                                        actionFilter: function (args) {
                                            var allowedActions = [];
                                            var jsonObj = providerObj;
                                            if (jsonObj.state == "Enabled")
                                                allowedActions.push("disable"); else if (jsonObj.state == "Disabled")
                                                allowedActions.push("enable");
                                            return allowedActions;
                                        },
                                        data: providerObj
                                    });
                                }
                            }
                        },
                        actions: {
                            enable: {
                                label: 'label.enable.provider',
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("updateNetworkServiceProvider&id=" + nspMap["securityGroups"].id + "&state=Enabled"),
                                        async: true,
                                        success: function (json) {
                                            var jid = json.updatenetworkserviceproviderresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getUpdatedItem: function (json) {
                                                        $(window).trigger('cloudStack.fullRefresh');
                                                    }
                                                }
                                            });
                                        }
                                    });
                                },
                                messages: {
                                    confirm: function (args) {
                                        return 'message.confirm.enable.provider';
                                    },
                                    notification: function () {
                                        return 'label.enable.provider';
                                    }
                                },
                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            },
                            disable: {
                                label: 'label.disable.provider',
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("updateNetworkServiceProvider&id=" + nspMap["securityGroups"].id + "&state=Disabled"),
                                        dataType: "json",
                                        success: function (json) {
                                            var jid = json.updatenetworkserviceproviderresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getUpdatedItem: function (json) {
                                                        $(window).trigger('cloudStack.fullRefresh');
                                                    }
                                                }
                                            });
                                        }
                                    });
                                },
                                messages: {
                                    confirm: function (args) {
                                        return 'message.confirm.disable.provider';
                                    },
                                    notification: function () {
                                        return 'label.disable.provider';
                                    }
                                },
                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            }
                        },

                        fields: {
                            id: {
                                label: 'label.id'
                            },
                            name: {
                                label: 'label.name'
                            }
                            //,
                            //state: { label: 'label.status' } //comment it for now, since dataProvider below doesn't get called by widget code after action is done
                        }
                    },
                    // Nicira Nvp provider detail view
                    niciraNvp: {
                        type: 'detailView',
                        id: 'niciraNvpProvider',
                        label: 'label.niciraNvp',
                        viewAll: {
                            label: 'label.devices',
                            path: '_zone.niciraNvpDevices'
                        },
                        tabs: {
                            details: {
                                title: 'label.details',
                                fields: [{
                                    name: {
                                        label: 'label.name'
                                    }
                                },
                                    {
                                        state: {
                                            label: 'label.state'
                                        }
                                    }],
                                dataProvider: function (args) {
                                    refreshNspData("NiciraNvp");
                                    var providerObj;
                                    $(nspHardcodingArray).each(function () {
                                        if (this.id == "niciraNvp") {
                                            providerObj = this;
                                            return false; //break each loop
                                        }
                                    });
                                    args.response.success({
                                        data: providerObj,
                                        actionFilter: networkProviderActionFilter('niciraNvp')
                                    });
                                }
                            }
                        },
                        actions: {
                            add: {
                                label: 'label.add.NiciraNvp.device',
                                createForm: {
                                    title: 'label.add.NiciraNvp.device',
                                    preFilter: function (args) {
                                    },
                                    // TODO What is this?
                                    fields: {
                                        host: {
                                            label: 'label.ip.address'
                                        },
                                        username: {
                                            label: 'label.username'
                                        },
                                        password: {
                                            label: 'label.password',
                                            isPassword: true
                                        },
                                        numretries: {
                                            label: 'label.numretries',
                                            defaultValue: '2'
                                        },
                                        transportzoneuuid: {
                                            label: 'label.nicira.transportzoneuuid'
                                        },
                                        l3gatewayserviceuuid: {
                                            label: 'label.nicira.l3gatewayserviceuuid'
                                        },
                                        l2gatewayserviceuuid: {
                                            label: 'label.nicira.l2gatewayserviceuuid'
                                        }
                                    }
                                },
                                action: function (args) {
                                    if (nspMap["niciraNvp"] == null) {
                                        $.ajax({
                                            url: createURL("addNetworkServiceProvider&name=NiciraNvp&physicalnetworkid=" + selectedPhysicalNetworkObj.id),
                                            dataType: "json",
                                            async: true,
                                            success: function (json) {
                                                var jobId = json.addnetworkserviceproviderresponse.jobid;
                                                var addNiciraNvpProviderIntervalID = setInterval(function () {
                                                        $.ajax({
                                                            url: createURL("queryAsyncJobResult&jobId=" + jobId),
                                                            dataType: "json",
                                                            success: function (json) {
                                                                var result = json.queryasyncjobresultresponse;
                                                                if (result.jobstatus == 0) {
                                                                    return; //Job has not completed
                                                                } else {
                                                                    clearInterval(addNiciraNvpProviderIntervalID);
                                                                    if (result.jobstatus == 1) {
                                                                        nspMap["niciraNvp"] = json.queryasyncjobresultresponse.jobresult.networkserviceprovider;
                                                                        addNiciraNvpDevice(args, selectedPhysicalNetworkObj, "addNiciraNvpDevice", "addniciranvpdeviceresponse", "niciranvpdevice")
                                                                    } else if (result.jobstatus == 2) {
                                                                        alert("addNetworkServiceProvider&name=NiciraNvp failed. Error: " + _s(result.jobresult.errortext));
                                                                    }
                                                                }
                                                            },
                                                            error: function (XMLHttpResponse) {
                                                                var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
                                                                alert("addNetworkServiceProvider&name=NiciraNvp failed. Error: " + errorMsg);
                                                            }
                                                        });
                                                    },
                                                    g_queryAsyncJobResultInterval);
                                            }
                                        });
                                    } else {
                                        addNiciraNvpDevice(args, selectedPhysicalNetworkObj, "addNiciraNvpDevice", "addniciranvpdeviceresponse", "niciranvpdevice")
                                    }
                                },
                                messages: {
                                    notification: function (args) {
                                        return 'label.add.NiciraNvp.device';
                                    }
                                },
                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            },
                            enable: {
                                label: 'label.enable.provider',
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("updateNetworkServiceProvider&id=" + nspMap["niciraNvp"].id + "&state=Enabled"),
                                        dataType: "json",
                                        success: function (json) {
                                            var jid = json.updatenetworkserviceproviderresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getUpdatedItem: function (json) {
                                                        $(window).trigger('cloudStack.fullRefresh');
                                                    }
                                                }
                                            });
                                        }
                                    });
                                },
                                messages: {
                                    confirm: function (args) {
                                        return 'message.confirm.enable.provider';
                                    },
                                    notification: function () {
                                        return 'label.enable.provider';
                                    }
                                },
                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            },
                            disable: {
                                label: 'label.disable.provider',
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("updateNetworkServiceProvider&id=" + nspMap["niciraNvp"].id + "&state=Disabled"),
                                        dataType: "json",
                                        success: function (json) {
                                            var jid = json.updatenetworkserviceproviderresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getUpdatedItem: function (json) {
                                                        $(window).trigger('cloudStack.fullRefresh');
                                                    }
                                                }
                                            });
                                        }
                                    });
                                },
                                messages: {
                                    confirm: function (args) {
                                        return 'message.confirm.disable.provider';
                                    },
                                    notification: function () {
                                        return 'label.disable.provider';
                                    }
                                },
                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            },
                            destroy: {
                                label: 'label.shutdown.provider',
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("deleteNetworkServiceProvider&id=" + nspMap["niciraNvp"].id),
                                        dataType: "json",
                                        success: function (json) {
                                            var jid = json.deletenetworkserviceproviderresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid
                                                }
                                            });

                                            $(window).trigger('cloudStack.fullRefresh');
                                        }
                                    });
                                },
                                messages: {
                                    confirm: function (args) {
                                        return 'message.confirm.shutdown.provider';
                                    },
                                    notification: function (args) {
                                        return 'label.shutdown.provider';
                                    }
                                },
                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            }
                        }
                    },
                    // MidoNet provider detailView
                    midoNet: {
                        id: 'midoNet',
                        label: 'label.midoNet',
                        isMaximized: true,
                        type: 'detailView',
                        fields: {
                            name: {
                                label: 'label.name'
                            },
                            //ipaddress: { label: 'label.ip.address' },
                            state: {
                                label: 'label.status',
                                indicator: {
                                    'Enabled': 'on'
                                }
                            }
                        },
                        tabs: {
                            details: {
                                title: 'label.network',
                                fields: [{
                                    name: {
                                        label: 'label.name'
                                    }
                                },
                                    {
                                        id: {
                                            label: 'label.id'
                                        },
                                        state: {
                                            label: 'label.state'
                                        },
                                        physicalnetworkid: {
                                            label: 'label.physical.network.ID'
                                        },
                                        destinationphysicalnetworkid: {
                                            label: 'label.destination.physical.network.id'
                                        },
                                        supportedServices: {
                                            label: 'label.supported.services'
                                        }
                                    }],
                                dataProvider: function (args) {
                                    refreshNspData("MidoNet");
                                    args.response.success({
                                        actionFilter: virtualRouterProviderActionFilter,
                                        data: $.extend(nspMap["midoNet"], {
                                            supportedServices: nspMap["midoNet"].servicelist.join(', ')
                                        })
                                    });
                                }
                            }
                        },
                        actions: {
                            enable: {
                                label: 'label.enable.provider',
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("updateNetworkServiceProvider&id=" + nspMap["midoNet"].id + "&state=Enabled"),
                                        dataType: "json",
                                        success: function (json) {
                                            var jid = json.updatenetworkserviceproviderresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getUpdatedItem: function (json) {
                                                        $(window).trigger('cloudStack.fullRefresh');
                                                    }
                                                }
                                            });
                                        }
                                    });
                                },
                                messages: {
                                    confirm: function (args) {
                                        return 'message.confirm.enable.provider';
                                    },
                                    notification: function () {
                                        return 'label.enable.provider';
                                    }
                                },
                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            },
                            disable: {
                                label: 'label.disable.provider',
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("updateNetworkServiceProvider&id=" + nspMap["midoNet"].id + "&state=Disabled"),
                                        dataType: "json",
                                        success: function (json) {
                                            var jid = json.updatenetworkserviceproviderresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getUpdatedItem: function (json) {
                                                        $(window).trigger('cloudStack.fullRefresh');
                                                    }
                                                }
                                            });
                                        }
                                    });
                                },
                                messages: {
                                    confirm: function (args) {
                                        return 'message.confirm.disable.provider';
                                    },
                                    notification: function () {
                                        return 'label.disable.provider';
                                    }
                                },
                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            }
                        }
                    }
                }
            }
        },
        physicalResourceSection: {
            sections: {
                physicalResources: {
                    type: 'select',
                    title: 'label.menu.physical.resources',
                    listView: {
                        zones: {
                            id: 'physicalResources',
                            label: 'label.menu.physical.resources',
                            fields: {
                                name: {
                                    label: 'label.zone'
                                },
                                networktype: {
                                    label: 'label.network.type'
                                },
                                domainid: {
                                    label: 'label.public',
                                    converter: function (args) {
                                        if (args == null)
                                            return "Yes"; else
                                            return "No";
                                    }
                                },
                                allocationstate: {
                                    label: 'label.allocation.state',
                                    converter: function (str) {
                                        // For localization
                                        return str;
                                    },
                                    indicator: {
                                        'Enabled': 'on',
                                        'Disabled': 'off'
                                    }
                                }
                            },

                            dataProvider: function (args) {
                                var array1 = [];
                                if (args.filterBy != null) {
                                    if (args.filterBy.search != null && args.filterBy.search.by != null && args.filterBy.search.value != null) {
                                        switch (args.filterBy.search.by) {
                                            case "name":
                                                if (args.filterBy.search.value.length > 0)
                                                    array1.push("&keyword=" + args.filterBy.search.value);
                                                break;
                                        }
                                    }
                                }
                                $.ajax({
                                    url: createURL("listZones&page=" + args.page + "&pagesize=" + pageSize + array1.join("")),
                                    dataType: "json",
                                    async: true,
                                    success: function (json) {
                                        zoneObjs = json.listzonesresponse.zone;
                                        args.response.success({
                                            actionFilter: zoneActionfilter,
                                            data: zoneObjs
                                        });
                                    }
                                });
                            },

                            actions: {
                                add: {
                                    label: 'label.add.zone',
                                    action: {
                                        custom: cloudStack.uiCustom.zoneWizard(cloudStack.zoneWizard)
                                    },
                                    messages: {
                                        notification: function (args) {
                                            return 'label.add.zone';
                                        }
                                    },
                                    notification: {
                                        poll: function (args) {
                                            args.complete({
                                                actionFilter: zoneActionfilter,
                                                data: args._custom.zone
                                            });
                                        }
                                    }
                                },
                                viewMetrics: {
                                    label: 'label.metrics',
                                    isHeader: true,
                                    addRow: false,
                                    action: {
                                        custom: cloudStack.uiCustom.metricsView({resource: 'zones'})
                                    },
                                    messages: {
                                        notification: function (args) {
                                            return 'label.metrics';
                                        }
                                    }
                                }
                            },

                            detailView: {
                                isMaximized: true,
                                actions: {
                                    enable: {
                                        label: 'label.action.enable.zone',
                                        messages: {
                                            confirm: function (args) {
                                                return 'message.action.enable.zone';
                                            },
                                            notification: function (args) {
                                                return 'label.action.enable.zone';
                                            }
                                        },
                                        action: function (args) {
                                            $.ajax({
                                                url: createURL("updateZone&id=" + args.context.physicalResources[0].id + "&allocationstate=Enabled"), //embedded objects in listView is called physicalResources while embedded objects in detailView is called zones
                                                dataType: "json",
                                                async: true,
                                                success: function (json) {
                                                    var item = json.updatezoneresponse.zone;
                                                    args.response.success({
                                                        actionFilter: zoneActionfilter,
                                                        data: item
                                                    });
                                                }
                                            });
                                        },
                                        notification: {
                                            poll: function (args) {
                                                args.complete();
                                            }
                                        }
                                    },

                                    disable: {
                                        label: 'label.action.disable.zone',
                                        messages: {
                                            confirm: function (args) {
                                                return 'message.action.disable.zone';
                                            },
                                            notification: function (args) {
                                                return 'label.action.disable.zone';
                                            }
                                        },
                                        action: function (args) {
                                            $.ajax({
                                                url: createURL("updateZone&id=" + args.context.physicalResources[0].id + "&allocationstate=Disabled"), //embedded objects in listView is called physicalResources while embedded objects in detailView is called zones
                                                dataType: "json",
                                                async: true,
                                                success: function (json) {
                                                    var item = json.updatezoneresponse.zone;
                                                    args.response.success({
                                                        actionFilter: zoneActionfilter,
                                                        data: item
                                                    });
                                                }
                                            });
                                        },
                                        notification: {
                                            poll: function (args) {
                                                args.complete();
                                            }
                                        }
                                    },

                                    dedicateZone: {
                                        label: 'label.dedicate.zone',
                                        messages: {
                                            confirm: function (args) {
                                                return 'message.confirm.dedicate.zone';
                                            },
                                            notification: function (args) {
                                                return 'label.zone.dedicated';
                                            }
                                        },
                                        createForm: {
                                            title: 'label.dedicate.zone',
                                            fields: {
                                                domainId: {
                                                    label: 'label.domain',
                                                    validation: {
                                                        required: true
                                                    },
                                                    select: function (args) {
                                                        $.ajax({
                                                            url: createURL("listDomains&listAll=true"),
                                                            dataType: "json",
                                                            async: false,
                                                            success: function (json) {
                                                                var domainObjs = json.listdomainsresponse.domain;
                                                                var items = [];

                                                                $(domainObjs).each(function () {
                                                                    items.push({
                                                                        id: this.id,
                                                                        description: this.name
                                                                    });
                                                                });
                                                                items.sort(function (a, b) {
                                                                    return a.description.localeCompare(b.description);
                                                                });

                                                                args.response.success({
                                                                    data: items
                                                                });
                                                            }
                                                        });
                                                    }
                                                },
                                                accountId: {
                                                    label: 'label.account',
                                                    docID: 'helpAccountForDedication',
                                                    validation: {
                                                        required: false
                                                    }
                                                }
                                            }
                                        },
                                        action: function (args) {
                                            //EXPLICIT DEDICATION
                                            var array2 = [];
                                            if (args.data.accountId != "")
                                                array2.push("&account=" + todb(args.data.accountId));

                                            $.ajax({
                                                url: createURL("dedicateZone&zoneId=" +
                                                    args.context.physicalResources[0].id +
                                                    "&domainId=" + args.data.domainId + array2.join("")),
                                                dataType: "json",
                                                success: function (json) {
                                                    var jid = json.dedicatezoneresponse.jobid;
                                                    args.response.success({
                                                        _custom: {
                                                            jobId: jid,
                                                            getActionFilter: function () {
                                                                return zoneActionfilter;
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
                                    releaseDedicatedZone: {
                                        label: 'label.release.dedicated.zone',
                                        messages: {
                                            confirm: function (args) {
                                                return 'message.confirm.release.dedicated.zone';
                                            },
                                            notification: function (args) {
                                                return 'message.dedicated.zone.released';
                                            }
                                        },
                                        action: function (args) {
                                            $.ajax({
                                                url: createURL("releaseDedicatedZone&zoneid=" +
                                                    args.context.physicalResources[0].id),
                                                dataType: "json",
                                                async: true,
                                                success: function (json) {
                                                    var jid = json.releasededicatedzoneresponse.jobid;
                                                    args.response.success({
                                                        _custom: {
                                                            jobId: jid,
                                                            getActionFilter: function () {
                                                                return zoneActionfilter;
                                                            }
                                                        }
                                                    });
                                                },
                                                error: function (json) {
                                                    args.response.error(parseXMLHttpResponse(json));
                                                }
                                            });
                                        },
                                        notification: {
                                            poll: pollAsyncJobResult
                                        }
                                    },

                                    'remove': {
                                        label: 'label.action.delete.zone',
                                        messages: {
                                            confirm: function (args) {
                                                return 'message.action.delete.zone';
                                            },
                                            notification: function (args) {
                                                return 'label.action.delete.zone';
                                            }
                                        },
                                        action: function (args) {
                                            $.ajax({
                                                url: createURL("deleteZone&id=" + args.context.physicalResources[0].id), //embedded objects in listView is called physicalResources while embedded objects in detailView is called zones
                                                dataType: "json",
                                                async: true,
                                                success: function (json) {
                                                    args.response.success({
                                                        data: {}
                                                    });
                                                },
                                                error: function (json) {
                                                    args.response.error(parseXMLHttpResponse(json));
                                                }
                                            });
                                        },
                                        notification: {
                                            poll: function (args) {
                                                args.complete();
                                            }
                                        }
                                    },
                                    edit: {
                                        label: 'label.edit',
                                        action: function (args) {
                                            var array1 = [];
                                            array1.push("&name=" + todb(args.data.name));
                                            array1.push("&dns1=" + todb(args.data.dns1));
                                            array1.push("&dns2=" + todb(args.data.dns2));
                                            //dns2 can be empty ("") when passed to API, so a user gets to update this field from an existing value to blank.
                                            array1.push("&ip6dns1=" + todb(args.data.ip6dns1));
                                            //p6dns1 can be empty ("") when passed to API, so a user gets to update this field from an existing value to blank.
                                            array1.push("&ip6dns2=" + todb(args.data.ip6dns2));
                                            //ip6dns2 can be empty ("") when passed to API, so a user gets to update this field from an existing value to blank.

                                            if (selectedZoneObj.networktype == "Advanced" && args.data.guestcidraddress) {
                                                array1.push("&guestcidraddress=" + todb(args.data.guestcidraddress));
                                            }

                                            array1.push("&internaldns1=" + todb(args.data.internaldns1));
                                            array1.push("&internaldns2=" + todb(args.data.internaldns2));
                                            //internaldns2 can be empty ("") when passed to API, so a user gets to update this field from an existing value to blank.
                                            array1.push("&domain=" + todb(args.data.domain));
                                            array1.push("&localstorageenabled=" + (args.data.localstorageenabled == 'on'));
                                            $.ajax({
                                                url: createURL("updateZone&id=" + args.context.physicalResources[0].id + array1.join("")),
                                                dataType: "json",
                                                async: false,
                                                success: function (json) {
                                                    selectedZoneObj = json.updatezoneresponse.zone; //override selectedZoneObj after update zone
                                                    args.response.success({
                                                        data: selectedZoneObj
                                                    });
                                                },
                                                error: function (json) {
                                                    args.response.error('Could not edit zone information; please ensure all fields are valid.');
                                                }
                                            });
                                        }
                                    }
                                },
                                tabs: {
                                    details: {
                                        title: 'label.details',

                                        preFilter: function (args) {
                                            var hiddenFields = [];
                                            if (selectedZoneObj.networktype == "Basic")
                                                hiddenFields.push("guestcidraddress");
                                            return hiddenFields;
                                        },

                                        fields: [{
                                            name: {
                                                label: 'label.zone',
                                                isEditable: true,
                                                validation: {
                                                    required: true
                                                }
                                            }
                                        },
                                            {
                                                id: {
                                                    label: 'label.id'
                                                },
                                                allocationstate: {
                                                    label: 'label.allocation.state'
                                                },
                                                dns1: {
                                                    label: 'label.dns.1',
                                                    isEditable: true,
                                                    validation: {
                                                        required: true
                                                    }
                                                },
                                                dns2: {
                                                    label: 'label.dns.2',
                                                    isEditable: true
                                                },
                                                ip6dns1: {
                                                    label: 'label.ipv6.dns1',
                                                    isEditable: true
                                                },
                                                ip6dns2: {
                                                    label: 'label.ipv6.dns2',
                                                    isEditable: true
                                                },
                                                internaldns1: {
                                                    label: 'label.internal.dns.1',
                                                    isEditable: true,
                                                    validation: {
                                                        required: true
                                                    }
                                                },
                                                internaldns2: {
                                                    label: 'label.internal.dns.2',
                                                    isEditable: true
                                                },
                                                domainname: {
                                                    label: 'label.domain'
                                                },
                                                networktype: {
                                                    label: 'label.network.type'
                                                },
                                                guestcidraddress: {
                                                    label: 'label.guest.cidr',
                                                    isEditable: true
                                                },
                                                domain: {
                                                    label: 'label.network.domain',
                                                    isEditable: true
                                                },
                                                localstorageenabled: {
                                                    label: 'label.local.storage.enabled',
                                                    isBoolean: true,
                                                    isEditable: true,
                                                    converter: cloudStack.converters.toBooleanText
                                                }
                                            },
                                            {
                                                isdedicated: {
                                                    label: 'label.dedicated'
                                                },
                                                domainid: {
                                                    label: 'label.domain.id'
                                                }
                                            }],
                                        dataProvider: function (args) {
                                            $.ajax({
                                                url: createURL('listZones'),
                                                data: {
                                                    id: args.context.physicalResources[0].id
                                                },
                                                success: function (json) {
                                                    selectedZoneObj = json.listzonesresponse.zone[0];

                                                    $(window).trigger('cloudStack.module.sharedFunctions.addExtraProperties', {
                                                        obj: selectedZoneObj,
                                                        objType: "Zone"
                                                    });

                                                    $.ajax({
                                                        url: createURL('listDedicatedZones'),
                                                        data: {
                                                            zoneid: args.context.physicalResources[0].id
                                                        },
                                                        async: false,
                                                        success: function (json) {
                                                            if (json.listdedicatedzonesresponse.dedicatedzone != undefined) {
                                                                var dedicatedzoneObj = json.listdedicatedzonesresponse.dedicatedzone[0];
                                                                if (dedicatedzoneObj.domainid != null) {
                                                                    $.extend(selectedZoneObj, {
                                                                        isdedicated: 'Yes',
                                                                        domainid: dedicatedzoneObj.domainid,
                                                                        accountid: dedicatedzoneObj.accountid
                                                                    });
                                                                }
                                                            } else {
                                                                $.extend(selectedZoneObj, {
                                                                    isdedicated: 'No',
                                                                    domainid: null,
                                                                    accountid: null
                                                                })
                                                            }
                                                        }
                                                    });

                                                    args.response.success({
                                                        actionFilter: zoneActionfilter,
                                                        data: selectedZoneObj
                                                    });
                                                }
                                            });
                                        }
                                    },

                                    compute: {
                                        title: 'label.compute.and.storage',
                                        custom: cloudStack.uiCustom.systemChart('compute')
                                    },
                                    network: {
                                        title: 'label.physical.network',
                                        custom: cloudStack.uiCustom.systemChart('network')
                                    },
                                    resources: {
                                        title: 'label.resources',
                                        custom: cloudStack.uiCustom.systemChart('resources')
                                    },

                                    systemVMs: {
                                        title: 'label.system.vms',
                                        listView: {
                                            label: 'label.system.vms',
                                            id: 'systemVMs',
                                            fields: {
                                                name: {
                                                    label: 'label.name'
                                                },
                                                systemvmtype: {
                                                    label: 'label.type',
                                                    converter: function (args) {
                                                        if (args == "consoleproxy")
                                                            return "Console Proxy VM"; else if (args == "secondarystoragevm")
                                                            return "Secondary Storage VM"; else
                                                            return args;
                                                    }
                                                },
                                                zonename: {
                                                    label: 'label.zone'
                                                },
                                                state: {
                                                    label: 'label.status',
                                                    converter: function (str) {
                                                        // For localization
                                                        return str;
                                                    },
                                                    indicator: {
                                                        'Running': 'on',
                                                        'Stopped': 'off',
                                                        'Error': 'off',
                                                        'Destroyed': 'off'
                                                    }
                                                }
                                            },
                                            dataProvider: function (args) {
                                                var array1 = [];
                                                if (args.filterBy != null) {
                                                    if (args.filterBy.search != null && args.filterBy.search.by != null && args.filterBy.search.value != null) {
                                                        switch (args.filterBy.search.by) {
                                                            case "name":
                                                                if (args.filterBy.search.value.length > 0)
                                                                    array1.push("&keyword=" + args.filterBy.search.value);
                                                                break;
                                                        }
                                                    }
                                                }

                                                var selectedZoneObj = args.context.physicalResources[0];
                                                $.ajax({
                                                    url: createURL("listSystemVms&zoneid=" + selectedZoneObj.id + "&page=" + args.page + "&pagesize=" + pageSize + array1.join("")),
                                                    dataType: "json",
                                                    async: true,
                                                    success: function (json) {
                                                        var items = json.listsystemvmsresponse.systemvm;
                                                        args.response.success({
                                                            actionFilter: systemvmActionfilter,
                                                            data: items
                                                        });
                                                    }
                                                });
                                            },

                                            detailView: {
                                                noCompact: true,
                                                name: 'label.system.vm.details',
                                                actions: {
                                                    start: {
                                                        label: 'label.action.start.systemvm',
                                                        messages: {
                                                            confirm: function (args) {
                                                                return 'message.action.start.systemvm';
                                                            },
                                                            notification: function (args) {
                                                                return 'label.action.start.systemvm';
                                                            }
                                                        },
                                                        action: function (args) {
                                                            $.ajax({
                                                                url: createURL('startSystemVm&id=' + args.context.systemVMs[0].id),
                                                                dataType: 'json',
                                                                async: true,
                                                                success: function (json) {
                                                                    var jid = json.startsystemvmresponse.jobid;
                                                                    args.response.success({
                                                                        _custom: {
                                                                            jobId: jid,
                                                                            getUpdatedItem: function (json) {
                                                                                return json.queryasyncjobresultresponse.jobresult.systemvm;
                                                                            },
                                                                            getActionFilter: function () {
                                                                                return systemvmActionfilter;
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

                                                    stop: {
                                                        label: 'label.action.stop.systemvm',
                                                        messages: {
                                                            confirm: function (args) {
                                                                return 'message.action.stop.systemvm';
                                                            },
                                                            notification: function (args) {
                                                                return 'label.action.stop.systemvm';
                                                            }
                                                        },
                                                        action: function (args) {
                                                            $.ajax({
                                                                url: createURL('stopSystemVm&id=' + args.context.systemVMs[0].id),
                                                                dataType: 'json',
                                                                async: true,
                                                                success: function (json) {
                                                                    var jid = json.stopsystemvmresponse.jobid;
                                                                    args.response.success({
                                                                        _custom: {
                                                                            jobId: jid,
                                                                            getUpdatedItem: function (json) {
                                                                                return json.queryasyncjobresultresponse.jobresult.systemvm;
                                                                            },
                                                                            getActionFilter: function () {
                                                                                return systemvmActionfilter;
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

                                                    restart: {
                                                        label: 'label.action.reboot.systemvm',
                                                        messages: {
                                                            confirm: function (args) {
                                                                return 'message.action.reboot.systemvm';
                                                            },
                                                            notification: function (args) {
                                                                return 'label.action.reboot.systemvm';
                                                            }
                                                        },
                                                        action: function (args) {
                                                            $.ajax({
                                                                url: createURL('rebootSystemVm&id=' + args.context.systemVMs[0].id),
                                                                dataType: 'json',
                                                                async: true,
                                                                success: function (json) {
                                                                    var jid = json.rebootsystemvmresponse.jobid;
                                                                    args.response.success({
                                                                        _custom: {
                                                                            jobId: jid,
                                                                            getUpdatedItem: function (json) {
                                                                                return json.queryasyncjobresultresponse.jobresult.systemvm;
                                                                            },
                                                                            getActionFilter: function () {
                                                                                return systemvmActionfilter;
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

                                                    remove: {
                                                        label: 'label.action.destroy.systemvm',
                                                        messages: {
                                                            confirm: function (args) {
                                                                return 'message.action.destroy.systemvm';
                                                            },
                                                            notification: function (args) {
                                                                return 'label.action.destroy.systemvm';
                                                            }
                                                        },
                                                        action: function (args) {
                                                            $.ajax({
                                                                url: createURL('destroySystemVm&id=' + args.context.systemVMs[0].id),
                                                                dataType: 'json',
                                                                async: true,
                                                                success: function (json) {
                                                                    var jid = json.destroysystemvmresponse.jobid;
                                                                    args.response.success({
                                                                        _custom: {
                                                                            getUpdatedItem: function () {
                                                                                return {
                                                                                    state: 'Destroyed'
                                                                                };
                                                                            },
                                                                            jobId: jid
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
                                                        label: 'label.action.migrate.systemvm',
                                                        messages: {
                                                            notification: function (args) {
                                                                return 'label.action.migrate.systemvm';
                                                            }
                                                        },
                                                        createForm: {
                                                            title: 'label.action.migrate.systemvm',
                                                            desc: '',
                                                            fields: {
                                                                hostId: {
                                                                    label: 'label.host',
                                                                    validation: {
                                                                        required: true
                                                                    },
                                                                    select: function (args) {
                                                                        $.ajax({
                                                                            url: createURL("findHostsForMigration&VirtualMachineId=" + args.context.systemVMs[0].id),
                                                                            dataType: "json",
                                                                            async: true,
                                                                            success: function (json) {
                                                                                var hostObjs = json.findhostsformigrationresponse.host;
                                                                                var items = [];
                                                                                $(hostObjs).each(function () {
                                                                                    if (this.requiresStorageMotion == false) {
                                                                                        items.push({
                                                                                            id: this.id,
                                                                                            description: (this.name + " (" + (this.suitableformigration ? "Suitable" : "Not Suitable") + ")")
                                                                                        });
                                                                                    }
                                                                                });
                                                                                args.response.success({
                                                                                    data: items
                                                                                });
                                                                            }
                                                                        });
                                                                    },
                                                                    error: function (XMLHttpResponse) {
                                                                        var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
                                                                        args.response.error(errorMsg);
                                                                    }
                                                                }
                                                            }
                                                        },
                                                        action: function (args) {
                                                            $.ajax({
                                                                url: createURL("migrateSystemVm&hostid=" + args.data.hostId + "&virtualmachineid=" + args.context.systemVMs[0].id),
                                                                dataType: "json",
                                                                async: true,
                                                                success: function (json) {
                                                                    var jid = json.migratesystemvmresponse.jobid;
                                                                    args.response.success({
                                                                        _custom: {
                                                                            jobId: jid,
                                                                            getUpdatedItem: function (json) {
                                                                                //return json.queryasyncjobresultresponse.jobresult.systemvminstance;    //not all properties returned in systemvminstance
                                                                                $.ajax({
                                                                                    url: createURL("listSystemVms&id=" + json.queryasyncjobresultresponse.jobresult.systemvm.id),
                                                                                    dataType: "json",
                                                                                    async: false,
                                                                                    success: function (json) {
                                                                                        var items = json.listsystemvmsresponse.systemvm;
                                                                                        if (items != null && items.length > 0) {
                                                                                            return items[0];
                                                                                        }
                                                                                    }
                                                                                });
                                                                            },
                                                                            getActionFilter: function () {
                                                                                return systemvmActionfilter;
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

                                                    scaleUp: {
                                                        label: 'label.change.service.offering',
                                                        createForm: {
                                                            title: 'label.change.service.offering',
                                                            desc: function (args) {
                                                                var description = '';
                                                                var vmObj = args.jsonObj;
                                                                if (vmObj.state == 'Running') {
                                                                    description = 'message.read.admin.guide.scaling.up';
                                                                }
                                                                return description;
                                                            },
                                                            fields: {
                                                                serviceOfferingId: {
                                                                    label: 'label.compute.offering',
                                                                    select: function (args) {
                                                                        var apiCmd = "listServiceOfferings&issystem=true";
                                                                        if (args.context.systemVMs[0].systemvmtype == "secondarystoragevm")
                                                                            apiCmd += "&systemvmtype=secondarystoragevm"; else if (args.context.systemVMs[0].systemvmtype == "consoleproxy")
                                                                            apiCmd += "&systemvmtype=consoleproxy";
                                                                        $.ajax({
                                                                            url: createURL(apiCmd),
                                                                            dataType: "json",
                                                                            async: true,
                                                                            success: function (json) {
                                                                                var serviceofferings = json.listserviceofferingsresponse.serviceoffering;
                                                                                var items = [];
                                                                                $(serviceofferings).each(function () {
                                                                                    if (this.id != args.context.systemVMs[0].serviceofferingid) {
                                                                                        items.push({
                                                                                            id: this.id,
                                                                                            description: this.name
                                                                                        });
                                                                                    }
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
                                                            $.ajax({
                                                                url: createURL("scaleSystemVm&id=" + args.context.systemVMs[0].id + "&serviceofferingid=" + args.data.serviceOfferingId),
                                                                dataType: "json",
                                                                async: true,
                                                                success: function (json) {
                                                                    var jid = json.changeserviceforsystemvmresponse.jobid;
                                                                    args.response.success({
                                                                        _custom: {
                                                                            jobId: jid,
                                                                            getUpdatedItem: function (json) {
                                                                                return json.queryasyncjobresultresponse.jobresult.systemvm;
                                                                            },
                                                                            getActionFilter: function () {
                                                                                return systemvmActionfilter;
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
                                                            confirm: function (args) {
                                                                return 'message.confirm.scale.up.system.vm';
                                                            },
                                                            notification: function (args) {

                                                                return 'label.system.vm.scaled.up';
                                                            }
                                                        },
                                                        notification: {
                                                            poll: pollAsyncJobResult
                                                        }
                                                    },


                                                    viewConsole: {
                                                        label: 'label.view.console',
                                                        action: {
                                                            externalLink: {
                                                                url: function (args) {
                                                                    return clientConsoleUrl + '?cmd=access&vm=' + args.context.systemVMs[0].id;
                                                                },
                                                                title: function (args) {
                                                                    return args.context.systemVMs[0].id.substr(0, 8);
                                                                    //title in window.open() can't have space nor longer than 8 characters. Otherwise, IE browser will have error.
                                                                },
                                                                width: 820,
                                                                height: 640
                                                            }
                                                        }
                                                    }
                                                },
                                                tabs: {
                                                    details: {
                                                        title: 'label.details',
                                                        fields: [{
                                                            name: {
                                                                label: 'label.name'
                                                            }
                                                        },
                                                            {
                                                                id: {
                                                                    label: 'label.id'
                                                                },
                                                                state: {
                                                                    label: 'label.state'
                                                                },
                                                                systemvmtype: {
                                                                    label: 'label.type',
                                                                    converter: function (args) {
                                                                        if (args == "consoleproxy")
                                                                            return 'label.console.proxy.vm'; else if (args == "secondarystoragevm")
                                                                            return 'label.secondary.storage.vm'; else
                                                                            return args;
                                                                    }
                                                                },
                                                                zonename: {
                                                                    label: 'label.zone'
                                                                },
                                                                publicip: {
                                                                    label: 'label.public.ip'
                                                                },
                                                                privateip: {
                                                                    label: 'label.private.ip'
                                                                },
                                                                linklocalip: {
                                                                    label: 'label.linklocal.ip'
                                                                },
                                                                hostname: {
                                                                    label: 'label.host'
                                                                },
                                                                gateway: {
                                                                    label: 'label.gateway'
                                                                },
                                                                created: {
                                                                    label: 'label.created',
                                                                    converter: cloudStack.converters.toLocalDate
                                                                },
                                                                activeviewersessions: {
                                                                    label: 'label.active.sessions'
                                                                }
                                                            }],
                                                        dataProvider: function (args) {
                                                            $.ajax({
                                                                url: createURL("listSystemVms&id=" + args.context.systemVMs[0].id),
                                                                dataType: "json",
                                                                async: true,
                                                                success: function (json) {
                                                                    args.response.success({
                                                                        actionFilter: systemvmActionfilter,
                                                                        data: json.listsystemvmsresponse.systemvm[0]
                                                                    });
                                                                }
                                                            });
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },

                                    // Granular settings for zone
                                    settings: {
                                        title: 'label.settings',
                                        custom: cloudStack.uiCustom.granularSettings({
                                            dataProvider: function (args) {
                                                $.ajax({
                                                    url: createURL('listConfigurations&zoneid=' + args.context.physicalResources[0].id),
                                                    data: listViewDataProvider(args, {},
                                                        {
                                                            searchBy: 'name'
                                                        }),
                                                    success: function (json) {
                                                        args.response.success({
                                                            data: json.listconfigurationsresponse.configuration
                                                        });
                                                    },

                                                    error: function (json) {
                                                        args.response.error(parseXMLHttpResponse(json));
                                                    }
                                                });
                                            },
                                            actions: {
                                                edit: function (args) {
                                                    // call updateZoneLevelParamter
                                                    var data = {
                                                        name: args.data.jsonObj.name,
                                                        value: args.data.value
                                                    };

                                                    $.ajax({
                                                        url: createURL('updateConfiguration&zoneid=' + args.context.physicalResources[0].id),
                                                        data: data,
                                                        success: function (json) {
                                                            var item = json.updateconfigurationresponse.configuration;
                                                            args.response.success({
                                                                data: item
                                                            });
                                                        },

                                                        error: function (json) {
                                                            args.response.error(parseXMLHttpResponse(json));
                                                        }
                                                    });
                                                }
                                            }
                                        })
                                    }
                                }
                            }
                        },
                        pods: function () {
                            var listView = $.extend(true, {},
                                cloudStack.sections.system.subsections.pods.listView, {
                                    dataProvider: function (args) {
                                        var data = {};
                                        listViewDataProvider(args, data);

                                        $.ajax({
                                            url: createURL('listPods'),
                                            data: data,
                                            success: function (json) {
                                                args.response.success({
                                                    data: json.listpodsresponse.pod
                                                });
                                            },
                                            error: function (json) {
                                                args.response.error(parseXMLHttpResponse(json));
                                            }
                                        });
                                    },

                                    detailView: {
                                        updateContext: function (args) {
                                            var zone;

                                            $.ajax({
                                                url: createURL('listZones'),
                                                data: {
                                                    id: args.context.pods[0].zoneid
                                                },
                                                async: false,
                                                success: function (json) {
                                                    zone = json.listzonesresponse.zone[0];
                                                }
                                            });

                                            selectedZoneObj = zone;

                                            return {
                                                zones: [zone]
                                            };
                                        }
                                    }
                                });

                            return listView;
                        },
                        clusters: function () {
                            var listView = $.extend(true, {},
                                cloudStack.sections.system.subsections.clusters.listView, {
                                    dataProvider: function (args) {
                                        var data = {};
                                        listViewDataProvider(args, data);

                                        $.ajax({
                                            url: createURL('listClusters'),
                                            data: data,
                                            success: function (json) {
                                                args.response.success({
                                                    data: json.listclustersresponse.cluster
                                                });
                                            },
                                            error: function (json) {
                                                args.response.error(parseXMLHttpResponse(json));
                                            }
                                        });
                                    },

                                    detailView: {
                                        updateContext: function (args) {
                                            var zone;

                                            $.ajax({
                                                url: createURL('listZones'),
                                                data: {
                                                    id: args.context.clusters[0].zoneid
                                                },
                                                async: false,
                                                success: function (json) {
                                                    zone = json.listzonesresponse.zone[0];
                                                }
                                            });

                                            selectedZoneObj = zone;

                                            return {
                                                zones: [zone]
                                            };
                                        }
                                    }
                                });

                            return listView;
                        },
                        hosts: function () {
                            var listView = $.extend(true, {},
                                cloudStack.sections.system.subsections.hosts.listView, {
                                    dataProvider: function (args) {
                                        var data = {
                                            type: 'routing'
                                        };
                                        listViewDataProvider(args, data);

                                        $.ajax({
                                            url: createURL('listHosts'),
                                            data: data,
                                            success: function (json) {
                                                args.response.success({
                                                    data: json.listhostsresponse.host
                                                });
                                            },
                                            error: function (json) {
                                                args.response.error(parseXMLHttpResponse(json));
                                            }
                                        });
                                    },

                                    detailView: {
                                        updateContext: function (args) {
                                            var zone;

                                            $.ajax({
                                                url: createURL('listZones'),
                                                data: {
                                                    id: args.context.hosts[0].zoneid
                                                },
                                                async: false,
                                                success: function (json) {
                                                    zone = json.listzonesresponse.zone[0];
                                                }
                                            });

                                            selectedZoneObj = zone;

                                            return {
                                                zones: [zone]
                                            };
                                        }
                                    }
                                });

                            return listView;
                        },
                        primaryStorage: function () {
                            var listView = $.extend(true, {},
                                cloudStack.sections.system.subsections['primary-storage'].listView, {
                                    dataProvider: function (args) {
                                        var data = {};
                                        listViewDataProvider(args, data);

                                        $.ajax({
                                            url: createURL('listStoragePools'),
                                            data: data,
                                            success: function (json) {
                                                args.response.success({
                                                    data: json.liststoragepoolsresponse.storagepool
                                                });
                                            },
                                            error: function (json) {
                                                args.response.error(parseXMLHttpResponse(json));
                                            }
                                        });
                                    },

                                    detailView: {
                                        updateContext: function (args) {
                                            var zone;

                                            $.ajax({
                                                url: createURL('listZones'),
                                                data: {
                                                    id: args.context.primarystorages[0].zoneid
                                                },
                                                async: false,
                                                success: function (json) {
                                                    zone = json.listzonesresponse.zone[0];
                                                }
                                            });

                                            selectedZoneObj = zone;

                                            return {
                                                zones: [zone]
                                            };
                                        }
                                    }
                                });

                            return listView;
                        },

                        secondaryStorage: function () {
                            var listView = $.extend(
                                true, {},
                                cloudStack.sections.system.subsections['secondary-storage'], {
                                    sections: {
                                        secondaryStorage: {
                                            listView: {
                                                dataProvider: function (args) {
                                                    var data = {
                                                        type: 'SecondaryStorage'
                                                    };
                                                    listViewDataProvider(args, data);

                                                    $.ajax({
                                                        url: createURL('listImageStores'),
                                                        data: data,
                                                        success: function (json) {
                                                            var items = json.listimagestoresresponse.imagestore;
                                                            if (items != undefined) {
                                                                for (var i = 0; i < items.length; i++) {
                                                                    processPropertiesInImagestoreObject(items[i]);
                                                                }
                                                            }
                                                            args.response.success({
                                                                data: items
                                                            });
                                                        },
                                                        error: function (json) {
                                                            args.response.error(parseXMLHttpResponse(json));
                                                        }
                                                    });
                                                }
                                            }
                                        },
                                        cacheStorage: {
                                            listView: {
                                                dataProvider: function (args) {
                                                    var data = {};
                                                    listViewDataProvider(args, data);

                                                    $.ajax({
                                                        url: createURL('listSecondaryStagingStores'),
                                                        data: data,
                                                        success: function (json) {
                                                            args.response.success({
                                                                data: json.listsecondarystagingstoreresponse.imagestore
                                                            });
                                                        },
                                                        error: function (json) {
                                                            args.response.error(parseXMLHttpResponse(json));
                                                        }
                                                    });
                                                }

                                                /*
                                                 ,
                                                 detailView: {
                                                 updateContext: function (args) {
                                                 return {
                                                 zones: [{}]
                                                 };
                                                 }
                                                 }
                                                 */
                                            }
                                        }
                                    }
                                });

                            return listView;
                        },
                        systemVms: function () {
                            var listView = $.extend(true, {},
                                cloudStack.sections.system.subsections.systemVms.listView, {
                                    dataProvider: function (args) {
                                        var data = {};
                                        listViewDataProvider(args, data);

                                        $.ajax({
                                            url: createURL('listSystemVms'),
                                            data: data,
                                            success: function (json) {
                                                var systemvmObjs = json.listsystemvmsresponse.systemvm;
                                                if (systemvmObjs != undefined) {
                                                    $.ajax({
                                                        url: createURL('listHosts'),
                                                        data: {
                                                            details: 'min'
                                                        },
                                                        success: function (json) {
                                                            var hostObjs = json.listhostsresponse.host;
                                                            for (var i = 0; i < systemvmObjs.length; i++) {
                                                                for (var k = 0; k < hostObjs.length; k++) {
                                                                    if (hostObjs[k].name == systemvmObjs[i].name) {
                                                                        systemvmObjs[i].agentstate = hostObjs[k].state;
                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                            args.response.success({
                                                                data: systemvmObjs
                                                            });
                                                        }
                                                    });
                                                } else {
                                                    args.response.success({
                                                        data: []
                                                    });
                                                }
                                            }
                                        });
                                    },

                                    detailView: {
                                        updateContext: function (args) {
                                            var zone;

                                            $.ajax({
                                                url: createURL('listZones'),
                                                data: {
                                                    id: args.context.systemVMs[0].zoneid
                                                },
                                                async: false,
                                                success: function (json) {
                                                    zone = json.listzonesresponse.zone[0];
                                                }
                                            });

                                            selectedZoneObj = zone;

                                            return {
                                                zones: [zone]
                                            };
                                        }
                                    }
                                });

                            return listView;
                        },
                        virtualRouters: function () {
                            var listView = $.extend(true, {},
                                cloudStack.sections.system.subsections.virtualRouters, {
                                    sections: {
                                        virtualRouterNoGrouping: {
                                            listView: {
                                                dataProvider: function (args) {
                                                    var data = {};
                                                    listViewDataProvider(args, data);

                                                    var routers = [];

                                                    //get account-owned routers
                                                    $.ajax({
                                                        url: createURL('listRouters'),
                                                        data: $.extend(data, {
                                                            listAll: true
                                                        }),
                                                        async: false,
                                                        success: function (json) {
                                                            var items = json.listroutersresponse.router ? json.listroutersresponse.router : [];
                                                            $(items).map(function (index, item) {
                                                                routers.push(item);
                                                            });

                                                            //if account is specified in advanced search, don't search project-owned routers
                                                            var accountIsNotSpecifiedInAdvSearch = true;
                                                            if (args.filterBy != null) {
                                                                if (args.filterBy.advSearch != null && typeof(args.filterBy.advSearch) == "object") { //advanced search
                                                                    if ('account' in args.filterBy.advSearch && args.filterBy.advSearch.account.length > 0) {
                                                                        accountIsNotSpecifiedInAdvSearch = false;  //since account and projectid can't be specified together
                                                                    }
                                                                }
                                                            }
                                                            if (accountIsNotSpecifiedInAdvSearch) {
                                                                /*
                                                                 * In project view, the first listRotuers API(without projectid=-1) will return the same objects as the second listRouters API(with projectid=-1),
                                                                 * because in project view, all API calls are appended with projectid=[projectID].
                                                                 * Therefore, we only call the second listRouters API(with projectid=-1) in non-project view.
                                                                 */
                                                                if (cloudStack.context && cloudStack.context.projects == null) { //non-project view
                                                                    $.ajax({
                                                                        url: createURL("listRouters&listAll=true&page=" + args.page + "&pagesize=" + pageSize + "&projectid=-1"),
                                                                        async: false,
                                                                        success: function (json) {
                                                                            var items = json.listroutersresponse.router ? json.listroutersresponse.router : [];
                                                                            $(items).map(function (index, item) {
                                                                                routers.push(item);
                                                                            });
                                                                        }
                                                                    });

                                                                }
                                                            }

                                                            args.response.success({
                                                                actionFilter: routerActionfilter,
                                                                data: $(routers).map(mapRouterType)
                                                            });
                                                        }
                                                    });

                                                    args.response.success({
                                                        actionFilter: routerActionfilter,
                                                        data: $(routers).map(mapRouterType)
                                                    });
                                                },

                                                detailView: {
                                                    updateContext: function (args) {
                                                        var zone;

                                                        $.ajax({
                                                            url: createURL('listZones'),
                                                            data: {
                                                                id: args.context.routers[0].zoneid
                                                            },
                                                            async: false,
                                                            success: function (json) {
                                                                zone = json.listzonesresponse.zone[0];
                                                            }
                                                        });

                                                        selectedZoneObj = zone;

                                                        return {
                                                            zones: [zone]
                                                        };
                                                    }
                                                }
                                            }
                                        }
                                    }
                                });

                            return listView;
                        },

                        sockets: function () {
                            var listView = {
                                id: 'sockets',
                                fields: {
                                    hypervisor: {
                                        label: 'label.hypervisor'
                                    },
                                    hosts: {
                                        label: 'label.hosts'
                                    },
                                    sockets: {
                                        label: 'label.sockets'
                                    }
                                },
                                dataProvider: function (args) {
                                    var array1 = [];

                                    // ***** non XenServer (begin) *****
                                    var hypervisors = ["KVM", "Ovm3"];

                                    var supportSocketHypervisors = {
                                        "KVM": 1,
                                        "Ovm3": 1
                                    };

                                    for (var h = 0; h < hypervisors.length; h++) {
                                        var totalHostCount = 0;
                                        var currentPage = 1;
                                        var returnedHostCount = 0;
                                        var returnedHostCpusocketsSum = 0;

                                        var callListHostsWithPage = function () {
                                            $.ajax({
                                                url: createURL('listHosts'),
                                                async: false,
                                                data: {
                                                    type: 'routing',
                                                    hypervisor: hypervisors[h],
                                                    page: currentPage,
                                                    pagesize: pageSize //global variable
                                                },
                                                success: function (json) {
                                                    if (json.listhostsresponse.count == undefined) {
                                                        return;
                                                    }

                                                    totalHostCount = json.listhostsresponse.count;
                                                    returnedHostCount += json.listhostsresponse.host.length;

                                                    var items = json.listhostsresponse.host;
                                                    for (var i = 0; i < items.length; i++) {
                                                        if (items[i].cpusockets != undefined && isNaN(items[i].cpusockets) == false) {
                                                            returnedHostCpusocketsSum += items[i].cpusockets;
                                                        }
                                                    }

                                                    if (returnedHostCount < totalHostCount) {
                                                        currentPage++;
                                                        callListHostsWithPage();
                                                    }
                                                }
                                            });
                                        }

                                        callListHostsWithPage();

                                        if ((hypervisors[h] in supportSocketHypervisors) == false) {
                                            returnedHostCpusocketsSum = 'N/A';
                                        }

                                        var hypervisorName = hypervisors[h];

                                        array1.push({
                                            hypervisor: hypervisorName,
                                            hosts: totalHostCount,
                                            sockets: returnedHostCpusocketsSum
                                        });
                                    }
                                    // ***** non XenServer (end) *****


                                    // ***** XenServer (begin) *****
                                    var totalHostCount = 0;
                                    var currentPage = 1;
                                    var returnedHostCount = 0;

                                    var returnedHostCountForXenServer650 = 0;  //'XenServer 6.5.0'
                                    var returnedHostCpusocketsSumForXenServer650 = 0;

                                    var returnedHostCountForXenServer620 = 0;  //'XenServer 6.2.0'
                                    var returnedHostCpusocketsSumForXenServer620 = 0;

                                    var returnedHostCountForXenServer61x = 0;  //'XenServer 6.1.x and before'

                                    var callListHostsWithPage = function () {
                                        $.ajax({
                                            url: createURL('listHosts'),
                                            async: false,
                                            data: {
                                                type: 'routing',
                                                hypervisor: 'XenServer',
                                                page: currentPage,
                                                pagesize: pageSize //global variable
                                            },
                                            success: function (json) {
                                                if (json.listhostsresponse.count == undefined) {
                                                    return;
                                                }

                                                totalHostCount = json.listhostsresponse.count;
                                                returnedHostCount += json.listhostsresponse.host.length;

                                                var items = json.listhostsresponse.host;
                                                for (var i = 0; i < items.length; i++) {
                                                    if (items[i].hypervisorversion == "6.5.0") {
                                                        returnedHostCountForXenServer650++;
                                                        if (items[i].cpusockets != undefined && isNaN(items[i].cpusockets) == false) {
                                                            returnedHostCpusocketsSumForXenServer650 += items[i].cpusockets;
                                                        }
                                                    } else if (items[i].hypervisorversion == "6.2.0") {
                                                        returnedHostCountForXenServer620++;
                                                        if (items[i].cpusockets != undefined && isNaN(items[i].cpusockets) == false) {
                                                            returnedHostCpusocketsSumForXenServer620 += items[i].cpusockets;
                                                        }
                                                    } else {
                                                        returnedHostCountForXenServer61x++;
                                                    }
                                                }

                                                if (returnedHostCount < totalHostCount) {
                                                    currentPage++;
                                                    callListHostsWithPage();
                                                }
                                            }
                                        });
                                    }

                                    callListHostsWithPage();

                                    array1.push({
                                        hypervisor: 'XenServer 6.5.0',
                                        hosts: returnedHostCountForXenServer650,
                                        sockets: returnedHostCpusocketsSumForXenServer650
                                    });

                                    array1.push({
                                        hypervisor: 'XenServer 6.2.0',
                                        hosts: returnedHostCountForXenServer620,
                                        sockets: returnedHostCpusocketsSumForXenServer620
                                    });

                                    array1.push({
                                        hypervisor: 'XenServer 6.1.x and before',
                                        hosts: returnedHostCountForXenServer61x,
                                        sockets: 'N/A'
                                    });

                                    // ***** XenServer (end) *****


                                    args.response.success({
                                        data: array1
                                    });

                                }
                            };

                            return listView;
                        }
                    }
                }
            }
        },
        subsections: {
            virtualRouters: {
                sectionSelect: {
                    label: 'label.select-view',
                    preFilter: function (args) {
                        //Only clicking ViewAll Link("view all Virtual Routers") in "Virtual Routers group by XXXXXXX" detailView will have "routerGroupByXXXXXXX" included in args.context
                        if ("routerGroupByZone" in args.context) {
                            return ["routerGroupByZone"]; // read-only (i.e. text "group by Zone")
                        } else if ("routerGroupByPod" in args.context) {
                            return ["routerGroupByPod"]; // read-only (i.e. text "group by Pod")
                        } else if ("routerGroupByCluster" in args.context) {
                            return ["routerGroupByCluster"]; // read-only (i.e. text "group by Cluster")
                        } else if ("routerGroupByAccount" in args.context) {
                            return ["routerGroupByAccount"]; // read-only (i.e. text "group by Account")
                        } else {
                            return ["routerNoGroup", "routerGroupByZone", "routerGroupByPod", "routerGroupByCluster", "routerGroupByAccount"]; //editable dropdown
                        }
                    }
                },
                sections: {
                    routerNoGroup: {
                        id: 'routers',
                        type: 'select',
                        title: 'label.no.grouping',
                        listView: {
                            id: 'routers',
                            label: 'label.virtual.appliances',
                            fields: {
                                name: {
                                    label: 'label.name'
                                },
                                publicip: {
                                    label: 'label.public.ip'
                                },
                                routerType: {
                                    label: 'label.type'
                                },
                                state: {
                                    converter: function (str) {
                                        // For localization
                                        return str;
                                    },
                                    label: 'label.status',
                                    indicator: {
                                        'Running': 'on',
                                        'Stopped': 'off',
                                        'Error': 'off'
                                    }
                                },
                                requiresupgrade: {
                                    label: 'label.requires.upgrade',
                                    converter: cloudStack.converters.toBooleanText
                                }
                            },
                            dataProvider: function (args) {
                                var array1 = [];
                                if (args.filterBy != null) {
                                    if (args.filterBy.search != null && args.filterBy.search.by != null && args.filterBy.search.value != null) {
                                        switch (args.filterBy.search.by) {
                                            case "name":
                                                if (args.filterBy.search.value.length > 0)
                                                    array1.push("&keyword=" + args.filterBy.search.value);
                                                break;
                                        }
                                    }
                                }

                                var data2 = {
                                    // forvpc: false
                                };

                                if (args.context != undefined) {
                                    if ("routerGroupByZone" in args.context) {
                                        $.extend(data2, {
                                            zoneid: args.context.routerGroupByZone[0].id
                                        })
                                    } else if ("routerGroupByPod" in args.context) {
                                        $.extend(data2, {
                                            podid: args.context.routerGroupByPod[0].id
                                        })
                                    } else if ("routerGroupByCluster" in args.context) {
                                        $.extend(data2, {
                                            clusterid: args.context.routerGroupByCluster[0].id
                                        })
                                    } else if ("routerGroupByAccount" in args.context) {
                                        $.extend(data2, {
                                            account: args.context.routerGroupByAccount[0].name,
                                            domainid: args.context.routerGroupByAccount[0].domainid
                                        })
                                    }
                                }

                                var routers = [];
                                $.ajax({
                                    url: createURL("listRouters&listAll=true&page=" + args.page + "&pagesize=" + pageSize + array1.join("")),
                                    data: data2,
                                    success: function (json) {
                                        var items = json.listroutersresponse.router ?
                                            json.listroutersresponse.router : [];

                                        $(items).map(function (index, item) {
                                            routers.push(item);
                                        });

                                        /*
                                         * In project view, the first listRotuers API(without projectid=-1) will return the same objects as the second listRouters API(with projectid=-1),
                                         * because in project view, all API calls are appended with projectid=[projectID].
                                         * Therefore, we only call the second listRouters API(with projectid=-1) in non-project view.
                                         */
                                        if (cloudStack.context && cloudStack.context.projects == null) { //non-project view
                                            /*
                                             * account parameter(account+domainid) and project parameter(projectid) are not allowed to be passed together to listXXXXXXX API.
                                             * So, remove account parameter(account+domainid) from data2
                                             */
                                            if ("account" in data2) {
                                                delete data2.account;
                                            }
                                            if ("domainid" in data2) {
                                                delete data2.domainid;
                                            }

                                            $.ajax({
                                                url: createURL("listRouters&listAll=true&page=" + args.page + "&pagesize=" + pageSize + array1.join("") + "&projectid=-1"),
                                                data: data2,
                                                async: false,
                                                success: function (json) {
                                                    var items = json.listroutersresponse.router ?
                                                        json.listroutersresponse.router : [];

                                                    $(items).map(function (index, item) {
                                                        routers.push(item);
                                                    });
                                                }
                                            });
                                        }

                                        args.response.success({
                                            actionFilter: routerActionfilter,
                                            data: $(routers).map(mapRouterType)
                                        });
                                    }
                                });
                            },
                            detailView: {
                                name: 'label.virtual.appliance.details',
                                actions: {
                                    start: {
                                        label: 'label.action.start.router',
                                        messages: {
                                            confirm: function (args) {
                                                return 'message.action.start.router';
                                            },
                                            notification: function (args) {
                                                return 'label.action.start.router';
                                            }
                                        },
                                        action: function (args) {
                                            $.ajax({
                                                url: createURL('startRouter&id=' + args.context.routers[0].id),
                                                dataType: 'json',
                                                async: true,
                                                success: function (json) {
                                                    var jid = json.startrouterresponse.jobid;
                                                    args.response.success({
                                                        _custom: {
                                                            jobId: jid,
                                                            getUpdatedItem: function (json) {
                                                                return json.queryasyncjobresultresponse.jobresult.router;
                                                            },
                                                            getActionFilter: function () {
                                                                return routerActionfilter;
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

                                    stop: {
                                        label: 'label.action.stop.router',
                                        createForm: {
                                            title: 'label.action.stop.router',
                                            desc: 'message.action.stop.router',
                                            fields: {
                                                forced: {
                                                    label: 'force.stop',
                                                    isBoolean: true,
                                                    isChecked: false
                                                }
                                            }
                                        },
                                        messages: {
                                            notification: function (args) {
                                                return 'label.action.stop.router';
                                            }
                                        },
                                        action: function (args) {
                                            var array1 = [];
                                            array1.push("&forced=" + (args.data.forced == "on"));
                                            $.ajax({
                                                url: createURL('stopRouter&id=' + args.context.routers[0].id + array1.join("")),
                                                dataType: 'json',
                                                async: true,
                                                success: function (json) {
                                                    var jid = json.stoprouterresponse.jobid;
                                                    args.response.success({
                                                        _custom: {
                                                            jobId: jid,
                                                            getUpdatedItem: function (json) {
                                                                return json.queryasyncjobresultresponse.jobresult.router;
                                                            },
                                                            getActionFilter: function () {
                                                                return routerActionfilter;
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

                                    upgradeRouterToUseNewerTemplate: {
                                        label: 'label.upgrade.router.newer.template',
                                        messages: {
                                            confirm: function (args) {
                                                return 'message.confirm.upgrade.router.newer.template';
                                            },
                                            notification: function (args) {
                                                return 'label.upgrade.router.newer.template';
                                            }
                                        },
                                        action: function (args) {
                                            $.ajax({
                                                url: createURL('upgradeRouterTemplate'),
                                                data: {
                                                    id: args.context.routers[0].id
                                                },
                                                success: function (json) {
                                                    var jobs = json.upgraderoutertemplateresponse.asyncjobs;
                                                    if (jobs != undefined) {
                                                        args.response.success({
                                                            _custom: {
                                                                jobId: jobs[0].jobid
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        },
                                        notification: {
                                            poll: pollAsyncJobResult
                                        }
                                    },

                                    'remove': {
                                        label: 'label.destroy.router',
                                        messages: {
                                            confirm: function (args) {
                                                return 'message.confirm.destroy.router';
                                            },
                                            notification: function (args) {
                                                return 'label.destroy.router';
                                            }
                                        },
                                        action: function (args) {
                                            $.ajax({
                                                url: createURL("destroyRouter&id=" + args.context.routers[0].id),
                                                dataType: "json",
                                                async: true,
                                                success: function (json) {
                                                    var jid = json.destroyrouterresponse.jobid;
                                                    args.response.success({
                                                        _custom: {
                                                            jobId: jid
                                                        }
                                                    });
                                                }
                                            });
                                        },
                                        notification: {
                                            poll: pollAsyncJobResult
                                        }
                                    },

                                    restart: {
                                        label: 'label.action.reboot.router',
                                        messages: {
                                            confirm: function (args) {
                                                return 'message.action.reboot.router';
                                            },
                                            notification: function (args) {
                                                return 'label.action.reboot.router';
                                            }
                                        },
                                        action: function (args) {
                                            $.ajax({
                                                url: createURL('rebootRouter&id=' + args.context.routers[0].id),
                                                dataType: 'json',
                                                async: true,
                                                success: function (json) {
                                                    var jid = json.rebootrouterresponse.jobid;
                                                    args.response.success({
                                                        _custom: {
                                                            jobId: jid,
                                                            getUpdatedItem: function (json) {
                                                                return json.queryasyncjobresultresponse.jobresult.router;
                                                            },
                                                            getActionFilter: function () {
                                                                return routerActionfilter;
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
                                        label: 'label.action.migrate.router',
                                        createForm: {
                                            title: 'label.action.migrate.router',
                                            desc: '',
                                            fields: {
                                                hostId: {
                                                    label: 'label.host',
                                                    validation: {
                                                        required: true
                                                    },
                                                    select: function (args) {
                                                        $.ajax({
                                                            url: createURL("findHostsForMigration&VirtualMachineId=" + args.context.routers[0].id),
                                                            dataType: "json",
                                                            async: true,
                                                            success: function (json) {
                                                                var hostObjs = json.findhostsformigrationresponse.host;
                                                                var items = [];
                                                                $(hostObjs).each(function () {
                                                                    items.push({
                                                                        id: this.id,
                                                                        description: (this.name + " (" + (this.suitableformigration ? "Suitable" : "Not Suitable") + ")")
                                                                    });
                                                                });
                                                                args.response.success({
                                                                    data: items
                                                                });
                                                            }
                                                        });
                                                    },
                                                    error: function (XMLHttpResponse) {
                                                        var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
                                                        args.response.error(errorMsg);
                                                    }
                                                }
                                            }
                                        },
                                        messages: {
                                            notification: function (args) {
                                                return 'label.action.migrate.router';
                                            }
                                        },
                                        action: function (args) {
                                            $.ajax({
                                                url: createURL("migrateSystemVm&hostid=" + args.data.hostId + "&virtualmachineid=" + args.context.routers[0].id),
                                                dataType: "json",
                                                async: true,
                                                success: function (json) {
                                                    var jid = json.migratesystemvmresponse.jobid;
                                                    args.response.success({
                                                        _custom: {
                                                            jobId: jid,
                                                            getUpdatedItem: function (json) {
                                                                //return json.queryasyncjobresultresponse.jobresult.systemvminstance;    //not all properties returned in systemvminstance
                                                                $.ajax({
                                                                    url: createURL("listRouters&id=" + json.queryasyncjobresultresponse.jobresult.systemvm.id),
                                                                    dataType: "json",
                                                                    async: false,
                                                                    success: function (json) {
                                                                        var items = json.listroutersresponse.router;
                                                                        if (items != null && items.length > 0) {
                                                                            return items[0];
                                                                        }
                                                                    }
                                                                });
                                                            },
                                                            getActionFilter: function () {
                                                                return routerActionfilter;
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

                                    scaleUp: { //*** Infrastructure > Virtual Routers > change service offering ***
                                        label: 'label.change.service.offering',
                                        createForm: {
                                            title: 'label.change.service.offering',
                                            desc: function (args) {
                                                var description = '';
                                                var vmObj = args.jsonObj;
                                                if (vmObj.state == 'Running') {
                                                    description = 'message.read.admin.guide.scaling.up';
                                                }
                                                return description;
                                            },
                                            fields: {
                                                serviceOfferingId: {
                                                    label: 'label.compute.offering',
                                                    select: function (args) {
                                                        $.ajax({
                                                            url: createURL('listServiceOfferings'),
                                                            data: {
                                                                issystem: true,
                                                                systemvmtype: 'domainrouter',
                                                                virtualmachineid: args.context.routers[0].id
                                                            },
                                                            success: function (json) {
                                                                var serviceofferings = json.listserviceofferingsresponse.serviceoffering;
                                                                var items = [];
                                                                $(serviceofferings).each(function () {
                                                                    // if(this.id != args.context.routers[0].serviceofferingid) {
                                                                    items.push({
                                                                        id: this.id,
                                                                        description: this.name
                                                                    });
                                                                    //default one (i.e. "System Offering For Software Router") doesn't have displaytext property. So, got to use name property instead.
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
                                            $.ajax({
                                                url: createURL("scaleSystemVm&id=" + args.context.routers[0].id + "&serviceofferingid=" + args.data.serviceOfferingId),
                                                dataType: "json",
                                                async: true,
                                                success: function (json) {
                                                    var jid = json.changeserviceforsystemvmresponse.jobid;
                                                    args.response.success({
                                                        _custom: {
                                                            jobId: jid,
                                                            getUpdatedItem: function (json) {
                                                                return json.queryasyncjobresultresponse.jobresult.systemvm;
                                                            },
                                                            getActionFilter: function () {
                                                                return routerActionfilter;
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
                                            confirm: function (args) {
                                                return 'message.confirm.scale.up.router.vm';
                                            },
                                            notification: function (args) {

                                                return 'label.router.vm.scaled.up';
                                            }
                                        },
                                        notification: {
                                            poll: pollAsyncJobResult
                                        }
                                    },


                                    viewConsole: {
                                        label: 'label.view.console',
                                        action: {
                                            externalLink: {
                                                url: function (args) {
                                                    return clientConsoleUrl + '?cmd=access&vm=' + args.context.routers[0].id;
                                                },
                                                title: function (args) {
                                                    return args.context.routers[0].id.substr(0, 8);
                                                    //title in window.open() can't have space nor longer than 8 characters. Otherwise, IE browser will have error.
                                                },
                                                width: 820,
                                                height: 640
                                            }
                                        }
                                    }
                                },
                                tabs: {
                                    details: {
                                        title: 'label.details',
                                        preFilter: function (args) {
                                            var hiddenFields = [];
                                            if (!args.context.routers[0].project) {
                                                hiddenFields.push('project');
                                                hiddenFields.push('projectid');
                                            }
                                            $.ajax({
                                                url: createURL('listZones'),
                                                data: {
                                                    id: args.context.routers[0].zoneid
                                                },
                                                async: false,
                                                success: function (json) {
                                                    if (json.listzonesresponse.zone != undefined) {
                                                        var zoneObj = json.listzonesresponse.zone[0];
                                                        if (zoneObj.networktype == 'Basic') {
                                                            hiddenFields.push('publicip');
                                                            //In Basic zone, guest IP is public IP. So, publicip is not returned by listRouters API. Only guestipaddress is returned by listRouters API.
                                                        }
                                                    }
                                                }
                                            });

                                            if ('routers' in args.context && args.context.routers[0].vpcid != undefined) {
                                                hiddenFields.push('guestnetworkid');
                                                hiddenFields.push('guestnetworkname');
                                            } else if ('routers' in args.context && args.context.routers[0].guestnetworkid != undefined) {
                                                hiddenFields.push('vpcid');
                                                hiddenFields.push('vpcname');
                                            }

                                            return hiddenFields;
                                        },
                                        fields: [{
                                            name: {
                                                label: 'label.name'
                                            },
                                            project: {
                                                label: 'label.project'
                                            }
                                        },
                                            {
                                                id: {
                                                    label: 'label.id'
                                                },
                                                projectid: {
                                                    label: 'label.project.id'
                                                },
                                                state: {
                                                    label: 'label.state'
                                                },
                                                version: {
                                                    label: 'label.version'
                                                },
                                                requiresupgrade: {
                                                    label: 'label.requires.upgrade',
                                                    converter: cloudStack.converters.toBooleanText
                                                },
                                                guestnetworkid: {
                                                    label: 'label.network.id'
                                                },
                                                guestnetworkname: {
                                                    label: 'label.network.name'
                                                },
                                                vpcid: {
                                                    label: 'label.vpc.id'
                                                },
                                                vpcname: {
                                                    label: 'label.vpc'
                                                },
                                                publicip: {
                                                    label: 'label.public.ip'
                                                },
                                                guestipaddress: {
                                                    label: 'label.guest.ip'
                                                },
                                                linklocalip: {
                                                    label: 'label.linklocal.ip'
                                                },
                                                hostname: {
                                                    label: 'label.host'
                                                },
                                                serviceofferingname: {
                                                    label: 'label.compute.offering'
                                                },
                                                networkdomain: {
                                                    label: 'label.network.domain'
                                                },
                                                domain: {
                                                    label: 'label.domain'
                                                },
                                                account: {
                                                    label: 'label.account'
                                                },
                                                created: {
                                                    label: 'label.created',
                                                    converter: cloudStack.converters.toLocalDate
                                                },
                                                isredundantrouter: {
                                                    label: 'label.redundant.router',
                                                    converter: cloudStack.converters.toBooleanText
                                                },
                                                redundantRouterState: {
                                                    label: 'label.redundant.state'
                                                },
                                                vpcid: {
                                                    label: 'label.vpc.id'
                                                }
                                            }],
                                        dataProvider: function (args) {
                                            $.ajax({
                                                url: createURL("listRouters&id=" + args.context.routers[0].id),
                                                dataType: 'json',
                                                async: true,
                                                success: function (json) {
                                                    var jsonObj = json.listroutersresponse.router[0];
                                                    addExtraPropertiesToRouterInstanceObject(jsonObj);
                                                    args.response.success({
                                                        actionFilter: routerActionfilter,
                                                        data: jsonObj
                                                    });
                                                }
                                            });
                                        }
                                    },
                                    nics: {
                                        title: 'label.nics',
                                        multiple: true,
                                        fields: [{
                                            name: {
                                                label: 'label.name',
                                                header: true
                                            },
                                            type: {
                                                label: 'label.type'
                                            },
                                            traffictype: {
                                                label: 'label.traffic.type'
                                            },
                                            networkname: {
                                                label: 'label.network.name'
                                            },
                                            netmask: {
                                                label: 'label.netmask'
                                            },
                                            ipaddress: {
                                                label: 'label.ip.address'
                                            },
                                            id: {
                                                label: 'label.id'
                                            },
                                            networkid: {
                                                label: 'label.network.id'
                                            },
                                            isolationuri: {
                                                label: 'label.isolation.uri'
                                            },
                                            broadcasturi: {
                                                label: 'label.broadcast.uri'
                                            }
                                        }],
                                        dataProvider: function (args) {
                                            $.ajax({
                                                url: createURL("listRouters&id=" + args.context.routers[0].id),
                                                dataType: 'json',
                                                async: true,
                                                success: function (json) {
                                                    var jsonObj = json.listroutersresponse.router[0].nic;

                                                    args.response.success({
                                                        actionFilter: routerActionfilter,
                                                        data: $.map(jsonObj, function (nic, index) {
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
                                    }
                                }
                            }
                        }
                    },
                    routerGroupByZone: {
                        id: 'routerGroupByZone',
                        type: 'select',
                        title: 'label.group.by.zone',
                        listView: {
                            id: 'routerGroupByZone',
                            label: 'label.virtual.appliances',
                            fields: {
                                name: {
                                    label: 'label.zone'
                                },
                                routerCount: {
                                    label: 'label.total.virtual.routers'
                                },
                                routerRequiresUpgrade: {
                                    label: 'label.upgrade.required',
                                    converter: function (args) {
                                        if (args > 0) {
                                            return _l('label.yes');
                                        } else {
                                            return _l('label.no');
                                        }
                                    }
                                }
                            },

                            dataProvider: function (args) {
                                var array1 = [];
                                if (args.filterBy != null) {
                                    if (args.filterBy.search != null && args.filterBy.search.by != null && args.filterBy.search.value != null) {
                                        switch (args.filterBy.search.by) {
                                            case "name":
                                                if (args.filterBy.search.value.length > 0)
                                                    array1.push("&keyword=" + args.filterBy.search.value);
                                                break;
                                        }
                                    }
                                }
                                $.ajax({
                                    url: createURL("listZones&page=" + args.page + "&pagesize=" + pageSize + array1.join("")),
                                    dataType: "json",
                                    async: true,
                                    success: function (json) {
                                        var groupbyObjs = json.listzonesresponse.zone;
                                        if (groupbyObjs != null) {
                                            addExtraPropertiesToGroupbyObjects(groupbyObjs, 'zoneid');
                                        }
                                        args.response.success({
                                            data: groupbyObjs
                                        });
                                    }
                                });
                            },
                            detailView: {
                                name: 'label.virtual.routers.group.zone',
                                viewAll: {
                                    path: '_zone.virtualRouters',
                                    label: 'label.virtual.appliances'
                                },
                                actions: {
                                    upgradeRouterToUseNewerTemplate: {
                                        label: 'label.upgrade.router.newer.template',
                                        messages: {
                                            confirm: function (args) {
                                                return 'message.confirm.upgrade.routers.newtemplate';
                                            },
                                            notification: function (args) {
                                                return 'label.upgrade.router.newer.template';
                                            }
                                        },
                                        action: function (args) {
                                            $.ajax({
                                                url: createURL('upgradeRouterTemplate'),
                                                data: {
                                                    zoneid: args.context.routerGroupByZone[0].id
                                                },
                                                success: function (json) {
                                                    var jobs = json.upgraderoutertemplateresponse.asyncjobs;
                                                    if (jobs != undefined) {
                                                        args.response.success({
                                                            _custom: {
                                                                jobId: jobs[0].jobid
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        },
                                        notification: {
                                            poll: pollAsyncJobResult
                                        }
                                    }
                                },
                                tabs: {
                                    details: {
                                        title: 'label.virtual.routers.group.zone',
                                        fields: [{
                                            name: {
                                                label: 'label.zone'
                                            }
                                        },
                                            {
                                                routerCount: {
                                                    label: 'label.total.virtual.routers'
                                                },
                                                routerRequiresUpgrade: {
                                                    label: 'label.upgrade.required',
                                                    converter: function (args) {
                                                        if (args > 0) {
                                                            return _l('label.yes');
                                                        } else {
                                                            return _l('label.no');
                                                        }
                                                    }
                                                },
                                                numberOfRouterRequiresUpgrade: {
                                                    label: 'label.total.virtual.routers.upgrade'
                                                }
                                            }],
                                        dataProvider: function (args) {
                                            addExtraPropertiesToGroupbyObject(args.context.routerGroupByZone[0], 'zoneid');
                                            args.response.success({
                                                data: args.context.routerGroupByZone[0],
                                                actionFilter: routerGroupActionfilter
                                            });
                                        }
                                    }
                                }
                            }
                        }
                    },
                    routerGroupByPod: {
                        id: 'routerGroupByPod',
                        type: 'select',
                        title: 'label.group.by.pod',
                        listView: {
                            id: 'routerGroupByPod',
                            label: 'label.virtual.appliances',
                            fields: {
                                name: {
                                    label: 'label.pod'
                                },
                                routerCount: {
                                    label: 'label.total.virtual.routers'
                                },
                                routerRequiresUpgrade: {
                                    label: 'label.upgrade.required',
                                    converter: function (args) {
                                        if (args > 0) {
                                            return _l('label.yes');
                                        } else {
                                            return _l('label.no');
                                        }
                                    }
                                }
                            },

                            dataProvider: function (args) {
                                var array1 = [];
                                if (args.filterBy != null) {
                                    if (args.filterBy.search != null && args.filterBy.search.by != null && args.filterBy.search.value != null) {
                                        switch (args.filterBy.search.by) {
                                            case "name":
                                                if (args.filterBy.search.value.length > 0)
                                                    array1.push("&keyword=" + args.filterBy.search.value);
                                                break;
                                        }
                                    }
                                }
                                $.ajax({
                                    url: createURL("listPods&page=" + args.page + "&pagesize=" + pageSize + array1.join("")),
                                    dataType: "json",
                                    async: true,
                                    success: function (json) {
                                        var groupbyObjs = json.listpodsresponse.pod;
                                        if (groupbyObjs != null) {
                                            addExtraPropertiesToGroupbyObjects(groupbyObjs, 'podid');
                                        }
                                        args.response.success({
                                            data: groupbyObjs
                                        });
                                    }
                                });
                            },
                            detailView: {
                                name: 'label.virtual.routers.group.pod',
                                viewAll: {
                                    path: '_zone.virtualRouters',
                                    label: 'label.virtual.appliances'
                                },
                                actions: {
                                    upgradeRouterToUseNewerTemplate: {
                                        label: 'label.upgrade.router.newer.template',
                                        messages: {
                                            confirm: function (args) {
                                                return 'message.confirm.upgrade.routers.pod.newtemplate';
                                            },
                                            notification: function (args) {
                                                return 'label.upgrade.router.newer.template';
                                            }
                                        },
                                        action: function (args) {
                                            $.ajax({
                                                url: createURL('upgradeRouterTemplate'),
                                                data: {
                                                    podid: args.context.routerGroupByPod[0].id
                                                },
                                                success: function (json) {
                                                    var jobs = json.upgraderoutertemplateresponse.asyncjobs;
                                                    if (jobs != undefined) {
                                                        args.response.success({
                                                            _custom: {
                                                                jobId: jobs[0].jobid
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        },
                                        notification: {
                                            poll: pollAsyncJobResult
                                        }
                                    }
                                },
                                tabs: {
                                    details: {
                                        title: 'label.virtual.routers.group.pod',
                                        fields: [{
                                            name: {
                                                label: 'label.pod'
                                            }
                                        },
                                            {
                                                routerCount: {
                                                    label: 'label.total.virtual.routers'
                                                },
                                                routerRequiresUpgrade: {
                                                    label: 'label.upgrade.required',
                                                    converter: function (args) {
                                                        if (args > 0) {
                                                            return _l('label.yes');
                                                        } else {
                                                            return _l('label.no');
                                                        }
                                                    }
                                                },
                                                numberOfRouterRequiresUpgrade: {
                                                    label: 'label.total.virtual.routers.upgrade'
                                                },
                                                zonename: {
                                                    label: 'label.zone'
                                                }
                                            }],
                                        dataProvider: function (args) {
                                            addExtraPropertiesToGroupbyObject(args.context.routerGroupByPod[0], 'podid');
                                            args.response.success({
                                                data: args.context.routerGroupByPod[0],
                                                actionFilter: routerGroupActionfilter
                                            });
                                        }
                                    }
                                }
                            }
                        }
                    },
                    routerGroupByCluster: {
                        id: 'routerGroupByCluster',
                        type: 'select',
                        title: 'label.group.by.cluster',
                        listView: {
                            id: 'routerGroupByCluster',
                            label: 'label.virtual.appliances',
                            fields: {
                                name: {
                                    label: 'label.cluster'
                                },
                                routerCount: {
                                    label: 'label.total.virtual.routers'
                                },
                                routerRequiresUpgrade: {
                                    label: 'label.upgrade.required',
                                    converter: function (args) {
                                        if (args > 0) {
                                            return _l('label.yes');
                                        } else {
                                            return _l('label.no');
                                        }
                                    }
                                }
                            },

                            dataProvider: function (args) {
                                var array1 = [];
                                if (args.filterBy != null) {
                                    if (args.filterBy.search != null && args.filterBy.search.by != null && args.filterBy.search.value != null) {
                                        switch (args.filterBy.search.by) {
                                            case "name":
                                                if (args.filterBy.search.value.length > 0)
                                                    array1.push("&keyword=" + args.filterBy.search.value);
                                                break;
                                        }
                                    }
                                }
                                $.ajax({
                                    url: createURL("listClusters&page=" + args.page + "&pagesize=" + pageSize + array1.join("")),
                                    dataType: "json",
                                    async: true,
                                    success: function (json) {
                                        var groupbyObjs = json.listclustersresponse.cluster;
                                        if (groupbyObjs != null) {
                                            addExtraPropertiesToGroupbyObjects(groupbyObjs, 'clusterid');
                                        }
                                        args.response.success({
                                            data: groupbyObjs
                                        });
                                    }
                                });
                            },
                            detailView: {
                                name: 'label.virtual.routers.group.cluster',
                                viewAll: {
                                    path: '_zone.virtualRouters',
                                    label: 'label.virtual.appliances'
                                },
                                actions: {
                                    upgradeRouterToUseNewerTemplate: {
                                        label: 'label.upgrade.router.newer.template',
                                        messages: {
                                            confirm: function (args) {
                                                return 'message.confirm.upgrade.routers.cluster.newtemplate';
                                            },
                                            notification: function (args) {
                                                return 'label.upgrade.router.newer.template';
                                            }
                                        },
                                        action: function (args) {
                                            $.ajax({
                                                url: createURL('upgradeRouterTemplate'),
                                                data: {
                                                    clusterid: args.context.routerGroupByCluster[0].id
                                                },
                                                success: function (json) {
                                                    var jobs = json.upgraderoutertemplateresponse.asyncjobs;
                                                    if (jobs != undefined) {
                                                        args.response.success({
                                                            _custom: {
                                                                jobId: jobs[0].jobid
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        },
                                        notification: {
                                            poll: pollAsyncJobResult
                                        }
                                    }
                                },
                                tabs: {
                                    details: {
                                        title: 'label.virtual.routers.group.cluster',
                                        fields: [{
                                            name: {
                                                label: 'label.cluster'
                                            }
                                        },
                                            {
                                                routerCount: {
                                                    label: 'label.total.virtual.routers'
                                                },
                                                routerRequiresUpgrade: {
                                                    label: 'label.upgrade.required',
                                                    converter: function (args) {
                                                        if (args > 0) {
                                                            return _l('label.yes');
                                                        } else {
                                                            return _l('label.no');
                                                        }
                                                    }
                                                },
                                                numberOfRouterRequiresUpgrade: {
                                                    label: 'label.total.virtual.routers.upgrade'
                                                },
                                                podname: {
                                                    label: 'label.pod'
                                                },
                                                zonename: {
                                                    label: 'label.zone.lower'
                                                }
                                            }],
                                        dataProvider: function (args) {
                                            addExtraPropertiesToGroupbyObject(args.context.routerGroupByCluster[0], 'clusterid');
                                            args.response.success({
                                                data: args.context.routerGroupByCluster[0],
                                                actionFilter: routerGroupActionfilter
                                            });
                                        }
                                    }
                                }
                            }
                        }
                    },
                    routerGroupByAccount: {
                        id: 'routerGroupByAccount',
                        type: 'select',
                        title: 'label.group.by.account',
                        listView: {
                            id: 'routerGroupByAccount',
                            label: 'label.virtual.appliances',
                            fields: {
                                name: {
                                    label: 'label.account'
                                },
                                domain: {
                                    label: 'label.domain'
                                },
                                routerCount: {
                                    label: 'label.total.virtual.routers'
                                },
                                routerRequiresUpgrade: {
                                    label: 'label.upgrade.required',
                                    converter: function (args) {
                                        if (args > 0) {
                                            return _l('label.yes');
                                        } else {
                                            return _l('label.no');
                                        }
                                    }
                                }
                            },

                            dataProvider: function (args) {
                                var array1 = [];
                                if (args.filterBy != null) {
                                    if (args.filterBy.search != null && args.filterBy.search.by != null && args.filterBy.search.value != null) {
                                        switch (args.filterBy.search.by) {
                                            case "name":
                                                if (args.filterBy.search.value.length > 0)
                                                    array1.push("&keyword=" + args.filterBy.search.value);
                                                break;
                                        }
                                    }
                                }
                                $.ajax({
                                    url: createURL("listAccounts&listAll=true&page=" + args.page + "&pagesize=" + pageSize + array1.join("")),
                                    success: function (json) {
                                        var accountObjs = json.listaccountsresponse.account;
                                        if (accountObjs != null) {
                                            for (var i = 0; i < accountObjs.length; i++) {
                                                var currentPage = 1;
                                                $.ajax({
                                                    url: createURL('listRouters'),
                                                    data: {
                                                        account: accountObjs[i].name,
                                                        domainid: accountObjs[i].domainid,
                                                        listAll: true,
                                                        page: currentPage,
                                                        pagesize: pageSize //global variable
                                                    },
                                                    async: false,
                                                    success: function (json) {
                                                        if (json.listroutersresponse.count != undefined) {
                                                            accountObjs[i].routerCount = json.listroutersresponse.count;
                                                            var routerCountFromAllPages = json.listroutersresponse.count;
                                                            var routerCountFromFirstPageToCurrentPage = json.listroutersresponse.router.length;
                                                            var routerRequiresUpgrade = 0;

                                                            var items = json.listroutersresponse.router;
                                                            for (var k = 0; k < items.length; k++) {
                                                                if (items[k].requiresupgrade) {
                                                                    routerRequiresUpgrade++;
                                                                }
                                                            }

                                                            var callListApiWithPage = function () {
                                                                $.ajax({
                                                                    url: createURL('listRouters'),
                                                                    async: false,
                                                                    data: {
                                                                        account: accountObjs[i].name,
                                                                        domainid: accountObjs[i].domainid,
                                                                        listAll: true,
                                                                        page: currentPage,
                                                                        pagesize: pageSize //global variable
                                                                    },
                                                                    success: function (json) {
                                                                        routerCountFromFirstPageToCurrentPage += json.listroutersresponse.router.length;
                                                                        var items = json.listroutersresponse.router;
                                                                        for (var k = 0; k < items.length; k++) {
                                                                            if (items[k].requiresupgrade) {
                                                                                routerRequiresUpgrade++;
                                                                            }
                                                                        }
                                                                        if (routerCountFromFirstPageToCurrentPage < routerCountFromAllPages) {
                                                                            currentPage++;
                                                                            callListApiWithPage();
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                            if (routerCountFromFirstPageToCurrentPage < routerCountFromAllPages) {
                                                                currentPage++;
                                                                callListApiWithPage();
                                                            }
                                                            accountObjs[i].routerRequiresUpgrade = routerRequiresUpgrade;
                                                        } else {
                                                            accountObjs[i].routerCount = 0;
                                                            accountObjs[i].routerRequiresUpgrade = 0;
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                        args.response.success({
                                            data: accountObjs
                                        });
                                    }
                                });
                            },
                            detailView: {
                                name: 'label.virtual.routers.group.account',
                                viewAll: {
                                    path: '_zone.virtualRouters',
                                    label: 'label.virtual.appliances'
                                },
                                actions: {
                                    upgradeRouterToUseNewerTemplate: {
                                        label: 'label.upgrade.router.newer.template',
                                        messages: {
                                            confirm: function (args) {
                                                return 'message.confirm.upgrade.routers.account.newtemplate';
                                            },
                                            notification: function (args) {
                                                return 'label.upgrade.router.newer.template';
                                            }
                                        },
                                        action: function (args) {
                                            $.ajax({
                                                url: createURL('upgradeRouterTemplate'),
                                                data: {
                                                    account: args.context.routerGroupByAccount[0].name,
                                                    domainid: args.context.routerGroupByAccount[0].domainid
                                                },
                                                success: function (json) {
                                                    var jobs = json.upgraderoutertemplateresponse.asyncjobs;
                                                    if (jobs != undefined) {
                                                        args.response.success({
                                                            _custom: {
                                                                jobId: jobs[0].jobid
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        },
                                        notification: {
                                            poll: pollAsyncJobResult
                                        }
                                    }
                                },
                                tabs: {
                                    details: {
                                        title: 'label.virtual.routers.group.account',
                                        fields: [{
                                            name: {
                                                label: 'label.account'
                                            },
                                            domain: {
                                                label: 'label.domain'
                                            }
                                        },
                                            {
                                                routerCount: {
                                                    label: 'label.total.virtual.routers'
                                                },
                                                routerRequiresUpgrade: {
                                                    label: 'label.upgrade.required',
                                                    converter: function (args) {
                                                        if (args > 0) {
                                                            return _l('label.yes');
                                                        } else {
                                                            return _l('label.no');
                                                        }
                                                    }
                                                },
                                                numberOfRouterRequiresUpgrade: {
                                                    label: 'label.total.virtual.routers.upgrade'
                                                }
                                            }],
                                        dataProvider: function (args) {
                                            var currentPage = 1;
                                            $.ajax({
                                                url: createURL('listRouters'),
                                                data: {
                                                    account: args.context.routerGroupByAccount[0].name,
                                                    domainid: args.context.routerGroupByAccount[0].domainid,
                                                    listAll: true,
                                                    page: currentPage,
                                                    pagesize: pageSize //global variable
                                                },
                                                async: false,
                                                success: function (json) {
                                                    if (json.listroutersresponse.count != undefined) {
                                                        args.context.routerGroupByAccount[0].routerCount = json.listroutersresponse.count;
                                                        var routerCountFromAllPages = json.listroutersresponse.count;
                                                        var routerCountFromFirstPageToCurrentPage = json.listroutersresponse.router.length;
                                                        var routerRequiresUpgrade = 0;

                                                        var items = json.listroutersresponse.router;
                                                        for (var k = 0; k < items.length; k++) {
                                                            if (items[k].requiresupgrade) {
                                                                routerRequiresUpgrade++;
                                                            }
                                                        }

                                                        var callListApiWithPage = function () {
                                                            $.ajax({
                                                                url: createURL('listRouters'),
                                                                async: false,
                                                                data: {
                                                                    account: args.context.routerGroupByAccount[0].name,
                                                                    domainid: args.context.routerGroupByAccount[0].domainid,
                                                                    listAll: true,
                                                                    page: currentPage,
                                                                    pagesize: pageSize //global variable
                                                                },
                                                                success: function (json) {
                                                                    routerCountFromFirstPageToCurrentPage += json.listroutersresponse.router.length;
                                                                    var items = json.listroutersresponse.router;
                                                                    for (var k = 0; k < items.length; k++) {
                                                                        if (items[k].requiresupgrade) {
                                                                            routerRequiresUpgrade++;
                                                                        }
                                                                    }
                                                                    if (routerCountFromFirstPageToCurrentPage < routerCountFromAllPages) {
                                                                        currentPage++;
                                                                        callListApiWithPage();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                        if (routerCountFromFirstPageToCurrentPage < routerCountFromAllPages) {
                                                            currentPage++;
                                                            callListApiWithPage();
                                                        }
                                                        args.context.routerGroupByAccount[0].routerRequiresUpgrade = routerRequiresUpgrade;
                                                        args.context.routerGroupByAccount[0].numberOfRouterRequiresUpgrade = routerRequiresUpgrade;
                                                    } else {
                                                        args.context.routerGroupByAccount[0].routerCount = 0;
                                                        args.context.routerGroupByAccount[0].routerRequiresUpgrade = 0;
                                                        args.context.routerGroupByAccount[0].numberOfRouterRequiresUpgrade = 0;
                                                    }
                                                }
                                            });
                                            setTimeout(function () {
                                                args.response.success({
                                                    data: args.context.routerGroupByAccount[0],
                                                    actionFilter: routerGroupActionfilter
                                                });
                                            });
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            systemVms: {
                listView: {
                    label: 'label.system.vms',
                    id: 'systemVMs',
                    fields: {
                        name: {
                            label: 'label.name'
                        },
                        systemvmtype: {
                            label: 'label.type',
                            converter: function (args) {
                                if (args == "consoleproxy")
                                    return "Console Proxy VM"; else if (args == "secondarystoragevm")
                                    return "Secondary Storage VM"; else
                                    return args;
                            }
                        },
                        zonename: {
                            label: 'label.zone'
                        },
                        state: {
                            label: 'label.vm.state',
                            converter: function (str) {
                                // For localization
                                return str;
                            },
                            indicator: {
                                'Running': 'on',
                                'Stopped': 'off',
                                'Error': 'off',
                                'Destroyed': 'off'
                            }
                        },

                        agentstate: {
                            label: 'label.agent.state',
                            indicator: {
                                'Up': 'on',
                                'Down': 'off'
                            }
                        }
                    },
                    dataProvider: function (args) {
                        var array1 = [];
                        if (args.filterBy != null) {
                            if (args.filterBy.search != null && args.filterBy.search.by != null && args.filterBy.search.value != null) {
                                switch (args.filterBy.search.by) {
                                    case "name":
                                        if (args.filterBy.search.value.length > 0)
                                            array1.push("&keyword=" + args.filterBy.search.value);
                                        break;
                                }
                            }
                        }

                        var selectedZoneObj = args.context.physicalResources[0];
                        $.ajax({
                            url: createURL("listSystemVms&zoneid=" + selectedZoneObj.id + "&page=" + args.page + "&pagesize=" + pageSize + array1.join("")),
                            dataType: "json",
                            async: true,
                            success: function (json) {
                                var items = json.listsystemvmsresponse.systemvm;
                                args.response.success({
                                    actionFilter: systemvmActionfilter,
                                    data: items
                                });
                            }
                        });
                    },

                    detailView: {
                        name: 'label.system.vm.details',
                        actions: {
                            start: {
                                label: 'label.action.start.systemvm',
                                messages: {
                                    confirm: function (args) {
                                        return 'message.action.start.systemvm';
                                    },
                                    notification: function (args) {
                                        return 'label.action.start.systemvm';
                                    }
                                },
                                action: function (args) {
                                    $.ajax({
                                        url: createURL('startSystemVm&id=' + args.context.systemVMs[0].id),
                                        dataType: 'json',
                                        async: true,
                                        success: function (json) {
                                            var jid = json.startsystemvmresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getUpdatedItem: function (json) {
                                                        return json.queryasyncjobresultresponse.jobresult.systemvm;
                                                    },
                                                    getActionFilter: function () {
                                                        return systemvmActionfilter;
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

                            stop: {
                                label: 'label.action.stop.systemvm',
                                messages: {
                                    confirm: function (args) {
                                        return 'message.action.stop.systemvm';
                                    },
                                    notification: function (args) {
                                        return 'label.action.stop.systemvm';
                                    }
                                },
                                action: function (args) {
                                    $.ajax({
                                        url: createURL('stopSystemVm&id=' + args.context.systemVMs[0].id),
                                        dataType: 'json',
                                        async: true,
                                        success: function (json) {
                                            var jid = json.stopsystemvmresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getUpdatedItem: function (json) {
                                                        return json.queryasyncjobresultresponse.jobresult.systemvm;
                                                    },
                                                    getActionFilter: function () {
                                                        return systemvmActionfilter;
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

                            restart: {
                                label: 'label.action.reboot.systemvm',
                                messages: {
                                    confirm: function (args) {
                                        return 'message.action.reboot.systemvm';
                                    },
                                    notification: function (args) {
                                        return 'label.action.reboot.systemvm';
                                    }
                                },
                                action: function (args) {
                                    $.ajax({
                                        url: createURL('rebootSystemVm&id=' + args.context.systemVMs[0].id),
                                        dataType: 'json',
                                        async: true,
                                        success: function (json) {
                                            var jid = json.rebootsystemvmresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getUpdatedItem: function (json) {
                                                        return json.queryasyncjobresultresponse.jobresult.systemvm;
                                                    },
                                                    getActionFilter: function () {
                                                        return systemvmActionfilter;
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

                            remove: {
                                label: 'label.action.destroy.systemvm',
                                messages: {
                                    confirm: function (args) {
                                        return 'message.action.destroy.systemvm';
                                    },
                                    notification: function (args) {
                                        return 'label.action.destroy.systemvm';
                                    }
                                },
                                action: function (args) {
                                    $.ajax({
                                        url: createURL('destroySystemVm&id=' + args.context.systemVMs[0].id),
                                        dataType: 'json',
                                        async: true,
                                        success: function (json) {
                                            var jid = json.destroysystemvmresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    getUpdatedItem: function () {
                                                        return {
                                                            state: 'Destroyed'
                                                        };
                                                    },
                                                    jobId: jid
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
                                label: 'label.action.migrate.systemvm',
                                messages: {
                                    notification: function (args) {
                                        return 'label.action.migrate.systemvm';
                                    }
                                },
                                createForm: {
                                    title: 'label.action.migrate.systemvm',
                                    desc: '',
                                    fields: {
                                        hostId: {
                                            label: 'label.host',
                                            validation: {
                                                required: true
                                            },
                                            select: function (args) {
                                                $.ajax({
                                                    url: createURL("findHostsForMigration&VirtualMachineId=" + args.context.systemVMs[0].id),
                                                    dataType: "json",
                                                    async: true,
                                                    success: function (json) {
                                                        var hostObjs = json.findhostsformigrationresponse.host;
                                                        var items = [];
                                                        $(hostObjs).each(function () {
                                                            if (this.requiresStorageMotion == false) {
                                                                items.push({
                                                                    id: this.id,
                                                                    description: (this.name + " (" + (this.suitableformigration ? "Suitable" : "Not Suitable") + ")")
                                                                });
                                                            }
                                                        });
                                                        args.response.success({
                                                            data: items
                                                        });
                                                    }
                                                });
                                            },
                                            error: function (XMLHttpResponse) {
                                                var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
                                                args.response.error(errorMsg);
                                            }
                                        }
                                    }
                                },
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("migrateSystemVm&hostid=" + args.data.hostId + "&virtualmachineid=" + args.context.systemVMs[0].id),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var jid = json.migratesystemvmresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getUpdatedItem: function (json) {
                                                        //return json.queryasyncjobresultresponse.jobresult.systemvminstance;    //not all properties returned in systemvminstance
                                                        $.ajax({
                                                            url: createURL("listSystemVms&id=" + json.queryasyncjobresultresponse.jobresult.systemvm.id),
                                                            dataType: "json",
                                                            async: false,
                                                            success: function (json) {
                                                                var items = json.listsystemvmsresponse.systemvm;
                                                                if (items != null && items.length > 0) {
                                                                    return items[0];
                                                                }
                                                            }
                                                        });
                                                    },
                                                    getActionFilter: function () {
                                                        return systemvmActionfilter;
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

                            scaleUp: { //*** Infrastructure > System VMs (consoleProxy or SSVM) > change service offering ***
                                label: 'label.change.service.offering',
                                createForm: {
                                    title: 'label.change.service.offering',
                                    desc: function (args) {
                                        var description = '';
                                        var vmObj = args.jsonObj;
                                        if (vmObj.state == 'Running') {
                                            description = 'message.read.admin.guide.scaling.up';
                                        }
                                        return description;
                                    },
                                    fields: {
                                        serviceOfferingId: {
                                            label: 'label.compute.offering',
                                            select: function (args) {
                                                var data1 = {
                                                    issystem: 'true',
                                                    virtualmachineid: args.context.systemVMs[0].id
                                                };
                                                if (args.context.systemVMs[0].systemvmtype == "secondarystoragevm") {
                                                    $.extend(data1, {
                                                        systemvmtype: 'secondarystoragevm'
                                                    });
                                                }
                                                else if (args.context.systemVMs[0].systemvmtype == "consoleproxy") {
                                                    $.extend(data1, {
                                                        systemvmtype: 'consoleproxy'
                                                    });
                                                }
                                                $.ajax({
                                                    url: createURL('listServiceOfferings'),
                                                    data: data1,
                                                    success: function (json) {
                                                        var serviceofferings = json.listserviceofferingsresponse.serviceoffering;
                                                        var items = [];
                                                        $(serviceofferings).each(function () {
                                                            if (this.id != args.context.systemVMs[0].serviceofferingid) {
                                                                items.push({
                                                                    id: this.id,
                                                                    description: this.name
                                                                });
                                                            }
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
                                    $.ajax({
                                        url: createURL("scaleSystemVm&id=" + args.context.systemVMs[0].id + "&serviceofferingid=" + args.data.serviceOfferingId),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var jid = json.changeserviceforsystemvmresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getUpdatedItem: function (json) {
                                                        return json.queryasyncjobresultresponse.jobresult.systemvm;
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
                                    confirm: function (args) {
                                        return 'message.confirm.scale.up.system.vm';
                                    },
                                    notification: function (args) {

                                        return 'label.system.vm.scaled.up';
                                    }
                                },
                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            },


                            viewConsole: {
                                label: 'label.view.console',
                                action: {
                                    externalLink: {
                                        url: function (args) {
                                            return clientConsoleUrl + '?cmd=access&vm=' + args.context.systemVMs[0].id;
                                        },
                                        title: function (args) {
                                            return args.context.systemVMs[0].id.substr(0, 8);
                                            //title in window.open() can't have space nor longer than 8 characters. Otherwise, IE browser will have error.
                                        },
                                        width: 820,
                                        height: 640
                                    }
                                }
                            }
                        },
                        tabs: {
                            details: {
                                title: 'label.details',
                                fields: [{
                                    name: {
                                        label: 'label.name'
                                    }
                                },
                                    {
                                        id: {
                                            label: 'label.id'
                                        },
                                        state: {
                                            label: 'label.state'
                                        },
                                        systemvmtype: {
                                            label: 'label.type',
                                            converter: function (args) {
                                                if (args == "consoleproxy")
                                                    return "Console Proxy VM"; else if (args == "secondarystoragevm")
                                                    return "Secondary Storage VM"; else
                                                    return args;
                                            }
                                        },
                                        zonename: {
                                            label: 'label.zone'
                                        },
                                        publicip: {
                                            label: 'label.public.ip'
                                        },
                                        privateip: {
                                            label: 'label.private.ip'
                                        },
                                        linklocalip: {
                                            label: 'label.linklocal.ip'
                                        },
                                        hostname: {
                                            label: 'label.host'
                                        },
                                        gateway: {
                                            label: 'label.gateway'
                                        },
                                        created: {
                                            label: 'label.created',
                                            converter: cloudStack.converters.toLocalDate
                                        },
                                        activeviewersessions: {
                                            label: 'label.active.sessions'
                                        }
                                    }],
                                dataProvider: function (args) {
                                    $.ajax({
                                        url: createURL("listSystemVms&id=" + args.context.systemVMs[0].id),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            args.response.success({
                                                actionFilter: systemvmActionfilter,
                                                data: json.listsystemvmsresponse.systemvm[0]
                                            });
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            },

            //SRX devices listView
            srxDevices: {
                id: 'srxDevices',
                title: 'label.devices',
                listView: {
                    id: 'srxDevices',
                    fields: {
                        ipaddress: {
                            label: 'label.ip.address'
                        },
                        fwdevicestate: {
                            label: 'label.status'
                        },
                        fwdevicename: {
                            label: 'label.type'
                        }
                    },
                    actions: {
                        add: {
                            label: 'label.add.SRX.device',
                            createForm: {
                                title: 'label.add.SRX.device',
                                fields: {
                                    ip: {
                                        label: 'label.ip.address'
                                    },
                                    username: {
                                        label: 'label.username'
                                    },
                                    password: {
                                        label: 'label.password',
                                        isPassword: true
                                    },
                                    networkdevicetype: {
                                        label: 'label.type',
                                        select: function (args) {
                                            var items = [];
                                            items.push({
                                                id: "JuniperSRXFirewall",
                                                description: "Juniper SRX Firewall"
                                            });
                                            args.response.success({
                                                data: items
                                            });
                                        }
                                    },
                                    publicinterface: {
                                        label: 'label.public.interface'
                                    },
                                    privateinterface: {
                                        label: 'label.private.interface'
                                    },
                                    usageinterface: {
                                        label: 'label.usage.interface'
                                    },
                                    numretries: {
                                        label: 'label.numretries',
                                        defaultValue: '2'
                                    },
                                    timeout: {
                                        label: 'label.timeout',
                                        defaultValue: '300'
                                    },
                                    publicnetwork: {
                                        label: 'label.public.network',
                                        defaultValue: 'untrusted',
                                        isDisabled: true
                                    },
                                    privatenetwork: {
                                        label: 'label.private.network',
                                        defaultValue: 'trusted',
                                        isDisabled: true
                                    },
                                    capacity: {
                                        label: 'label.capacity',
                                        validation: {
                                            required: false,
                                            number: true
                                        }
                                    },
                                    dedicated: {
                                        label: 'label.dedicated',
                                        isBoolean: true,
                                        isChecked: false
                                    }
                                }
                            },
                            action: function (args) {
                                if (nspMap["srx"] == null) {
                                    $.ajax({
                                        url: createURL("addNetworkServiceProvider&name=JuniperSRX&physicalnetworkid=" + selectedPhysicalNetworkObj.id),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var jobId = json.addnetworkserviceproviderresponse.jobid;
                                            var addJuniperSRXProviderIntervalID = setInterval(function () {
                                                    $.ajax({
                                                        url: createURL("queryAsyncJobResult&jobId=" + jobId),
                                                        dataType: "json",
                                                        success: function (json) {
                                                            var result = json.queryasyncjobresultresponse;
                                                            if (result.jobstatus == 0) {
                                                                return; //Job has not completed
                                                            } else {
                                                                clearInterval(addJuniperSRXProviderIntervalID);
                                                                if (result.jobstatus == 1) {
                                                                    nspMap["srx"] = json.queryasyncjobresultresponse.jobresult.networkserviceprovider;
                                                                    addExternalFirewall(args, selectedPhysicalNetworkObj, "addSrxFirewall", "addsrxfirewallresponse", "srxfirewall");
                                                                } else if (result.jobstatus == 2) {
                                                                    alert("addNetworkServiceProvider&name=JuniperSRX failed. Error: " + _s(result.jobresult.errortext));
                                                                }
                                                            }
                                                        },
                                                        error: function (XMLHttpResponse) {
                                                            var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
                                                            alert("addNetworkServiceProvider&name=JuniperSRX failed. Error: " + errorMsg);
                                                        }
                                                    });
                                                },
                                                g_queryAsyncJobResultInterval);
                                        }
                                    });
                                } else {
                                    addExternalFirewall(args, selectedPhysicalNetworkObj, "addSrxFirewall", "addsrxfirewallresponse", "srxfirewall");
                                }
                            },
                            messages: {
                                notification: function (args) {
                                    return 'label.add.SRX.device';
                                }
                            },
                            notification: {
                                poll: pollAsyncJobResult
                            }
                        }
                    },
                    dataProvider: function (args) {
                        $.ajax({
                            url: createURL("listSrxFirewalls&physicalnetworkid=" + selectedPhysicalNetworkObj.id),
                            data: {
                                page: args.page,
                                pageSize: pageSize
                            },
                            dataType: "json",
                            async: false,
                            success: function (json) {
                                var items = json.listsrxfirewallresponse.srxfirewall;
                                args.response.success({
                                    data: items
                                });
                            }
                        });
                    },
                    detailView: {
                        name: 'label.srx.details',
                        actions: {
                            'remove': {
                                label: 'label.delete.SRX',
                                messages: {
                                    confirm: function (args) {
                                        return 'message.confirm.delete.SRX';
                                    },
                                    notification: function (args) {
                                        return 'label.delete.SRX';
                                    }
                                },
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("deleteSrxFirewall&fwdeviceid=" + args.context.srxDevices[0].fwdeviceid),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var jid = json.deletesrxfirewallresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid
                                                }
                                            });
                                        }
                                    });
                                },
                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            }
                        },
                        tabs: {
                            details: {
                                title: 'label.details',
                                fields: [{
                                    fwdeviceid: {
                                        label: 'label.id'
                                    },
                                    ipaddress: {
                                        label: 'label.ip.address'
                                    },
                                    fwdevicestate: {
                                        label: 'label.status'
                                    },
                                    fwdevicename: {
                                        label: 'label.type'
                                    },
                                    fwdevicecapacity: {
                                        label: 'label.capacity'
                                    },
                                    timeout: {
                                        label: 'label.timeout'
                                    }
                                }],
                                dataProvider: function (args) {
                                    $.ajax({
                                        url: createURL("listSrxFirewalls&fwdeviceid=" + args.context.srxDevices[0].fwdeviceid),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var item = json.listsrxfirewallresponse.srxfirewall[0];
                                            args.response.success({
                                                data: item
                                            });
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            },

            // FIXME convert to nicira detailview
            // NiciraNvp devices listView
            niciraNvpDevices: {
                id: 'niciraNvpDevices',
                title: 'label.devices',
                listView: {
                    id: 'niciraNvpDevices',
                    fields: {
                        hostname: {
                            label: 'label.nicira.controller.address'
                        },
                        transportzoneuuid: {
                            label: 'label.nicira.transportzoneuuid'
                        },
                        l3gatewayserviceuuid: {
                            label: 'label.nicira.l3gatewayserviceuuid'
                        },
                        l2gatewayserviceuuid: {
                            label: 'label.nicira.l2gatewayserviceuuid'
                        }
                    },
                    actions: {
                        add: {
                            label: 'label.add.NiciraNvp.device',
                            createForm: {
                                title: 'label.add.NiciraNvp.device',
                                preFilter: function (args) {
                                },
                                // TODO What is this?
                                fields: {
                                    host: {
                                        label: 'label.ip.address'
                                    },
                                    username: {
                                        label: 'label.username'
                                    },
                                    password: {
                                        label: 'label.password',
                                        isPassword: true
                                    },
                                    numretries: {
                                        label: 'label.numretries',
                                        defaultValue: '2'
                                    },
                                    transportzoneuuid: {
                                        label: 'label.nicira.transportzoneuuid'
                                    },
                                    l3gatewayserviceuuid: {
                                        label: 'label.nicira.l3gatewayserviceuuid'
                                    },
                                    l2gatewayserviceuuid: {
                                        label: 'label.nicira.l2gatewayserviceuuid'
                                    }
                                }
                            },
                            action: function (args) {
                                if (nspMap["niciraNvp"] == null) {
                                    $.ajax({
                                        url: createURL("addNetworkServiceProvider&name=NiciraNvp&physicalnetworkid=" + selectedPhysicalNetworkObj.id),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var jobId = json.addnetworkserviceproviderresponse.jobid;
                                            var addNiciraNvpProviderIntervalID = setInterval(function () {
                                                    $.ajax({
                                                        url: createURL("queryAsyncJobResult&jobId=" + jobId),
                                                        dataType: "json",
                                                        success: function (json) {
                                                            var result = json.queryasyncjobresultresponse;
                                                            if (result.jobstatus == 0) {
                                                                return; // Job has not completed
                                                            } else {
                                                                clearInterval(addNiciraNvpProviderIntervalID);
                                                                if (result.jobstatus == 1) {
                                                                    nspMap["niciraNvp"] = json.queryasyncjobresultresponse.jobresult.networkserviceprovider;
                                                                    addNiciraNvpDevice(args, selectedPhysicalNetworkObj, "addNiciraNvpDevice", "addniciranvpdeviceresponse", "niciranvpdevice")
                                                                } else if (result.jobstatus == 2) {
                                                                    alert("addNetworkServiceProvider&name=NiciraNvp failed. Error: " + _s(result.jobresult.errortext));
                                                                }
                                                            }
                                                        },
                                                        error: function (XMLHttpResponse) {
                                                            var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
                                                            alert("addNetworkServiceProvider&name=NiciraNvp failed. Error: " + errorMsg);
                                                        }
                                                    });
                                                },
                                                g_queryAsyncJobResultInterval);
                                        }
                                    });
                                } else {
                                    addNiciraNvpDevice(args, selectedPhysicalNetworkObj, "addNiciraNvpDevice", "addniciranvpdeviceresponse", "niciranvpdevice")
                                }
                            },

                            messages: {
                                notification: function (args) {
                                    return 'label.added.nicira.nvp.controller';
                                }
                            },
                            notification: {
                                poll: pollAsyncJobResult
                            }
                        }
                    },
                    dataProvider: function (args) {
                        $.ajax({
                            url: createURL("listNiciraNvpDevices&physicalnetworkid=" + selectedPhysicalNetworkObj.id),
                            data: {
                                page: args.page,
                                pageSize: pageSize
                            },
                            dataType: "json",
                            async: false,
                            success: function (json) {
                                var items = json.listniciranvpdeviceresponse.niciranvpdevice;
                                args.response.success({
                                    data: items
                                });
                            }
                        });
                    },
                    detailView: {
                        name: 'label.nicira.nvp.details',
                        actions: {
                            'remove': {
                                label: 'label.delete.NiciaNvp',
                                messages: {
                                    confirm: function (args) {
                                        return 'message.confirm.delete.NiciraNvp';
                                    },
                                    notification: function (args) {
                                        return 'label.delete.NiciraNvp';
                                    }
                                },
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("deleteNiciraNvpDevice&nvpdeviceid=" + args.context.niciraNvpDevices[0].nvpdeviceid),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var jid = json.deleteniciranvpdeviceresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid
                                                }
                                            });
                                        }
                                    });
                                },
                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            }
                        },
                        tabs: {
                            details: {
                                title: 'label.details',
                                fields: [{
                                    nvpdeviceid: {
                                        label: 'label.id'
                                    },
                                    hostname: {
                                        label: 'label.ip.address'
                                    },
                                    transportzoneuuid: {
                                        label: 'label.nicira.transportzoneuuid'
                                    },
                                    l3gatewayserviceuuid: {
                                        label: 'label.nicira.l3gatewayserviceuuid'
                                    },
                                    l2gatewayserviceuuid: {
                                        label: 'label.nicira.l2gatewayserviceuuid'
                                    }
                                }],
                                dataProvider: function (args) {
                                    $.ajax({
                                        url: createURL("listNiciraNvpDevices&nvpdeviceid=" + args.context.niciraNvpDevices[0].nvpdeviceid),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var item = json.listniciranvpdeviceresponse.niciranvpdevice[0];
                                            args.response.success({
                                                data: item
                                            });
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            },
            pods: {
                title: 'label.pods',
                listView: {
                    id: 'pods',
                    section: 'pods',
                    fields: {
                        name: {
                            label: 'label.name'
                        },
                        gateway: {
                            label: 'label.gateway'
                        },
                        netmask: {
                            label: 'label.netmask'
                        },
                        allocationstate: {
                            converter: function (str) {
                                // For localization
                                return str;
                            },
                            label: 'label.allocation.state'
                        }
                    },

                    dataProvider: function (args) {
                        var array1 = [];
                        if (args.filterBy != null) {
                            if (args.filterBy.search != null && args.filterBy.search.by != null && args.filterBy.search.value != null) {
                                switch (args.filterBy.search.by) {
                                    case "name":
                                        if (args.filterBy.search.value.length > 0)
                                            array1.push("&keyword=" + args.filterBy.search.value);
                                        break;
                                }
                            }
                        }

                        $.ajax({
                            url: createURL("listPods&zoneid=" + args.context.zones[0].id + "&page=" + args.page + "&pagesize=" + pageSize + array1.join("")),
                            dataType: "json",
                            async: true,
                            success: function (json) {
                                var items = json.listpodsresponse.pod;
                                args.response.success({
                                    actionFilter: podActionfilter,
                                    data: items
                                });
                            }
                        });
                    },

                    actions: {
                        add: {
                            label: 'label.add.pod',

                            createForm: {
                                title: 'label.add.pod',
                                fields: {
                                    zoneid: {
                                        label: 'label.zone',
                                        docID: 'helpPodZone',
                                        validation: {
                                            required: true
                                        },
                                        select: function (args) {
                                            var data = args.context.zones ? {
                                                id: args.context.zones[0].id
                                            } : {};

                                            $.ajax({
                                                url: createURL('listZones'),
                                                data: data,
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
                                    podname: {
                                        label: 'label.pod.name',
                                        docID: 'helpPodName',
                                        validation: {
                                            required: true
                                        }
                                    },
                                    reservedSystemGateway: {
                                        label: 'label.reserved.system.gateway',
                                        docID: 'helpPodGateway',
                                        validation: {
                                            required: true
                                        }
                                    },
                                    reservedSystemNetmask: {
                                        label: 'label.reserved.system.netmask',
                                        docID: 'helpPodNetmask',
                                        validation: {
                                            required: true
                                        }
                                    },
                                    reservedSystemStartIp: {
                                        label: 'label.start.reserved.system.IP',
                                        docID: 'helpPodStartIP',
                                        validation: {
                                            required: true
                                        }
                                    },
                                    reservedSystemEndIp: {
                                        label: 'label.end.reserved.system.IP',
                                        docID: 'helpPodEndIP',
                                        validation: {
                                            required: false
                                        }
                                    },

                                    isDedicated: {
                                        label: 'label.dedicate',
                                        isBoolean: true,
                                        isChecked: false,
                                        docID: 'helpDedicateResource'
                                    },

                                    domainId: {
                                        label: 'label.domain',
                                        isHidden: true,
                                        validation: {
                                            required: true
                                        },
                                        dependsOn: 'isDedicated',
                                        select: function (args) {
                                            $.ajax({
                                                url: createURL("listDomains&listAll=true"),
                                                dataType: "json",
                                                async: false,
                                                success: function (json) {
                                                    var domainObjs = json.listdomainsresponse.domain;
                                                    var items = [];

                                                    $(domainObjs).each(function () {
                                                        items.push({
                                                            id: this.id,
                                                            description: this.name
                                                        });
                                                    });
                                                    items.sort(function (a, b) {
                                                        return a.description.localeCompare(b.description);
                                                    });

                                                    args.response.success({
                                                        data: items
                                                    });
                                                }
                                            });
                                        }
                                    },

                                    accountId: {
                                        label: 'label.account',
                                        isHidden: true,
                                        dependsOn: 'isDedicated',
                                        docID: 'helpAccountForDedication',
                                        validation: {
                                            required: false
                                        }
                                    }
                                }
                            },

                            action: function (args) {
                                var array1 = [];
                                var appendData = args.data.append ? args.data.append : {};

                                array1.push("&zoneId=" + args.data.zoneid);
                                array1.push("&name=" + todb(args.data.podname));
                                array1.push("&gateway=" + todb(args.data.reservedSystemGateway));
                                array1.push("&netmask=" + todb(args.data.reservedSystemNetmask));
                                array1.push("&startIp=" + todb(args.data.reservedSystemStartIp));

                                var endip = args.data.reservedSystemEndIp; //optional
                                if (endip != null && endip.length > 0)
                                    array1.push("&endIp=" + todb(endip));
                                var podId = null;
                                $.ajax({
                                    url: createURL("createPod" + array1.join("")),
                                    data: appendData,
                                    dataType: "json",
                                    success: function (json) {
                                        var item = json.createpodresponse.pod;
                                        podId = json.createpodresponse.pod.id;

                                        //EXPLICIT DEDICATION
                                        if (args.$form.find('.form-item[rel=isDedicated]').find('input[type=checkbox]').is(':Checked') == true) {
                                            var array2 = [];
                                            if (args.data.accountId != "")
                                                array2.push("&account=" + todb(args.data.accountId));

                                            if (podId != null) {
                                                $.ajax({
                                                    url: createURL("dedicatePod&podId=" + podId + "&domainId=" + args.data.domainId + array2.join("")),
                                                    dataType: "json",
                                                    success: function (json) {
                                                        var jid = json.dedicatepodresponse.jobid;
                                                        args.response.success({
                                                            _custom: {
                                                                jobId: jid
                                                            },
                                                            notification: {
                                                                poll: pollAsyncJobResult,
                                                                interval: 4500,
                                                                desc: "Dedicate Pod"
                                                            },

                                                            data: item
                                                        });
                                                    },

                                                    error: function (json) {
                                                        args.response.error(parseXMLHttpResponse(XMLHttpResponse));
                                                    }
                                                });
                                            }
                                        }
                                        args.response.success({
                                            data: item
                                        });
                                    },
                                    error: function (XMLHttpResponse) {
                                        var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
                                        args.response.error(errorMsg);
                                    }
                                });
                            },

                            notification: {
                                poll: function (args) {
                                    args.complete({
                                        actionFilter: podActionfilter
                                    });
                                }
                            },

                            messages: {
                                notification: function (args) {
                                    return 'label.add.pod';
                                }
                            }
                        }
                    },

                    detailView: {
                        viewAll: {
                            path: '_zone.clusters',
                            label: 'label.clusters'
                        },
                        tabFilter: function (args) {
                            var hiddenTabs = [];
                            if (selectedZoneObj.networktype == "Basic") {
                                //basic-mode network (pod-wide VLAN)
                                //$("#tab_ipallocation, #add_iprange_button, #tab_network_device, #add_network_device_button").show();
                            } else if (selectedZoneObj.networktype == "Advanced") {
                                //advanced-mode network (zone-wide VLAN)
                                //$("#tab_ipallocation, #add_iprange_button, #tab_network_device, #add_network_device_button").hide();
                                hiddenTabs.push("ipAllocations");
                                //hiddenTabs.push("networkDevices"); //network devices tab is moved out of pod page at 3.0 UI. It will go to new network page.
                            }
                            return hiddenTabs;
                        },
                        actions: {
                            edit: {
                                label: 'label.edit',
                                action: function (args) {
                                    var array1 = [];
                                    array1.push("&name=" + todb(args.data.name));
                                    array1.push("&netmask=" + todb(args.data.netmask));
                                    array1.push("&startIp=" + todb(args.data.startip));
                                    if (args.data.endip != null && args.data.endip.length > 0)
                                        array1.push("&endIp=" + todb(args.data.endip));
                                    if (args.data.gateway != null && args.data.gateway.length > 0)
                                        array1.push("&gateway=" + todb(args.data.gateway));

                                    $.ajax({
                                        url: createURL("updatePod&id=" + args.context.pods[0].id + array1.join("")),
                                        dataType: "json",
                                        success: function (json) {
                                            var item = json.updatepodresponse.pod;
                                            args.response.success({
                                                actionFilter: podActionfilter,
                                                data: item
                                            });
                                        },
                                        error: function (data) {
                                            args.response.error(parseXMLHttpResponse(data));
                                        }
                                    });
                                }
                            },

                            enable: {
                                label: 'label.action.enable.pod',
                                messages: {
                                    confirm: function (args) {
                                        return 'message.action.enable.pod';
                                    },
                                    notification: function (args) {
                                        return 'label.action.enable.pod';
                                    }
                                },
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("updatePod&id=" + args.context.pods[0].id + "&allocationstate=Enabled"),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var item = json.updatepodresponse.pod;
                                            args.response.success({
                                                actionFilter: podActionfilter,
                                                data: item
                                            });
                                        }
                                    });
                                },
                                notification: {
                                    poll: function (args) {
                                        args.complete();
                                    }
                                }
                            },

                            dedicate: {
                                label: 'label.dedicate.pod',
                                messages: {
                                    confirm: function (args) {
                                        return 'message.confirm.dedicate.pod.domain.account';
                                    },
                                    notification: function (args) {
                                        return 'label.pod.dedicated';
                                    }
                                },
                                createForm: {
                                    title: 'label.dedicate.pod',
                                    fields: {
                                        domainId: {
                                            label: 'label.domain',
                                            validation: {
                                                required: true
                                            },
                                            select: function (args) {
                                                $.ajax({
                                                    url: createURL("listDomains&listAll=true"),
                                                    dataType: "json",
                                                    async: false,
                                                    success: function (json) {
                                                        var domainObjs = json.listdomainsresponse.domain;
                                                        var items = [];

                                                        $(domainObjs).each(function () {
                                                            items.push({
                                                                id: this.id,
                                                                description: this.name
                                                            });
                                                        });
                                                        items.sort(function (a, b) {
                                                            return a.description.localeCompare(b.description);
                                                        });

                                                        args.response.success({
                                                            data: items
                                                        });
                                                    }
                                                });
                                            }
                                        },
                                        accountId: {
                                            label: 'label.account',
                                            docID: 'helpAccountForDedication',
                                            validation: {
                                                required: false
                                            }
                                        }
                                    }
                                },
                                action: function (args) {
                                    //EXPLICIT DEDICATION
                                    var array2 = [];
                                    if (args.data.accountId != "")
                                        array2.push("&account=" + todb(args.data.accountId));

                                    $.ajax({
                                        url: createURL("dedicatePod&podId=" +
                                            args.context.pods[0].id +
                                            "&domainId=" + args.data.domainId + array2.join("")),
                                        dataType: "json",
                                        success: function (json) {
                                            var jid = json.dedicatepodresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getActionFilter: function () {
                                                        return podActionfilter;
                                                    }
                                                }
                                            });
                                        },
                                        error: function (json) {
                                            args.response.error(parseXMLHttpResponse(XMLHttpResponse));
                                        }
                                    });
                                },
                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            },
                            release: {
                                label: 'label.release.dedicated.pod',
                                messages: {
                                    confirm: function (args) {
                                        return 'message.confirm.release.dedicated.pod';
                                    },
                                    notification: function (args) {
                                        return 'message.pod.dedication.released';
                                    }
                                },
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("releaseDedicatedPod&podid=" + args.context.pods[0].id),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var jid = json.releasededicatedpodresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getActionFilter: function () {
                                                        return podActionfilter;
                                                    }
                                                }
                                            });
                                        },
                                        error: function (json) {
                                            args.response.error(parseXMLHttpResponse(json));
                                        }
                                    });
                                },
                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            },


                            disable: {
                                label: 'label.action.disable.pod',
                                messages: {
                                    confirm: function (args) {
                                        return 'message.action.disable.pod';
                                    },
                                    notification: function (args) {
                                        return 'label.action.disable.pod';
                                    }
                                },
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("updatePod&id=" + args.context.pods[0].id + "&allocationstate=Disabled"),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var item = json.updatepodresponse.pod;
                                            args.response.success({
                                                actionFilter: podActionfilter,
                                                data: item
                                            });
                                        }
                                    });
                                },
                                notification: {
                                    poll: function (args) {
                                        args.complete();
                                    }
                                }
                            },

                            'remove': {
                                label: 'label.delete',
                                messages: {
                                    confirm: function (args) {
                                        return 'message.action.delete.pod';
                                    },
                                    notification: function (args) {
                                        return 'label.action.delete.pod';
                                    }
                                },
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("deletePod&id=" + args.context.pods[0].id),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            args.response.success({
                                                data: {}
                                            });
                                        }
                                    });
                                },
                                notification: {
                                    poll: function (args) {
                                        args.complete();
                                    }
                                }
                            }
                        },
                        tabs: {
                            details: {
                                title: 'label.details',
                                fields: [{
                                    name: {
                                        label: 'label.name',
                                        isEditable: true,
                                        validation: {
                                            required: true
                                        }
                                    }
                                },
                                    {
                                        id: {
                                            label: 'label.id'
                                        },
                                        netmask: {
                                            label: 'label.netmask',
                                            isEditable: true,
                                            validation: {
                                                required: true
                                            }
                                        },
                                        startip: {
                                            label: 'label.start.IP',
                                            isEditable: true,
                                            validation: {
                                                required: true
                                            }
                                        },
                                        endip: {
                                            label: 'label.end.IP',
                                            isEditable: true
                                        },
                                        gateway: {
                                            label: 'label.gateway',
                                            isEditable: true,
                                            validation: {
                                                required: true
                                            }
                                        },
                                        allocationstate: {
                                            converter: function (str) {
                                                // For localization
                                                return str;
                                            },
                                            label: 'label.allocation.state'
                                        }
                                    }, {

                                        isdedicated: {
                                            label: 'label.dedicated'
                                        },
                                        domainid: {
                                            label: 'label.domain.id'
                                        }
                                    }],

                                dataProvider: function (args) {

                                    $.ajax({
                                        url: createURL("listPods&id=" + args.context.pods[0].id),
                                        success: function (json) {
                                            var item = json.listpodsresponse.pod[0];


                                            $.ajax({
                                                url: createURL("listDedicatedPods&podid=" + args.context.pods[0].id),
                                                success: function (json) {
                                                    if (json.listdedicatedpodsresponse.dedicatedpod != undefined) {
                                                        var podItem = json.listdedicatedpodsresponse.dedicatedpod[0];
                                                        if (podItem.domainid != null) {
                                                            $.extend(item, podItem, {
                                                                isdedicated: _l('label.yes')
                                                            });
                                                        }
                                                    } else
                                                        $.extend(item, {
                                                            isdedicated: _l('label.no')
                                                        });

                                                    args.response.success({
                                                        actionFilter: podActionfilter,
                                                        data: item
                                                    });
                                                },
                                                error: function (json) {
                                                    args.response.error(parseXMLHttpResponse(XMLHttpResponse));
                                                }
                                            });
                                            //  args.response.success({
                                            //     actionFilter: podActionfilter,
                                            //     data: item
                                            // });
                                        }
                                    });
                                }
                            },

                            ipAllocations: {
                                title: 'label.ip.allocations',
                                multiple: true,
                                fields: [{
                                    id: {
                                        label: 'label.id'
                                    },
                                    gateway: {
                                        label: 'label.gateway'
                                    },
                                    netmask: {
                                        label: 'label.netmask'
                                    },
                                    startip: {
                                        label: 'label.start.IP'
                                    },
                                    endip: {
                                        label: 'label.end.IP'
                                    }
                                }],
                                dataProvider: function (args) {
                                    $.ajax({
                                        url: createURL("listVlanIpRanges&zoneid=" + args.context.zones[0].id + "&podid=" + args.context.pods[0].id),
                                        dataType: "json",
                                        success: function (json) {
                                            var items = json.listvlaniprangesresponse.vlaniprange;
                                            args.response.success({
                                                data: items
                                            });
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            },
            clusters: {
                title: 'label.clusters',
                listView: {
                    id: 'clusters',
                    section: 'clusters',
                    fields: {
                        name: {
                            label: 'label.name'
                        },
                        podname: {
                            label: 'label.pod'
                        },
                        hypervisortype: {
                            label: 'label.hypervisor'
                        },
                        //allocationstate: { label: 'label.allocation.state' },
                        //managedstate: { label: 'Managed State' },
                        allocationstate: {
                            converter: function (str) {
                                // For localization
                                return str;
                            },
                            label: 'label.state',
                            indicator: {
                                'Enabled': 'on',
                                'Destroyed': 'off'
                            }
                        }
                    },

                    dataProvider: function (args) {
                        var array1 = [];
                        if (args.filterBy != null) {
                            if (args.filterBy.search != null && args.filterBy.search.by != null && args.filterBy.search.value != null) {
                                switch (args.filterBy.search.by) {
                                    case "name":
                                        if (args.filterBy.search.value.length > 0)
                                            array1.push("&keyword=" + args.filterBy.search.value);
                                        break;
                                }
                            }
                        }
                        array1.push("&zoneid=" + args.context.zones[0].id);
                        if ("pods" in args.context)
                            array1.push("&podid=" + args.context.pods[0].id);
                        $.ajax({
                            url: createURL("listClusters" + array1.join("") + "&page=" + args.page + "&pagesize=" + pageSize),
                            dataType: "json",
                            async: true,
                            success: function (json) {
                                var items = json.listclustersresponse.cluster;
                                $(items).each(function () {
                                    addExtraPropertiesToClusterObject(this);
                                });

                                args.response.success({
                                    actionFilter: clusterActionfilter,
                                    data: items
                                });
                            }
                        });
                    },

                    actions: {
                        add: {
                            label: 'label.add.cluster',
                            messages: {
                                notification: function (args) {
                                    return 'label.add.cluster';
                                }
                            },
                            createForm: {
                                title: 'label.add.cluster',
                                preFilter: function (args) {
                                    var $form = args.$form;
                                    $form.click(function () {
                                        var $nexusDvsOptFields = $form.find('.form-item').filter(function () {
                                            var nexusDvsOptFields = [
                                                'vsmipaddress',
                                                'vsmusername',
                                                'vsmpassword'];
                                            return $.inArray($(this).attr('rel'), nexusDvsOptFields) > -1;
                                        });
                                        var $nexusDvsReqFields = $form.find('.form-item').filter(function () {
                                            var nexusDvsReqFields = [
                                                'vsmipaddress_req',
                                                'vsmusername_req',
                                                'vsmpassword_req'];
                                            return $.inArray($(this).attr('rel'), nexusDvsReqFields) > -1;
                                        });

                                        //XenServer, KVM, etc
                                        $form.find('.form-item[rel=vCenterHost]').css('display', 'none');
                                        $form.find('.form-item[rel=vCenterUsername]').css('display', 'none');
                                        $form.find('.form-item[rel=vCenterPassword]').css('display', 'none');
                                        $form.find('.form-item[rel=vCenterDatacenter]').css('display', 'none');
                                        $form.find('.form-item[rel=enableNexusVswitch]').css('display', 'none');

                                        $form.find('.form-item[rel=overridepublictraffic]').css('display', 'none');
                                        $form.find('.form-item[rel=overrideguesttraffic]').css('display', 'none');
                                        $nexusDvsOptFields.hide();
                                        $nexusDvsReqFields.hide();

                                        $form.find('.form-item[rel=vSwitchPublicType]').css('display', 'none');
                                        $form.find('.form-item[rel=vSwitchPublicName]').css('display', 'none');
                                        $form.find('.form-item[rel=vSwitchGuestType]').css('display', 'none');
                                        $form.find('.form-item[rel=vSwitchGuestName]').css('display', 'none');

                                    });

                                    $form.trigger('click');
                                },
                                fields: {
                                    zoneid: {
                                        label: 'label.zone.name',
                                        docID: 'helpClusterZone',
                                        validation: {
                                            required: true
                                        },
                                        select: function (args) {
                                            var data = args.context.zones ? {
                                                id: args.context.zones[0].id
                                            } : {};

                                            $.ajax({
                                                url: createURL('listZones'),
                                                data: data,
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
                                    hypervisor: {
                                        label: 'label.hypervisor',
                                        docID: 'helpClusterHypervisor',
                                        select: function (args) {
                                            $.ajax({
                                                url: createURL("listHypervisors"),
                                                dataType: "json",
                                                async: false,
                                                success: function (json) {
                                                    var hypervisors = json.listhypervisorsresponse.hypervisor;
                                                    var items = [];
                                                    $(hypervisors).each(function () {
                                                        items.push({
                                                            id: this.name,
                                                            description: this.name
                                                        });
                                                    });
                                                    args.response.success({
                                                        data: items
                                                    });
                                                }
                                            });
                                        }
                                    },
                                    podId: {
                                        label: 'label.pod.name',
                                        docID: 'helpClusterPod',
                                        dependsOn: 'zoneid',
                                        select: function (args) {
                                            $.ajax({
                                                url: createURL("listPods&zoneid=" + args.zoneid),
                                                dataType: "json",
                                                async: true,
                                                success: function (json) {
                                                    var pods = json.listpodsresponse.pod;
                                                    var items = [];
                                                    $(pods).each(function () {
                                                        if (("pods" in args.context) && (this.id == args.context.pods[0].id))
                                                            items.unshift({
                                                                id: this.id,
                                                                description: this.name
                                                            }); else
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
                                    },
                                    name: {
                                        label: 'label.cluster.name',
                                        docID: 'helpClusterName',
                                        validation: {
                                            required: true
                                        }
                                    },

                                    isDedicated: {
                                        label: 'label.dedicate',
                                        isBoolean: true,
                                        isChecked: false,
                                        docID: 'helpDedicateResource'
                                    },

                                    domainId: {
                                        label: 'label.domain',
                                        isHidden: true,
                                        validation: {
                                            required: true
                                        },
                                        dependsOn: 'isDedicated',
                                        select: function (args) {
                                            $.ajax({
                                                url: createURL("listDomains&listAll=true"),
                                                dataType: "json",
                                                async: false,
                                                success: function (json) {
                                                    var domainObjs = json.listdomainsresponse.domain;
                                                    var items = [];

                                                    $(domainObjs).each(function () {
                                                        items.push({
                                                            id: this.id,
                                                            description: this.name
                                                        });
                                                    });
                                                    items.sort(function (a, b) {
                                                        return a.description.localeCompare(b.description);
                                                    });

                                                    args.response.success({
                                                        data: items
                                                    });
                                                }
                                            });
                                        }
                                    },

                                    accountId: {
                                        label: 'label.account',
                                        isHidden: true,
                                        dependsOn: 'isDedicated',
                                        docID: 'helpAccountForDedication',
                                        validation: {
                                            required: false
                                        }
                                    },

                                    //hypervisor==Ovm3 begins here
                                    ovm3pool: {
                                        label: 'label.ovm3.pool',
                                        isHidden: true,
                                        isBoolean: true,
                                        isChecked: true,
                                        docID: 'helpOvm3pool'
                                    },
                                    ovm3cluster: {
                                        label: 'label.ovm3.cluster',
                                        isHidden: true,
                                        isBoolean: true,
                                        isChecked: false,
                                        docID: 'helpOvm3cluster'
                                    },
                                    ovm3vip: {
                                        label: 'label.ovm3.vip',
                                        isHidden: true,
                                        docID: 'helpOvm3Vip',
                                        validation: {
                                            required: false
                                        }
                                    }
                                }
                            },

                            action: function (args) {
                                var array1 = [];
                                array1.push("&zoneId=" + args.data.zoneid);
                                array1.push("&hypervisor=" + args.data.hypervisor);

                                var clusterType = "CloudManaged";
                                array1.push("&clustertype=" + clusterType);

                                array1.push("&podId=" + args.data.podId);

                                var clusterName = args.data.name;
                                if (args.data.hypervisor == "Ovm3") {
                                    array1.push("&ovm3pool=" + todb(args.data.ovm3pool));
                                    array1.push("&ovm3cluster=" + todb(args.data.ovm3cluster));
                                    array1.push("&ovm3vip=" + todb(args.data.ovm3vip));
                                }

                                array1.push("&clustername=" + todb(clusterName));
                                var clusterId = null;
                                $.ajax({
                                    url: createURL("addCluster" + array1.join("")),
                                    dataType: "json",
                                    type: "POST",
                                    success: function (json) {
                                        var item = json.addclusterresponse.cluster[0];
                                        clusterId = json.addclusterresponse.cluster[0].id;

                                        //EXPLICIT DEDICATION
                                        var array2 = [];
                                        if (args.$form.find('.form-item[rel=isDedicated]').find('input[type=checkbox]').is(':Checked') == true) {
                                            if (args.data.accountId != "")
                                                array2.push("&account=" + todb(args.data.accountId));

                                            if (clusterId != null) {
                                                $.ajax({
                                                    url: createURL("dedicateCluster&clusterId=" + clusterId + "&domainId=" + args.data.domainId + array2.join("")),
                                                    dataType: "json",
                                                    success: function (json) {
                                                        var jid = json.dedicateclusterresponse.jobid;
                                                        args.response.success({
                                                            _custom: {
                                                                jobId: jid
                                                            },
                                                            notification: {
                                                                poll: pollAsyncJobResult,
                                                                interval: 4500,
                                                                desc: "Dedicate Cluster"
                                                            },

                                                            data: $.extend(item, {
                                                                state: 'Enabled'
                                                            })
                                                        });
                                                    },
                                                    error: function (json) {
                                                        args.response.error(parseXMLHttpResponse(XMLHttpResponse));
                                                    }
                                                });
                                            }
                                        }
                                        args.response.success({
                                            data: item,
                                            actionFilter: clusterActionfilter
                                        });
                                    },
                                    error: function (XMLHttpResponse) {
                                        var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
                                        args.response.error(errorMsg);
                                    }
                                });
                            }
                        },
                        viewMetrics: {
                            label: 'label.metrics',
                            isHeader: true,
                            addRow: false,
                            action: {
                                custom: cloudStack.uiCustom.metricsView({resource: 'clusters'})
                            },
                            messages: {
                                notification: function (args) {
                                    return 'label.metrics';
                                }
                            }
                        }
                    },

                    detailView: {
                        viewAll: {
                            path: '_zone.hosts',
                            label: 'label.hosts'
                        },
                        isMaximized: true,
                        tabFilter: function (args) {
                            return [];
                        },

                        actions: {

                            edit: {
                                label: 'label.edit',
                                action: function (args) {
                                    var array1 = [];

                                    $.ajax({
                                        url: createURL("updateCluster&id=" + args.context.clusters[0].id + array1.join("")),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var item = json.updateclusterresponse.cluster;
                                            addExtraPropertiesToClusterObject(item);
                                            args.response.success({
                                                actionFilter: clusterActionfilter,
                                                data: item
                                            });
                                        }
                                    });
                                }
                            },

                            enable: {
                                label: 'label.action.enable.cluster',
                                messages: {
                                    confirm: function (args) {
                                        return 'message.action.enable.cluster';
                                    },
                                    notification: function (args) {
                                        return 'label.action.enable.cluster';
                                    }
                                },
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("updateCluster&id=" + args.context.clusters[0].id + "&allocationstate=Enabled"),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var item = json.updateclusterresponse.cluster;
                                            args.context.clusters[0].state = item.allocationstate;
                                            addExtraPropertiesToClusterObject(item);
                                            args.response.success({
                                                actionFilter: clusterActionfilter,
                                                data: item
                                            });
                                        }
                                    });
                                },
                                notification: {
                                    poll: function (args) {
                                        args.complete();
                                    }
                                }
                            },

                            disable: {
                                label: 'label.action.disable.cluster',
                                messages: {
                                    confirm: function (args) {
                                        return 'message.action.disable.cluster';
                                    },
                                    notification: function (args) {
                                        return 'label.action.disable.cluster';
                                    }
                                },
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("updateCluster&id=" + args.context.clusters[0].id + "&allocationstate=Disabled"),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var item = json.updateclusterresponse.cluster;
                                            args.context.clusters[0].state = item.allocationstate;
                                            addExtraPropertiesToClusterObject(item);
                                            args.response.success({
                                                actionFilter: clusterActionfilter,
                                                data: item
                                            });
                                        }
                                    });
                                },
                                notification: {
                                    poll: function (args) {
                                        args.complete();
                                    }
                                }
                            },

                            dedicate: {
                                label: 'label.dedicate.cluster',
                                messages: {
                                    confirm: function (args) {
                                        return 'message.confirm.dedicate.cluster.domain.account';
                                    },
                                    notification: function (args) {
                                        return 'message.cluster.dedicated';
                                    }
                                },
                                createForm: {
                                    title: 'label.dedicate.cluster',
                                    fields: {
                                        domainId: {
                                            label: 'label.domain',
                                            validation: {
                                                required: true
                                            },
                                            select: function (args) {
                                                $.ajax({
                                                    url: createURL("listDomains&listAll=true"),
                                                    dataType: "json",
                                                    async: false,
                                                    success: function (json) {
                                                        var domainObjs = json.listdomainsresponse.domain;
                                                        var items = [];

                                                        $(domainObjs).each(function () {
                                                            items.push({
                                                                id: this.id,
                                                                description: this.name
                                                            });
                                                        });
                                                        items.sort(function (a, b) {
                                                            return a.description.localeCompare(b.description);
                                                        });

                                                        args.response.success({
                                                            data: items
                                                        });
                                                    }
                                                });
                                            }
                                        },
                                        accountId: {
                                            label: 'label.account',
                                            docID: 'helpAccountForDedication',
                                            validation: {
                                                required: false
                                            }
                                        }
                                    }
                                },
                                action: function (args) {
                                    //EXPLICIT DEDICATION
                                    var array2 = [];
                                    if (args.data.accountId != "")
                                        array2.push("&account=" + todb(args.data.accountId));
                                    $.ajax({
                                        url: createURL("dedicateCluster&clusterId=" +
                                            args.context.clusters[0].id +
                                            "&domainId=" + args.data.domainId + array2.join("")),
                                        dataType: "json",
                                        success: function (json) {
                                            var jid = json.dedicateclusterresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getActionFilter: function () {
                                                        return clusterActionfilter;
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
                            release: {
                                label: 'label.release.dedicated.cluster',
                                messages: {
                                    confirm: function (args) {
                                        return 'message.confirm.release.dedicated.cluster';
                                    },
                                    notification: function (args) {
                                        return 'message.cluster.dedication.released';
                                    }
                                },
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("releaseDedicatedCluster&clusterid=" + args.context.clusters[0].id),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var jid = json.releasededicatedclusterresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getActionFilter: function () {
                                                        return clusterActionfilter;
                                                    }
                                                }
                                            });
                                        },
                                        error: function (json) {
                                            args.response.error(parseXMLHttpResponse(json));
                                        }
                                    });
                                },
                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            },


                            manage: {
                                label: 'label.action.manage.cluster',
                                messages: {
                                    confirm: function (args) {
                                        return 'message.action.manage.cluster';
                                    },
                                    notification: function (args) {
                                        return 'label.action.manage.cluster';
                                    }
                                },
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("updateCluster&id=" + args.context.clusters[0].id + "&managedstate=Managed"),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var item = json.updateclusterresponse.cluster;
                                            addExtraPropertiesToClusterObject(item);
                                            args.response.success({
                                                actionFilter: clusterActionfilter,
                                                data: item
                                            });
                                        }
                                    });
                                },
                                notification: {
                                    poll: function (args) {
                                        args.complete();
                                    }
                                }
                            },

                            unmanage: {
                                label: 'label.action.unmanage.cluster',
                                messages: {
                                    confirm: function (args) {
                                        return 'message.action.unmanage.cluster';
                                    },
                                    notification: function (args) {
                                        return 'label.action.unmanage.cluster';
                                    }
                                },
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("updateCluster&id=" + args.context.clusters[0].id + "&managedstate=Unmanaged"),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var item = json.updateclusterresponse.cluster;
                                            addExtraPropertiesToClusterObject(item);
                                            args.response.success({
                                                actionFilter: clusterActionfilter,
                                                data: item
                                            });
                                        }
                                    });
                                },
                                notification: {
                                    poll: function (args) {
                                        args.complete();
                                    }
                                }
                            },

                            'remove': {
                                label: 'label.action.delete.cluster',
                                messages: {
                                    confirm: function (args) {
                                        return 'message.action.delete.cluster';
                                    },
                                    notification: function (args) {
                                        return 'label.action.delete.cluster';
                                    }
                                },
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("deleteCluster&id=" + args.context.clusters[0].id),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            args.response.success({
                                                data: {}
                                            });
                                        }
                                    });
                                },
                                notification: {
                                    poll: function (args) {
                                        args.complete();
                                    }
                                }
                            }
                        },

                        tabs: {
                            details: {
                                title: 'label.details',
                                fields: [{
                                    name: {
                                        label: 'label.name'
                                    }
                                },
                                    {
                                        id: {
                                            label: 'label.id'
                                        },
                                        zonename: {
                                            label: 'label.zone'
                                        },
                                        podname: {
                                            label: 'label.pod'
                                        },
                                        hypervisortype: {
                                            label: 'label.hypervisor'
                                        },
                                        clustertype: {
                                            label: 'label.cluster.type'
                                        },
                                        //allocationstate: { label: 'label.allocation.state' },
                                        //managedstate: { label: 'Managed State' },
                                        state: {
                                            label: 'label.state'
                                        }
                                    }, {
                                        isdedicated: {
                                            label: 'label.dedicated'
                                        },
                                        domainid: {
                                            label: 'label.domain.id'
                                        }
                                    }],
                                dataProvider: function (args) {
                                    $.ajax({
                                        url: createURL("listClusters&id=" + args.context.clusters[0].id),
                                        dataType: "json",
                                        success: function (json) {
                                            var item = json.listclustersresponse.cluster[0];
                                            addExtraPropertiesToClusterObject(item);
                                            $.ajax({
                                                url: createURL("listDedicatedClusters&clusterid=" + args.context.clusters[0].id),
                                                dataType: "json",
                                                async: false,
                                                success: function (json) {
                                                    if (json.listdedicatedclustersresponse.dedicatedcluster != undefined) {
                                                        var clusterItem = json.listdedicatedclustersresponse.dedicatedcluster[0];
                                                        if (clusterItem.domainid != null) {
                                                            $.extend(item, clusterItem, {
                                                                isdedicated: _l('label.yes')
                                                            });
                                                        }
                                                    } else
                                                        $.extend(item, {
                                                            isdedicated: _l('label.no')
                                                        })
                                                },
                                                error: function (json) {
                                                    args.response.error(parseXMLHttpResponse(XMLHttpResponse));
                                                }
                                            });
                                            args.response.success({
                                                actionFilter: clusterActionfilter,
                                                data: item
                                            });
                                        },

                                        error: function (json) {
                                            args.response.error(parseXMLHttpResponse(XMLHttpResponse));
                                        }
                                    });
                                }
                            },

                            // Granular settings for cluster
                            settings: {
                                title: 'label.settings',
                                custom: cloudStack.uiCustom.granularSettings({
                                    dataProvider: function (args) {
                                        $.ajax({
                                            url: createURL('listConfigurations&clusterid=' + args.context.clusters[0].id),
                                            data: listViewDataProvider(args, {},
                                                {
                                                    searchBy: 'name'
                                                }),
                                            success: function (json) {
                                                args.response.success({
                                                    data: json.listconfigurationsresponse.configuration
                                                });
                                            },

                                            error: function (json) {
                                                args.response.error(parseXMLHttpResponse(json));
                                            }
                                        });
                                    },
                                    actions: {
                                        edit: function (args) {
                                            // call updateClusterLevelParameters

                                            var data = {
                                                name: args.data.jsonObj.name,
                                                value: args.data.value
                                            };

                                            $.ajax({
                                                url: createURL('updateConfiguration&clusterid=' + args.context.clusters[0].id),
                                                data: data,
                                                success: function (json) {
                                                    var item = json.updateconfigurationresponse.configuration;

                                                    if (args.data.jsonObj.name == 'cpu.overprovisioning.factor' || args.data.jsonObj.name == 'mem.overprovisioning.factor') {
                                                        cloudStack.dialog.notice({
                                                            message: 'Please note - if you are changing the over provisioning factor for a cluster with vms running, please refer to the admin guide to understand the capacity calculation.'
                                                        });
                                                    }

                                                    args.response.success({
                                                        data: item
                                                    });
                                                },

                                                error: function (json) {
                                                    args.response.error(parseXMLHttpResponse(json));
                                                }
                                            });
                                        }
                                    }
                                })
                            }
                        }
                    }
                }
            },
            hosts: {
                title: 'label.hosts',
                id: 'hosts',
                listView: {
                    section: 'hosts',
                    id: 'hosts',
                    fields: {
                        name: {
                            label: 'label.name'
                        },
                        zonename: {
                            label: 'label.zone'
                        },
                        podname: {
                            label: 'label.pod'
                        },
                        clustername: {
                            label: 'label.cluster'
                        },
                        state: {
                            label: 'label.state',
                            indicator: {
                                'Up': 'on',
                                'Down': 'off',
                                'Disconnected': 'off',
                                'Alert': 'off',
                                'Error': 'off'
                            }
                        }
                    },

                    dataProvider: function (args) {
                        var array1 = [];
                        if (args.filterBy != null) {
                            if (args.filterBy.search != null && args.filterBy.search.by != null && args.filterBy.search.value != null) {
                                switch (args.filterBy.search.by) {
                                    case "name":
                                        if (args.filterBy.search.value.length > 0)
                                            array1.push("&keyword=" + args.filterBy.search.value);
                                        break;
                                }
                            }
                        }

                        if (!args.context.instances) {
                            if ("zones" in args.context)
                                array1.push("&zoneid=" + args.context.zones[0].id);
                            if ("pods" in args.context)
                                array1.push("&podid=" + args.context.pods[0].id);
                            if ("clusters" in args.context)
                                array1.push("&clusterid=" + args.context.clusters[0].id);
                        } else {
                            //Instances menu > Instance detailView > View Hosts
                            array1.push("&id=" + args.context.instances[0].hostid);
                        }

                        $.ajax({
                            url: createURL("listHosts&type=Routing" + array1.join("") + "&page=" + args.page + "&pagesize=" + pageSize),
                            dataType: "json",
                            async: true,
                            success: function (json) {
                                var items = json.listhostsresponse.host;
                                args.response.success({
                                    actionFilter: hostActionfilter,
                                    data: items
                                });
                            }
                        });
                    },

                    actions: {
                        add: {
                            label: 'label.add.host',

                            createForm: {
                                title: 'label.add.host',
                                fields: {
                                    zoneid: {
                                        docID: 'helpHostZone',
                                        label: 'label.zone',
                                        validation: {
                                            required: true
                                        },
                                        select: function (args) {
                                            var data = args.context.zones ? {
                                                id: args.context.zones[0].id
                                            } : {};

                                            $.ajax({
                                                url: createURL('listZones'),
                                                data: data,
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

                                    //always appear (begin)
                                    podId: {
                                        label: 'label.pod',
                                        docID: 'helpHostPod',
                                        validation: {
                                            required: true
                                        },
                                        dependsOn: 'zoneid',
                                        select: function (args) {
                                            $.ajax({
                                                url: createURL("listPods&zoneid=" + args.zoneid),
                                                dataType: "json",
                                                async: true,
                                                success: function (json) {
                                                    var pods = json.listpodsresponse.pod;
                                                    var items = [];
                                                    $(pods).each(function () {
                                                        if (("pods" in args.context) && (this.id == args.context.pods[0].id))
                                                            items.unshift({
                                                                id: this.id,
                                                                description: this.name
                                                            }); else
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
                                    },

                                    clusterId: {
                                        label: 'label.cluster',
                                        docID: 'helpHostCluster',
                                        validation: {
                                            required: true
                                        },
                                        dependsOn: 'podId',
                                        select: function (args) {
                                            $.ajax({
                                                url: createURL("listClusters&podid=" + args.podId),
                                                dataType: "json",
                                                async: false,
                                                success: function (json) {
                                                    clusterObjs = json.listclustersresponse.cluster;
                                                    var items = [];
                                                    $(clusterObjs).each(function () {
                                                        if (("clusters" in args.context) && (this.id == args.context.clusters[0].id))
                                                            items.unshift({
                                                                id: this.id,
                                                                description: this.name
                                                            }); else
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

                                            args.$select.change(function () {
                                                var $form = $(this).closest('form');

                                                var clusterId = $(this).val();
                                                if (clusterId == null)
                                                    return;

                                                var items = [];
                                                $(clusterObjs).each(function () {
                                                    if (this.id == clusterId) {
                                                        selectedClusterObj = this;
                                                        return false; //break the $.each() loop
                                                    }
                                                });
                                                if (selectedClusterObj == null)
                                                    return;

                                                if (selectedClusterObj.hypervisortype == "Ovm3") {
                                                    //$('li[input_group="general"]', $dialogAddHost).show();
                                                    $form.find('.form-item[rel=hostname]').css('display', 'inline-block');
                                                    $form.find('.form-item[rel=username]').css('display', 'inline-block');
                                                    $form.find('.form-item[rel=password]').css('display', 'inline-block');

                                                    $form.find('.form-item[rel=vcenterHost]').hide();

                                                    //$('li[input_group="Ovm3"]', $dialogAddHost).show();
                                                    $form.find('.form-item[rel=agentUsername]').css('display', 'inline-block');
                                                    $form.find('.form-item[rel=agentUsername]').find('input').val("oracle");
                                                    $form.find('.form-item[rel=agentPassword]').css('display', 'inline-block');
                                                    $form.find('.form-item[rel=agentPort]').css('display', 'inline-block');
                                                    $form.find('.form-item[rel=agentPort]').find('input').val("8899");
                                                    $form.find('.form-item[rel=ovm3vip]').css('display', 'inline-block');
                                                    $form.find('.form-item[rel=ovm3pool]').css('display', 'inline-block');
                                                    $form.find('.form-item[rel=ovm3cluster]').css('display', 'inline-block');
                                                } else {
                                                    //$('li[input_group="general"]', $dialogAddHost).show();
                                                    $form.find('.form-item[rel=hostname]').css('display', 'inline-block');
                                                    $form.find('.form-item[rel=username]').css('display', 'inline-block');
                                                    $form.find('.form-item[rel=password]').css('display', 'inline-block');

                                                    $form.find('.form-item[rel=vcenterHost]').hide();

                                                    //$('li[input_group="Ovm"]', $dialogAddHost).hide();
                                                    $form.find('.form-item[rel=agentUsername]').hide();
                                                    $form.find('.form-item[rel=agentPassword]').hide();

                                                    //$('li[input_group="Ovm3"]', $dialogAddHost).hide();
                                                    $form.find('.form-item[rel=agentUsername]').hide();
                                                    $form.find('.form-item[rel=agentPassword]').hide();
                                                    $form.find('.form-item[rel=agentPort]').hide();
                                                    $form.find('.form-item[rel=ovm3vip]').hide();
                                                    $form.find('.form-item[rel=ovm3pool]').hide();
                                                    $form.find('.form-item[rel=ovm3cluster]').hide();
                                                }
                                            });

                                            args.$select.trigger("change");
                                        }
                                    },
                                    //always appear (end)

                                    //input_group="general" starts here
                                    hostname: {
                                        label: 'label.host.name',
                                        docID: 'helpHostName',
                                        validation: {
                                            required: true
                                        },
                                        isHidden: true
                                    },

                                    username: {
                                        label: 'label.username',
                                        docID: 'helpHostUsername',
                                        validation: {
                                            required: true
                                        },
                                        isHidden: true
                                    },

                                    password: {
                                        label: 'label.password',
                                        docID: 'helpHostPassword',
                                        validation: {
                                            required: true
                                        },
                                        isHidden: true,
                                        isPassword: true
                                    },

                                    isDedicated: {
                                        label: 'label.dedicate',
                                        isBoolean: true,
                                        isChecked: false,
                                        docID: 'helpDedicateResource'
                                    },

                                    domainId: {
                                        label: 'label.domain',
                                        isHidden: true,
                                        validation: {
                                            required: true
                                        },
                                        dependsOn: 'isDedicated',
                                        select: function (args) {
                                            $.ajax({
                                                url: createURL("listDomains&listAll=true"),
                                                dataType: "json",
                                                success: function (json) {
                                                    var domainObjs = json.listdomainsresponse.domain;
                                                    var items = [];

                                                    $(domainObjs).each(function () {
                                                        items.push({
                                                            id: this.id,
                                                            description: this.name
                                                        });
                                                    });
                                                    items.sort(function (a, b) {
                                                        return a.description.localeCompare(b.description);
                                                    });

                                                    args.response.success({
                                                        data: items
                                                    });
                                                }
                                            });
                                        }
                                    },

                                    accountId: {
                                        label: 'label.account',
                                        isHidden: true,
                                        dependsOn: 'isDedicated',
                                        docID: 'helpAccountForDedication',
                                        validation: {
                                            required: false
                                        }
                                    },

                                    //input_group="general" ends here

                                    //input_group="OVM3" starts here
                                    agentPort: {
                                        label: 'label.agent.port',
                                        validation: {
                                            required: false
                                        },
                                        isHidden: true
                                    },
                                    //input_group="OVM3" ends here

                                    //always appear (begin)
                                    hosttags: {
                                        label: 'label.host.tags',
                                        isTokenInput: true,
                                        docID: 'helpHostTags',
                                        validation: {
                                            required: false
                                        },
                                        dataProvider: function (args) {
                                            $.ajax({
                                                url: createURL("listHostTags"),
                                                dataType: "json",
                                                success: function (json) {
                                                    var item = json.listhosttagsresponse.hosttag;
                                                    var tags = [];

                                                    if (item != null) {
                                                        tags = $.map(item, function (tag) {
                                                            return {
                                                                id: tag.name,
                                                                name: tag.name
                                                            };
                                                        });
                                                    }

                                                    args.response.success({
                                                        data: tags,
                                                        hintText: _l('hint.type.part.host.tag'),
                                                        noResultsText: _l('hint.no.host.tags')
                                                    });
                                                },
                                                error: function (XMLHttpResponse) {
                                                    var errorMsg = parseXMLHttpResponse(XMLHttpResponse);

                                                    args.response.error(errorMsg);
                                                }
                                            });
                                        }
                                    }
                                    //always appear (end)
                                }
                            },

                            action: function (args) {
                                var data = {
                                    zoneid: args.data.zoneid,
                                    podid: args.data.podId,
                                    clusterid: args.data.clusterId,
                                    hypervisor: selectedClusterObj.hypervisortype,
                                    clustertype: selectedClusterObj.clustertype,
                                    hosttags: args.data.hosttags
                                };

                                $.extend(data, {
                                    username: args.data.username,
                                    password: args.data.password
                                });

                                var hostname = args.data.hostname;
                                var url;
                                if (hostname.indexOf("http://") == -1)
                                    url = "http://" + hostname; else
                                    url = hostname;

                                $.extend(data, {
                                    url: url
                                });

                                if (selectedClusterObj.hypervisortype == "Ovm3") {
                                    $.extend(data, {
                                        agentusername: args.data.agentUsername,
                                        agentpassword: args.data.agentPassword,
                                        agentport: args.data.agentPort
                                    });
                                }

                                var hostId = null;
                                $.ajax({
                                    url: createURL("addHost"),
                                    type: "POST",
                                    data: data,
                                    success: function (json) {
                                        var item = json.addhostresponse.host[0];

                                        hostId = json.addhostresponse.host[0].id;

                                        //EXPLICIT DEDICATION
                                        var array2 = [];

                                        if (args.$form.find('.form-item[rel=isDedicated]').find('input[type=checkbox]').is(':Checked') == true) {
                                            if (args.data.accountId != "")
                                                array2.push("&account=" + todb(args.data.accountId));


                                            if (hostId != null) {
                                                $.ajax({
                                                    url: createURL("dedicateHost&hostId=" + hostId + "&domainId=" + args.data.domainId + array2.join("")),
                                                    dataType: "json",
                                                    success: function (json) {
                                                        var jid = json.dedicatehostresponse.jobid;
                                                        args.response.success({
                                                            _custom: {
                                                                jobId: jid
                                                            },
                                                            notification: {
                                                                poll: pollAsyncJobResult,
                                                                interval: 4500,
                                                                desc: "Dedicate Host"
                                                            },

                                                            data: item
                                                        });
                                                    },

                                                    error: function (json) {
                                                        args.response.error(parseXMLHttpResponse(XMLHttpResponse));
                                                    }
                                                });
                                            }
                                        }
                                        args.response.success({
                                            data: item
                                        });
                                    },

                                    error: function (XMLHttpResponse) {
                                        var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
                                        args.response.error(errorMsg);
                                    }
                                });
                            },

                            notification: {
                                poll: function (args) {
                                    args.complete({
                                        actionFilter: hostActionfilter
                                    });
                                }
                            },

                            messages: {
                                notification: function (args) {
                                    return 'label.add.host';
                                }
                            }
                        },
                        viewMetrics: {
                            label: 'label.metrics',
                            isHeader: true,
                            addRow: false,
                            action: {
                                custom: cloudStack.uiCustom.metricsView({resource: 'hosts'})
                            },
                            messages: {
                                notification: function (args) {
                                    return 'label.metrics';
                                }
                            }
                        }
                    },
                    detailView: {
                        name: "Host details",
                        viewAll: {
                            label: 'label.instances',
                            path: 'instances'
                        },
                        actions: {
                            edit: {
                                label: 'label.edit',
                                action: function (args) {
                                    var array1 = [];
                                    array1.push("&hosttags=" + todb(args.data.hosttags));

                                    if (args.data.oscategoryid != null && args.data.oscategoryid.length > 0)
                                        array1.push("&osCategoryId=" + args.data.oscategoryid);

                                    $.ajax({
                                        url: createURL("updateHost&id=" + args.context.hosts[0].id + array1.join("")),
                                        dataType: "json",
                                        success: function (json) {
                                            var item = json.updatehostresponse.host;
                                            args.response.success({
                                                actionFilter: hostActionfilter,
                                                data: item
                                            });
                                        }
                                    });
                                }
                            },

                            dedicate: {
                                label: 'label.dedicate.host',
                                messages: {
                                    confirm: function (args) {
                                        return 'message.confirm.dedicate.host.domain.account';
                                    },
                                    notification: function (args) {
                                        return 'message.host.dedicated';
                                    }
                                },
                                createForm: {
                                    title: 'label.dedicate.host',
                                    fields: {
                                        domainId: {
                                            label: 'label.domain',
                                            validation: {
                                                required: true
                                            },
                                            select: function (args) {
                                                $.ajax({
                                                    url: createURL("listDomains&listAll=true"),
                                                    dataType: "json",
                                                    async: false,
                                                    success: function (json) {
                                                        var domainObjs = json.listdomainsresponse.domain;
                                                        var items = [];

                                                        $(domainObjs).each(function () {
                                                            items.push({
                                                                id: this.id,
                                                                description: this.name
                                                            });
                                                        });
                                                        items.sort(function (a, b) {
                                                            return a.description.localeCompare(b.description);
                                                        });

                                                        args.response.success({
                                                            data: items
                                                        });
                                                    }
                                                });
                                            }
                                        },
                                        accountId: {
                                            label: 'label.account',
                                            docID: 'helpAccountForDedication',
                                            validation: {
                                                required: false
                                            }
                                        }
                                    }
                                },
                                action: function (args) {
                                    //EXPLICIT DEDICATION
                                    var array2 = [];
                                    if (args.data.accountId != "")
                                        array2.push("&account=" + todb(args.data.accountId));

                                    $.ajax({
                                        url: createURL("dedicateHost&hostId=" +
                                            args.context.hosts[0].id +
                                            "&domainId=" + args.data.domainId + array2.join("")),
                                        dataType: "json",
                                        success: function (json) {
                                            var jid = json.dedicatehostresponse.jobid;

                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getActionFilter: function () {
                                                        return hostActionfilter;
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
                            release: {
                                label: 'label.release.dedicated.host',
                                messages: {
                                    confirm: function (args) {
                                        return 'message.confirm.release.dedicated.host';
                                    },
                                    notification: function (args) {
                                        return 'message.host.dedication.released';
                                    }
                                },
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("releaseDedicatedHost&hostid=" + args.context.hosts[0].id),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var jid = json.releasededicatedhostresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getActionFilter: function () {
                                                        return hostActionfilter;
                                                    }
                                                }
                                            });
                                        },
                                        error: function (json) {
                                            args.response.error(parseXMLHttpResponse(json));
                                        }
                                    });
                                },
                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            },


                            enableMaintenanceMode: {
                                label: 'label.action.enable.maintenance.mode',
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("prepareHostForMaintenance&id=" + args.context.hosts[0].id),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var jid = json.preparehostformaintenanceresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getUpdatedItem: function (json) {
                                                        return json.queryasyncjobresultresponse.jobresult.host;
                                                    },
                                                    getActionFilter: function () {
                                                        return hostActionfilter;
                                                    }
                                                }
                                            });
                                        }
                                    });
                                },
                                messages: {
                                    confirm: function (args) {
                                        return 'message.action.host.enable.maintenance.mode';
                                    },
                                    notification: function (args) {
                                        return 'label.action.enable.maintenance.mode';
                                    }
                                },
                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            },

                            cancelMaintenanceMode: {
                                label: 'label.action.cancel.maintenance.mode',
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("cancelHostMaintenance&id=" + args.context.hosts[0].id),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var jid = json.cancelhostmaintenanceresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getUpdatedItem: function (json) {
                                                        return json.queryasyncjobresultresponse.jobresult.host;
                                                    },
                                                    getActionFilter: function () {
                                                        return hostActionfilter;
                                                    }
                                                }
                                            });
                                        }
                                    });
                                },
                                messages: {
                                    confirm: function (args) {
                                        return 'message.action.cancel.maintenance.mode';
                                    },
                                    notification: function (args) {
                                        return 'label.action.cancel.maintenance.mode';
                                    }
                                },
                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            },

                            forceReconnect: {
                                label: 'label.action.force.reconnect',
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("reconnectHost&id=" + args.context.hosts[0].id),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var jid = json.reconnecthostresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getUpdatedItem: function (json) {
                                                        return json.queryasyncjobresultresponse.jobresult.host;
                                                    },
                                                    getActionFilter: function () {
                                                        return hostActionfilter;
                                                    }
                                                }
                                            });
                                        }
                                    });
                                },
                                messages: {
                                    confirm: function (args) {
                                        return 'message.confirm.action.force.reconnect';
                                    },
                                    notification: function (args) {
                                        return 'label.action.force.reconnect';
                                    }
                                },
                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            },

                            enable: {
                                label: 'label.enable.host',
                                action: function (args) {
                                    var data = {
                                        id: args.context.hosts[0].id,
                                        allocationstate: "Enable"
                                    };
                                    $.ajax({
                                        url: createURL("updateHost"),
                                        data: data,
                                        success: function (json) {
                                            var item = json.updatehostresponse.host;
                                            args.response.success({
                                                actionFilter: hostActionfilter,
                                                data: item
                                            });
                                        }
                                    });
                                },
                                messages: {
                                    confirm: function (args) {
                                        return 'message.confirm.enable.host';
                                    },
                                    notification: function (args) {
                                        return 'label.enable.host';
                                    }
                                },
                                notification: {
                                    poll: function (args) {
                                        args.complete();
                                    }
                                }
                            },

                            disable: {
                                label: 'label.disable.host',
                                action: function (args) {
                                    var data = {
                                        id: args.context.hosts[0].id,
                                        allocationstate: "Disable"
                                    };
                                    $.ajax({
                                        url: createURL("updateHost"),
                                        data: data,
                                        success: function (json) {
                                            var item = json.updatehostresponse.host;
                                            args.response.success({
                                                actionFilter: hostActionfilter,
                                                data: item
                                            });
                                        }
                                    });
                                },
                                messages: {
                                    confirm: function (args) {
                                        return 'message.confirm.disable.host';
                                    },
                                    notification: function (args) {
                                        return 'label.disable.host';
                                    }
                                },
                                notification: {
                                    poll: function (args) {
                                        args.complete();
                                    }
                                }
                            },

                            'remove': {
                                label: 'label.action.remove.host',
                                messages: {
                                    notification: function (args) {
                                        return 'label.action.remove.host';
                                    }
                                },
                                createForm: {
                                    title: 'label.action.remove.host',
                                    desc: 'message.action.remove.host',
                                    preFilter: function (args) { //bug to fix: preFilter is not picked up from here
                                        if (!isAdmin()) {
                                            args.$form.find('.form-item[rel=isForced]').hide();
                                        }
                                    },
                                    fields: {
                                        isForced: {
                                            label: 'force.remove',
                                            isBoolean: true,
                                            isHidden: false
                                        }
                                    }
                                },
                                action: function (args) {
                                    var data = {
                                        id: args.context.hosts[0].id
                                    };
                                    if (args.$form.find('.form-item[rel=isForced]').css("display") != "none") {
                                        $.extend(data, {
                                            forced: (args.data.isForced == "on")
                                        });
                                    }

                                    $.ajax({
                                        url: createURL("deleteHost"),
                                        data: data,
                                        success: function (json) {
                                            //{ "deletehostresponse" : { "success" : "true"}  }
                                            args.response.success({
                                                data: {}
                                            });

                                            if (args.context.hosts[0].hypervisor == "XenServer") {
                                                cloudStack.dialog.notice({message: _s("The host has been deleted. Please eject the host from XenServer Pool")})
                                            }
                                        }
                                    });
                                },
                                notification: {
                                    poll: function (args) {
                                        args.complete();
                                    }
                                }
                            }
                        },
                        tabFilter: function (args) {
                            var hiddenTabs = [];
                            if (args.context.hosts[0].gpugroup == null) {
                                hiddenTabs.push("gpu");
                            }
                            return hiddenTabs;
                        },
                        tabs: {
                            details: {
                                title: 'label.details',

                                preFilter: function (args) {
                                    var hiddenFields = [];
                                    $.ajax({
                                        url: createURL('listConfigurations&name=ha.tag'),
                                        dataType: 'json',
                                        async: false,
                                        success: function (json) {
                                            if (json.listconfigurationsresponse.configuration == null || json.listconfigurationsresponse.configuration[0].value == null || json.listconfigurationsresponse.configuration[0].value.length == 0) {
                                                hiddenFields.push('hahost');
                                            }
                                        }
                                    });
                                    return hiddenFields;
                                },

                                fields: [{
                                    name: {
                                        label: 'label.name'
                                    }
                                },
                                    {
                                        id: {
                                            label: 'label.id'
                                        },
                                        resourcestate: {
                                            label: 'label.resource.state'
                                        },
                                        state: {
                                            label: 'label.state'
                                        },
                                        type: {
                                            label: 'label.type'
                                        },
                                        hypervisor: {
                                            label: 'label.hypervisor'
                                        },
                                        hypervisorversion: {
                                            label: 'label.hypervisor.version'
                                        },
                                        hosttags: {
                                            label: 'label.host.tags',
                                            isEditable: true,
                                            isTokenInput: true,
                                            dataProvider: function (args) {
                                                $.ajax({
                                                    url: createURL("listHostTags"),
                                                    dataType: "json",
                                                    success: function (json) {
                                                        var item = json.listhosttagsresponse.hosttag;
                                                        var tags = [];

                                                        if (item != null) {
                                                            tags = $.map(item, function (tag) {
                                                                return {
                                                                    id: tag.name,
                                                                    name: tag.name
                                                                };
                                                            });
                                                        }

                                                        args.response.success({
                                                            data: tags,
                                                            hintText: _l('hint.type.part.host.tag'),
                                                            noResultsText: _l('hint.no.host.tags')
                                                        });
                                                    },
                                                    error: function (XMLHttpResponse) {
                                                        var errorMsg = parseXMLHttpResponse(XMLHttpResponse);

                                                        args.response.error(errorMsg);
                                                    }
                                                });
                                            }
                                        },
                                        hahost: {
                                            label: 'label.ha.enabled',
                                            converter: cloudStack.converters.toBooleanText
                                        },
                                        oscategoryid: {
                                            label: 'label.os.preference',
                                            isEditable: true,
                                            select: function (args) {
                                                $.ajax({
                                                    url: createURL("listOsCategories"),
                                                    dataType: "json",
                                                    async: true,
                                                    success: function (json) {
                                                        var oscategoryObjs = json.listoscategoriesresponse.oscategory;
                                                        var items = [{
                                                            id: '',
                                                            description: _l('')
                                                        }];
                                                        $(oscategoryObjs).each(function () {
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
                                        },
                                        zonename: {
                                            label: 'label.zone'
                                        },
                                        podname: {
                                            label: 'label.pod'
                                        },
                                        clustername: {
                                            label: 'label.cluster'
                                        },
                                        ipaddress: {
                                            label: 'label.ip.address'
                                        },
                                        disconnected: {
                                            label: 'label.last.disconnected'
                                        },
                                        cpusockets: {
                                            label: 'label.number.of.cpu.sockets'
                                        }
                                    }, {

                                        isdedicated: {
                                            label: 'label.dedicated'
                                        },
                                        domainid: {
                                            label: 'label.domain.id'
                                        }
                                    }],

                                dataProvider: function (args) {
                                    $.ajax({
                                        url: createURL("listHosts&id=" + args.context.hosts[0].id),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var item = json.listhostsresponse.host[0];
                                            $.ajax({
                                                url: createURL("listDedicatedHosts&hostid=" + args.context.hosts[0].id),
                                                dataType: "json",
                                                async: false,
                                                success: function (json) {
                                                    if (json.listdedicatedhostsresponse.dedicatedhost != undefined) {
                                                        var hostItem = json.listdedicatedhostsresponse.dedicatedhost[0];
                                                        if (hostItem.domainid != null) {
                                                            $.extend(item, {
                                                                isdedicated: _l('label.yes'),
                                                                domainid: hostItem.domainid
                                                            });
                                                        }
                                                    } else
                                                        $.extend(item, {
                                                            isdedicated: _l('label.no')
                                                        })
                                                },
                                                error: function (json) {
                                                    args.response.error(parseXMLHttpResponse(XMLHttpResponse));
                                                }
                                            });
                                            args.response.success({
                                                actionFilter: hostActionfilter,
                                                data: item
                                            });
                                        }
                                    });
                                }
                            },

                            stats: {
                                title: 'label.statistics',
                                fields: {
                                    totalCPU: {
                                        label: 'label.total.cpu'
                                    },
                                    cpuused: {
                                        label: 'label.cpu.utilized'
                                    },
                                    cpuallocated: {
                                        label: 'label.cpu.allocated.for.VMs'
                                    },
                                    memorytotal: {
                                        label: 'label.memory.total'
                                    },
                                    memoryallocated: {
                                        label: 'label.memory.allocated'
                                    },
                                    memoryused: {
                                        label: 'label.memory.used'
                                    },
                                    networkkbsread: {
                                        label: 'label.network.read'
                                    },
                                    networkkbswrite: {
                                        label: 'label.network.write'
                                    }
                                },
                                dataProvider: function (args) {
                                    $.ajax({
                                        url: createURL("listHosts&id=" + args.context.hosts[0].id),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var jsonObj = json.listhostsresponse.host[0];
                                            args.response.success({
                                                data: {
                                                    totalCPU: jsonObj.cpunumber + " x " + cloudStack.converters.convertHz(jsonObj.cpuspeed),
                                                    cpuused: jsonObj.cpuused,
                                                    cpuallocated: (jsonObj.cpuallocated == null || jsonObj.cpuallocated == 0) ? "N/A" : jsonObj.cpuallocated,
                                                    memorytotal: (jsonObj.memorytotal == null || jsonObj.memorytotal == 0) ? "N/A" : cloudStack.converters.convertBytes(jsonObj.memorytotal),
                                                    memoryallocated: (jsonObj.memoryallocated == null || jsonObj.memoryallocated == 0) ? "N/A" : cloudStack.converters.convertBytes(jsonObj.memoryallocated),
                                                    memoryused: (jsonObj.memoryused == null || jsonObj.memoryused == 0) ? "N/A" : cloudStack.converters.convertBytes(jsonObj.memoryused),
                                                    networkkbsread: (jsonObj.networkkbsread == null) ? "N/A" : cloudStack.converters.convertBytes(jsonObj.networkkbsread * 1024),
                                                    networkkbswrite: (jsonObj.networkkbswrite == null) ? "N/A" : cloudStack.converters.convertBytes(jsonObj.networkkbswrite * 1024)
                                                }
                                            });
                                        }
                                    });
                                }
                            },
                            gpu: {
                                title: 'label.gpu',
                                custom: function (args) {
                                    var gpugroups = null;
                                    $.ajax({
                                        url: createURL("listHosts&id=" + args.context.hosts[0].id),
                                        dataType: "json",
                                        async: false,
                                        success: function (json) {
                                            var item = json.listhostsresponse.host[0];
                                            if (item != null && item.gpugroup != null)
                                                gpugroups = item.gpugroup;
                                        }
                                    });

                                    var $tabcontent = $('<div>').addClass('gpugroups');

                                    $(gpugroups).each(function () {
                                        var gpugroupObj = this;

                                        var $groupcontainer = $('<div>').addClass('gpugroup-container');

                                        //group name
                                        $groupcontainer.append($('<div>').addClass('title')
                                            .append($('<span>').html(gpugroupObj.gpugroupname)));
                                        //vgpu details
                                        var $groupdetails = $('<div>').listView({
                                            context: args.context,
                                            listView: {
                                                id: 'gputypes',
                                                hideToolbar: true,
                                                fields: {
                                                    vgputype: {
                                                        label: 'label.vgpu.type'
                                                    },
                                                    maxvgpuperpgpu: {
                                                        label: 'label.vgpu.max.vgpu.per.gpu',
                                                        converter: function (args) {
                                                            return (args == null || args == 0) ? "" : args;
                                                        }
                                                    },
                                                    videoram: {
                                                        label: 'label.vgpu.video.ram',
                                                        converter: function (args) {
                                                            return (args == null || args == 0) ? "" : cloudStack.converters.convertBytes(args);
                                                        }
                                                    },
                                                    maxresolution: {
                                                        label: 'label.vgpu.max.resolution'
                                                    },
                                                    remainingcapacity: {
                                                        label: 'label.vgpu.remaining.capacity'
                                                    }
                                                },
                                                dataProvider: function (args) {
                                                    var items = gpugroupObj.vgpu.sort(function (a, b) {
                                                        return a.maxvgpuperpgpu >= b.maxvgpuperpgpu;
                                                    });
                                                    $(items).each(function () {
                                                        this.maxresolution = (this.maxresolutionx == null || this.maxresolutionx == 0
                                                        || this.maxresolutiony == null || this.maxresolutiony == 0)
                                                            ? "" : this.maxresolutionx + " x " + this.maxresolutiony;
                                                    });
                                                    args.response.success({
                                                        data: items
                                                    });
                                                }
                                            }
                                        });
                                        $groupcontainer.append($groupdetails);
                                        $tabcontent.append($groupcontainer);
                                    });
                                    return $tabcontent;
                                }
                            }
                        }
                    }
                }
            },
            'primary-storage': {
                title: 'label.primary.storage',
                id: 'primarystorages',
                listView: {
                    id: 'primarystorages',
                    section: 'primary-storage',
                    fields: {
                        name: {
                            label: 'label.name',
                            truncate: true
                        },
                        ipaddress: {
                            label: 'label.server'
                        },
                        path: {
                            label: 'label.path',
                            truncate: true
                        },
                        clustername: {
                            label: 'label.cluster',
                            truncate: true
                        },
                        scope: {
                            label: 'label.scope'
                        }
                    },

                    dataProvider: function (args) {
                        var array1 = [];
                        if (args.filterBy != null) {
                            if (args.filterBy.search != null && args.filterBy.search.by != null && args.filterBy.search.value != null) {
                                switch (args.filterBy.search.by) {
                                    case "name":
                                        if (args.filterBy.search.value.length > 0)
                                            array1.push("&keyword=" + args.filterBy.search.value);
                                        break;
                                }
                            }
                        }
                        array1.push("&zoneid=" + args.context.zones[0].id);
                        if ("pods" in args.context)
                            array1.push("&podid=" + args.context.pods[0].id);
                        if ("clusters" in args.context)
                            array1.push("&clusterid=" + args.context.clusters[0].id);
                        $.ajax({
                            url: createURL("listStoragePools&page=" + args.page + "&pagesize=" + pageSize + array1.join("")),
                            dataType: "json",
                            async: true,
                            success: function (json) {
                                var items = json.liststoragepoolsresponse.storagepool;
                                args.response.success({
                                    actionFilter: primarystorageActionfilter,
                                    data: items
                                });
                            }
                        });
                    },

                    actions: {
                        add: {
                            label: 'label.add.primary.storage',

                            createForm: {
                                title: 'label.add.primary.storage',
                                fields: {
                                    scope: {
                                        label: 'label.scope',
                                        select: function (args) {
                                            var scope = [{
                                                id: 'cluster',
                                                description: _l('label.cluster')
                                            },
                                                {
                                                    id: 'zone',
                                                    description: _l('label.zone.wide')
                                                }
                                                // { id: 'host', description: _l('label.host') }
                                            ];

                                            args.response.success({
                                                data: scope
                                            });

                                            args.$select.change(function () {
                                                var $form = $(this).closest('form');
                                                var scope = $(this).val();

                                                if (scope == 'zone') {
                                                    $form.find('.form-item[rel=podId]').hide();
                                                    $form.find('.form-item[rel=clusterId]').hide();
                                                    $form.find('.form-item[rel=hostId]').hide();
                                                    $form.find('.form-item[rel=hypervisor]').css('display', 'inline-block');
                                                } else if (scope == 'cluster') {

                                                    $form.find('.form-item[rel=hostId]').hide();
                                                    $form.find('.form-item[rel=podId]').css('display', 'inline-block');
                                                    $form.find('.form-item[rel=clusterId]').css('display', 'inline-block');
                                                    $form.find('.form-item[rel=hypervisor]').hide();
                                                } else if (scope == 'host') {
                                                    $form.find('.form-item[rel=podId]').css('display', 'inline-block');
                                                    $form.find('.form-item[rel=clusterId]').css('display', 'inline-block');
                                                    $form.find('.form-item[rel=hostId]').css('display', 'inline-block');
                                                    $form.find('.form-item[rel=hypervisor]').hide();
                                                }
                                            })
                                        }
                                    },


                                    hypervisor: {
                                        label: 'label.hypervisor',
                                        isHidden: true,
                                        select: function (args) {
                                            var items = [];
                                            items.push({
                                                id: 'KVM',
                                                description: _l('KVM')
                                            });
                                            items.push({
                                                id: 'Any',
                                                description: _l('Any')
                                            });
                                            args.response.success({
                                                data: items
                                            });
                                        }
                                    },

                                    zoneid: {
                                        label: 'label.zone',
                                        docID: 'helpPrimaryStorageZone',
                                        validation: {
                                            required: true
                                        },
                                        select: function (args) {
                                            var data = args.context.zones ? {
                                                id: args.context.zones[0].id
                                            } : {};

                                            $.ajax({
                                                url: createURL('listZones'),
                                                data: data,
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
                                    podId: {
                                        label: 'label.pod',
                                        dependsOn: 'zoneid',
                                        docID: 'helpPrimaryStoragePod',
                                        validation: {
                                            required: true
                                        },
                                        select: function (args) {
                                            $.ajax({
                                                url: createURL("listPods&zoneid=" + args.zoneid),
                                                dataType: "json",
                                                async: true,
                                                success: function (json) {
                                                    var pods = json.listpodsresponse.pod;
                                                    var items = [];
                                                    $(pods).each(function () {
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
                                    },

                                    clusterId: {
                                        label: 'label.cluster',
                                        docID: 'helpPrimaryStorageCluster',
                                        validation: {
                                            required: true
                                        },
                                        dependsOn: 'podId',
                                        select: function (args) {
                                            $.ajax({
                                                url: createURL("listClusters&podid=" + args.podId),
                                                dataType: "json",
                                                async: false,
                                                success: function (json) {
                                                    clusterObjs = json.listclustersresponse.cluster;
                                                    var items = [];
                                                    $(clusterObjs).each(function () {
                                                        items.push({
                                                            id: this.id,
                                                            description: this.name
                                                        });
                                                    });
                                                    args.response.success({
                                                        actionFilter: clusterActionfilter,
                                                        data: items
                                                    });
                                                }
                                            });
                                        }
                                    },

                                    hostId: {
                                        label: 'label.host',
                                        validation: {
                                            required: true
                                        },
                                        dependsOn: 'clusterId',
                                        select: function (args) {
                                            $.ajax({
                                                url: createURL('listHosts'),
                                                data: {
                                                    clusterid: args.clusterId
                                                },
                                                success: function (json) {
                                                    var hosts = json.listhostsresponse.host ?
                                                        json.listhostsresponse.host : []
                                                    args.response.success({
                                                        data: $.map(hosts, function (host) {
                                                            return {
                                                                id: host.id,
                                                                description: host.name
                                                            }
                                                        })
                                                    });
                                                }
                                            });
                                        }
                                    },

                                    name: {
                                        label: 'label.name',
                                        docID: 'helpPrimaryStorageName',
                                        validation: {
                                            required: true
                                        }
                                    },

                                    protocol: {
                                        label: 'label.protocol',
                                        docID: 'helpPrimaryStorageProtocol',
                                        validation: {
                                            required: true
                                        },
                                        dependsOn: 'clusterId',
                                        select: function (args) {
                                            var clusterId = args.clusterId;
                                            if (clusterId == null || clusterId.length == 0) {
                                                args.response.success({
                                                    data: []
                                                });
                                                return;
                                            }

                                            $(clusterObjs).each(function () {
                                                if (this.id == clusterId) {
                                                    selectedClusterObj = this;
                                                    return false; //break the $.each() loop
                                                }
                                            });

                                            if (selectedClusterObj.hypervisortype == "KVM") {
                                                var items = [];
                                                items.push({
                                                    id: "nfs",
                                                    description: "nfs"
                                                });
                                                items.push({
                                                    id: "SharedMountPoint",
                                                    description: "SharedMountPoint"
                                                });
                                                items.push({
                                                    id: "rbd",
                                                    description: "RBD"
                                                });
                                                items.push({
                                                    id: "clvm",
                                                    description: "CLVM"
                                                });
                                                items.push({
                                                    id: "gluster",
                                                    description: "Gluster"
                                                });
                                                args.response.success({
                                                    data: items
                                                });
                                            } else if (selectedClusterObj.hypervisortype == "XenServer") {
                                                var items = [];
                                                items.push({
                                                    id: "nfs",
                                                    description: "nfs"
                                                });
                                                items.push({
                                                    id: "PreSetup",
                                                    description: "PreSetup"
                                                });
                                                items.push({
                                                    id: "iscsi",
                                                    description: "iscsi"
                                                });
                                                items.push({
                                                    id: "custom",
                                                    description: "custom"
                                                });
                                                args.response.success({
                                                    data: items
                                                });
                                                // 3.3.2 has ceph/ocfs2/iscsi etc
                                            } else if (selectedClusterObj.hypervisortype == "Ovm3") {
                                                var items = [];
                                                items.push({
                                                    id: "nfs",
                                                    description: "nfs"
                                                });
                                            } else {
                                                args.response.success({
                                                    data: []
                                                });
                                            }

                                            args.$select.change(function () {
                                                var $form = $(this).closest('form');

                                                var protocol = $(this).val();
                                                if (protocol == null)
                                                    return;


                                                if (protocol == "nfs") {
                                                    $form.find('.form-item[rel=server]').css('display', 'inline-block');
                                                    $form.find('.form-item[rel=server]').find(".value").find("input").val("");

                                                    $form.find('.form-item[rel=path]').css('display', 'inline-block');
                                                    var $required = $form.find('.form-item[rel=path]').find(".name").find("label span");
                                                    $form.find('.form-item[rel=path]').find(".name").find("label").text("Path:").prepend($required);

                                                    $form.find('.form-item[rel=smbUsername]').hide();
                                                    $form.find('.form-item[rel=smbPassword]').hide();
                                                    $form.find('.form-item[rel=smbDomain]').hide();

                                                    $form.find('.form-item[rel=iqn]').hide();
                                                    $form.find('.form-item[rel=lun]').hide();

                                                    $form.find('.form-item[rel=volumegroup]').hide();

                                                    $form.find('.form-item[rel=vCenterDataCenter]').hide();
                                                    $form.find('.form-item[rel=vCenterDataStore]').hide();

                                                    $form.find('.form-item[rel=rbdmonitor]').hide();
                                                    $form.find('.form-item[rel=rbdpool]').hide();
                                                    $form.find('.form-item[rel=rbdid]').hide();
                                                    $form.find('.form-item[rel=rbdsecret]').hide();

                                                    $form.find('.form-item[rel=glustervolume]').hide();
                                                } else if (protocol == "SMB") {
                                                    //"SMB" show almost the same fields as "nfs" does, except 3 more SMB-specific fields.
                                                    $form.find('.form-item[rel=server]').css('display', 'inline-block');
                                                    $form.find('.form-item[rel=server]').find(".value").find("input").val("");

                                                    $form.find('.form-item[rel=path]').css('display', 'inline-block');
                                                    var $required = $form.find('.form-item[rel=path]').find(".name").find("label span");
                                                    $form.find('.form-item[rel=path]').find(".name").find("label").text("Path:").prepend($required);

                                                    $form.find('.form-item[rel=smbUsername]').css('display', 'inline-block');
                                                    $form.find('.form-item[rel=smbPassword]').css('display', 'inline-block');
                                                    $form.find('.form-item[rel=smbDomain]').css('display', 'inline-block');

                                                    $form.find('.form-item[rel=iqn]').hide();
                                                    $form.find('.form-item[rel=lun]').hide();

                                                    $form.find('.form-item[rel=volumegroup]').hide();

                                                    $form.find('.form-item[rel=vCenterDataCenter]').hide();
                                                    $form.find('.form-item[rel=vCenterDataStore]').hide();

                                                    $form.find('.form-item[rel=rbdmonitor]').hide();
                                                    $form.find('.form-item[rel=rbdpool]').hide();
                                                    $form.find('.form-item[rel=rbdid]').hide();
                                                    $form.find('.form-item[rel=rbdsecret]').hide();

                                                    $form.find('.form-item[rel=glustervolume]').hide();
                                                } else if (protocol == "ocfs2") {
                                                    //ocfs2 is the same as nfs, except no server field.
                                                    $form.find('.form-item[rel=server]').hide();
                                                    $form.find('.form-item[rel=server]').find(".value").find("input").val("");

                                                    $form.find('.form-item[rel=path]').css('display', 'inline-block');
                                                    var $required = $form.find('.form-item[rel=path]').find(".name").find("label span");
                                                    $form.find('.form-item[rel=path]').find(".name").find("label").text("Path:").prepend($required);

                                                    $form.find('.form-item[rel=smbUsername]').hide();
                                                    $form.find('.form-item[rel=smbPassword]').hide();
                                                    $form.find('.form-item[rel=smbDomain]').hide();

                                                    $form.find('.form-item[rel=iqn]').hide();
                                                    $form.find('.form-item[rel=lun]').hide();

                                                    $form.find('.form-item[rel=volumegroup]').hide();

                                                    $form.find('.form-item[rel=vCenterDataCenter]').hide();
                                                    $form.find('.form-item[rel=vCenterDataStore]').hide();

                                                    $form.find('.form-item[rel=rbdmonitor]').hide();
                                                    $form.find('.form-item[rel=rbdpool]').hide();
                                                    $form.find('.form-item[rel=rbdid]').hide();
                                                    $form.find('.form-item[rel=rbdsecret]').hide();

                                                    $form.find('.form-item[rel=glustervolume]').hide();
                                                } else if (protocol == "PreSetup") {
                                                    $form.find('.form-item[rel=server]').hide();
                                                    $form.find('.form-item[rel=server]').find(".value").find("input").val("localhost");

                                                    $form.find('.form-item[rel=path]').css('display', 'inline-block');
                                                    var $required = $form.find('.form-item[rel=path]').find(".name").find("label span");
                                                    $form.find('.form-item[rel=path]').find(".name").find("label").text("SR Name-Label:").prepend($required);

                                                    $form.find('.form-item[rel=smbUsername]').hide();
                                                    $form.find('.form-item[rel=smbPassword]').hide();
                                                    $form.find('.form-item[rel=smbDomain]').hide();

                                                    $form.find('.form-item[rel=iqn]').hide();
                                                    $form.find('.form-item[rel=lun]').hide();

                                                    $form.find('.form-item[rel=volumegroup]').hide();

                                                    $form.find('.form-item[rel=vCenterDataCenter]').hide();
                                                    $form.find('.form-item[rel=vCenterDataStore]').hide();

                                                    $form.find('.form-item[rel=rbdmonitor]').hide();
                                                    $form.find('.form-item[rel=rbdpool]').hide();
                                                    $form.find('.form-item[rel=rbdid]').hide();
                                                    $form.find('.form-item[rel=rbdsecret]').hide();

                                                    $form.find('.form-item[rel=glustervolume]').hide();
                                                } else if (protocol == "custom") {
                                                    $form.find('.form-item[rel=server]').hide();
                                                    $form.find('.form-item[rel=server]').find(".value").find("input").val("localhost");

                                                    $form.find('.form-item[rel=path]').hide();

                                                    $form.find('.form-item[rel=smbUsername]').hide();
                                                    $form.find('.form-item[rel=smbPassword]').hide();
                                                    $form.find('.form-item[rel=smbDomain]').hide();

                                                    $form.find('.form-item[rel=iqn]').hide();
                                                    $form.find('.form-item[rel=lun]').hide();

                                                    $form.find('.form-item[rel=volumegroup]').hide();

                                                    $form.find('.form-item[rel=vCenterDataCenter]').hide();
                                                    $form.find('.form-item[rel=vCenterDataStore]').hide();

                                                    $form.find('.form-item[rel=rbdmonitor]').hide();
                                                    $form.find('.form-item[rel=rbdpool]').hide();
                                                    $form.find('.form-item[rel=rbdid]').hide();
                                                    $form.find('.form-item[rel=rbdsecret]').hide();

                                                    $form.find('.form-item[rel=glustervolume]').hide();
                                                } else if (protocol == "iscsi") {
                                                    $form.find('.form-item[rel=server]').css('display', 'inline-block');
                                                    $form.find('.form-item[rel=server]').find(".value").find("input").val("");

                                                    $form.find('.form-item[rel=path]').hide();

                                                    $form.find('.form-item[rel=smbUsername]').hide();
                                                    $form.find('.form-item[rel=smbPassword]').hide();
                                                    $form.find('.form-item[rel=smbDomain]').hide();

                                                    $form.find('.form-item[rel=iqn]').css('display', 'inline-block');
                                                    $form.find('.form-item[rel=lun]').css('display', 'inline-block');

                                                    $form.find('.form-item[rel=volumegroup]').hide();

                                                    $form.find('.form-item[rel=vCenterDataCenter]').hide();
                                                    $form.find('.form-item[rel=vCenterDataStore]').hide();

                                                    $form.find('.form-item[rel=rbdmonitor]').hide();
                                                    $form.find('.form-item[rel=rbdpool]').hide();
                                                    $form.find('.form-item[rel=rbdid]').hide();
                                                    $form.find('.form-item[rel=rbdsecret]').hide();

                                                    $form.find('.form-item[rel=glustervolume]').hide();
                                                } else if ($(this).val() == "clvm") {
                                                    $form.find('.form-item[rel=server]').hide();
                                                    $form.find('.form-item[rel=server]').find(".value").find("input").val("localhost");

                                                    $form.find('.form-item[rel=path]').hide();

                                                    $form.find('.form-item[rel=smbUsername]').hide();
                                                    $form.find('.form-item[rel=smbPassword]').hide();
                                                    $form.find('.form-item[rel=smbDomain]').hide();

                                                    $form.find('.form-item[rel=iqn]').hide();
                                                    $form.find('.form-item[rel=lun]').hide();

                                                    $form.find('.form-item[rel=volumegroup]').css('display', 'inline-block');

                                                    $form.find('.form-item[rel=vCenterDataCenter]').hide();
                                                    $form.find('.form-item[rel=vCenterDataStore]').hide();

                                                    $form.find('.form-item[rel=rbdmonitor]').hide();
                                                    $form.find('.form-item[rel=rbdpool]').hide();
                                                    $form.find('.form-item[rel=rbdid]').hide();
                                                    $form.find('.form-item[rel=rbdsecret]').hide();

                                                    $form.find('.form-item[rel=glustervolume]').hide();
                                                } else if (protocol == "vmfs") {
                                                    $form.find('.form-item[rel=server]').css('display', 'inline-block');
                                                    $form.find('.form-item[rel=server]').find(".value").find("input").val("");

                                                    $form.find('.form-item[rel=path]').hide();

                                                    $form.find('.form-item[rel=smbUsername]').hide();
                                                    $form.find('.form-item[rel=smbPassword]').hide();
                                                    $form.find('.form-item[rel=smbDomain]').hide();

                                                    $form.find('.form-item[rel=iqn]').hide();
                                                    $form.find('.form-item[rel=lun]').hide();

                                                    $form.find('.form-item[rel=volumegroup]').hide();

                                                    $form.find('.form-item[rel=vCenterDataCenter]').css('display', 'inline-block');
                                                    $form.find('.form-item[rel=vCenterDataStore]').css('display', 'inline-block');

                                                    $form.find('.form-item[rel=rbdmonitor]').hide();
                                                    $form.find('.form-item[rel=rbdpool]').hide();
                                                    $form.find('.form-item[rel=rbdid]').hide();
                                                    $form.find('.form-item[rel=rbdsecret]').hide();

                                                    $form.find('.form-item[rel=glustervolume]').hide();
                                                } else if (protocol == "SharedMountPoint") {
                                                    //"SharedMountPoint" show the same fields as "nfs" does.
                                                    $form.find('.form-item[rel=server]').hide();
                                                    $form.find('.form-item[rel=server]').find(".value").find("input").val("localhost");

                                                    $form.find('.form-item[rel=path]').css('display', 'inline-block');
                                                    var $required = $form.find('.form-item[rel=path]').find(".name").find("label span");
                                                    $form.find('.form-item[rel=path]').find(".name").find("label").text("Path:").prepend($required);

                                                    $form.find('.form-item[rel=smbUsername]').hide();
                                                    $form.find('.form-item[rel=smbPassword]').hide();
                                                    $form.find('.form-item[rel=smbDomain]').hide();

                                                    $form.find('.form-item[rel=iqn]').hide();
                                                    $form.find('.form-item[rel=lun]').hide();

                                                    $form.find('.form-item[rel=volumegroup]').hide();

                                                    $form.find('.form-item[rel=vCenterDataCenter]').hide();
                                                    $form.find('.form-item[rel=vCenterDataStore]').hide();

                                                    $form.find('.form-item[rel=rbdmonitor]').hide();
                                                    $form.find('.form-item[rel=rbdpool]').hide();
                                                    $form.find('.form-item[rel=rbdid]').hide();
                                                    $form.find('.form-item[rel=rbdsecret]').hide();

                                                    $form.find('.form-item[rel=glustervolume]').hide();
                                                } else if (protocol == "rbd") {
                                                    $form.find('.form-item[rel=rbdmonitor]').css('display', 'inline-block');
                                                    $form.find('.form-item[rel=rbdmonitor]').find(".name").find("label").text("RADOS Monitor:");

                                                    $form.find('.form-item[rel=rbdpool]').css('display', 'inline-block');
                                                    $form.find('.form-item[rel=rbdpool]').find(".name").find("label").text("RADOS Pool:");

                                                    $form.find('.form-item[rel=rbdid]').css('display', 'inline-block');
                                                    $form.find('.form-item[rel=rbdid]').find(".name").find("label").text("RADOS User:");

                                                    $form.find('.form-item[rel=rbdsecret]').css('display', 'inline-block');
                                                    $form.find('.form-item[rel=rbdsecret]').find(".name").find("label").text("RADOS Secret:");

                                                    $form.find('.form-item[rel=server]').hide();
                                                    $form.find('.form-item[rel=iqn]').hide();
                                                    $form.find('.form-item[rel=lun]').hide();
                                                    $form.find('.form-item[rel=volumegroup]').hide();
                                                    $form.find('.form-item[rel=path]').hide();
                                                    $form.find('.form-item[rel=vCenterDataCenter]').hide();
                                                    $form.find('.form-item[rel=vCenterDataStore]').hide();

                                                    $form.find('.form-item[rel=smbUsername]').hide();
                                                    $form.find('.form-item[rel=smbPassword]').hide();
                                                    $form.find('.form-item[rel=smbDomain]').hide();

                                                    $form.find('.form-item[rel=glustervolume]').hide();
                                                } else if (protocol == "gluster") {
                                                    $form.find('.form-item[rel=server]').css('display', 'inline-block');
                                                    $form.find('.form-item[rel=server]').find(".value").find("input");

                                                    $form.find('.form-item[rel=glustervolume]').css('display', 'inline-block');
                                                    $form.find('.form-item[rel=glustervolume]').find(".name").find("label").text("Volume:");

                                                    $form.find('.form-item[rel=path]').hide();

                                                    $form.find('.form-item[rel=smbUsername]').hide();
                                                    $form.find('.form-item[rel=smbPassword]').hide();
                                                    $form.find('.form-item[rel=smbDomain]').hide();

                                                    $form.find('.form-item[rel=iqn]').hide();
                                                    $form.find('.form-item[rel=lun]').hide();

                                                    $form.find('.form-item[rel=volumegroup]').hide();

                                                    $form.find('.form-item[rel=vCenterDataCenter]').hide();
                                                    $form.find('.form-item[rel=vCenterDataStore]').hide();

                                                    $form.find('.form-item[rel=rbdmonitor]').hide();
                                                    $form.find('.form-item[rel=rbdpool]').hide();
                                                    $form.find('.form-item[rel=rbdid]').hide();
                                                    $form.find('.form-item[rel=rbdsecret]').hide();
                                                } else {
                                                    $form.find('.form-item[rel=server]').css('display', 'inline-block');
                                                    $form.find('.form-item[rel=server]').find(".value").find("input").val("");

                                                    $form.find('.form-item[rel=iqn]').hide();
                                                    $form.find('.form-item[rel=lun]').hide();

                                                    $form.find('.form-item[rel=volumegroup]').hide();

                                                    $form.find('.form-item[rel=vCenterDataCenter]').hide();
                                                    $form.find('.form-item[rel=vCenterDataStore]').hide();

                                                    $form.find('.form-item[rel=rbdmonitor]').hide();
                                                    $form.find('.form-item[rel=rbdpool]').hide();
                                                    $form.find('.form-item[rel=rbdid]').hide();
                                                    $form.find('.form-item[rel=rbdsecret]').hide();

                                                    $form.find('.form-item[rel=smbUsername]').hide();
                                                    $form.find('.form-item[rel=smbPassword]').hide();
                                                    $form.find('.form-item[rel=smbDomain]').hide();

                                                    $form.find('.form-item[rel=glustervolume]').hide();
                                                }
                                            });

                                            args.$select.trigger("change");
                                        }
                                    },
                                    //always appear (end)

                                    server: {
                                        label: 'label.server',
                                        docID: 'helpPrimaryStorageServer',
                                        validation: {
                                            required: true
                                        },
                                        isHidden: true
                                    },

                                    //nfs
                                    path: {
                                        label: 'label.path',
                                        docID: 'helpPrimaryStoragePath',
                                        validation: {
                                            required: true
                                        },
                                        isHidden: true
                                    },
                                    provider: {
                                        label: 'label.provider',
                                        validation: {
                                            required: true
                                        },
                                        select: function (args) {
                                            var data = args.context.providers ?
                                            {id: args.context.providers[0].id} :
                                            {};

                                            $.ajax({
                                                url: createURL('listStorageProviders'),
                                                data: {
                                                    type: 'primary'
                                                },
                                                success: function (json) {
                                                    var providers = json.liststorageprovidersresponse.dataStoreProvider ? json.liststorageprovidersresponse.dataStoreProvider : [];

                                                    args.response.success({
                                                        data: $.map(providers, function (provider) {
                                                            return {
                                                                id: provider.name,
                                                                description: provider.name
                                                            };
                                                        })
                                                    });
                                                }
                                            });
                                            args.$select.change(function () {
                                                    var $form = $(this).closest('form');
                                                    var scope = $(this).val();

                                                    if (scope == 'DefaultPrimary') {
                                                        $form.find('.form-item[rel=isManaged]').hide();
                                                        $form.find('.form-item[rel=capacityIops]').hide();
                                                        $form.find('.form-item[rel=capacityBytes]').hide();
                                                        $form.find('.form-item[rel=url]').hide();
                                                    } else {
                                                        $form.find('.form-item[rel=isManaged]').css('display', 'inline-block');
                                                        $form.find('.form-item[rel=capacityIops]').css('display', 'inline-block');
                                                        $form.find('.form-item[rel=capacityBytes]').css('display', 'inline-block');
                                                        $form.find('.form-item[rel=url]').css('display', 'inline-block');
                                                    }
                                                }
                                            )
                                        }
                                    },
                                    isManaged: {
                                        label: 'label.managed',
                                        docID: 'helpManaged',
                                        isBoolean: true,
                                        isChecked: false,
                                        validation: {
                                            required: false
                                        }
                                    },
                                    capacityBytes: {
                                        label: 'label.capacity.bytes',
                                        docID: 'helpCapacityBytes',
                                        validation: {
                                            required: false
                                        }
                                    },
                                    capacityIops: {
                                        label: 'label.capacity.iops',
                                        docID: 'helpCapacityIops',
                                        validation: {
                                            required: false
                                        }
                                    },
                                    url: {
                                        label: 'label.url',
                                        docID: 'helpUrl',
                                        validation: {
                                            required: false
                                        }
                                    },
                                    //SMB
                                    smbUsername: {
                                        label: 'label.smb.username',
                                        validation: {
                                            required: true
                                        },
                                        isHidden: true
                                    },
                                    smbPassword: {
                                        label: 'label.smb.password',
                                        isPassword: true,
                                        validation: {
                                            required: true
                                        },
                                        isHidden: true
                                    },
                                    smbDomain: {
                                        label: 'label.smb.domain',
                                        validation: {
                                            required: true
                                        },
                                        isHidden: true
                                    },

                                    //iscsi
                                    iqn: {
                                        label: 'label.target.iqn',
                                        docID: 'helpPrimaryStorageTargetIQN',
                                        validation: {
                                            required: true
                                        },
                                        isHidden: true
                                    },
                                    lun: {
                                        label: 'label.LUN.number',
                                        docID: 'helpPrimaryStorageLun',
                                        validation: {
                                            required: true
                                        },
                                        isHidden: true
                                    },

                                    //clvm
                                    volumegroup: {
                                        label: 'label.volgroup',
                                        validation: {
                                            required: true
                                        },
                                        isHidden: true
                                    },

                                    //vmfs
                                    vCenterDataCenter: {
                                        label: 'label.vcenter.datacenter',
                                        validation: {
                                            required: true
                                        },
                                        isHidden: true
                                    },
                                    vCenterDataStore: {
                                        label: 'label.vcenter.datastore',
                                        validation: {
                                            required: true
                                        },
                                        isHidden: true
                                    },

                                    // RBD
                                    rbdmonitor: {
                                        label: 'label.rbd.monitor',
                                        docID: 'helpPrimaryStorageRBDMonitor',
                                        validation: {
                                            required: true
                                        },
                                        isHidden: true
                                    },
                                    rbdpool: {
                                        label: 'label.rbd.pool',
                                        docID: 'helpPrimaryStorageRBDPool',
                                        validation: {
                                            required: true
                                        },
                                        isHidden: true
                                    },
                                    rbdid: {
                                        label: 'label.rbd.id',
                                        docID: 'helpPrimaryStorageRBDId',
                                        validation: {
                                            required: false
                                        },
                                        isHidden: true
                                    },
                                    rbdsecret: {
                                        label: 'label.rbd.secret',
                                        docID: 'helpPrimaryStorageRBDSecret',
                                        validation: {
                                            required: false
                                        },
                                        isHidden: true
                                    },

                                    //gluster
                                    glustervolume: {
                                        label: 'label.gluster.volume',
                                        validation: {
                                            required: true
                                        },
                                        isHidden: true
                                    },

                                    //always appear (begin)
                                    storageTags: {
                                        label: 'label.storage.tags',
                                        docID: 'helpPrimaryStorageTags',
                                        isTokenInput: true,
                                        validation: {
                                            required: false
                                        },
                                        dataProvider: function (args) {
                                            $.ajax({
                                                url: createURL("listStorageTags"),
                                                dataType: "json",
                                                success: function (json) {
                                                    var item = json.liststoragetagsresponse.storagetag;
                                                    var tags = [];

                                                    if (item != null) {
                                                        tags = $.map(item, function (tag) {
                                                            return {
                                                                id: tag.name,
                                                                name: tag.name
                                                            };
                                                        });
                                                    }

                                                    args.response.success({
                                                        data: tags,
                                                        hintText: _l('hint.type.part.storage.tag'),
                                                        noResultsText: _l('hint.no.storage.tags')
                                                    });
                                                },
                                                error: function (XMLHttpResponse) {
                                                    var errorMsg = parseXMLHttpResponse(XMLHttpResponse);

                                                    args.response.error(errorMsg);
                                                }
                                            });
                                        }
                                    }
                                    //always appear (end)
                                }
                            },

                            /******************************/
                            action: function (args) {
                                var array1 = [];
                                array1.push("&scope=" + todb(args.data.scope));

                                array1.push("&zoneid=" + args.data.zoneid);

                                if (args.data.scope == 'zone') {

                                    array1.push("&hypervisor=" + args.data.hypervisor);
                                }

                                if (args.data.scope == 'cluster') {

                                    array1.push("&podid=" + args.data.podId);
                                    array1.push("&clusterid=" + args.data.clusterId);
                                }

                                if (args.data.scope == 'host') {
                                    array1.push("&podid=" + args.data.podId);
                                    array1.push("&clusterid=" + args.data.clusterId);
                                    array1.push("&hostid=" + args.data.hostId);
                                }

                                array1.push("&name=" + todb(args.data.name));

                                array1.push("&provider=" + todb(args.data.provider));

                                if (args.data.provider == "DefaultPrimary") {
                                    var server = args.data.server;
                                    var url = null;
                                    if (args.data.protocol == "nfs") {
                                        var path = args.data.path;
                                        if (path.substring(0, 1) != "/")
                                            path = "/" + path;
                                        url = nfsURL(server, path);
                                    } else if (args.data.protocol == "SMB") {
                                        var path = args.data.path;
                                        if (path.substring(0, 1) != "/")
                                            path = "/" + path;
                                        url = smbURL(server, path);
                                        array1.push("&details[0].user=" + args.data.smbUsername);
                                        array1.push("&details[1].password=" + todb(args.data.smbPassword));
                                        array1.push("&details[2].domain=" + args.data.smbDomain);
                                    } else if (args.data.protocol == "PreSetup") {
                                        var path = args.data.path;
                                        if (path.substring(0, 1) != "/")
                                            path = "/" + path;
                                        url = presetupURL(server, path);
                                    } else if (args.data.protocol == "ocfs2") {
                                        var path = args.data.path;
                                        if (path.substring(0, 1) != "/")
                                            path = "/" + path;
                                        url = ocfs2URL(server, path);
                                    } else if (args.data.protocol == "SharedMountPoint") {
                                        var path = args.data.path;
                                        if (path.substring(0, 1) != "/")
                                            path = "/" + path;
                                        url = SharedMountPointURL(server, path);
                                    } else if (args.data.protocol == "clvm") {
                                        var vg = args.data.volumegroup;
                                        if (vg.substring(0, 1) != "/")
                                            vg = "/" + vg;
                                        url = clvmURL(vg);
                                    } else if (args.data.protocol == "rbd") {
                                        var rbdmonitor = args.data.rbdmonitor;
                                        var rbdpool = args.data.rbdpool;
                                        var rbdid = args.data.rbdid;
                                        var rbdsecret = args.data.rbdsecret;
                                        url = rbdURL(rbdmonitor, rbdpool, rbdid, rbdsecret);
                                    } else if (args.data.protocol == "vmfs") {
                                        var path = args.data.vCenterDataCenter;
                                        if (path.substring(0, 1) != "/")
                                            path = "/" + path;
                                        path += "/" + args.data.vCenterDataStore;
                                        url = vmfsURL("dummy", path);
                                    } else if (args.data.protocol == "gluster") {
                                        var glustervolume = args.data.glustervolume;

                                        if (glustervolume.substring(0, 1) != "/")
                                            glustervolume = "/" + glustervolume;
                                        url = glusterURL(server, glustervolume);
                                    } else if (args.data.protocol == "iscsi") {
                                        var iqn = args.data.iqn;
                                        if (iqn.substring(0, 1) != "/")
                                            iqn = "/" + iqn;
                                        var lun = args.data.lun;
                                        url = iscsiURL(server, iqn, lun);
                                    } else {
                                        url = "";
                                    }

                                    array1.push("&url=" + todb(url));
                                }
                                else {
                                    array1.push("&managed=" + (args.data.isManaged == "on").toString());

                                    if (args.data.capacityBytes != null && args.data.capacityBytes.length > 0) {
                                        array1.push("&capacityBytes=" + args.data.capacityBytes.split(",").join(""));
                                    }

                                    if (args.data.capacityIops != null && args.data.capacityIops.length > 0) {
                                        array1.push("&capacityIops=" + args.data.capacityIops.split(",").join(""));
                                    }

                                    if (args.data.url != null && args.data.url.length > 0) {
                                        array1.push("&url=" + todb(args.data.url));
                                    }
                                }

                                if (args.data.storageTags != null && args.data.storageTags.length > 0) {
                                    array1.push("&tags=" + todb(args.data.storageTags));
                                }

                                if ("custom" in args.response) {
                                    args.response.custom(array1);
                                    return;
                                }

                                $.ajax({
                                    url: createURL("createStoragePool" + array1.join("")),
                                    dataType: "json",
                                    success: function (json) {
                                        var item = json.createstoragepoolresponse.storagepool;
                                        args.response.success({
                                            data: item
                                        });
                                    },
                                    error: function (XMLHttpResponse) {
                                        var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
                                        args.response.error(errorMsg);
                                    }
                                });
                            },

                            notification: {
                                poll: function (args) {
                                    args.complete({
                                        actionFilter: primarystorageActionfilter
                                    });
                                }
                            },

                            messages: {
                                notification: function (args) {
                                    return 'label.add.primary.storage';
                                }
                            }
                        },
                        viewMetrics: {
                            label: 'label.metrics',
                            isHeader: true,
                            addRow: false,
                            action: {
                                custom: cloudStack.uiCustom.metricsView({resource: 'storagepool'})
                            },
                            messages: {
                                notification: function (args) {
                                    return 'label.metrics';
                                }
                            }
                        }
                    },

                    detailView: {
                        name: "Primary storage details",
                        viewAll: {
                            label: 'label.volumes',
                            path: 'storage.volumes'
                        },
                        isMaximized: true,
                        actions: {
                            edit: {
                                label: 'label.edit',
                                action: function (args) {
                                    var array1 = [];
                                    array1.push("&tags=" + todb(args.data.tags));

                                    if (args.data.disksizetotal != null && args.data.disksizetotal.length > 0) {
                                        var diskSizeTotal = args.data.disksizetotal.split(",").join("");

                                        array1.push("&capacitybytes=" + cloudStack.converters.toBytes(diskSizeTotal));
                                    }

                                    if (args.data.capacityiops != null && args.data.capacityiops.length > 0) {
                                        var capacityIops = args.data.capacityiops.split(",").join("");

                                        array1.push("&capacityiops=" + capacityIops);
                                    }

                                    $.ajax({
                                        url: createURL("updateStoragePool&id=" + args.context.primarystorages[0].id + array1.join("")),
                                        dataType: "json",
                                        success: function (json) {
                                            var item = json.updatestoragepoolresponse.storagepool;
                                            args.response.success({
                                                data: item
                                            });
                                        },
                                        error: function (XMLHttpResponse) {
                                            args.response.error(parseXMLHttpResponse(XMLHttpResponse));
                                        }
                                    });
                                }
                            },

                            enableMaintenanceMode: {
                                label: 'label.action.enable.maintenance.mode',
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("enableStorageMaintenance&id=" + args.context.primarystorages[0].id),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var jid = json.prepareprimarystorageformaintenanceresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getUpdatedItem: function (json) {
                                                        return json.queryasyncjobresultresponse.jobresult.storagepool;
                                                    },
                                                    getActionFilter: function () {
                                                        return primarystorageActionfilter;
                                                    }
                                                }
                                            });
                                        }
                                    });
                                },
                                messages: {
                                    confirm: function (args) {
                                        return 'message.action.primarystorage.enable.maintenance.mode';
                                    },
                                    notification: function (args) {
                                        return 'label.action.enable.maintenance.mode';
                                    }
                                },
                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            },

                            cancelMaintenanceMode: {
                                label: 'label.action.cancel.maintenance.mode',
                                messages: {
                                    confirm: function (args) {
                                        return 'message.action.cancel.maintenance.mode';
                                    },
                                    notification: function (args) {
                                        return 'label.action.cancel.maintenance.mode';
                                    }
                                },
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("cancelStorageMaintenance&id=" + args.context.primarystorages[0].id),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var jid = json.cancelprimarystoragemaintenanceresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getUpdatedItem: function (json) {
                                                        return json.queryasyncjobresultresponse.jobresult.storagepool;
                                                    },
                                                    getActionFilter: function () {
                                                        return primarystorageActionfilter;
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

                            'remove': {
                                label: 'label.action.delete.primary.storage',
                                messages: {
                                    notification: function (args) {
                                        return 'label.action.delete.primary.storage';
                                    }
                                },
                                createForm: {
                                    title: 'label.action.delete.primary.storage',
                                    fields: {
                                        isForced: {
                                            label: 'force.remove',
                                            isBoolean: true
                                        }
                                    }
                                },
                                action: function (args) {
                                    var array1 = [];
                                    array1.push("&forced=" + (args.data.isForced == "on"));
                                    $.ajax({
                                        url: createURL("deleteStoragePool&id=" + args.context.primarystorages[0].id + array1.join("")),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            args.response.success({
                                                data: {}
                                            });
                                        }
                                    });
                                },
                                notification: {
                                    poll: function (args) {
                                        args.complete();
                                    }
                                }
                            }
                        },

                        tabs: {
                            details: {
                                title: 'label.details',
                                fields: [{
                                    name: {
                                        label: 'label.name'
                                    }
                                },
                                    {
                                        id: {
                                            label: 'label.id'
                                        },
                                        state: {
                                            label: 'label.state'
                                        },
                                        tags: {
                                            label: 'label.storage.tags',
                                            isTokenInput: true,
                                            isEditable: true,
                                            dataProvider: function (args) {
                                                $.ajax({
                                                    url: createURL("listStorageTags"),
                                                    dataType: "json",
                                                    success: function (json) {
                                                        var item = json.liststoragetagsresponse.storagetag;
                                                        var tags = [];

                                                        if (item != null) {
                                                            tags = $.map(item, function (tag) {
                                                                return {
                                                                    id: tag.name,
                                                                    name: tag.name
                                                                };
                                                            });
                                                        }

                                                        args.response.success({
                                                            data: tags,
                                                            hintText: _l('hint.type.part.storage.tag'),
                                                            noResultsText: _l('hint.no.storage.tags')
                                                        });
                                                    },
                                                    error: function (XMLHttpResponse) {
                                                        var errorMsg = parseXMLHttpResponse(XMLHttpResponse);

                                                        args.response.error(errorMsg);
                                                    }
                                                });
                                            }
                                        },
                                        zonename: {
                                            label: 'label.zone'
                                        },
                                        podname: {
                                            label: 'label.pod'
                                        },
                                        clustername: {
                                            label: 'label.cluster'
                                        },
                                        type: {
                                            label: 'label.type'
                                        },
                                        ipaddress: {
                                            label: 'label.ip.address'
                                        },
                                        path: {
                                            label: 'label.path'
                                        },
                                        disksizetotal: {
                                            label: 'label.disk.total',
                                            isEditable: true,
                                            converter: function (args) {
                                                if (args == null || args == 0)
                                                    return ""; else
                                                    return cloudStack.converters.convertBytes(args);
                                            }
                                        },
                                        disksizeallocated: {
                                            label: 'label.disk.allocated',
                                            converter: function (args) {
                                                if (args == null || args == 0)
                                                    return ""; else
                                                    return cloudStack.converters.convertBytes(args);
                                            }
                                        },
                                        capacityiops: {
                                            label: 'label.disk.iops.total',
                                            isEditable: true,
                                            converter: function (args) {
                                                if (args == null || args == 0)
                                                    return ""; else
                                                    return args;
                                            }
                                        }
                                    }],

                                dataProvider: function (args) {
                                    $.ajax({
                                        url: createURL("listStoragePools&id=" + args.context.primarystorages[0].id),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var item = json.liststoragepoolsresponse.storagepool[0];
                                            args.response.success({
                                                actionFilter: primarystorageActionfilter,
                                                data: item
                                            });
                                        }
                                    });
                                }
                            },

                            // Granular settings for storage pool
                            settings: {
                                title: 'label.settings',
                                custom: cloudStack.uiCustom.granularSettings({
                                    dataProvider: function (args) {

                                        $.ajax({
                                            url: createURL('listConfigurations&storageid=' + args.context.primarystorages[0].id),
                                            data: listViewDataProvider(args, {},
                                                {
                                                    searchBy: 'name'
                                                }),
                                            success: function (json) {
                                                args.response.success({
                                                    data: json.listconfigurationsresponse.configuration
                                                });
                                            },

                                            error: function (json) {
                                                args.response.error(parseXMLHttpResponse(json));
                                            }
                                        });
                                    },
                                    actions: {
                                        edit: function (args) {
                                            // call updateStorageLevelParameters
                                            var data = {
                                                name: args.data.jsonObj.name,
                                                value: args.data.value
                                            };

                                            $.ajax({
                                                url: createURL('updateConfiguration&storageid=' + args.context.primarystorages[0].id),
                                                data: data,
                                                success: function (json) {
                                                    var item = json.updateconfigurationresponse.configuration;
                                                    args.response.success({
                                                        data: item
                                                    });
                                                },

                                                error: function (json) {
                                                    args.response.error(parseXMLHttpResponse(json));
                                                }
                                            });
                                        }
                                    }
                                })
                            }
                        }
                    }
                }
            },

            'secondary-storage': {
                title: 'label.secondary.storage',
                id: 'secondarystorages',
                sectionSelect: {
                    label: 'label.select-view'
                },
                sections: {
                    secondaryStorage: {
                        type: 'select',
                        title: 'label.secondary.storage',
                        listView: {
                            id: 'secondarystorages',
                            section: 'seconary-storage',
                            fields: {
                                name: {
                                    label: 'label.name'
                                },
                                protocol: {
                                    label: 'label.protocol'
                                }
                            },


                            dataProvider: function (args) {
                                var array1 = [];
                                if (args.filterBy != null) {
                                    if (args.filterBy.search != null && args.filterBy.search.by != null && args.filterBy.search.value != null) {
                                        switch (args.filterBy.search.by) {
                                            case "name":
                                                if (args.filterBy.search.value.length > 0)
                                                    array1.push("&keyword=" + args.filterBy.search.value);
                                                break;
                                        }
                                    }
                                }
                                array1.push("&zoneid=" + args.context.zones[0].id);

                                $.ajax({
                                    url: createURL("listImageStores&page=" + args.page + "&pagesize=" + pageSize + array1.join("")),
                                    dataType: "json",
                                    async: true,
                                    success: function (json) {
                                        var items = json.listimagestoresresponse.imagestore;
                                        args.response.success({
                                            actionFilter: secondarystorageActionfilter,
                                            data: items
                                        });
                                    }
                                });
                            },

                            actions: {
                                add: {
                                    label: 'label.add.secondary.storage',

                                    createForm: {
                                        title: 'label.add.secondary.storage',

                                        fields: {
                                            name: {
                                                label: 'label.name'
                                            },
                                            provider: {
                                                label: 'label.provider',
                                                select: function (args) {
                                                    var items = [{
                                                        id: 'NFS',
                                                        description: 'NFS'
                                                    },
                                                        {
                                                            id: 'SMB',
                                                            description: 'SMB/CIFS'
                                                        },
                                                        {
                                                            id: 'S3',
                                                            description: 'S3'
                                                        },
                                                        {
                                                            id: 'Swift',
                                                            description: 'Swift'
                                                        }];

                                                    args.response.success({
                                                        data: items
                                                    });

                                                    args.$select.change(function () {
                                                        var $form = $(this).closest('form');
                                                        if ($(this).val() == "NFS") {
                                                            //NFS, SMB
                                                            $form.find('.form-item[rel=zoneid]').css('display', 'inline-block');
                                                            $form.find('.form-item[rel=nfsServer]').css('display', 'inline-block');
                                                            $form.find('.form-item[rel=path]').css('display', 'inline-block');

                                                            //SMB
                                                            $form.find('.form-item[rel=smbUsername]').hide();
                                                            $form.find('.form-item[rel=smbPassword]').hide();
                                                            $form.find('.form-item[rel=smbDomain]').hide();

                                                            //S3
                                                            $form.find('.form-item[rel=accesskey]').hide();
                                                            $form.find('.form-item[rel=secretkey]').hide();
                                                            $form.find('.form-item[rel=bucket]').hide();
                                                            $form.find('.form-item[rel=endpoint]').hide();
                                                            $form.find('.form-item[rel=usehttps]').hide();
                                                            $form.find('.form-item[rel=connectiontimeout]').hide();
                                                            $form.find('.form-item[rel=maxerrorretry]').hide();
                                                            $form.find('.form-item[rel=sockettimeout]').hide();

                                                            $form.find('.form-item[rel=createNfsCache]').find('input').removeAttr('checked');
                                                            $form.find('.form-item[rel=createNfsCache]').hide();
                                                            $form.find('.form-item[rel=nfsCacheZoneid]').hide();
                                                            $form.find('.form-item[rel=nfsCacheNfsServer]').hide();
                                                            $form.find('.form-item[rel=nfsCachePath]').hide();

                                                            //Swift
                                                            $form.find('.form-item[rel=url]').hide();
                                                            $form.find('.form-item[rel=account]').hide();
                                                            $form.find('.form-item[rel=username]').hide();
                                                            $form.find('.form-item[rel=key]').hide();
                                                        } else if ($(this).val() == "SMB") {
                                                            //NFS, SMB
                                                            $form.find('.form-item[rel=zoneid]').css('display', 'inline-block');
                                                            $form.find('.form-item[rel=nfsServer]').css('display', 'inline-block');
                                                            $form.find('.form-item[rel=path]').css('display', 'inline-block');

                                                            //SMB
                                                            $form.find('.form-item[rel=smbUsername]').css('display', 'inline-block');
                                                            $form.find('.form-item[rel=smbPassword]').css('display', 'inline-block');
                                                            $form.find('.form-item[rel=smbDomain]').css('display', 'inline-block');

                                                            //S3
                                                            $form.find('.form-item[rel=accesskey]').hide();
                                                            $form.find('.form-item[rel=secretkey]').hide();
                                                            $form.find('.form-item[rel=bucket]').hide();
                                                            $form.find('.form-item[rel=endpoint]').hide();
                                                            $form.find('.form-item[rel=usehttps]').hide();
                                                            $form.find('.form-item[rel=connectiontimeout]').hide();
                                                            $form.find('.form-item[rel=maxerrorretry]').hide();
                                                            $form.find('.form-item[rel=sockettimeout]').hide();

                                                            $form.find('.form-item[rel=createNfsCache]').find('input').removeAttr('checked');
                                                            $form.find('.form-item[rel=createNfsCache]').hide();
                                                            $form.find('.form-item[rel=nfsCacheZoneid]').hide();
                                                            $form.find('.form-item[rel=nfsCacheNfsServer]').hide();
                                                            $form.find('.form-item[rel=nfsCachePath]').hide();

                                                            //Swift
                                                            $form.find('.form-item[rel=url]').hide();
                                                            $form.find('.form-item[rel=account]').hide();
                                                            $form.find('.form-item[rel=username]').hide();
                                                            $form.find('.form-item[rel=key]').hide();
                                                        } else if ($(this).val() == "S3") {
                                                            //NFS, SMB
                                                            $form.find('.form-item[rel=zoneid]').hide();
                                                            $form.find('.form-item[rel=nfsServer]').hide();
                                                            $form.find('.form-item[rel=path]').hide();

                                                            //SMB
                                                            $form.find('.form-item[rel=smbUsername]').hide();
                                                            $form.find('.form-item[rel=smbPassword]').hide();
                                                            $form.find('.form-item[rel=smbDomain]').hide();

                                                            //S3
                                                            $form.find('.form-item[rel=accesskey]').css('display', 'inline-block');
                                                            $form.find('.form-item[rel=secretkey]').css('display', 'inline-block');
                                                            $form.find('.form-item[rel=bucket]').css('display', 'inline-block');
                                                            $form.find('.form-item[rel=endpoint]').css('display', 'inline-block');
                                                            $form.find('.form-item[rel=usehttps]').css('display', 'inline-block');
                                                            $form.find('.form-item[rel=connectiontimeout]').css('display', 'inline-block');
                                                            $form.find('.form-item[rel=maxerrorretry]').css('display', 'inline-block');
                                                            $form.find('.form-item[rel=sockettimeout]').css('display', 'inline-block');

                                                            $form.find('.form-item[rel=createNfsCache]').find('input').attr('checked', 'checked');
                                                            //$form.find('.form-item[rel=createNfsCache]').find('input').attr('disabled', 'disabled');  //This checkbox should not be disabled any more because NFS staging (of a zone) might already exist (from "NFS secondary storage => Prepare Object Store Migration => NFS staging")
                                                            $form.find('.form-item[rel=createNfsCache]').css('display', 'inline-block');
                                                            $form.find('.form-item[rel=nfsCacheZoneid]').css('display', 'inline-block');
                                                            $form.find('.form-item[rel=nfsCacheNfsServer]').css('display', 'inline-block');
                                                            $form.find('.form-item[rel=nfsCachePath]').css('display', 'inline-block');


                                                            //Swift
                                                            $form.find('.form-item[rel=url]').hide();
                                                            $form.find('.form-item[rel=account]').hide();
                                                            $form.find('.form-item[rel=username]').hide();
                                                            $form.find('.form-item[rel=key]').hide();
                                                        } else if ($(this).val() == "Swift") {
                                                            //NFS, SMB
                                                            $form.find('.form-item[rel=zoneid]').hide();
                                                            $form.find('.form-item[rel=nfsServer]').hide();
                                                            $form.find('.form-item[rel=path]').hide();

                                                            //SMB
                                                            $form.find('.form-item[rel=smbUsername]').hide();
                                                            $form.find('.form-item[rel=smbPassword]').hide();
                                                            $form.find('.form-item[rel=smbDomain]').hide();

                                                            //S3
                                                            $form.find('.form-item[rel=accesskey]').hide();
                                                            $form.find('.form-item[rel=secretkey]').hide();
                                                            $form.find('.form-item[rel=bucket]').hide();
                                                            $form.find('.form-item[rel=endpoint]').hide();
                                                            $form.find('.form-item[rel=usehttps]').hide();
                                                            $form.find('.form-item[rel=connectiontimeout]').hide();
                                                            $form.find('.form-item[rel=maxerrorretry]').hide();
                                                            $form.find('.form-item[rel=sockettimeout]').hide();

                                                            $form.find('.form-item[rel=createNfsCache]').find('input').removeAttr('checked');
                                                            $form.find('.form-item[rel=createNfsCache]').hide();
                                                            $form.find('.form-item[rel=nfsCacheZoneid]').hide();
                                                            $form.find('.form-item[rel=nfsCacheNfsServer]').hide();
                                                            $form.find('.form-item[rel=nfsCachePath]').hide();

                                                            //Swift
                                                            $form.find('.form-item[rel=url]').css('display', 'inline-block');
                                                            $form.find('.form-item[rel=account]').css('display', 'inline-block');
                                                            $form.find('.form-item[rel=username]').css('display', 'inline-block');
                                                            $form.find('.form-item[rel=key]').css('display', 'inline-block');
                                                        }
                                                    });

                                                    args.$select.change();
                                                }
                                            },


                                            //NFS, SMB (begin)
                                            zoneid: {
                                                label: 'label.zone',
                                                docID: 'helpSecondaryStorageZone',
                                                validation: {
                                                    required: true
                                                },
                                                select: function (args) {
                                                    $.ajax({
                                                        url: createURL('listZones'),
                                                        data: {},
                                                        success: function (json) {
                                                            var zones = json.listzonesresponse.zone ? json.listzonesresponse.zone : [];

                                                            if (zones != null) {
                                                                //$.map(items, fn) - items can not be null
                                                                args.response.success({
                                                                    data: $.map(zones, function (zone) {
                                                                        return {
                                                                            id: zone.id,
                                                                            description: zone.name
                                                                        };
                                                                    })
                                                                });
                                                            } else {
                                                                args.response.success({
                                                                    data: null
                                                                });
                                                            }
                                                        }
                                                    });
                                                }
                                            },
                                            nfsServer: {
                                                label: 'label.server', //change label from "NFS Server" to "Server" since this field is also shown when provider "SMB/CIFS" is elected.
                                                docID: 'helpSecondaryStorageNFSServer',
                                                validation: {
                                                    required: true
                                                }
                                            },
                                            path: {
                                                label: 'label.path',
                                                docID: 'helpSecondaryStoragePath',
                                                validation: {
                                                    required: true
                                                }
                                            },
                                            //NFS, SMB (end)


                                            //SMB (begin)
                                            smbUsername: {
                                                label: 'label.smb.username',
                                                validation: {
                                                    required: true
                                                }
                                            },
                                            smbPassword: {
                                                label: 'label.smb.password',
                                                isPassword: true,
                                                validation: {
                                                    required: true
                                                }
                                            },
                                            smbDomain: {
                                                label: 'label.smb.domain',
                                                validation: {
                                                    required: true
                                                }
                                            },
                                            //SMB (end)

                                            //S3 (begin)
                                            accesskey: {
                                                label: 'label.s3.access_key',
                                                docID: 'helpS3AccessKey',
                                                validation: {
                                                    required: true
                                                }
                                            },
                                            secretkey: {
                                                label: 'label.s3.secret_key',
                                                docID: 'helpS3SecretKey',
                                                validation: {
                                                    required: true
                                                }
                                            },
                                            bucket: {
                                                label: 'label.s3.bucket',
                                                docID: 'helpS3Bucket',
                                                validation: {
                                                    required: true
                                                }
                                            },
                                            endpoint: {
                                                label: 'label.s3.endpoint',
                                                docID: 'helpS3Endpoint'
                                            },
                                            usehttps: {
                                                label: 'label.s3.use_https',
                                                isEditable: true,
                                                isBoolean: true,
                                                isChecked: true,
                                                converter: cloudStack.converters.toBooleanText
                                            },
                                            connectiontimeout: {
                                                label: 'label.s3.connection_timeout',
                                                docID: 'helpS3ConnectionTimeout'
                                            },
                                            maxerrorretry: {
                                                label: 'label.s3.max_error_retry',
                                                docID: 'helpS3MaxErrorRetry'
                                            },
                                            sockettimeout: {
                                                label: 'label.s3.socket_timeout',
                                                docID: 'helpS3SocketTimeout'
                                            },

                                            createNfsCache: {
                                                label: 'label.create.nfs.secondary.staging.store',
                                                isBoolean: true,
                                                isChecked: true
                                            },
                                            nfsCacheZoneid: {
                                                dependsOn: 'createNfsCache',
                                                label: 'label.zone',
                                                validation: {
                                                    required: true
                                                },
                                                select: function (args) {
                                                    $.ajax({
                                                        url: createURL('listZones'),
                                                        data: {},
                                                        success: function (json) {
                                                            var zones = json.listzonesresponse.zone;

                                                            if (zones != null) {
                                                                //$.map(items, fn) - items can not be null
                                                                args.response.success({
                                                                    data: $.map(zones, function (zone) {
                                                                        return {
                                                                            id: zone.id,
                                                                            description: zone.name
                                                                        };
                                                                    })
                                                                });
                                                            } else {
                                                                args.response.success({
                                                                    data: null
                                                                });
                                                            }
                                                        }
                                                    });
                                                }
                                            },
                                            nfsCacheNfsServer: {
                                                dependsOn: 'createNfsCache',
                                                label: 'label.nfs.server',
                                                docID: 'helpNFSStagingServer',
                                                validation: {
                                                    required: true
                                                }
                                            },
                                            nfsCachePath: {
                                                dependsOn: 'createNfsCache',
                                                label: 'label.path',
                                                docID: 'helpNFSStagingPath',
                                                validation: {
                                                    required: true
                                                }
                                            },
                                            //S3 (end)


                                            //Swift (begin)
                                            url: {
                                                label: 'label.url',
                                                validation: {
                                                    required: true
                                                }
                                            },
                                            account: {
                                                label: 'label.account'
                                            },
                                            username: {
                                                label: 'label.username'
                                            },
                                            key: {
                                                label: 'label.key'
                                            }
                                            //Swift (end)
                                        }
                                    },

                                    action: function (args) {
                                        var data = {};
                                        if (args.data.name != null && args.data.name.length > 0) {
                                            $.extend(data, {
                                                name: args.data.name
                                            });
                                        }

                                        if (args.data.provider == 'NFS') {
                                            var zoneid = args.data.zoneid;
                                            var nfs_server = args.data.nfsServer;
                                            var path = args.data.path;
                                            var url = nfsURL(nfs_server, path);

                                            $.extend(data, {
                                                provider: args.data.provider,
                                                zoneid: zoneid,
                                                url: url
                                            });

                                            $.ajax({
                                                url: createURL('addImageStore'),
                                                data: data,
                                                success: function (json) {
                                                    var item = json.addimagestoreresponse.imagestore;
                                                    args.response.success({
                                                        data: item
                                                    });
                                                },
                                                error: function (XMLHttpResponse) {
                                                    var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
                                                    args.response.error(errorMsg);
                                                }
                                            });
                                        } else if (args.data.provider == 'SMB') {
                                            var zoneid = args.data.zoneid;
                                            var nfs_server = args.data.nfsServer;
                                            var path = args.data.path;
                                            var url = smbURL(nfs_server, path);
                                            $.extend(data, {
                                                provider: args.data.provider,
                                                zoneid: zoneid,
                                                url: url,
                                                'details[0].key': 'user',
                                                'details[0].value': args.data.smbUsername,
                                                'details[1].key': 'password',
                                                'details[1].value': args.data.smbPassword,
                                                'details[2].key': 'domain',
                                                'details[2].value': args.data.smbDomain
                                            });

                                            $.ajax({
                                                url: createURL('addImageStore'),
                                                data: data,
                                                success: function (json) {
                                                    var item = json.addimagestoreresponse.imagestore;
                                                    args.response.success({
                                                        data: item
                                                    });
                                                },
                                                error: function (XMLHttpResponse) {
                                                    var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
                                                    args.response.error(errorMsg);
                                                }
                                            });
                                        } else if (args.data.provider == 'S3') {
                                            $.extend(data, {
                                                provider: args.data.provider,
                                                'details[0].key': 'accesskey',
                                                'details[0].value': args.data.accesskey,
                                                'details[1].key': 'secretkey',
                                                'details[1].value': args.data.secretkey,
                                                'details[2].key': 'bucket',
                                                'details[2].value': args.data.bucket,
                                                'details[3].key': 'usehttps',
                                                'details[3].value': (args.data.usehttps != null && args.data.usehttps == 'on' ? 'true' : 'false')
                                            });

                                            var index = 4;
                                            if (args.data.endpoint != null && args.data.endpoint.length > 0) {
                                                data['details[' + index.toString() + '].key'] = 'endpoint';
                                                data['details[' + index.toString() + '].value'] = args.data.endpoint;
                                                index++;
                                            }
                                            if (args.data.connectiontimeout != null && args.data.connectiontimeout.length > 0) {
                                                data['details[' + index.toString() + '].key'] = 'connectiontimeout';
                                                data['details[' + index.toString() + '].value'] = args.data.connectiontimeout;
                                                index++;
                                            }
                                            if (args.data.maxerrorretry != null && args.data.maxerrorretry.length > 0) {
                                                data['details[' + index.toString() + '].key'] = 'maxerrorretry';
                                                data['details[' + index.toString() + '].value'] = args.data.maxerrorretry;
                                                index++;
                                            }
                                            if (args.data.sockettimeout != null && args.data.sockettimeout.length > 0) {
                                                data['details[' + index.toString() + '].key'] = 'sockettimeout';
                                                data['details[' + index.toString() + '].value'] = args.data.sockettimeout;
                                                index++;
                                            }

                                            $.ajax({
                                                url: createURL('addImageStore'),
                                                data: data,
                                                success: function (json) {
                                                    g_regionsecondaryenabled = true;

                                                    var item = json.addimagestoreresponse.imagestore;
                                                    args.response.success({
                                                        data: item
                                                    });
                                                },
                                                error: function (json) {
                                                    args.response.error(parseXMLHttpResponse(json));
                                                }
                                            });

                                            if (args.data.createNfsCache == 'on') {
                                                var zoneid = args.data.nfsCacheZoneid;
                                                var nfs_server = args.data.nfsCacheNfsServer;
                                                var path = args.data.nfsCachePath;
                                                var url = nfsURL(nfs_server, path);

                                                var nfsCacheData = {
                                                    provider: 'NFS',
                                                    zoneid: zoneid,
                                                    url: url
                                                };

                                                $.ajax({
                                                    url: createURL('createSecondaryStagingStore'),
                                                    data: nfsCacheData,
                                                    success: function (json) {
                                                        //do nothing
                                                    },
                                                    error: function (json) {
                                                        args.response.error(parseXMLHttpResponse(json));
                                                    }
                                                });
                                            }
                                        } else if (args.data.provider == 'Swift') {
                                            $.extend(data, {
                                                provider: args.data.provider,
                                                url: args.data.url
                                            });

                                            var index = 0;
                                            if (args.data.account != null && args.data.account.length > 0) {
                                                data['details[' + index.toString() + '].key'] = 'account';
                                                data['details[' + index.toString() + '].value'] = args.data.account;
                                                index++;
                                            }
                                            if (args.data.username != null && args.data.username.length > 0) {
                                                data['details[' + index.toString() + '].key'] = 'username';
                                                data['details[' + index.toString() + '].value'] = args.data.username;
                                                index++;
                                            }
                                            if (args.data.key != null && args.data.key.length > 0) {
                                                data['details[' + index.toString() + '].key'] = 'key';
                                                data['details[' + index.toString() + '].value'] = args.data.key;
                                                index++;
                                            }
                                            $.ajax({
                                                url: createURL('addImageStore'),
                                                data: data,
                                                success: function (json) {
                                                    g_regionsecondaryenabled = true;

                                                    var item = json.addimagestoreresponse.imagestore;
                                                    args.response.success({
                                                        data: item
                                                    });
                                                },
                                                error: function (json) {
                                                    args.response.error(parseXMLHttpResponse(json));
                                                }
                                            });
                                        }
                                    },

                                    notification: {
                                        poll: function (args) {
                                            args.complete({
                                                actionFilter: secondarystorageActionfilter
                                            });
                                        }
                                    },

                                    messages: {
                                        notification: function (args) {
                                            return 'label.add.secondary.storage';
                                        }
                                    }
                                }
                            },

                            detailView: {
                                name: 'label.secondary.storage.details',
                                isMaximized: true,
                                actions: {
                                    remove: {
                                        label: 'label.action.delete.secondary.storage',
                                        messages: {
                                            confirm: function (args) {
                                                return 'message.action.delete.secondary.storage';
                                            },
                                            notification: function (args) {
                                                return 'label.action.delete.secondary.storage';
                                            }
                                        },
                                        action: function (args) {
                                            $.ajax({
                                                url: createURL("deleteImageStore&id=" + args.context.secondaryStorage[0].id),
                                                dataType: "json",
                                                async: true,
                                                success: function (json) {
                                                    args.response.success();
                                                }
                                            });
                                        },
                                        notification: {
                                            poll: function (args) {
                                                args.complete({
                                                    data: {
                                                        resourcestate: 'Destroyed'
                                                    }
                                                });
                                            }
                                        }
                                    }
                                },
                                tabs: {
                                    details: {
                                        title: 'label.details',
                                        fields: [{
                                            name: {
                                                label: 'label.name'
                                            }
                                        },
                                            {
                                                url: {
                                                    label: 'label.url'
                                                },
                                                protocol: {
                                                    label: 'label.protocol'
                                                },
                                                providername: {
                                                    label: 'label.provider'
                                                },
                                                scope: {
                                                    label: 'label.scope'
                                                },
                                                zonename: {
                                                    label: 'label.zone'
                                                },
                                                details: {
                                                    label: 'label.details',
                                                    converter: function (array1) {
                                                        var string1 = '';
                                                        if (array1 != null) {
                                                            for (var i = 0; i < array1.length; i++) {
                                                                if (i > 0)
                                                                    string1 += ', ';

                                                                string1 += array1[i].name + ': ' + array1[i].value;
                                                            }
                                                        }
                                                        return string1;
                                                    }
                                                },
                                                id: {
                                                    label: 'label.id'
                                                }
                                            }],

                                        dataProvider: function (args) {
                                            $.ajax({
                                                url: createURL("listImageStores&id=" + args.context.secondaryStorage[0].id),
                                                dataType: "json",
                                                async: true,
                                                success: function (json) {
                                                    var item = json.listimagestoresresponse.imagestore[0];
                                                    processPropertiesInImagestoreObject(item);
                                                    args.response.success({
                                                        actionFilter: secondarystorageActionfilter,
                                                        data: item
                                                    });
                                                }
                                            });
                                        }
                                    }

                                    // Granular settings for storage pool for secondary storage is not required
                                    /*  settings: {
                                     title: 'label.menu.global.settings',
                                     custom: cloudStack.uiCustom.granularSettings({
                                     dataProvider: function(args) {
                                     args.response.success({
                                     data: [
                                     { name: 'config.param.1', value: 1 },
                                     { name: 'config.param.2', value: 2 }
                                     ]
                                     });
                                     },
                                     actions: {
                                     edit: function(args) {
                                     // call updateStorageLevelParameters
                                     args.response.success();
                                     }
                                     }
                                     })
                                     } */
                                }
                            }
                        }
                    },
                    cacheStorage: {
                        type: 'select',
                        title: 'label.secondary.staging.store',
                        listView: {
                            id: 'secondarystorages',
                            section: 'seconary-storage',
                            fields: {
                                name: {
                                    label: 'label.name'
                                },
                                url: {
                                    label: 'label.url'
                                },
                                providername: {
                                    label: 'label.provider'
                                }
                            },

                            /*
                             dataProvider: function(args) {  //being replaced with dataProvider in line 6898
                             var array1 = [];
                             if(args.filterBy != null) {
                             if(args.filterBy.search != null && args.filterBy.search.by != null && args.filterBy.search.value != null) {
                             switch(args.filterBy.search.by) {
                             case "name":
                             if(args.filterBy.search.value.length > 0)
                             array1.push("&keyword=" + args.filterBy.search.value);
                             break;
                             }
                             }
                             }
                             array1.push("&zoneid=" + args.context.zones[0].id);

                             $.ajax({
                             url: createURL("listImageStores&page=" + args.page + "&pagesize=" + pageSize + array1.join("")),
                             dataType: "json",
                             async: true,
                             success: function(json) {
                             var items = json.listimagestoreresponse.imagestore;
                             args.response.success({
                             actionFilter: secondarystorageActionfilter,
                             data:items
                             });
                             }
                             });
                             },
                             */

                            actions: {
                                add: {
                                    label: 'label.add.nfs.secondary.staging.store',
                                    createForm: {
                                        title: 'label.add.nfs.secondary.staging.store',
                                        fields: {
                                            zoneid: {
                                                label: 'label.zone',
                                                validation: {
                                                    required: true
                                                },
                                                select: function (args) {
                                                    $.ajax({
                                                        url: createURL('listZones'),
                                                        data: {},
                                                        success: function (json) {
                                                            var zones = json.listzonesresponse.zone ? json.listzonesresponse.zone : [];

                                                            if (zones != null) {
                                                                //$.map(items, fn) - items can not be null
                                                                args.response.success({
                                                                    data: $.map(zones, function (zone) {
                                                                        return {
                                                                            id: zone.id,
                                                                            description: zone.name
                                                                        };
                                                                    })
                                                                });
                                                            } else {
                                                                args.response.success({
                                                                    data: null
                                                                });
                                                            }
                                                        }
                                                    });
                                                }
                                            },
                                            nfsServer: {
                                                label: 'label.nfs.server',
                                                validation: {
                                                    required: true
                                                }
                                            },
                                            path: {
                                                label: 'label.path',
                                                validation: {
                                                    required: true
                                                }
                                            }
                                        }
                                    },
                                    action: function (args) {
                                        var data = {
                                            provider: 'NFS',
                                            zoneid: args.data.zoneid,
                                            url: nfsURL(args.data.nfsServer, args.data.path)
                                        };
                                        $.ajax({
                                            url: createURL('createSecondaryStagingStore'),
                                            data: data,
                                            success: function (json) {
                                                var item = json.createsecondarystagingstoreresponse.secondarystorage;
                                                args.response.success({
                                                    data: item
                                                });
                                            },
                                            error: function (json) {
                                                args.response.error(parseXMLHttpResponse(json));
                                            }
                                        });
                                    },
                                    notification: {
                                        poll: function (args) {
                                            args.complete();
                                        }
                                    },
                                    messages: {
                                        notification: function (args) {
                                            return 'label.add.nfs.secondary.staging.store';
                                        }
                                    }
                                }
                            },

                            detailView: {
                                name: 'label.secondary.staging.store.details',
                                isMaximized: true,
                                actions: {
                                    remove: {
                                        label: 'label.delete.secondary.staging.store',
                                        messages: {
                                            confirm: function (args) {
                                                return 'message.confirm.delete.secondary.staging.store';
                                            },
                                            notification: function (args) {
                                                return 'label.delete.secondary.staging.store';
                                            }
                                        },
                                        action: function (args) {
                                            var data = {
                                                id: args.context.cacheStorage[0].id
                                            };
                                            $.ajax({
                                                url: createURL('deleteSecondaryStagingStore'),
                                                data: data,
                                                async: true,
                                                success: function (json) {
                                                    args.response.success();
                                                },
                                                error: function (data) {
                                                    args.response.error(parseXMLHttpResponse(data));
                                                }
                                            });
                                        },
                                        notification: {
                                            poll: function (args) {
                                                args.complete();
                                            }
                                        }
                                    }
                                },
                                tabs: {
                                    details: {
                                        title: 'label.details',
                                        fields: [{
                                            name: {
                                                label: 'label.name'
                                            }
                                        },
                                            {
                                                url: {
                                                    label: 'label.url'
                                                },
                                                providername: {
                                                    label: 'label.provider'
                                                },
                                                scope: {
                                                    label: 'label.scope'
                                                },
                                                zonename: {
                                                    label: 'label.zone'
                                                },
                                                details: {
                                                    label: 'label.details',
                                                    converter: function (array1) {
                                                        var string1 = '';
                                                        if (array1 != null) {
                                                            for (var i = 0; i < array1.length; i++) {
                                                                if (i > 0)
                                                                    string1 += ', ';

                                                                string1 += array1[i].name + ': ' + array1[i].value;
                                                            }
                                                        }
                                                        return string1;
                                                    }
                                                },
                                                id: {
                                                    label: 'label.id'
                                                }
                                            }],

                                        dataProvider: function (args) {
                                            $.ajax({
                                                url: createURL('listSecondaryStagingStores'),
                                                data: {
                                                    id: args.context.cacheStorage[0].id
                                                },
                                                async: false,
                                                success: function (json) {
                                                    var item = json.listsecondarystagingstoreresponse.imagestore[0];
                                                    args.response.success({
                                                        data: item
                                                    });
                                                }
                                            });
                                        }
                                    }

                                    // Granular settings for storage pool for secondary storage is not required
                                    /*  settings: {
                                     title: 'label.menu.global.settings',
                                     custom: cloudStack.uiCustom.granularSettings({
                                     dataProvider: function(args) {
                                     args.response.success({
                                     data: [
                                     { name: 'config.param.1', value: 1 },
                                     { name: 'config.param.2', value: 2 }
                                     ]
                                     });
                                     },
                                     actions: {
                                     edit: function(args) {
                                     // call updateStorageLevelParameters
                                     args.response.success();
                                     }
                                     }
                                     })
                                     } */
                                }
                            }
                        }
                    }
                }
            },

            guestIpRanges: {
                //Advanced zone - Guest traffic type - Network tab - Network detailView - View IP Ranges
                title: 'label.guest.ip.range',
                id: 'guestIpRanges',
                listView: {
                    section: 'guest-IP-range',
                    fields: {
                        startip: {
                            label: 'label.ipv4.start.ip'
                        },
                        endip: {
                            label: 'label.ipv4.end.ip'
                        },
                        startipv6: {
                            label: 'label.ipv6.start.ip'
                        },
                        endipv6: {
                            label: 'label.ipv6.end.ip'
                        },
                        gateway: {
                            label: 'label.gateway'
                        },
                        netmask: {
                            label: 'label.netmask'
                        }
                    },

                    dataProvider: function (args) {
                        $.ajax({
                            url: createURL("listVlanIpRanges&zoneid=" + selectedZoneObj.id + "&networkid=" + args.context.networks[0].id + "&page=" + args.page + "&pagesize=" + pageSize),
                            dataType: "json",
                            async: true,
                            success: function (json) {
                                var items = json.listvlaniprangesresponse.vlaniprange;
                                args.response.success({
                                    data: items
                                });
                            }
                        });
                    },

                    actions: {
                        add: {
                            label: 'label.add.ip.range',
                            createForm: {
                                title: 'label.add.ip.range',
                                fields: {
                                    gateway: {
                                        label: 'label.gateway'
                                    },
                                    netmask: {
                                        label: 'label.netmask'
                                    },
                                    startipv4: {
                                        label: 'label.ipv4.start.ip'
                                    },
                                    endipv4: {
                                        label: 'label.ipv4.end.ip'
                                    },
                                    ip6cidr: {
                                        label: 'label.ipv6.CIDR'
                                    },
                                    ip6gateway: {
                                        label: 'label.ipv6.gateway'
                                    },
                                    startipv6: {
                                        label: 'label.ipv6.start.ip'
                                    },
                                    endipv6: {
                                        label: 'label.ipv6.end.ip'
                                    }
                                }
                            },
                            action: function (args) {
                                var array2 = [];

                                if (args.data.gateway != null && args.data.gateway.length > 0)
                                    array2.push("&gateway=" + args.data.gateway);
                                if (args.data.netmask != null && args.data.netmask.length > 0)
                                    array2.push("&netmask=" + args.data.netmask);

                                if (args.data.startipv4 != null && args.data.startipv4.length > 0)
                                    array2.push("&startip=" + args.data.startipv4);
                                if (args.data.endipv4 != null && args.data.endipv4.length > 0)
                                    array2.push("&endip=" + args.data.endipv4);

                                if (args.data.ip6cidr != null && args.data.ip6cidr.length > 0)
                                    array2.push("&ip6cidr=" + args.data.ip6cidr);
                                if (args.data.ip6gateway != null && args.data.ip6gateway.length > 0)
                                    array2.push("&ip6gateway=" + args.data.ip6gateway);

                                if (args.data.startipv6 != null && args.data.startipv6.length > 0)
                                    array2.push("&startipv6=" + args.data.startipv6);
                                if (args.data.endipv6 != null && args.data.endipv6.length > 0)
                                    array2.push("&endipv6=" + args.data.endipv6);

                                $.ajax({
                                    url: createURL("createVlanIpRange&forVirtualNetwork=false&networkid=" + args.context.networks[0].id + array2.join("")),
                                    dataType: "json",
                                    success: function (json) {
                                        var item = json.createvlaniprangeresponse.vlan;
                                        args.response.success({
                                            data: item
                                        });
                                    },
                                    error: function (XMLHttpResponse) {
                                        var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
                                        args.response.error(errorMsg);
                                    }
                                });
                            },
                            notification: {
                                poll: function (args) {
                                    args.complete();
                                }
                            },
                            messages: {
                                notification: function (args) {
                                    return 'label.add.ip.range';
                                }
                            }
                        },

                        'remove': {
                            label: 'label.remove.ip.range',
                            messages: {
                                confirm: function (args) {
                                    return 'message.confirm.remove.IP.range';
                                },
                                notification: function (args) {
                                    return 'label.remove.ip.range';
                                }
                            },
                            action: function (args) {
                                $.ajax({
                                    url: createURL("deleteVlanIpRange&id=" + args.data.id),
                                    dataType: "json",
                                    async: true,
                                    success: function (json) {
                                        args.response.success({
                                            data: {}
                                        });
                                    },
                                    error: function (json) {
                                        args.response.error(parseXMLHttpResponse(json));
                                    }
                                });
                            },
                            notification: {
                                poll: function (args) {
                                    args.complete();
                                }
                            }
                        }
                    }
                }
            }
        }
    };

    // Inject cloudStack infra page
    cloudStack.sections.system.show = cloudStack.uiCustom.physicalResources(cloudStack.sections.system.physicalResourceSection);

    function addExternalLoadBalancer(args, physicalNetworkObj, apiCmd, apiCmdRes, apiCmdObj) {
        var array1 = [];
        array1.push("&physicalnetworkid=" + physicalNetworkObj.id);
        array1.push("&username=" + todb(args.data.username));
        array1.push("&password=" + todb(args.data.password));
        array1.push("&networkdevicetype=" + todb(args.data.networkdevicetype));

        //construct URL starts here
        var url = [];

        var ip = args.data.ip;
        url.push("https://" + ip);

        var isQuestionMarkAdded = false;

        var publicInterface = args.data.publicinterface;
        if (publicInterface != null && publicInterface.length > 0) {
            if (isQuestionMarkAdded == false) {
                url.push("?");
                isQuestionMarkAdded = true;
            } else {
                url.push("&");
            }
            url.push("publicinterface=" + publicInterface);
        }

        var privateInterface = args.data.privateinterface;
        if (privateInterface != null && privateInterface.length > 0) {
            if (isQuestionMarkAdded == false) {
                url.push("?");
                isQuestionMarkAdded = true;
            } else {
                url.push("&");
            }
            url.push("privateinterface=" + privateInterface);
        }

        var numretries = args.data.numretries;
        if (numretries != null && numretries.length > 0) {
            if (isQuestionMarkAdded == false) {
                url.push("?");
                isQuestionMarkAdded = true;
            } else {
                url.push("&");
            }
            url.push("numretries=" + numretries);
        }

        var isInline = args.data.inline;
        if (isInline != null && isInline.length > 0) {
            if (isQuestionMarkAdded == false) {
                url.push("?");
                isQuestionMarkAdded = true;
            } else {
                url.push("&");
            }
            url.push("inline=" + isInline);
        }

        var capacity = args.data.capacity;
        if (capacity != null && capacity.length > 0) {
            if (isQuestionMarkAdded == false) {
                url.push("?");
                isQuestionMarkAdded = true;
            } else {
                url.push("&");
            }
            url.push("lbdevicecapacity=" + capacity);
        }

        var dedicated = (args.data.dedicated == "on");
        //boolean    (true/false)
        if (isQuestionMarkAdded == false) {
            url.push("?");
            isQuestionMarkAdded = true;
        } else {
            url.push("&");
        }
        url.push("lbdevicededicated=" + dedicated.toString());


        array1.push("&url=" + todb(url.join("")));
        //construct URL ends here

        $.ajax({
            url: createURL(apiCmd + array1.join("")),
            dataType: "json",
            type: "POST",
            success: function (json) {
                var jid = json[apiCmdRes].jobid;
                args.response.success({
                    _custom: {
                        jobId: jid,
                        getUpdatedItem: function (json) {
                            var item = json.queryasyncjobresultresponse.jobresult[apiCmdObj];

                            return item;
                        }
                    }
                });
            }
        });
    }

    function addExternalFirewall(args, physicalNetworkObj, apiCmd, apiCmdRes, apiCmdObj) {
        var array1 = [];
        array1.push("&physicalnetworkid=" + physicalNetworkObj.id);
        array1.push("&username=" + todb(args.data.username));
        array1.push("&password=" + todb(args.data.password));
        array1.push("&networkdevicetype=" + todb(args.data.networkdevicetype));

        //construct URL starts here
        var url = [];

        var ip = args.data.ip;
        url.push("https://" + ip);

        var isQuestionMarkAdded = false;

        var publicInterface = args.data.publicinterface;
        if (publicInterface != null && publicInterface.length > 0) {
            if (isQuestionMarkAdded == false) {
                url.push("?");
                isQuestionMarkAdded = true;
            } else {
                url.push("&");
            }
            url.push("publicinterface=" + publicInterface);
        }

        var privateInterface = args.data.privateinterface;
        if (privateInterface != null && privateInterface.length > 0) {
            if (isQuestionMarkAdded == false) {
                url.push("?");
                isQuestionMarkAdded = true;
            } else {
                url.push("&");
            }
            url.push("privateinterface=" + privateInterface);
        }

        var usageInterface = args.data.usageinterface;
        if (usageInterface != null && usageInterface.length > 0) {
            if (isQuestionMarkAdded == false) {
                url.push("?");
                isQuestionMarkAdded = true;
            } else {
                url.push("&");
            }
            url.push("usageinterface=" + usageInterface);
        }

        var numretries = args.data.numretries;
        if (numretries != null && numretries.length > 0) {
            if (isQuestionMarkAdded == false) {
                url.push("?");
                isQuestionMarkAdded = true;
            } else {
                url.push("&");
            }
            url.push("numretries=" + numretries);
        }

        var timeout = args.data.timeout;
        if (timeout != null && timeout.length > 0) {
            if (isQuestionMarkAdded == false) {
                url.push("?");
                isQuestionMarkAdded = true;
            } else {
                url.push("&");
            }
            url.push("timeout=" + timeout);
        }

        var isInline = args.data.inline;
        if (isInline != null && isInline.length > 0) {
            if (isQuestionMarkAdded == false) {
                url.push("?");
                isQuestionMarkAdded = true;
            } else {
                url.push("&");
            }
            url.push("inline=" + isInline);
        }

        var publicNetwork = args.data.publicnetwork;
        if (publicNetwork != null && publicNetwork.length > 0) {
            if (isQuestionMarkAdded == false) {
                url.push("?");
                isQuestionMarkAdded = true;
            } else {
                url.push("&");
            }
            url.push("publicnetwork=" + publicNetwork);
        }

        var privateNetwork = args.data.privatenetwork;
        if (privateNetwork != null && privateNetwork.length > 0) {
            if (isQuestionMarkAdded == false) {
                url.push("?");
                isQuestionMarkAdded = true;
            } else {
                url.push("&");
            }
            url.push("privatenetwork=" + privateNetwork);
        }

        var capacity = args.data.capacity;
        if (capacity != null && capacity.length > 0) {
            if (isQuestionMarkAdded == false) {
                url.push("?");
                isQuestionMarkAdded = true;
            } else {
                url.push("&");
            }
            url.push("fwdevicecapacity=" + capacity);
        }

        var dedicated = (args.data.dedicated == "on");
        //boolean    (true/false)
        if (isQuestionMarkAdded == false) {
            url.push("?");
            isQuestionMarkAdded = true;
        } else {
            url.push("&");
        }
        url.push("fwdevicededicated=" + dedicated.toString());

        // START - Palo Alto Specific Fields
        var externalVirtualRouter = args.data.pavr;
        if (externalVirtualRouter != null && externalVirtualRouter.length > 0) {
            if (isQuestionMarkAdded == false) {
                url.push("?");
                isQuestionMarkAdded = true;
            } else {
                url.push("&");
            }
            url.push("pavr=" + encodeURIComponent(externalVirtualRouter));
        }

        var externalThreatProfile = args.data.patp;
        if (externalThreatProfile != null && externalThreatProfile.length > 0) {
            if (isQuestionMarkAdded == false) {
                url.push("?");
                isQuestionMarkAdded = true;
            } else {
                url.push("&");
            }
            url.push("patp=" + encodeURIComponent(externalThreatProfile));
        }

        var externalLogProfile = args.data.palp;
        if (externalLogProfile != null && externalLogProfile.length > 0) {
            if (isQuestionMarkAdded == false) {
                url.push("?");
                isQuestionMarkAdded = true;
            } else {
                url.push("&");
            }
            url.push("palp=" + encodeURIComponent(externalLogProfile));
        }
        // END - Palo Alto Specific Fields

        array1.push("&url=" + todb(url.join("")));
        //construct URL ends here

        $.ajax({
            url: createURL(apiCmd + array1.join("")),
            dataType: "json",
            type: "POST",
            success: function (json) {
                var jid = json[apiCmdRes].jobid;
                args.response.success({
                    _custom: {
                        jobId: jid,
                        getUpdatedItem: function (json) {
                            var item = json.queryasyncjobresultresponse.jobresult[apiCmdObj];

                            return item;
                        }
                    }
                });
            }
        });
    }

    function addNiciraNvpDevice(args, physicalNetworkObj, apiCmd, apiCmdRes, apiCmdObj) {
        var array1 = [];
        array1.push("&physicalnetworkid=" + physicalNetworkObj.id);
        array1.push("&username=" + todb(args.data.username));
        array1.push("&password=" + todb(args.data.password));
        array1.push("&hostname=" + todb(args.data.host));
        array1.push("&transportzoneuuid=" + todb(args.data.transportzoneuuid));

        var l3GatewayServiceUuid = args.data.l3gatewayserviceuuid;
        if (l3GatewayServiceUuid != null && l3GatewayServiceUuid.length > 0) {
            array1.push("&l3gatewayserviceuuid=" + todb(args.data.l3gatewayserviceuuid));
        }

        var l2GatewayServiceUuid = args.data.l2gatewayserviceuuid;
        if (l2GatewayServiceUuid != null && l2GatewayServiceUuid.length > 0) {
            array1.push("&l2gatewayserviceuuid=" + todb(args.data.l2gatewayserviceuuid));
        }

        $.ajax({
            url: createURL(apiCmd + array1.join("")),
            dataType: "json",
            type: "POST",
            success: function (json) {
                var jid = json[apiCmdRes].jobid;
                args.response.success({
                    _custom: {
                        jobId: jid,
                        getUpdatedItem: function (json) {
                            var item = json.queryasyncjobresultresponse.jobresult[apiCmdObj];

                            return item;
                        }
                    }
                });
            }
        });
    }

    function addNuageVspDevice(args, physicalNetworkObj, apiCmd, apiCmdRes, apiCmdObj) {
        var array1 = [];
        array1.push("&physicalnetworkid=" + physicalNetworkObj.id);
        array1.push("&hostname=" + todb(args.data.hostname));
        array1.push("&username=" + todb(args.data.username));
        array1.push("&password=" + todb(args.data.password));
        array1.push("&port=" + todb(args.data.port));
        array1.push("&apiversion=" + todb(args.data.apiversion));
        array1.push("&retrycount=" + todb(args.data.retrycount));
        array1.push("&retryinterval=" + todb(args.data.retryinterval));

        $.ajax({
            url: createURL(apiCmd + array1.join("")),
            dataType: "json",
            type: "POST",
            success: function (json) {
                var jid = json[apiCmdRes].jobid;
                args.response.success({
                    _custom: {
                        jobId: jid,
                        getUpdatedItem: function (json) {
                            var item = json.queryasyncjobresultresponse.jobresult[apiCmdObj];

                            return item;
                        }
                    }
                });
            }
        });
    }

    var afterCreateZonePhysicalNetworkTrafficTypes = function (args, newZoneObj, newPhysicalnetwork) {
        $.ajax({
            url: createURL("updatePhysicalNetwork&state=Enabled&id=" + newPhysicalnetwork.id),
            dataType: "json",
            success: function (json) {
                var jobId = json.updatephysicalnetworkresponse.jobid;
                var enablePhysicalNetworkIntervalID = setInterval(function () {
                        $.ajax({
                            url: createURL("queryAsyncJobResult&jobId=" + jobId),
                            dataType: "json",
                            success: function (json) {
                                var result = json.queryasyncjobresultresponse;
                                if (result.jobstatus == 0) {
                                    return; //Job has not completed
                                } else {
                                    clearInterval(enablePhysicalNetworkIntervalID);
                                    if (result.jobstatus == 1) {
                                        //alert("updatePhysicalNetwork succeeded.");

                                        // get network service provider ID of Virtual Router
                                        var virtualRouterProviderId;
                                        $.ajax({
                                            url: createURL("listNetworkServiceProviders&name=VirtualRouter&physicalNetworkId=" + newPhysicalnetwork.id),
                                            dataType: "json",
                                            async: false,
                                            success: function (json) {
                                                var items = json.listnetworkserviceprovidersresponse.networkserviceprovider;
                                                if (items != null && items.length > 0) {
                                                    virtualRouterProviderId = items[0].id;
                                                }
                                            }
                                        });
                                        if (virtualRouterProviderId == null) {
                                            alert("error: listNetworkServiceProviders API doesn't return VirtualRouter provider ID");
                                            return;
                                        }

                                        var virtualRouterElementId;
                                        $.ajax({
                                            url: createURL("listVirtualRouterElements&nspid=" + virtualRouterProviderId),
                                            dataType: "json",
                                            async: false,
                                            success: function (json) {
                                                var items = json.listvirtualrouterelementsresponse.virtualrouterelement;
                                                if (items != null && items.length > 0) {
                                                    virtualRouterElementId = items[0].id;
                                                }
                                            }
                                        });
                                        if (virtualRouterElementId == null) {
                                            alert("error: listVirtualRouterElements API doesn't return Virtual Router Element Id");
                                            return;
                                        }

                                        $.ajax({
                                            url: createURL("configureVirtualRouterElement&enabled=true&id=" + virtualRouterElementId),
                                            dataType: "json",
                                            async: false,
                                            success: function (json) {
                                                var jobId = json.configurevirtualrouterelementresponse.jobid;
                                                var enableVirtualRouterElementIntervalID = setInterval(function () {
                                                        $.ajax({
                                                            url: createURL("queryAsyncJobResult&jobId=" + jobId),
                                                            dataType: "json",
                                                            success: function (json) {
                                                                var result = json.queryasyncjobresultresponse;
                                                                if (result.jobstatus == 0) {
                                                                    return; //Job has not completed
                                                                } else {
                                                                    clearInterval(enableVirtualRouterElementIntervalID);
                                                                    if (result.jobstatus == 1) {
                                                                        //alert("configureVirtualRouterElement succeeded.");

                                                                        $.ajax({
                                                                            url: createURL("updateNetworkServiceProvider&state=Enabled&id=" + virtualRouterProviderId),
                                                                            dataType: "json",
                                                                            async: false,
                                                                            success: function (json) {
                                                                                var jobId = json.updatenetworkserviceproviderresponse.jobid;
                                                                                var enableVirtualRouterProviderIntervalID = setInterval(function () {
                                                                                        $.ajax({
                                                                                            url: createURL("queryAsyncJobResult&jobId=" + jobId),
                                                                                            dataType: "json",
                                                                                            success: function (json) {
                                                                                                var result = json.queryasyncjobresultresponse;
                                                                                                if (result.jobstatus == 0) {
                                                                                                    return; //Job has not completed
                                                                                                } else {
                                                                                                    clearInterval(enableVirtualRouterProviderIntervalID);
                                                                                                    if (result.jobstatus == 1) {
                                                                                                        //alert("Virtual Router Provider is enabled");

                                                                                                        if (newZoneObj.networktype == "Basic") {
                                                                                                            if (args.data["security-groups-enabled"] == "on") {
                                                                                                                //need to Enable security group provider first
                                                                                                                // get network service provider ID of Security Group
                                                                                                                var securityGroupProviderId;
                                                                                                                $.ajax({
                                                                                                                    url: createURL("listNetworkServiceProviders&name=SecurityGroupProvider&physicalNetworkId=" + newPhysicalnetwork.id),
                                                                                                                    dataType: "json",
                                                                                                                    async: false,
                                                                                                                    success: function (json) {
                                                                                                                        var items = json.listnetworkserviceprovidersresponse.networkserviceprovider;
                                                                                                                        if (items != null && items.length > 0) {
                                                                                                                            securityGroupProviderId = items[0].id;
                                                                                                                        }
                                                                                                                    }
                                                                                                                });
                                                                                                                if (securityGroupProviderId == null) {
                                                                                                                    alert("error: listNetworkServiceProviders API doesn't return security group provider ID");
                                                                                                                    return;
                                                                                                                }

                                                                                                                $.ajax({
                                                                                                                    url: createURL("updateNetworkServiceProvider&state=Enabled&id=" + securityGroupProviderId),
                                                                                                                    dataType: "json",
                                                                                                                    async: false,
                                                                                                                    success: function (json) {
                                                                                                                        var jobId = json.updatenetworkserviceproviderresponse.jobid;
                                                                                                                        var enableSecurityGroupProviderIntervalID = setInterval(function () {
                                                                                                                                $.ajax({
                                                                                                                                    url: createURL("queryAsyncJobResult&jobId=" + jobId),
                                                                                                                                    dataType: "json",
                                                                                                                                    success: function (json) {
                                                                                                                                        var result = json.queryasyncjobresultresponse;
                                                                                                                                        if (result.jobstatus == 0) {
                                                                                                                                            return; //Job has not completed
                                                                                                                                        } else {
                                                                                                                                            clearInterval(enableSecurityGroupProviderIntervalID);
                                                                                                                                            if (result.jobstatus == 1) {
                                                                                                                                                //alert("Security group provider is enabled");

                                                                                                                                                //create network (for basic zone only)
                                                                                                                                                var array2 = [];
                                                                                                                                                array2.push("&zoneid=" + newZoneObj.id);
                                                                                                                                                array2.push("&name=guestNetworkForBasicZone");
                                                                                                                                                array2.push("&displaytext=guestNetworkForBasicZone");
                                                                                                                                                array2.push("&networkofferingid=" + args.data.networkOfferingId);
                                                                                                                                                $.ajax({
                                                                                                                                                    url: createURL("createNetwork" + array2.join("")),
                                                                                                                                                    dataType: "json",
                                                                                                                                                    async: false,
                                                                                                                                                    success: function (json) {
                                                                                                                                                        //create pod
                                                                                                                                                        var array3 = [];
                                                                                                                                                        array3.push("&zoneId=" + newZoneObj.id);
                                                                                                                                                        array3.push("&name=" + todb(args.data.podName));
                                                                                                                                                        array3.push("&gateway=" + todb(args.data.podGateway));
                                                                                                                                                        array3.push("&netmask=" + todb(args.data.podNetmask));
                                                                                                                                                        array3.push("&startIp=" + todb(args.data.podStartIp));

                                                                                                                                                        var endip = args.data.podEndIp; //optional
                                                                                                                                                        if (endip != null && endip.length > 0)
                                                                                                                                                            array3.push("&endIp=" + todb(endip));

                                                                                                                                                        $.ajax({
                                                                                                                                                            url: createURL("createPod" + array3.join("")),
                                                                                                                                                            dataType: "json",
                                                                                                                                                            async: false,
                                                                                                                                                            success: function (json) {
                                                                                                                                                            },
                                                                                                                                                            error: function (XMLHttpResponse) {
                                                                                                                                                                var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
                                                                                                                                                                alert("createPod failed. Error: " + errorMsg);
                                                                                                                                                            }
                                                                                                                                                        });
                                                                                                                                                    }
                                                                                                                                                });
                                                                                                                                            } else if (result.jobstatus == 2) {
                                                                                                                                                alert("failed to enable security group provider. Error: " + _s(result.jobresult.errortext));
                                                                                                                                            }
                                                                                                                                        }
                                                                                                                                    },
                                                                                                                                    error: function (XMLHttpResponse) {
                                                                                                                                        var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
                                                                                                                                        alert("updateNetworkServiceProvider failed. Error: " + errorMsg);
                                                                                                                                    }
                                                                                                                                });
                                                                                                                            },
                                                                                                                            g_queryAsyncJobResultInterval);
                                                                                                                    }
                                                                                                                });
                                                                                                            } else {
                                                                                                                //create network (for basic zone only)
                                                                                                                var array2 = [];
                                                                                                                array2.push("&zoneid=" + newZoneObj.id);
                                                                                                                array2.push("&name=guestNetworkForBasicZone");
                                                                                                                array2.push("&displaytext=guestNetworkForBasicZone");
                                                                                                                array2.push("&networkofferingid=" + args.data.networkOfferingId);
                                                                                                                $.ajax({
                                                                                                                    url: createURL("createNetwork" + array2.join("")),
                                                                                                                    dataType: "json",
                                                                                                                    async: false,
                                                                                                                    success: function (json) {
                                                                                                                        //create pod
                                                                                                                        var array3 = [];
                                                                                                                        array3.push("&zoneId=" + newZoneObj.id);
                                                                                                                        array3.push("&name=" + todb(args.data.podName));
                                                                                                                        array3.push("&gateway=" + todb(args.data.podGateway));
                                                                                                                        array3.push("&netmask=" + todb(args.data.podNetmask));
                                                                                                                        array3.push("&startIp=" + todb(args.data.podStartIp));

                                                                                                                        var endip = args.data.podEndIp; //optional
                                                                                                                        if (endip != null && endip.length > 0)
                                                                                                                            array3.push("&endIp=" + todb(endip));

                                                                                                                        $.ajax({
                                                                                                                            url: createURL("createPod" + array3.join("")),
                                                                                                                            dataType: "json",
                                                                                                                            async: false,
                                                                                                                            success: function (json) {
                                                                                                                            },
                                                                                                                            error: function (XMLHttpResponse) {
                                                                                                                                var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
                                                                                                                                alert("createPod failed. Error: " + errorMsg);
                                                                                                                            }
                                                                                                                        });
                                                                                                                    }
                                                                                                                });
                                                                                                            }
                                                                                                        } else {
                                                                                                            //Advanced zone
                                                                                                            //create pod
                                                                                                            var array3 = [];
                                                                                                            array3.push("&zoneId=" + newZoneObj.id);
                                                                                                            array3.push("&name=" + todb(args.data.podName));
                                                                                                            array3.push("&gateway=" + todb(args.data.podGateway));
                                                                                                            array3.push("&netmask=" + todb(args.data.podNetmask));
                                                                                                            array3.push("&startIp=" + todb(args.data.podStartIp));

                                                                                                            var endip = args.data.podEndIp; //optional
                                                                                                            if (endip != null && endip.length > 0)
                                                                                                                array3.push("&endIp=" + todb(endip));

                                                                                                            $.ajax({
                                                                                                                url: createURL("createPod" + array3.join("")),
                                                                                                                dataType: "json",
                                                                                                                async: false,
                                                                                                                success: function (json) {
                                                                                                                },
                                                                                                                error: function (XMLHttpResponse) {
                                                                                                                    var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
                                                                                                                    alert("createPod failed. Error: " + errorMsg);
                                                                                                                }
                                                                                                            });
                                                                                                        }
                                                                                                    } else if (result.jobstatus == 2) {
                                                                                                        alert("failed to enable Virtual Router Provider. Error: " + _s(result.jobresult.errortext));
                                                                                                    }
                                                                                                }
                                                                                            },
                                                                                            error: function (XMLHttpResponse) {
                                                                                                var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
                                                                                                alert("updateNetworkServiceProvider failed. Error: " + errorMsg);
                                                                                            }
                                                                                        });
                                                                                    },
                                                                                    g_queryAsyncJobResultInterval);
                                                                            }
                                                                        });
                                                                    } else if (result.jobstatus == 2) {
                                                                        alert("configureVirtualRouterElement failed. Error: " + _s(result.jobresult.errortext));
                                                                    }
                                                                }
                                                            },
                                                            error: function (XMLHttpResponse) {
                                                                var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
                                                                alert("configureVirtualRouterElement failed. Error: " + errorMsg);
                                                            }
                                                        });
                                                    },
                                                    g_queryAsyncJobResultInterval);
                                            }
                                        });
                                    } else if (result.jobstatus == 2) {
                                        alert("updatePhysicalNetwork failed. Error: " + _s(result.jobresult.errortext));
                                    }
                                }
                            },
                            error: function (XMLHttpResponse) {
                                var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
                                alert("updatePhysicalNetwork failed. Error: " + errorMsg);
                            }
                        });
                    },
                    g_queryAsyncJobResultInterval);
            }
        });
    };

    //action filters (begin)
    var zoneActionfilter = cloudStack.actionFilter.zoneActionfilter = function (args) {
        var jsonObj = args.context.item;
        var allowedActions = ['enableSwift'];

        if (jsonObj.domainid != null)
            allowedActions.push("releaseDedicatedZone"); else
            allowedActions.push("dedicateZone");

        allowedActions.push("edit");

        if (jsonObj.allocationstate == "Disabled")
            allowedActions.push("enable"); else if (jsonObj.allocationstate == "Enabled")
            allowedActions.push("disable");

        allowedActions.push("remove");
        return allowedActions;
    }


    var nexusActionfilter = function (args) {
        var nexusObj = args.context.item;
        var allowedActions = [];
        allowedActions.push("edit");
        if (nexusObj.vsmdevicestate == "Disabled")
            allowedActions.push("enable"); else if (nexusObj.vsmdevicestate == "Enabled")
            allowedActions.push("disable");
        allowedActions.push("remove");
        return allowedActions;
    }

    var podActionfilter = function (args) {
        var podObj = args.context.item;
        var dedicatedPodObj = args.context.podItem;
        var allowedActions = [];

        if (podObj.domainid != null)
            allowedActions.push("release"); else
            allowedActions.push("dedicate");


        allowedActions.push("edit");
        if (podObj.allocationstate == "Disabled")
            allowedActions.push("enable"); else if (podObj.allocationstate == "Enabled")
            allowedActions.push("disable");
        allowedActions.push("remove");

        /*
         var selectedZoneObj;
         $(zoneObjs).each(function(){
         if(this.id == podObj.zoneid) {
         selectedZoneObj = this;
         return false;  //break the $.each() loop
         }
         });
         */

        if (selectedZoneObj.networktype == "Basic") {
            //basic-mode network (pod-wide VLAN)
            //$("#tab_ipallocation, #add_iprange_button, #tab_network_device, #add_network_device_button").show();
            allowedActions.push("addIpRange");
            allowedActions.push("addNetworkDevice");
        } else if (selectedZoneObj.networktype == "Advanced") {
            //advanced-mode network (zone-wide VLAN)
            //$("#tab_ipallocation, #add_iprange_button, #tab_network_device, #add_network_device_button").hide();
        }

        return allowedActions;
    }

    var networkDeviceActionfilter = function (args) {
        var jsonObj = args.context.item;
        var allowedActions = [];
        return allowedActions;
    }

    var clusterActionfilter = function (args) {
        var jsonObj = args.context.item;
        var allowedActions = [];

        if (jsonObj.domainid != null)
            allowedActions.push("release"); else
            allowedActions.push("dedicate");

        if (jsonObj.state == "Enabled") {
            //managed, allocation enabled
            allowedActions.push("unmanage");
            allowedActions.push("disable");
            //allowedActions.push("edit"); // No fields to edit
        } else if (jsonObj.state == "Disabled") {
            //managed, allocation disabled
            allowedActions.push("unmanage");
            allowedActions.push("enable");
            //allowedActions.push("edit"); // No fields to edit
        } else {
            //Unmanaged, PrepareUnmanaged , PrepareUnmanagedError
            allowedActions.push("manage");
        }

        allowedActions.push("remove");

        return allowedActions;
    }

    var hostActionfilter = function (args) {
        var jsonObj = args.context.item;
        var allowedActions = [];

        if (jsonObj.domainid != null)
            allowedActions.push("release"); else
            allowedActions.push("dedicate");


        if (jsonObj.resourcestate == "Enabled") {
            allowedActions.push("edit");
            allowedActions.push("enableMaintenanceMode");
            allowedActions.push("disable");

            if (jsonObj.state != "Disconnected")
                allowedActions.push("forceReconnect");
        } else if (jsonObj.resourcestate == "ErrorInMaintenance") {
            allowedActions.push("edit");
            allowedActions.push("enableMaintenanceMode");
            allowedActions.push("cancelMaintenanceMode");
        } else if (jsonObj.resourcestate == "PrepareForMaintenance") {
            allowedActions.push("edit");
            allowedActions.push("cancelMaintenanceMode");
        } else if (jsonObj.resourcestate == "Maintenance") {
            allowedActions.push("edit");
            allowedActions.push("cancelMaintenanceMode");
            allowedActions.push("remove");
        } else if (jsonObj.resourcestate == "Disabled") {
            allowedActions.push("edit");
            allowedActions.push("enable");
            allowedActions.push("remove");
        }

        if ((jsonObj.state == "Down" || jsonObj.state == "Alert" || jsonObj.state == "Disconnected") && ($.inArray("remove", allowedActions) == -1)) {
            allowedActions.push("remove");
        }

        return allowedActions;
    }

    var primarystorageActionfilter = function (args) {
        var jsonObj = args.context.item;
        var allowedActions = [];

        allowedActions.push("edit");

        if (jsonObj.state == 'Up' || jsonObj.state == "Connecting") {
            allowedActions.push("enableMaintenanceMode");
        } else if (jsonObj.state == 'Down') {
            allowedActions.push("enableMaintenanceMode");
            allowedActions.push("remove");
        } else if (jsonObj.state == "Alert") {
            allowedActions.push("remove");
        } else if (jsonObj.state == "ErrorInMaintenance") {
            allowedActions.push("enableMaintenanceMode");
            allowedActions.push("cancelMaintenanceMode");
        } else if (jsonObj.state == "PrepareForMaintenance") {
            allowedActions.push("cancelMaintenanceMode");
        } else if (jsonObj.state == "Maintenance") {
            allowedActions.push("cancelMaintenanceMode");
            allowedActions.push("remove");
        } else if (jsonObj.state == "Disconnected") {
            allowedActions.push("remove");
        }
        return allowedActions;
    }

    var secondarystorageActionfilter = function (args) {
        var jsonObj = args.context.item;
        var allowedActions = [];
        allowedActions.push("remove");
        return allowedActions;
    }

    var routerActionfilter = cloudStack.sections.system.routerActionFilter = function (args) {
        var jsonObj = args.context.item;
        var allowedActions = [];

        if (jsonObj.requiresupgrade == true) {
            allowedActions.push('upgradeRouterToUseNewerTemplate');
        }

        if (jsonObj.state == 'Running') {
            allowedActions.push("stop");

            allowedActions.push("restart");

            allowedActions.push("viewConsole");
            if (isAdmin())
                allowedActions.push("migrate");
        } else if (jsonObj.state == 'Stopped') {
            allowedActions.push("start");

            //when router is Stopped, all hypervisors support scaleUp(change service offering)
            allowedActions.push("scaleUp");

            allowedActions.push("remove");
        }
        return allowedActions;
    }

    var internallbinstanceActionfilter = function (args) {
        var jsonObj = args.context.item;
        var allowedActions = [];

        if (jsonObj.state == 'Running') {
            allowedActions.push("stop");

            allowedActions.push("viewConsole");
            if (isAdmin())
                allowedActions.push("migrate");
        } else if (jsonObj.state == 'Stopped') {
            allowedActions.push("start");
        }
        return allowedActions;
    }

    var systemvmActionfilter = function (args) {
        var jsonObj = args.context.item;
        var allowedActions = [];

        if (jsonObj.state == 'Running') {
            allowedActions.push("stop");
            allowedActions.push("restart");
            allowedActions.push("remove");

            allowedActions.push("viewConsole");
            if (isAdmin())
                allowedActions.push("migrate");
        } else if (jsonObj.state == 'Stopped') {
            allowedActions.push("start");

            //when systemvm is Stopped, all hypervisors support scaleUp(change service offering)
            allowedActions.push("scaleUp");

            allowedActions.push("remove");
        } else if (jsonObj.state == 'Error') {
            allowedActions.push("remove");
        }
        return allowedActions;
    }

    var routerGroupActionfilter = function (args) {
        var jsonObj = args.context.item;
        var allowedActions = [];
        if (jsonObj.routerRequiresUpgrade > 0) {
            allowedActions.push("upgradeRouterToUseNewerTemplate");
        }
        return allowedActions;
    }

    var bladeActionfilter = function (args) {
        var jsonObj = args.context.item;
        var allowedActions = [];
        if (jsonObj.profiledn == null) {
            allowedActions.push("associateTemplateToBlade");
        } else {
            allowedActions.push("disassociateProfileFromBlade");
        }
        return allowedActions;
    }

    //action filters (end)

    var networkProviderActionFilter = function (id) {
        return function (args) {
            var allowedActions = [];
            var jsonObj = nspMap[id] ?
                nspMap[id] : {};

            if (jsonObj.state) {
                if (jsonObj.state == "Enabled")
                    allowedActions.push("disable"); else if (jsonObj.state == "Disabled")
                    allowedActions.push("enable");
                allowedActions.push("destroy");
            }

            allowedActions.push('add');

            return allowedActions;
        }
    };

    var addExtraPropertiesToClusterObject = function (jsonObj) {
        if (jsonObj.managedstate == "Managed") {
            jsonObj.state = jsonObj.allocationstate; //jsonObj.state == Enabled, Disabled
        } else {
            jsonObj.state = jsonObj.managedstate; //jsonObj.state == Unmanaged, PrepareUnmanaged, PrepareUnmanagedError
        }
    }

    var addExtraPropertiesToRouterInstanceObject = function (jsonObj) {
        if (jsonObj.isredundantrouter == true) {
            jsonObj["redundantRouterState"] = jsonObj.redundantstate;
        } else {
            jsonObj["redundantRouterState"] = "";
        }
    }

    var refreshNspData = function (nspName) {
        var array1 = [];
        if (nspName != null)
            array1.push("&name=" + nspName);

        $.ajax({
            url: createURL("listNetworkServiceProviders&physicalnetworkid=" + selectedPhysicalNetworkObj.id + array1.join("")),
            dataType: "json",
            async: false,
            success: function (json) {
                nspMap = {};
                //reset

                var items = json.listnetworkserviceprovidersresponse.networkserviceprovider;
                if (items != null) {
                    for (var i = 0; i < items.length; i++) {
                        switch (items[i].name) {
                            case "VirtualRouter":
                                nspMap["virtualRouter"] = items[i];
                                break;
                            case "InternalLbVm":
                                nspMap["InternalLbVm"] = items[i];
                                break;
                            case "VpcVirtualRouter":
                                nspMap["vpcVirtualRouter"] = items[i];
                                break;
                            case "MidoNet":
                                nspMap["midoNet"] = items[i];
                                break;
                            case "JuniperSRX":
                                nspMap["srx"] = items[i];
                                break;
                            case "SecurityGroupProvider":
                                nspMap["securityGroups"] = items[i];
                                break;
                            case "NiciraNvp":
                                nspMap["niciraNvp"] = items[i];
                                break;
                            case "NuageVsp":
                                nspMap["nuageVsp"] = items[i];
                                break;
                        }
                    }
                }
            }
        });

        nspHardcodingArray = [
            {
                id: 'virtualRouter',
                name: 'Virtual Router',
                state: nspMap.virtualRouter ? nspMap.virtualRouter.state : 'Disabled'
            },
            {
                id: 'niciraNvp',
                name: 'Nicira Nvp',
                state: nspMap.niciraNvp ? nspMap.niciraNvp.state : 'Disabled'
            }
        ];

        $(window).trigger('cloudStack.system.serviceProviders.makeHarcodedArray', {
            nspHardcodingArray: nspHardcodingArray,
            selectedZoneObj: selectedZoneObj,
            selectedPhysicalNetworkObj: selectedPhysicalNetworkObj
        });

        if (selectedZoneObj.networktype == "Basic") {
            nspHardcodingArray.push({
                id: 'securityGroups',
                name: 'Security Groups',
                state: nspMap.securityGroups ? nspMap.securityGroups.state : 'Disabled'
            });
        } else if (selectedZoneObj.networktype == "Advanced") {
            nspHardcodingArray.push({
                id: 'midoNet',
                name: 'MidoNet',
                state: nspMap.midoNet ? nspMap.midoNet.state : 'Disabled'
            });

            nspHardcodingArray.push({
                id: 'InternalLbVm',
                name: 'Internal LB VM',
                state: nspMap.InternalLbVm ? nspMap.InternalLbVm.state : 'Disabled'
            });

            nspHardcodingArray.push({
                id: 'vpcVirtualRouter',
                name: 'VPC Virtual Router',
                state: nspMap.vpcVirtualRouter ? nspMap.vpcVirtualRouter.state : 'Disabled'
            });
        }
    };

    cloudStack.actionFilter.physicalNetwork = function (args) {
        var state = args.context.item.state;

        if (state != 'Destroyed') {
            return ['remove'];
        }

        return [];
    };

    function addExtraPropertiesToGroupbyObjects(groupbyObjs, groupbyId) {
        for (var i = 0; i < groupbyObjs.length; i++) {
            addExtraPropertiesToGroupbyObject(groupbyObjs[i], groupbyId);
        }
    }

    function addExtraPropertiesToGroupbyObject(groupbyObj, groupbyId) {
        var currentPage = 1;

        var listRoutersData = {
            listAll: true,
            pagesize: pageSize //global variable
        };
        listRoutersData[groupbyId] = groupbyObj.id;

        $.ajax({
            url: createURL('listRouters'),
            data: $.extend({},
                listRoutersData, {
                    page: currentPage
                }),
            async: false,
            success: function (json) {
                if (json.listroutersresponse.count != undefined) {
                    var routerCountFromAllPages = json.listroutersresponse.count;
                    var routerCountFromFirstPageToCurrentPage = json.listroutersresponse.router.length;
                    var routerRequiresUpgrade = 0;

                    var items = json.listroutersresponse.router;
                    for (var k = 0; k < items.length; k++) {
                        if (items[k].requiresupgrade) {
                            routerRequiresUpgrade++;
                        }
                    }

                    $.ajax({
                        url: createURL('listRouters'),
                        data: $.extend({}, listRoutersData, {
                            page: currentPage,
                            projectid: -1
                        }),
                        async: false,
                        success: function (json) {
                            if (json.listroutersresponse.count != undefined) {
                                routerCountFromAllPages += json.listroutersresponse.count;
                                groupbyObj.routerCount = routerCountFromAllPages;

                                routerCountFromFirstPageToCurrentPage += json.listroutersresponse.router.length;

                                var items = json.listroutersresponse.router;
                                for (var k = 0; k < items.length; k++) {
                                    if (items[k].requiresupgrade) {
                                        routerRequiresUpgrade++;
                                    }
                                }
                            } else {
                                groupbyObj.routerCount = routerCountFromAllPages;
                            }
                        }
                    });

                    var callListApiWithPage = function () {
                        $.ajax({
                            url: createURL('listRouters'),
                            async: false,
                            data: $.extend({}, listRoutersData, {
                                page: currentPage
                            }),
                            success: function (json) {
                                routerCountFromFirstPageToCurrentPage += json.listroutersresponse.router.length;
                                var items = json.listroutersresponse.router;
                                for (var k = 0; k < items.length; k++) {
                                    if (items[k].requiresupgrade) {
                                        routerRequiresUpgrade++;
                                    }
                                }

                                $.ajax({
                                    url: createURL('listRouters'),
                                    async: false,
                                    data: $.extend({}, listRoutersData, {
                                        page: currentPage,
                                        projectid: -1
                                    }),
                                    success: function (json) {
                                        if (json.listroutersresponse.count != undefined) {
                                            routerCountFromAllPages += json.listroutersresponse.count;
                                            groupbyObj.routerCount = routerCountFromAllPages;

                                            routerCountFromFirstPageToCurrentPage += json.listroutersresponse.router.length;

                                            var items = json.listroutersresponse.router;
                                            for (var k = 0; k < items.length; k++) {
                                                if (items[k].requiresupgrade) {
                                                    routerRequiresUpgrade++;
                                                }
                                            }
                                        } else {
                                            groupbyObj.routerCount = routerCountFromAllPages;
                                        }
                                    }
                                });

                                if (routerCountFromFirstPageToCurrentPage < routerCountFromAllPages) {
                                    currentPage++;
                                    callListApiWithPage();
                                }
                            }
                        });
                    }

                    if (routerCountFromFirstPageToCurrentPage < routerCountFromAllPages) {
                        currentPage++;
                        callListApiWithPage();
                    }

                    groupbyObj.routerRequiresUpgrade = routerRequiresUpgrade;
                    groupbyObj.numberOfRouterRequiresUpgrade = routerRequiresUpgrade;
                } else {
                    groupbyObj.routerCount = 0;
                    groupbyObj.routerRequiresUpgrade = 0;
                    groupbyObj.numberOfRouterRequiresUpgrade = 0;
                }
            }
        });
    }
})($, cloudStack);
