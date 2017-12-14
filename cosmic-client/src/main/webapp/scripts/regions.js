(function (cloudStack) {
    cloudStack.sections.regions = {
        title: 'label.menu.regions',
        id: 'regions',
        sectionSelect: {
            label: 'label.select-view',
            preFilter: function () {
                return ['regions'];
            }
        },
        regionSelector: {
            dataProvider: function (args) {
                $.ajax({
                    url: createURL('listRegions'),
                    success: function (json) {
                        var regions = json.listregionsresponse.region;

                        args.response.success({
                            data: regions ? regions : [{
                                id: -1,
                                name: _l('label.no.data')
                            }]
                        });
                    }
                });
            }
        },
        sections: {
            regions: {
                id: 'regions',
                type: 'select',
                title: 'label.menu.regions',
                listView: {
                    section: 'regions',
                    id: 'regions',
                    label: 'label.menu.regions',
                    fields: {
                        name: {
                            label: 'label.name'
                        },
                        id: {
                            label: 'label.id'
                        },
                        endpoint: {
                            label: 'label.endpoint'
                        }
                    },
                    actions: {
                        add: {
                            label: 'label.add.region',
                            preFilter: function (args) {
                                if (isAdmin())
                                    return true;
                                else
                                    return false;
                            },
                            messages: {
                                notification: function () {
                                    return 'label.add.region';
                                }
                            },
                            createForm: {
                                title: 'label.add.region',
                                desc: 'message.add.region',
                                fields: {
                                    id: {
                                        label: 'label.id',
                                        validation: {
                                            required: true
                                        }
                                    },
                                    name: {
                                        label: 'label.name',
                                        validation: {
                                            required: true
                                        }
                                    },
                                    endpoint: {
                                        label: 'label.endpoint',
                                        validation: {
                                            required: true
                                        }
                                    }
                                }
                            },
                            action: function (args) {
                                var data = {
                                    id: args.data.id,
                                    name: args.data.name,
                                    endpoint: args.data.endpoint
                                };

                                $.ajax({
                                    url: createURL('addRegion'),
                                    data: data,
                                    success: function (json) {
                                        var item = json.addregionresponse.region;
                                        args.response.success({
                                            data: item
                                        });
                                        $(window).trigger('cloudStack.refreshRegions');
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
                    },
                    dataProvider: function (args) {
                        $.ajax({
                            url: createURL('listRegions'),
                            success: function (json) {
                                var items = json.listregionsresponse.region;
                                args.response.success({
                                    data: items
                                });
                            },
                            error: function (json) {
                                args.response.error(parseXMLHttpResponse(json));
                            }
                        });
                    },
                    detailView: {
                        name: 'label.region.details',
                        viewAll: [{
                            path: 'network.vpc',
                            label: 'label.regionlevelvpc'
                        }, {
                            path: 'regions.portableIpRanges',
                            label: 'label.portable.ip',
                            preFilter: function (args) {
                                if (isAdmin())
                                    return true;

                                return false;
                            }
                        }],
                        actions: {
                            edit: {
                                label: 'label.edit.region',
                                action: function (args) {
                                    var data = {
                                        id: args.context.regions[0].id,
                                        name: args.data.name,
                                        endpoint: args.data.endpoint
                                    };

                                    $.ajax({
                                        url: createURL('updateRegion'),
                                        data: data,
                                        success: function (json) {
                                            args.response.success();
                                            $(window).trigger('cloudStack.refreshRegions');
                                        },
                                        error: function (json) {
                                            args.response.error(parseXMLHttpResponse(json));
                                        }
                                    });
                                }
                            },
                            remove: {
                                label: 'label.remove.region',
                                messages: {
                                    notification: function () {
                                        return 'label.remove.region';
                                    },
                                    confirm: function () {
                                        return 'message.remove.region';
                                    }
                                },
                                preAction: function (args) {
                                    var region = args.context.regions[0];

                                    return true;
                                },
                                action: function (args) {
                                    var region = args.context.regions[0];

                                    $.ajax({
                                        url: createURL('removeRegion'),
                                        data: {
                                            id: region.id
                                        },
                                        success: function (json) {
                                            args.response.success();
                                            $(window).trigger('cloudStack.refreshRegions');
                                        },
                                        error: function (json) {
                                            args.response.error(parseXMLHttpResponse(json));
                                        }
                                    });
                                }
                            }
                        },
                        tabs: {
                            details: {
                                title: 'label.details',
                                fields: [{
                                    id: {
                                        label: 'label.id'
                                    }
                                }, {
                                    name: {
                                        label: 'label.name',
                                        isEditable: true
                                    },
                                    endpoint: {
                                        label: 'label.endpoint',
                                        isEditable: true
                                    }
                                }],
                                dataProvider: function (args) {
                                    $.ajax({
                                        url: createURL('listRegions'),
                                        data: {
                                            id: args.context.regions[0].id
                                        },
                                        success: function (json) {
                                            var region = json.listregionsresponse.region

                                            args.response.success({
                                                actionFilter: regionActionfilter,
                                                data: region ? region[0] : {}
                                            });
                                        },
                                        error: function (json) {
                                            args.response.error(parseXMLHttpResponse(json));
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            },

            portableIpRanges: {
                id: 'portableIpRanges',
                type: 'select',
                title: 'label.portable.ip.ranges',
                listView: {
                    id: 'portableIpRanges',
                    label: 'label.portable.ip.ranges',
                    fields: {
                        startip: {
                            label: 'label.start.IP'
                        },
                        endip: {
                            label: 'label.end.IP'
                        },
                        gateway: {
                            label: 'label.gateway'
                        },
                        netmask: {
                            label: 'label.netmask'
                        },
                        vlan: {
                            label: 'label.vlan'
                        }
                    },
                    dataProvider: function (args) {
                        $.ajax({
                            url: createURL('listPortableIpRanges'),
                            data: {
                                regionid: args.context.regions[0].id
                            },
                            success: function (json) {
                                var items = json.listportableipresponse.portableiprange;
                                args.response.success({
                                    data: items
                                });
                            },
                            error: function (json) {
                                args.response.error(parseXMLHttpResponse(json));
                            }
                        });
                    },
                    actions: {
                        add: {
                            label: 'label.add.portable.ip.range',
                            messages: {
                                notification: function (args) {
                                    return 'label.add.portable.ip.range';
                                }
                            },
                            createForm: {
                                title: 'label.add.portable.ip.range',
                                fields: {
                                    startip: {
                                        label: 'label.start.IP',
                                        validation: {
                                            required: true
                                        }
                                    },
                                    endip: {
                                        label: 'label.end.IP',
                                        validation: {
                                            required: true
                                        }
                                    },
                                    gateway: {
                                        label: 'label.gateway',
                                        validation: {
                                            required: true
                                        }
                                    },
                                    netmask: {
                                        label: 'label.netmask',
                                        validation: {
                                            required: true
                                        }
                                    },
                                    vlan: {
                                        label: 'label.vlan',
                                        validation: {
                                            required: false
                                        }
                                    }
                                }
                            },
                            action: function (args) {
                                var data = {
                                    regionid: args.context.regions[0].id,
                                    startip: args.data.startip,
                                    endip: args.data.endip,
                                    gateway: args.data.gateway,
                                    netmask: args.data.netmask
                                };
                                if (args.data.vlan != null && args.data.vlan.length > 0) {
                                    $.extend(data, {
                                        vlan: args.data.vlan
                                    })
                                }
                                $.ajax({
                                    url: createURL('createPortableIpRange'),
                                    data: data,
                                    success: function (json) {
                                        var jid = json.createportableiprangeresponse.jobid;
                                        args.response.success({
                                            _custom: {
                                                jobId: jid,
                                                getUpdatedItem: function (json) {
                                                    return json.queryasyncjobresultresponse.jobresult.portableiprange;
                                                }
                                            }
                                        });
                                    },
                                    error: function (data) {
                                        args.response.error(parseXMLHttpResponse(data));
                                    }
                                });
                            },
                            notification: {
                                poll: pollAsyncJobResult
                            }
                        }
                    },

                    detailView: {
                        name: 'label.portable.ip.range.details',
                        actions: {
                            remove: {
                                label: 'label.delete.portable.ip.range',
                                messages: {
                                    confirm: function (args) {
                                        return 'message.portable.ip.delete.confirm';
                                    },
                                    notification: function (args) {
                                        return 'label.delete.portable.ip.range';
                                    }
                                },
                                action: function (args) {
                                    var data = {
                                        id: args.context.portableIpRanges[0].id
                                    };
                                    $.ajax({
                                        url: createURL('deletePortableIpRange'),
                                        data: data,
                                        async: true,
                                        success: function (json) {
                                            var jid = json.deleteportablepublicipresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid
                                                }
                                            });
                                        },
                                        error: function (data) {
                                            args.response.error(parseXMLHttpResponse(data));
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
                                    id: {
                                        label: 'label.id'
                                    }
                                }, {
                                    startip: {
                                        label: 'label.start.IP'
                                    },
                                    endip: {
                                        label: 'label.end.IP'
                                    },
                                    gateway: {
                                        label: 'label.gateway'
                                    },
                                    netmask: {
                                        label: 'label.netmask'
                                    },
                                    vlan: {
                                        label: 'label.vlan'
                                    },
                                    portableipaddress: {
                                        label: 'label.portable.ips',
                                        converter: function (args) {
                                            var text1 = '';
                                            if (args != null) {
                                                for (var i = 0; i < args.length; i++) {
                                                    if (i > 0) {
                                                        text1 += ', ';
                                                    }
                                                    text1 += args[i].ipaddress;
                                                }
                                            }
                                            return text1;
                                        }
                                    }
                                }],
                                dataProvider: function (args) {
                                    $.ajax({
                                        url: createURL('listPortableIpRanges'),
                                        data: {
                                            id: args.context.portableIpRanges[0].id
                                        },
                                        success: function (json) {
                                            var item = json.listportableipresponse.portableiprange[0];
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
                        }
                    }
                }
            }
        }
    };

    var regionActionfilter = function (args) {
        var allowedActions = [];
        if (isAdmin()) {
            allowedActions.push("edit");
            allowedActions.push("remove");
        }
        return allowedActions;
    }

})(cloudStack);
