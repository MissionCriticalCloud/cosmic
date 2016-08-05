(function (cloudStack) {
    cloudStack.sections.affinityGroups = {
        title: 'label.affinity.groups',
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
                var data = {};
                listViewDataProvider(args, data);
                if (args.context != null) {
                    if ("instances" in args.context) {
                        $.extend(data, {
                            virtualmachineid: args.context.instances[0].id
                        });
                    }
                }
                $.ajax({
                    url: createURL('listAffinityGroups'),
                    data: data,
                    success: function (json) {
                        var items = json.listaffinitygroupsresponse.affinitygroup;
                        args.response.success({
                            data: items
                        });
                    }
                });
            },
            actions: {
                add: {
                    label: 'label.add.affinity.group',

                    messages: {
                        notification: function (args) {
                            return 'label.add.affinity.group';
                        }
                    },

                    createForm: {
                        title: 'label.add.affinity.group',
                        fields: {
                            name: {
                                label: 'label.name',
                                validation: {
                                    required: true
                                }
                            },
                            description: {
                                label: 'label.description'
                            },
                            type: {
                                label: 'label.type',
                                select: function (args) {
                                    $.ajax({
                                        url: createURL('listAffinityGroupTypes'),
                                        success: function (json) {
                                            var types = [];
                                            var items = json.listaffinitygrouptypesresponse.affinityGroupType;
                                            if (items != null) {
                                                for (var i = 0; i < items.length; i++) {
                                                    types.push({
                                                        id: items[i].type,
                                                        description: items[i].type
                                                    });
                                                }
                                            }
                                            args.response.success({
                                                data: types
                                            })
                                        }
                                    });
                                }
                            }
                        }
                    },

                    action: function (args) {
                        var data = {
                            name: args.data.name,
                            type: args.data.type
                        };
                        if (args.data.description != null && args.data.description.length > 0)
                            $.extend(data, {
                                description: args.data.description
                            });

                        $.ajax({
                            url: createURL('createAffinityGroup'),
                            data: data,
                            success: function (json) {
                                var jid = json.createaffinitygroupresponse.jobid;
                                args.response.success({
                                    _custom: {
                                        jobId: jid,
                                        getUpdatedItem: function (json) {
                                            return json.queryasyncjobresultresponse.jobresult.affinitygroup;
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
            detailView: {
                actions: {
                    remove: {
                        label: 'label.delete.affinity.group',
                        messages: {
                            confirm: function (args) {
                                return 'message.delete.affinity.group';
                            },
                            notification: function (args) {
                                return 'label.delete.affinity.group';
                            }
                        },
                        action: function (args) {
                            $.ajax({
                                url: createURL('deleteAffinityGroup'),
                                data: {
                                    id: args.context.affinityGroups[0].id
                                },
                                success: function (json) {
                                    var jid = json.deleteaffinitygroupresponse.jobid;
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

                viewAll: {
                    path: 'instances',
                    label: 'label.instances'
                },

                tabs: {
                    details: {
                        title: 'label.details',
                        fields: [{
                            name: {
                                label: 'label.name'
                            }
                        }, {
                            description: {
                                label: 'label.description'
                            },
                            type: {
                                label: 'label.type'
                            },
                            id: {
                                label: 'label.id'
                            }
                        }],

                        dataProvider: function (args) {
                            $.ajax({
                                url: createURL('listAffinityGroups'),
                                data: {
                                    id: args.context.affinityGroups[0].id
                                },
                                success: function (json) {
                                    var item = json.listaffinitygroupsresponse.affinitygroup[0];
                                    args.response.success({
                                        actionFilter: affinitygroupActionfilter,
                                        data: item
                                    });
                                }
                            });
                        }
                    }
                }
            }
        }
    };

    var affinitygroupActionfilter = cloudStack.actionFilter.affinitygroupActionfilter = function (args) {
        var jsonObj = args.context.item;
        var allowedActions = [];
        if (jsonObj.type != 'ExplicitDedication' || isAdmin()) {
            allowedActions.push("remove");
        }
        return allowedActions;
    }

})(cloudStack);
