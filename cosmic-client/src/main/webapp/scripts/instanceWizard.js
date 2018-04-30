(function ($, cloudStack) {
    var zoneObjs, hypervisorObjs, featuredTemplateObjs, communityTemplateObjs, myTemplateObjs, sharedTemplateObjs,
        featuredIsoObjs, communityIsoObjs, myIsoObjs, sharedIsoObjs, serviceOfferingObjs,
        community, networkObjs;
    var selectedZoneObj, selectedTemplateObj, selectedHypervisor, selectedDiskOfferingObj;
    var selectedTemplateOrIso; //'select-template', 'select-iso'
    var step6ContainerType = 'nothing-to-select';

    cloudStack.instanceWizard = {
        //min disk offering  size when custom disk size is used
        minDiskOfferingSize: function () {
            return g_capabilities.customdiskofferingminsize;
        },

        //max disk offering size when custom disk size is used
        maxDiskOfferingSize: function () {
            return g_capabilities.customdiskofferingmaxsize;
        },

        // Determines whether 'add new network' box is shown.
        // -- return true to show, false to hide
        showAddNetwork: function (args) {
            return true;
        },

        // Called in networks list, when VPC drop-down is changed
        // -- if vpcID given, return true if in network specified by vpcID
        // -- if vpcID == -1, always show all networks
        vpcFilter: function (data, vpcID) {
            return vpcID != -1 ?
                data.vpcid == vpcID : true;
        },

        // Data providers for each wizard step
        steps: [
            // Step 1: Setup
            function (args) {
                //from VPC Tier chart -- when the tier (network) has strechedl2subnet==false:
                //only own zone is populated to the dropdown
                if (args.initArgs.pluginForm != null && args.initArgs.pluginForm.name == "vpcTierInstanceWizard"
                    && args.context.networks[0].strechedl2subnet) {
                    zoneObjs = [{
                        id: args.context.vpc[0].zoneid,
                        name: args.context.vpc[0].zonename,
                        networktype: 'Advanced'
                    }];
                    args.response.success({
                        data: {
                            zones: zoneObjs
                        }
                    });
                }
                //in all other cases (as well as from instance page) all zones are populated to dropdown
                else {
                    $.ajax({
                        url: createURL("listZones&available=true"),
                        dataType: "json",
                        async: false,
                        success: function (json) {
                            zoneObjs = json.listzonesresponse.zone;
                            args.response.success({
                                data: {
                                    zones: zoneObjs
                                }
                            });
                        }
                    });
                }
            },

            // Step 2: Select template
            function (args) {
                $(zoneObjs).each(function () {
                    if (this.id == args.currentData.zoneid) {
                        selectedZoneObj = this;
                        return false; //break the $.each() loop
                    }
                });
                if (selectedZoneObj == null) {
                    alert("error: can't find matched zone object");
                    return;
                }

                $.ajax({
                    url: createURL("listHypervisors&zoneid=" + args.currentData.zoneid),
                    dataType: "json",
                    async: false,
                    success: function (json) {
                        hypervisorObjs = json.listhypervisorsresponse.hypervisor;
                    }
                });

                //***** get templates/ISOs (begin) *****
                selectedTemplateOrIso = args.currentData['select-template'];
                if (selectedTemplateOrIso == 'select-template') {
                    var hypervisorArray = [];
                    $(hypervisorObjs).each(function (index, item) {
                        hypervisorArray.push(item.name);
                    });

                    $.ajax({
                        url: createURL("listTemplates&templatefilter=featured&zoneid=" + args.currentData.zoneid),
                        dataType: "json",
                        async: false,
                        success: function (json) {
                            if (json.listtemplatesresponse.template == null) {
                                featuredTemplateObjs = null;
                            } else {
                                featuredTemplateObjs = $.grep(json.listtemplatesresponse.template, function (item, index) {
                                    if ($.inArray(item.hypervisor, hypervisorArray) > -1)
                                        return true;
                                });
                            }
                        }
                    });
                    $.ajax({
                        url: createURL("listTemplates&templatefilter=community&zoneid=" + args.currentData.zoneid),
                        dataType: "json",
                        async: false,
                        success: function (json) {
                            if (json.listtemplatesresponse.template == null) {
                                communityTemplateObjs = null;
                            } else {
                                communityTemplateObjs = $.grep(json.listtemplatesresponse.template, function (item, index) {
                                    if ($.inArray(item.hypervisor, hypervisorArray) > -1)
                                        return true;
                                });
                            }
                        }
                    });
                    $.ajax({
                        url: createURL("listTemplates&templatefilter=selfexecutable&zoneid=" + args.currentData.zoneid),
                        dataType: "json",
                        async: false,
                        success: function (json) {
                            if (json.listtemplatesresponse.template == null) {
                                myTemplateObjs = null;
                            } else {
                                myTemplateObjs = $.grep(json.listtemplatesresponse.template, function (item, index) {
                                    if ($.inArray(item.hypervisor, hypervisorArray) > -1)
                                        return true;
                                });
                            }
                        }
                    });
                    $.ajax({
                        url: createURL("listTemplates&templatefilter=sharedexecutable&zoneid=" + args.currentData.zoneid),
                        dataType: "json",
                        async: false,
                        success: function (json) {
                            if (json.listtemplatesresponse.template == null) {
                                sharedTemplateObjs = null;
                            } else {
                                sharedTemplateObjs = $.grep(json.listtemplatesresponse.template, function (item, index) {
                                    if ($.inArray(item.hypervisor, hypervisorArray) > -1)
                                        return true;
                                });
                            }
                        }
                    });
                } else if (selectedTemplateOrIso == 'select-iso') {
                    $.ajax({
                        url: createURL("listIsos&isofilter=featured&zoneid=" + args.currentData.zoneid + "&bootable=true"),
                        dataType: "json",
                        async: false,
                        success: function (json) {
                            if (json.listisosresponse.iso == null) {
                                featuredIsoObjs = null;
                            } else {
                                featuredIsoObjs = json.listisosresponse.iso;
                            }
                        }
                    });
                    $.ajax({
                        url: createURL("listIsos&isofilter=community&zoneid=" + args.currentData.zoneid + "&bootable=true"),
                        dataType: "json",
                        async: false,
                        success: function (json) {
                            if (json.listisosresponse.iso == null) {
                                communityIsoObjs = null;
                            } else {
                                communityIsoObjs = json.listisosresponse.iso;
                            }
                        }
                    });
                    $.ajax({
                        url: createURL("listIsos&isofilter=selfexecutable&zoneid=" + args.currentData.zoneid + "&bootable=true"),
                        dataType: "json",
                        async: false,
                        success: function (json) {
                            if (json.listisosresponse.iso == null) {
                                myIsoObjs = null;
                            } else {
                                myIsoObjs = json.listisosresponse.iso;
                            }
                        }
                    });
                    $.ajax({
                        url: createURL("listIsos&isofilter=sharedexecutable&zoneid=" + args.currentData.zoneid + "&bootable=true"),
                        dataType: "json",
                        async: false,
                        success: function (json) {
                            if (json.listisosresponse.iso == null) {
                                sharedIsoObjs = null;
                            } else {
                                sharedIsoObjs = json.listisosresponse.iso;
                            }
                        }
                    });
                }
                //***** get templates/ISOs (end) *****

                var templatesObj = {};
                var controllerObjs = [
                    {id: '', displaytext: 'Default'},
                    {id: 'IDE', displaytext: 'IDE (Legacy)'},
                    {id: 'VIRTIO', displaytext: 'VirtIO (virtio-blk)'},
                    {id: 'SCSI', displaytext: 'VirtIO SCSI (virtio-scsi)'}
                ];
                if (selectedTemplateOrIso == 'select-template') {
                    templatesObj = {
                        featuredtemplates: featuredTemplateObjs,
                        communitytemplates: communityTemplateObjs,
                        mytemplates: myTemplateObjs,
                        sharedtemplates: sharedTemplateObjs
                    };
                } else if (selectedTemplateOrIso == 'select-iso') {
                    templatesObj = {
                        featuredisos: featuredIsoObjs,
                        communityisos: communityIsoObjs,
                        myisos: myIsoObjs,
                        sharedisos: sharedIsoObjs
                    };
                }
                args.response.success({
                    hypervisor: {
                        idField: 'name',
                        nameField: 'name'
                    },
                    data: {
                        templates: templatesObj,
                        hypervisors: hypervisorObjs,
                        rootDiskControllers: controllerObjs
                    },
                    customHidden: function (args) {
                        if (selectedTemplateOrIso == 'select-template') {
                            return false; //show Root Disk Size field
                        } else { //selectedTemplateOrIso == 'select-iso'
                            return true;  //hide Root Disk Size field
                        }
                    }
                });
            },

            // Step 3: Service offering
            function (args) {
                selectedTemplateObj = null; //reset
                if (args.currentData["select-template"] == "select-template") {
                    if (featuredTemplateObjs != null && featuredTemplateObjs.length > 0) {
                        for (var i = 0; i < featuredTemplateObjs.length; i++) {
                            if (featuredTemplateObjs[i].id == args.currentData.templateid) {
                                selectedTemplateObj = featuredTemplateObjs[i];
                                break;
                            }
                        }
                    }
                    if (selectedTemplateObj == null) {
                        if (communityTemplateObjs != null && communityTemplateObjs.length > 0) {
                            for (var i = 0; i < communityTemplateObjs.length; i++) {
                                if (communityTemplateObjs[i].id == args.currentData.templateid) {
                                    selectedTemplateObj = communityTemplateObjs[i];
                                    break;
                                }
                            }
                        }
                    }
                    if (selectedTemplateObj == null) {
                        if (myTemplateObjs != null && myTemplateObjs.length > 0) {
                            for (var i = 0; i < myTemplateObjs.length; i++) {
                                if (myTemplateObjs[i].id == args.currentData.templateid) {
                                    selectedTemplateObj = myTemplateObjs[i];
                                    break;
                                }
                            }
                        }
                    }
                    if (selectedTemplateObj == null) {
                        if (sharedTemplateObjs != null && sharedTemplateObjs.length > 0) {
                            for (var i = 0; i < sharedTemplateObjs.length; i++) {
                                if (sharedTemplateObjs[i].id == args.currentData.templateid) {
                                    selectedTemplateObj = sharedTemplateObjs[i];
                                    break;
                                }
                            }
                        }
                    }
                    if (selectedTemplateObj == null) {
                        alert("unable to find matched template object");
                    } else {
                        selectedHypervisor = selectedTemplateObj.hypervisor;
                    }
                } else { //(args.currentData["select-template"] == "select-iso"
                    selectedHypervisor = args.currentData.hypervisorid;
                }

                // if the user is leveraging a template, then we can show custom IOPS, if applicable
                var canShowCustomIopsForServiceOffering = (args.currentData["select-template"] != "select-iso" ? true : false);


                // get serviceOfferingObjs
                $(window).removeData("cloudStack.module.instanceWizard.serviceOfferingObjs");
                $(window).trigger("cloudStack.module.instanceWizard.serviceOffering.dataProvider", {
                    context: args.context,
                    currentData: args.currentData
                });
                if ($(window).data("cloudStack.module.instanceWizard.serviceOfferingObjs") == undefined) {
                    $.ajax({
                        url: createURL("listServiceOfferings&issystem=false"),
                        dataType: "json",
                        async: false,
                        success: function (json) {
                            serviceOfferingObjs = json.listserviceofferingsresponse.serviceoffering;
                        }
                    });
                } else {
                    serviceOfferingObjs = $(window).data("cloudStack.module.instanceWizard.serviceOfferingObjs");
                }


                args.response.success({
                    canShowCustomIops: canShowCustomIopsForServiceOffering,
                    customFlag: 'iscustomized',
                    //customFlag: 'offerha', //for testing only
                    customIopsFlag: 'iscustomizediops',
                    data: {
                        serviceOfferings: serviceOfferingObjs
                    }
                });
            },

            // Step 4: Data disk offering
            function (args) {
                var isRequred = (args.currentData["select-template"] == "select-iso" ? true : false);
                $.ajax({
                    url: createURL("listDiskOfferings"),
                    dataType: "json",
                    async: true,
                    success: function (json) {
                        diskOfferingObjs = json.listdiskofferingsresponse.diskoffering;
                        args.response.success({
                            required: isRequred,
                            customFlag: 'iscustomized', // Field determines if custom slider is shown
                            customIopsDoFlag: 'iscustomizediops',
                            data: {
                                diskOfferings: diskOfferingObjs
                            },
                            multiDisk: false
                        });
                    }
                });
            },

            // Step 5: Affinity
            function (args) {
                $.ajax({
                    url: createURL('listAffinityGroups'),
                    success: function (json) {
                        var affinitygroups = json.listaffinitygroupsresponse.affinitygroup;
                        var data = {
                            affinityGroups: affinitygroups
                        };

                        if (selectedZoneObj.domainid != null && selectedZoneObj.affinitygroupid != null) {
                            var defaultAffinityGroup;
                            if (affinitygroups != null) {
                                for (var i = 0; i < affinitygroups.length; i++) {
                                    if (affinitygroups[i].id == selectedZoneObj.affinitygroupid) {
                                        defaultAffinityGroup = affinitygroups[i];
                                        break;
                                    }
                                }
                            }
                            $.extend(data, {
                                selectedObj: defaultAffinityGroup,
                                selectedObjNonEditable: true
                            });
                        }

                        args.response.success({
                            data: data
                        });
                    }
                });
            },

            // Step 6: Network
            function (args) {
                if (diskOfferingObjs != null && diskOfferingObjs.length > 0) {
                    for (var i = 0; i < diskOfferingObjs.length; i++) {
                        if (diskOfferingObjs[i].id == args.currentData.diskofferingid) {
                            selectedDiskOfferingObj = diskOfferingObjs[i];
                            break;
                        }
                    }
                }

                if (selectedZoneObj.networktype == "Advanced") { //Advanced zone. Show network list.
                    var $networkStep = $(".step.network:visible .nothing-to-select");
                    var $networkStepContainer = $('.step.network:visible');

                    if (args.initArgs.pluginForm != null && args.initArgs.pluginForm.name == "vpcTierInstanceWizard") { //from VPC Tier chart
                        step6ContainerType = 'nothing-to-select';
                        $networkStep.find("#from_instance_page_1").hide();
                        $networkStep.find("#from_instance_page_2").hide();
                        $networkStep.find("#from_vpc_tier").prepend("tier " + _s(args.context.networks[0].name));
                        $networkStep.find("#from_vpc_tier").show();
                    } else { //from Instance page
                        step6ContainerType = 'select-network';
                        $networkStep.find("#from_instance_page_1").show();
                        $networkStep.find("#from_instance_page_2").show();
                        $networkStep.find("#from_vpc_tier").text("");
                        $networkStep.find("#from_vpc_tier").hide();
                    }
                }

                //step6ContainerType = 'nothing-to-select'; //for testing only, comment it out before checking in
                if (step6ContainerType == 'select-network') {
                    var defaultNetworkArray = [],
                        optionalNetworkArray = [];
                    var networkData = {
                        zoneId: args.currentData.zoneid,
                        canusefordeploy: true
                    };

                    if (!(cloudStack.context.projects && cloudStack.context.projects[0])) {
                        networkData.domainid = g_domainid;
                        networkData.account = g_account;
                    }

                    var vpcObjs;

                    //listVPCs without account/domainid/listAll parameter will return only VPCs belonging to the current login. That's what should happen in Instances page's VM Wizard.
                    //i.e. If the current login is root-admin, do not show VPCs belonging to regular-user/domain-admin in Instances page's VM Wizard.
                    $.ajax({
                        url: createURL('listVPCs'),
                        async: false,
                        success: function (json) {
                            vpcObjs = json.listvpcsresponse.vpc ? json.listvpcsresponse.vpc : [];
                        }
                    });

                    var networkObjsToPopulate = [];
                    $.ajax({
                        url: createURL('listNetworks'),
                        data: networkData,
                        async: false,
                        success: function (json) {
                            networkObjs = json.listnetworksresponse.network ? json.listnetworksresponse.network : [];
                            if (networkObjs.length > 0) {
                                for (var i = 0; i < networkObjs.length; i++) {
                                    var networkObj = networkObjs[i];

                                    if (networkObj.vpcid) {
                                        networkObj._singleSelect = true;
                                    }

                                    networkObjsToPopulate.push(networkObj);
                                }
                            }
                        }
                    });

                    //In addition to the networks in the current zone, find networks in other zones that have stretchedL2subnet==true
                    //capability and show them on the UI
                    var allOtherAdvancedZones = [];
                    $.ajax({
                        url: createURL('listZones'),
                        dataType: "json",
                        async: false,
                        success: function (json) {
                            var result = $.grep(json.listzonesresponse.zone, function (zone) {
                                return (zone.networktype == 'Advanced');
                            });
                            $(result).each(function () {
                                if (selectedZoneObj.id != this.id)
                                    allOtherAdvancedZones.push(this);
                            });
                        }
                    });
                    if (allOtherAdvancedZones.length > 0) {
                        for (var i = 0; i < allOtherAdvancedZones.length; i++) {
                            var networkDataForZone = {
                                zoneId: allOtherAdvancedZones[i].id,
                                canusefordeploy: true
                            };
                            $.ajax({
                                url: createURL('listNetworks'),
                                data: networkDataForZone,
                                async: false,
                                success: function (json) {
                                    var networksInThisZone = json.listnetworksresponse.network ? json.listnetworksresponse.network : [];
                                    if (networksInThisZone.length > 0) {
                                        for (var i = 0; i < networksInThisZone.length; i++) {
                                            if (networksInThisZone[i].strechedl2subnet) {
                                                networkObjsToPopulate.push(networksInThisZone[i]);
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    }


                    // get networkObjsToPopulate
                    $(window).removeData("cloudStack.module.instanceWizard.networkObjs");
                    $(window).trigger("cloudStack.module.instanceWizard.network.dataProvider", {
                        context: args.context,
                        currentData: args.currentData,
                        networkObjsToPopulate: networkObjsToPopulate
                    });
                    if ($(window).data("cloudStack.module.instanceWizard.networkObjs") == undefined) {
                        //do nothing
                    } else {
                        networkObjsToPopulate = $(window).data("cloudStack.module.instanceWizard.networkObjs"); //override networkObjsToPopulate
                    }


                    $.ajax({
                        url: createURL("listNetworkOfferings"),
                        dataType: "json",
                        data: {
                            forvpc: false,
                            zoneid: args.currentData.zoneid,
                            guestiptype: 'Isolated',
                            supportedServices: 'SourceNat',
                            specifyvlan: false,
                            state: 'Enabled'
                        },
                        async: false,
                        success: function (json) {
                            networkOfferingObjs = json.listnetworkofferingsresponse.networkoffering;
                        }
                    });
                    //get network offerings (end)    ***

                    $networkStepContainer.removeClass('repeat');
                    $networkStepContainer.find('.select-network').removeClass('no-add-network');

                    args.response.success({
                        type: 'select-network',
                        data: {
                            networkObjs: networkObjsToPopulate,
                            networkOfferings: networkOfferingObjs,
                            vpcs: vpcObjs
                        }
                    });
                } else if (step6ContainerType == 'nothing-to-select') {
                    args.response.success({
                        type: 'nothing-to-select',
                        data: {
                            networkObjs: [],
                            networkOfferings: [],
                            vpcs: []
                        }
                    });
                }

            },

            // Step 7: SSH Key Pairs
            function (args) {
                $.ajax({
                    url: createURL('listSSHKeyPairs'),
                    success: function (json) {
                        var sshkeypair = json.listsshkeypairsresponse.sshkeypair;
                        args.response.success({
                            data: {
                                sshkeyPairs: sshkeypair
                            }
                        });
                    }
                });
            },

            // Step 8: Review
            function (args) {
                return false;
            }
        ],
        action: function (args) {
            // Create a new VM!!!!
            var deployVmData = {};

            //step 1 : select zone
            $.extend(deployVmData, {
                zoneid: args.data.zoneid
            });

            //step 2: select template
            $.extend(deployVmData, {
                templateid: args.data.templateid
            });

            $.extend(deployVmData, {
                hypervisor: selectedHypervisor
            });

            if (args.$wizard.find('input[name=rootDiskSize]').parent().css('display') != 'none') {
                if (args.$wizard.find('input[name=rootDiskSize]').val().length > 0) {
                    $.extend(deployVmData, {
                        rootdisksize: args.$wizard.find('input[name=rootDiskSize]').val()
                    });
                }
            }

            if (args.$wizard.find('select[name=rootdiskcontroller]').parent().css('display') != 'none') {
                if (args.$wizard.find('select[name=rootdiskcontroller]').val().length > 0) {
                    $.extend(deployVmData, {
                        diskcontroller: args.$wizard.find('select[name=rootdiskcontroller]').val()
                    });
                }
            }

            //step 3: select service offering
            $.extend(deployVmData, {
                serviceofferingid: args.data.serviceofferingid
            });

            if (args.$wizard.find('input[name=compute-cpu-cores]').parent().parent().css('display') != 'none') {
                if (args.$wizard.find('input[name=compute-cpu-cores]').val().length > 0) {
                    $.extend(deployVmData, {
                        'details[0].cpuNumber': args.$wizard.find('input[name=compute-cpu-cores]').val()
                    });
                }
                if (args.$wizard.find('input[name=compute-memory]').val().length > 0) {
                    $.extend(deployVmData, {
                        'details[0].memory': args.$wizard.find('input[name=compute-memory]').val()
                    });
                }
            }

            if (args.$wizard.find('input[name=disk-min-iops]').parent().parent().css('display') != 'none') {
                if (args.$wizard.find('input[name=disk-min-iops]').val().length > 0) {
                    $.extend(deployVmData, {
                        'details[0].minIops': args.$wizard.find('input[name=disk-min-iops]').val()
                    });
                }
                if (args.$wizard.find('input[name=disk-max-iops]').val().length > 0) {
                    $.extend(deployVmData, {
                        'details[0].maxIops': args.$wizard.find('input[name=disk-max-iops]').val()
                    });
                }
            }

            //step 4: select disk offering
            if (args.data.diskofferingid != null && args.data.diskofferingid != "0") {
                $.extend(deployVmData, {
                    diskofferingid: args.data.diskofferingid
                });

                if (selectedDiskOfferingObj.iscustomized == true) {
                    $.extend(deployVmData, {
                        size: args.data.size
                    });
                }

                if (selectedDiskOfferingObj.iscustomizediops == true) {
                    if (args.$wizard.find('input[name=disk-min-iops-do]').val().length > 0) {
                        $.extend(deployVmData, {
                            'details[0].minIopsDo': args.$wizard.find('input[name=disk-min-iops-do]').val()
                        });
                    }

                    if (args.$wizard.find('input[name=disk-max-iops-do]').val().length > 0) {
                        $.extend(deployVmData, {
                            'details[0].maxIopsDo': args.$wizard.find('input[name=disk-max-iops-do]').val()
                        });
                    }
                }
            }

            //step 5: select an affinity group
            var checkedAffinityGroupIdArray;
            if (typeof(args.data["affinity-groups"]) == "object" && args.data["affinity-groups"].length != null) { //args.data["affinity-groups"] is an array of string, e.g. ["2375f8cc-8a73-4b8d-9b26-50885a25ffe0", "27c60d2a-de7f-4bb7-96e5-a602cec681df","c6301d77-99b5-4e8a-85e2-3ea2ab31c342"],
                checkedAffinityGroupIdArray = args.data["affinity-groups"];
            } else if (typeof(args.data["affinity-groups"]) == "string" && args.data["affinity-groups"].length > 0) { //args.data["affinity-groups"] is a string, e.g. "2375f8cc-8a73-4b8d-9b26-50885a25ffe0"
                checkedAffinityGroupIdArray = [];
                checkedAffinityGroupIdArray.push(args.data["affinity-groups"]);
            } else { // typeof(args.data["affinity-groups"]) == null
                checkedAffinityGroupIdArray = [];
            }

            if (checkedAffinityGroupIdArray.length > 0) {
                $.extend(deployVmData, {
                    affinitygroupids: checkedAffinityGroupIdArray.join(",")
                });
            }

            //step 6: select network
            if (step6ContainerType == 'select-network') {
                var array2 = [];
                var array3 = [];
                var defaultNetworkId = args.data.defaultNetwork; //args.data.defaultNetwork might be equal to string "new-network" or a network ID

                var checkedNetworkIdArray;
                if (typeof(args.data["my-networks"]) == "object" && args.data["my-networks"].length != null) { //args.data["my-networks"] is an array of string, e.g. ["203", "202"],
                    checkedNetworkIdArray = args.data["my-networks"];
                } else if (typeof(args.data["my-networks"]) == "string" && args.data["my-networks"].length > 0) { //args.data["my-networks"] is a string, e.g. "202"
                    checkedNetworkIdArray = [];
                    checkedNetworkIdArray.push(args.data["my-networks"]);
                } else { // typeof(args.data["my-networks"]) == null
                    checkedNetworkIdArray = [];
                }

                //create new network starts here
                if (args.data["new-network"] == "create-new-network") {
                    var isCreateNetworkSuccessful = true;

                    var createNetworkData = {
                        networkOfferingId: args.data["new-network-networkofferingid"],
                        name: args.data["new-network-name"],
                        displayText: args.data["new-network-name"],
                        zoneId: selectedZoneObj.id
                    };

                    $.ajax({
                        url: createURL('createNetwork'),
                        data: createNetworkData,
                        async: false,
                        success: function (json) {
                            newNetwork = json.createnetworkresponse.network;
                            checkedNetworkIdArray.push(newNetwork.id);
                            if (defaultNetworkId == "new-network")
                                defaultNetworkId = newNetwork.id;
                        },
                        error: function (XMLHttpResponse) {
                            isCreateNetworkSuccessful = false;
                            var errorMsg = "Failed to create new network, unable to proceed to deploy VM. Error: " + parseXMLHttpResponse(XMLHttpResponse);
                            //alert(errorMsg);
                            args.response.error(errorMsg); //args.response.error(errorMsg) here doesn't show errorMsg. Waiting for Brian to fix it. use alert(errorMsg) to show errorMsg for now.
                        }
                    });
                    if (isCreateNetworkSuccessful == false)
                        return;
                }
                //create new network ends here


                if (defaultNetworkId == null) {
                    cloudStack.dialog.notice({
                        message: "Please select a default network in Network step."
                    });
                    return;
                }

                if (checkedNetworkIdArray.length > 0) {
                    for (var i = 0; i < checkedNetworkIdArray.length; i++) {
                        if (checkedNetworkIdArray[i] == defaultNetworkId) {
                            array2.unshift(defaultNetworkId);

                            var ipToNetwork = {
                                networkid: defaultNetworkId
                            };
                            if (args.data["new-network"] == "create-new-network") {
                                if (args.data['new-network-ip'] != null && args.data['new-network-ip'].length > 0) {
                                    $.extend(ipToNetwork, {
                                        ip: args.data['new-network-ip']
                                    });
                                }
                            } else {
                                if (args.data["my-network-ips"][i] != null && args.data["my-network-ips"][i].length > 0) {
                                    $.extend(ipToNetwork, {
                                        ip: args.data["my-network-ips"][i]
                                    });
                                }
                            }
                            array3.unshift(ipToNetwork);

                        } else {
                            array2.push(checkedNetworkIdArray[i]);

                            var ipToNetwork = {
                                networkid: checkedNetworkIdArray[i]
                            };
                            if (args.data["my-network-ips"][i] != null && args.data["my-network-ips"][i].length > 0) {
                                $.extend(ipToNetwork, {
                                    ip: args.data["my-network-ips"][i]
                                });
                            }
                            array3.push(ipToNetwork);
                        }
                    }
                }

                //deployVmData.push("&networkIds=" + array2.join(","));  //ipToNetworkMap can't be specified along with networkIds or ipAddress

                for (var k = 0; k < array3.length; k++) {
                    deployVmData["iptonetworklist[" + k + "].networkid"] = array3[k].networkid;
                    if (array3[k].ip != undefined && array3[k].ip.length > 0) {
                        deployVmData["iptonetworklist[" + k + "].ip"] = array3[k].ip;
                    }
                }

            } else if (step6ContainerType == 'nothing-to-select') {
                if ("vpc" in args.context) { //from VPC tier
                    deployVmData["iptonetworklist[0].networkid"] = args.context.networks[0].id;
                    if (args.data["vpc-specify-ip"] != undefined && args.data["vpc-specify-ip"].length > 0) {
                        deployVmData["iptonetworklist[0].ip"] = args.data["vpc-specify-ip"];
                    }

                    $.extend(deployVmData, {
                        domainid: args.context.vpc[0].domainid
                    });
                    if (args.context.vpc[0].account != null) {
                        $.extend(deployVmData, {
                            account: args.context.vpc[0].account
                        });
                    } else if (args.context.vpc[0].projectid != null) {
                        $.extend(deployVmData, {
                            projectid: args.context.vpc[0].projectid
                        });
                    }
                }
            }

            //step 4: select ssh key pair
            if (args.data.sshkeypair != null && args.data.sshkeypair.length > 0) {
                $.extend(deployVmData, {
                    keypair: args.data.sshkeypair
                });
            }

            var displayname = args.data.displayname;
            if (displayname != null && displayname.length > 0) {
                $.extend(deployVmData, {
                    displayname: displayname
                });
                $.extend(deployVmData, {
                    name: displayname
                });
            }

            var group = args.data.groupname;
            if (group != null && group.length > 0) {
                $.extend(deployVmData, {
                    group: group
                });
            }

            var keyboard = args.data.keyboardLanguage;
            if (keyboard != null && keyboard.length > 0) {  //when blank option (default option) is selected => args.data.keyboardLanguage == ""
                $.extend(deployVmData, {
                    keyboard: keyboard
                });
            }

            if (g_hostid != null) {
                $.extend(deployVmData, {
                    hostid: g_hostid
                });
            }

            var userdata = args.data.userdata;
            if (userdata != null && userdata.length > 0) {
                $.extend(deployVmData, {
                    userdata: todb(btoa(userdata))
                });
            }

            $(window).trigger('cloudStack.deployVirtualMachine', {
                deployVmData: deployVmData,
                formData: args.data
            });

            $.ajax({
                url: createURL('deployVirtualMachine'),
                data: deployVmData,
                success: function (json) {
                    var jid = json.deployvirtualmachineresponse.jobid;
                    var vmid = json.deployvirtualmachineresponse.id;
                    args.response.success({
                        _custom: {
                            jobId: jid,
                            getUpdatedItem: function (json) {
                                var item = json.queryasyncjobresultresponse.jobresult.virtualmachine;
                                if (item.password != null)
                                    cloudStack.dialog.notice({
                                        message: "Password of new VM " + item.displayname + " is  " + item.password
                                    });
                                return item;
                            },
                            getActionFilter: function () {
                                return cloudStack.actionFilter.vmActionFilter;
                            },
                            getUpdatedItemWhenAsyncJobFails: function () {
                                var item;
                                $.ajax({
                                    url: createURL("listVirtualMachines&id=" + vmid),
                                    dataType: "json",
                                    async: false,
                                    success: function (json) {
                                        item = json.listvirtualmachinesresponse.virtualmachine[0];
                                    }
                                });
                                return item;
                            }
                        }
                    });
                },
                error: function (XMLHttpResponse) {
                    args.response.error(parseXMLHttpResponse(XMLHttpResponse)); //wait for Brian to implement
                }
            });
        }
    };
}(jQuery, cloudStack));
