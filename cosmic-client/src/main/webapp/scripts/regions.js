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
