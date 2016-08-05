(function ($, cloudStack) {
    cloudStack.modules.infrastructure = function (module) {
        module.pluginAPI.extend({
            networkServiceProvider: function (args) {
                var name = args.name;
                var id = args.id;
                var state = args.state;
                var detailView = args.detailView;
                var listView = args.listView;

                cloudStack.sections.system.naas.networkProviders.types[id] = detailView;
                cloudStack.sections.system.subsections[listView.id] = {
                    id: listView.id,
                    title: name,
                    listView: listView
                };

                $(window).bind('cloudStack.system.serviceProviders.makeHarcodedArray', function (event, data) {
                    var nspHardcodingArray = data.nspHardcodingArray;
                    var selectedZoneObj = data.selectedZoneObj;
                    var selectedPhysicalNetworkObj = data.selectedPhysicalNetworkObj;
                    if (selectedZoneObj.networktype == "Advanced") {
                        var selectedProviderObj = null;
                        $.ajax({
                            url: createURL('listNetworkServiceProviders'),
                            data: {
                                name: id, //e.g. 'CiscoVnmc'
                                physicalnetworkid: selectedPhysicalNetworkObj.id
                            },
                            async: false,
                            success: function (json) {
                                var items = json.listnetworkserviceprovidersresponse.networkserviceprovider;
                                if (items != null && items.length > 0) {
                                    selectedProviderObj = items[0];
                                }
                            }
                        });

                        nspHardcodingArray.push({
                            id: id,
                            name: name,
                            state: selectedProviderObj ? selectedProviderObj.state : 'Absent'
                        });
                    }
                });
            },

            resource: function (args) {
                var type = args.type;

                if (type) {
                    return cloudStack.sections.system.subsections[type];
                } else {
                    return false;
                }
            }
        });
    };
}(jQuery, cloudStack));
