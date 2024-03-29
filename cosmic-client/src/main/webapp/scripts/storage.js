(function (cloudStack) {

    var diskofferingObjs, selectedDiskOfferingObj;

    cloudStack.sections.storage = {
        title: 'label.storage',
        id: 'storage',
        sectionSelect: {
            label: 'label.select-view'
        },
        sections: {
            /**
             * Volumes
             */
            volumes: {
                type: 'select',
                title: 'label.volumes',
                listView: {
                    id: 'volumes',
                    label: 'label.volumes',
                    preFilter: function (args) {
                        var hiddenFields = [];
                        if (isAdmin() != true)
                            hiddenFields.push('hypervisor');
                        return hiddenFields;
                    },
                    fields: {
                        name: {
                            label: 'label.name'
                        },
                        type: {
                            label: 'label.type'
                        },
                        format: {
                            label: 'label.format'
                        },
                        diskcontroller: {
                            label: 'label.disk.controller'
                        },
                        deviceid: {
                            label: 'label.device.id'
                        },
                        state: {
                            label: 'label.state',
                            indicator: {
                                'Ready': 'on',
                                'Migrating': 'transition',
                                'Creating': 'off',
                                'Expunging': 'off',
                                'Allocated': 'warning'
                            }
                        },
                        vmdisplayname: {
                            label: 'label.vm.display.name'
                        }
                    },

                    // List view actions
                    actions: {
                        // Add volume
                        add: {
                            label: 'label.add',

                            preFilter: function (args) {
                                return !args.context.instances;
                            },

                            messages: {
                                confirm: function (args) {
                                    return 'message.add.volume';
                                },
                                notification: function (args) {
                                    return 'label.add.volume';
                                }
                            },

                            createForm: {
                                title: 'label.add.volume',
                                desc: 'message.add.volume',
                                fields: {
                                    name: {
                                        docID: 'helpVolumeName',
                                        label: 'label.name'
                                    },
                                    availabilityZone: {
                                        label: 'label.availability.zone',
                                        docID: 'helpVolumeAvailabilityZone',
                                        select: function (args) {
                                            $.ajax({
                                                url: createURL("listZones&available=true"),
                                                dataType: "json",
                                                async: true,
                                                success: function (json) {
                                                    var zoneObjs = json.listzonesresponse.zone;
                                                    args.response.success({
                                                        descriptionField: 'name',
                                                        data: zoneObjs
                                                    });
                                                }
                                            });
                                        }
                                    },
                                    diskOffering: {
                                        label: 'label.disk.offering',
                                        docID: 'helpVolumeDiskOffering',
                                        select: function (args) {
                                            $.ajax({
                                                url: createURL("listDiskOfferings"),
                                                dataType: "json",
                                                async: false,
                                                success: function (json) {
                                                    diskofferingObjs = json.listdiskofferingsresponse.diskoffering;
                                                    var items = [];
                                                    $(diskofferingObjs).each(function () {
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

                                            args.$select.change(function () {
                                                var diskOfferingId = $(this).val();
                                                $(diskofferingObjs).each(function () {
                                                    if (this.id == diskOfferingId) {
                                                        selectedDiskOfferingObj = this;
                                                        return false; //break the $.each() loop
                                                    }
                                                });
                                                if (selectedDiskOfferingObj == null)
                                                    return;

                                                var $form = $(this).closest('form');
                                                var $diskSize = $form.find('.form-item[rel=diskSize]');
                                                if (selectedDiskOfferingObj.iscustomized == true) {
                                                    $diskSize.css('display', 'inline-block');
                                                } else {
                                                    $diskSize.hide();
                                                }
                                                var $minIops = $form.find('.form-item[rel=minIops]');
                                                var $maxIops = $form.find('.form-item[rel=maxIops]');
                                                if (selectedDiskOfferingObj.iscustomizediops == true) {
                                                    $minIops.css('display', 'inline-block');
                                                    $maxIops.css('display', 'inline-block');
                                                } else {
                                                    $minIops.hide();
                                                    $maxIops.hide();
                                                }
                                            });
                                        }
                                    }

                                    ,
                                    diskSize: {
                                        label: 'label.disk.size.gb',
                                        docID: 'helpVolumeSizeGb',
                                        validation: {
                                            required: true,
                                            number: true
                                        },
                                        isHidden: false
                                    },

                                    diskController: {
                                        label: 'label.disk.controller',
                                        docID: 'helpDiskController',
                                        select: function (args) {
                                            var items = [];
                                            items.push({
                                                id: 'VIRTIO',
                                                description: 'VirtIO (virtio-blk)'
                                            });
                                            items.push({
                                                id: 'SCSI',
                                                description: 'VirtIO SCSI (virtio-scsi)'
                                            });
                                            items.push({
                                                id: 'IDE',
                                                description: 'IDE (Legacy)'
                                            });
                                            args.response.success({
                                                data: items
                                            });
                                        }
                                    },
                                    format: {
                                        label: 'label.format',
                                        docID: 'helpVolumeFormat',
                                        select: function (args) {
                                            var items = [];
                                            items.push({
                                                id: 'QCOW2',
                                                description: 'QCOW2 (default and recommended)'
                                            });
                                            items.push({
                                                id: 'RAW',
                                                description: 'RAW'
                                            });
                                            args.response.success({
                                                data: items
                                            });
                                        }
                                    },
                                    minIops: {
                                        label: 'label.disk.iops.min',
                                        validation: {
                                            required: false,
                                            number: true
                                        },
                                        isHidden: true
                                    },

                                    maxIops: {
                                        label: 'label.disk.iops.max',
                                        validation: {
                                            required: false,
                                            number: true
                                        },
                                        isHidden: true
                                    }

                                }
                            },

                            action: function (args) {
                                var data = {
                                    name: args.data.name,
                                    zoneId: args.data.availabilityZone,
                                    diskOfferingId: args.data.diskOffering
                                };

                                if (args.data.diskController != "") {
                                    $.extend(data, {
                                        diskcontroller: args.data.diskController
                                    });
                                }

                                if (args.data.format != "") {
                                    $.extend(data, {
                                        format: args.data.format
                                    });
                                }

                                if (selectedDiskOfferingObj.iscustomized == true) {
                                    $.extend(data, {
                                        size: args.data.diskSize
                                    });
                                }

                                if (selectedDiskOfferingObj.iscustomizediops == true) {
                                    if (args.data.minIops != "" && args.data.minIops > 0) {
                                        $.extend(data, {
                                            miniops: args.data.minIops
                                        });
                                    }

                                    if (args.data.maxIops != "" && args.data.maxIops > 0) {
                                        $.extend(data, {
                                            maxiops: args.data.maxIops
                                        });
                                    }
                                }

                                $.ajax({
                                    url: createURL('createVolume'),
                                    data: data,
                                    success: function (json) {
                                        var jid = json.createvolumeresponse.jobid;
                                        args.response.success({
                                            _custom: {
                                                jobId: jid,
                                                getUpdatedItem: function (json) {
                                                    return json.queryasyncjobresultresponse.jobresult.volume;
                                                },
                                                getActionFilter: function () {
                                                    return volumeActionfilter;
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

                        viewMetrics: {
                            label: 'label.metrics',
                            isHeader: true,
                            addRow: false,
                            action: {
                                custom: cloudStack.uiCustom.metricsView({resource: 'volumes'})
                            },
                            messages: {
                                notification: function (args) {
                                    return 'label.metrics';
                                }
                            }
                        },

                        uploadVolume: {
                            isHeader: true,
                            label: 'label.upload',
                            preFilter: function (args) {
                                return !args.context.instances;
                            },
                            messages: {
                                notification: function () {
                                    return 'label.upload.volume.from.url';
                                }
                            },
                            createForm: {
                                title: 'label.upload.volume.from.url',
                                fields: {
                                    url: {
                                        label: 'label.url',
                                        docID: 'helpUploadVolumeURL',
                                        validation: {
                                            required: true
                                        }
                                    },
                                    name: {
                                        label: 'label.name',
                                        validation: {
                                            required: true
                                        },
                                        docID: 'helpUploadVolumeName'
                                    },
                                    availabilityZone: {
                                        label: 'label.availability.zone',
                                        docID: 'helpUploadVolumeZone',
                                        select: function (args) {
                                            $.ajax({
                                                url: createURL("listZones&available=true"),
                                                dataType: "json",
                                                async: true,
                                                success: function (json) {
                                                    var zoneObjs = json.listzonesresponse.zone;
                                                    args.response.success({
                                                        descriptionField: 'name',
                                                        data: zoneObjs
                                                    });
                                                }
                                            });
                                        }
                                    },
                                    format: {
                                        label: 'label.format',
                                        docID: 'helpUploadVolumeFormat',
                                        select: function (args) {
                                            var items = [];
                                            items.push({
                                                id: 'RAW',
                                                description: 'RAW'
                                            });
                                            items.push({
                                                id: 'VHD',
                                                description: 'VHD'
                                            });
                                            items.push({
                                                id: 'VHDX',
                                                description: 'VHDX'
                                            });
                                            items.push({
                                                id: 'OVA',
                                                description: 'OVA'
                                            });
                                            items.push({
                                                id: 'QCOW2',
                                                description: 'QCOW2'
                                            });
                                            args.response.success({
                                                data: items
                                            });
                                        }

                                    },
                                    diskOffering: {
                                        label: 'label.custom.disk.offering',
                                        docID: 'helpVolumeDiskOffering',
                                        select: function (args) {
                                            var diskofferingObjs;
                                            $.ajax({
                                                url: createURL("listDiskOfferings"),
                                                dataType: "json",
                                                async: false,
                                                success: function (json) {
                                                    diskofferingObjs = json.listdiskofferingsresponse.diskoffering;
                                                    var items = [{
                                                        id: '',
                                                        description: ''
                                                    }];
                                                    $(diskofferingObjs).each(function () {
                                                        if (this.iscustomized == true) {
                                                            items.push({
                                                                id: this.id,
                                                                description: this.displaytext
                                                            });
                                                        }
                                                    });
                                                    args.response.success({
                                                        data: items
                                                    });
                                                }
                                            });
                                        }
                                    },
                                    diskOffering: {
                                        label: 'label.custom.disk.offering',
                                        docID: 'helpVolumeDiskOffering',
                                        select: function (args) {
                                            var diskofferingObjs;
                                            $.ajax({
                                                url: createURL("listDiskOfferings"),
                                                dataType: "json",
                                                async: false,
                                                success: function (json) {
                                                    diskofferingObjs = json.listdiskofferingsresponse.diskoffering;
                                                    var items = [{
                                                        id: '',
                                                        description: ''
                                                    }];
                                                    $(diskofferingObjs).each(function () {
                                                        if (this.iscustomized == true) {
                                                            items.push({
                                                                id: this.id,
                                                                description: this.displaytext
                                                            });
                                                        }
                                                    });
                                                    args.response.success({
                                                        data: items
                                                    });
                                                }
                                            });
                                        }
                                    },
                                    checksum: {
                                        docID: 'helpUploadVolumeChecksum',
                                        label: 'label.md5.checksum'
                                    }
                                }
                            },

                            action: function (args) {
                                var data = {
                                    name: args.data.name,
                                    zoneId: args.data.availabilityZone,
                                    format: args.data.format,
                                    url: args.data.url
                                };

                                if (args.data.diskOffering != '' && args.data.diskOffering.length > 0) {
                                    $.extend(data, {
                                        diskofferingid: args.data.diskOffering
                                    });
                                }

                                if (args.data.checksum != null && args.data.checksum.length > 0) {
                                    $.extend(data, {
                                        checksum: args.data.checksum
                                    });
                                }

                                $.ajax({
                                    url: createURL('uploadVolume'),
                                    data: data,
                                    success: function (json) {
                                        var jid = json.uploadvolumeresponse.jobid;
                                        args.response.success({
                                            _custom: {
                                                jobId: jid,
                                                getUpdatedItem: function (json) {
                                                    return json.queryasyncjobresultresponse.jobresult.volume;
                                                },
                                                getActionFilter: function () {
                                                    return volumeActionfilter;
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

                        uploadVolumefromLocal: {
                            isHeader: false,
                            label: 'label.upload.from.local',
                            preFilter: function (args) {
                                return !args.context.instances;
                            },
                            messages: {
                                notification: function () {
                                    return 'label.upload.volume.from.local';
                                }
                            },
                            createForm: {
                                title: 'label.upload.volume.from.local',
                                fileUpload: {
                                    getURL: function (args) {
                                        args.data = args.formData;

                                        var data = {
                                            name: args.data.name,
                                            zoneId: args.data.availabilityZone,
                                            format: args.data.format,
                                            url: args.data.url
                                        };

                                        if (args.data.checksum != null && args.data.checksum.length > 0) {
                                            $.extend(data, {
                                                checksum: args.data.checksum
                                            });
                                        }

                                        $.ajax({
                                            url: createURL('getUploadParamsForVolume'),
                                            data: data,
                                            async: false,
                                            success: function (json) {
                                                var uploadparams = json.postuploadvolumeresponse.getuploadparams; //son.postuploadvolumeresponse.getuploadparams is an object, not an array of object.
                                                var volumeId = uploadparams.id;

                                                args.response.success({
                                                    url: uploadparams.postURL,
                                                    ajaxPost: true,
                                                    data: {
                                                        'X-signature': uploadparams.signature,
                                                        'X-expires': uploadparams.expires,
                                                        'X-metadata': uploadparams.metadata
                                                    }
                                                });
                                            }
                                        });
                                    },
                                    postUpload: function (args) {
                                        if (args.error) {
                                            args.response.error(args.errorMsg);
                                        } else {
                                            cloudStack.dialog.notice({
                                                message: "This volume file has been uploaded. Please check its status at Stroage menu > Volumes > " + args.data.name + " > Status field."
                                            });
                                            args.response.success();
                                        }
                                    }
                                },
                                fields: {
                                    volumeFileUpload: {
                                        label: 'label.local.file',
                                        isFileUpload: true,
                                        validation: {
                                            required: true
                                        }
                                    },
                                    name: {
                                        label: 'label.name',
                                        validation: {
                                            required: true
                                        },
                                        docID: 'helpUploadVolumeName'
                                    },
                                    availabilityZone: {
                                        label: 'label.availability.zone',
                                        docID: 'helpUploadVolumeZone',
                                        select: function (args) {
                                            $.ajax({
                                                url: createURL("listZones&available=true"),
                                                dataType: "json",
                                                async: true,
                                                success: function (json) {
                                                    var zoneObjs = json.listzonesresponse.zone;
                                                    args.response.success({
                                                        descriptionField: 'name',
                                                        data: zoneObjs
                                                    });
                                                }
                                            });
                                        }
                                    },
                                    format: {
                                        label: 'label.format',
                                        docID: 'helpUploadVolumeFormat',
                                        select: function (args) {
                                            var items = [];
                                            items.push({
                                                id: 'RAW',
                                                description: 'RAW'
                                            });
                                            items.push({
                                                id: 'VHD',
                                                description: 'VHD'
                                            });
                                            items.push({
                                                id: 'VHDX',
                                                description: 'VHDX'
                                            });
                                            items.push({
                                                id: 'OVA',
                                                description: 'OVA'
                                            });
                                            items.push({
                                                id: 'QCOW2',
                                                description: 'QCOW2'
                                            });
                                            args.response.success({
                                                data: items
                                            });
                                        }
                                    },
                                    checksum: {
                                        docID: 'helpUploadVolumeChecksum',
                                        label: 'label.md5.checksum'
                                    }
                                }
                            },

                            action: function (args) {
                                return; //createForm.fileUpload.getURL() has executed the whole action. Therefore, nothing needs to be done here.
                            },

                            notification: {
                                poll: pollAsyncJobResult
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
                                    data: {},
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

                    dataProvider: function (args) {
                        var data = {};
                        listViewDataProvider(args, data);

                        if (args.context != null) {
                            if ("instances" in args.context) {
                                $.extend(data, {
                                    virtualMachineId: args.context.instances[0].id
                                });
                            }
                            if ("primarystorages" in args.context) {
                                $.extend(data, {
                                    storageid: args.context.primarystorages[0].id
                                });
                            }
                        }

                        $.ajax({
                            url: createURL('listVolumes'),
                            data: data,
                            success: function (json) {
                                var items = json.listvolumesresponse.volume;
                                args.response.success({
                                    actionFilter: volumeActionfilter,
                                    data: items
                                });
                            },
                            error: function (XMLHttpResponse) {
                                cloudStack.dialog.notice({
                                    message: parseXMLHttpResponse(XMLHttpResponse)
                                });
                                args.response.error();
                            }
                        });
                    },

                    detailView: {
                        name: 'label.volume.details',
                        viewAll: {
                            path: 'storage.snapshots',
                            label: 'label.snapshots'
                        },
                        actions: {

                            migrateVolume: {
                                label: 'label.migrate.volume',
                                messages: {
                                    notification: function (args) {
                                        return 'label.volume.migrated';
                                    }
                                },
                                action: {
                                    custom: cloudStack.uiCustom.migrate({
                                        listView: {
                                            label: 'label.migrate.volume',
                                            listView: {
                                                id: 'availableStorage',
                                                fields: {
                                                    availableStorageName: {
                                                        label: 'label.storage.pool'
                                                    },
                                                    availableStorageSuitable: {
                                                        label: 'label.suitability',
                                                        indicator: {
                                                            'Suitable': 'suitable',
                                                            'Not Suitable': 'notsuitable',
                                                            '': 'off'
                                                        }
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
                                                        url: createURL("listStoragePools&zoneid=" + args.context.volumes[0].zoneid),
                                                        dataType: "json",
                                                        async: true,
                                                        data: data,
                                                        success: function (json) {
                                                            var items = [];
                                                            if (json.liststoragepoolsresponse.storagepool != undefined) {
                                                                var pools = json.liststoragepoolsresponse.storagepool;
                                                                pools.sort(function (a, b) {
                                                                    return a.name.localeCompare(b.name);
                                                                });
                                                                $(pools).each(function () {
                                                                    var suitability = "";
                                                                    if (this.suitableformigration != undefined) {
                                                                        suitability = this.suitableformigration ? "Suitable" : "Not Suitable";
                                                                    }
                                                                    items.push({
                                                                        id: this.id,
                                                                        availableStorageName: this.name,
                                                                        availableStorageSuitable: suitability
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
                                                url: createURL("migrateVolume&livemigrate=true&storageid=" + args.context.selectedHost[0].id + "&volumeid=" + args.context.volumes[0].id),
                                                dataType: "json",
                                                async: true,
                                                success: function (json) {
                                                    var jid = json.migratevolumeresponse.jobid;
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

                            takeSnapshot: {
                                label: 'label.action.take.snapshot',
                                messages: {
                                    notification: function (args) {
                                        return 'label.action.take.snapshot';
                                    }
                                },
                                createForm: {
                                    title: 'label.action.take.snapshot',
                                    desc: 'message.action.take.snapshot',
                                    fields: {
                                        name: {
                                            label: 'label.name'
                                        }
                                    }
                                },
                                action: function (args) {
                                    var data = {
                                        volumeId: args.context.volumes[0].id,
                                    };
                                    if (args.data.name != null && args.data.name.length > 0) {
                                        $.extend(data, {
                                            name: args.data.name
                                        });
                                    }
                                    $.ajax({
                                        url: createURL("createSnapshot"),
                                        data: data,
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var jid = json.createsnapshotresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid //take snapshot from a volume doesn't change any property in this volume. So, don't need to specify getUpdatedItem() to return updated volume. Besides, createSnapshot API doesn't return updated volume.
                                                }
                                            });
                                        }
                                    });
                                },
                                notification: {
                                    poll: pollAsyncJobResult
                                }
                            },
                            editDisk: {
                                addRow: 'false',
                                label: 'label.action.edit.disk',
                                messages: {
                                    confirm: function (args) {
                                        return 'message.confirm.edit.volume';
                                    },
                                    notification: function (args) {
                                        return 'label.action.edit.disk';
                                    }
                                },
                                createForm: {
                                    title: 'label.action.edit.disk',
                                    desc: 'label.action.edit.disk',
                                    fields: {
                                        name: {
                                            label: 'label.name',
                                            defaultValue: function(args) { return args.volumes[0].name }
                                        },
                                        diskController: {
                                            label: 'label.disk.controller',
                                            docID: 'helpDiskController',
                                            select: function (args) {
                                                var items = [];
                                                items.push({
                                                    id: 'SCSI',
                                                    description: 'VirtIO SCSI (virtio-scsi)'
                                                });
                                                items.push({
                                                    id: 'VIRTIO',
                                                    description: 'VirtIO (virtio-blk)',
                                                    selected: true
                                                });
                                                items.push({
                                                    id: 'IDE',
                                                    description: 'IDE (Legacy)'
                                                });
                                                args.response.success({
                                                    data: items
                                                });
                                            }
                                        }
                                    }
                                },
                                action: function (args) {
                                    var data = {
                                        id: args.context.volumes[0].id,
                                        diskcontroller: args.data.diskController,
                                        name: args.data.name
                                    };

                                    $.ajax({
                                        url: createURL("updateVolume"),
                                        data: data,
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var jid = json.updatevolumeresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getUpdatedItem: function (json) {
                                                        return json.queryasyncjobresultresponse.jobresult.volume;
                                                    },
                                                    getActionFilter: function () {
                                                        return volumeActionfilter;
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
                            attachDisk: {
                                addRow: 'false',
                                label: 'label.action.attach.disk',
                                messages: {
                                    confirm: function (args) {
                                        return 'message.confirm.attach.disk';
                                    },
                                    notification: function (args) {
                                        return 'label.action.attach.disk';
                                    }
                                },
                                createForm: {
                                    title: 'label.action.attach.disk',
                                    desc: 'label.action.attach.disk',
                                    fields: {
                                        virtualMachineId: {
                                            label: 'label.instance',
                                            select: function (args) {
                                                var zoneid = args.context.volumes[0].zoneid;
                                                var items = [];
                                                var data;

                                                if (!args.context.projects) {
                                                    data = {
                                                        zoneid: zoneid,
                                                        domainid: args.context.volumes[0].domainid,
                                                        account: args.context.volumes[0].account
                                                    };
                                                } else {
                                                    data = {
                                                        zoneid: zoneid,
                                                        projectid: args.context.projects[0].id
                                                    };
                                                }

                                                if (args.context.volumes[0].hypervisor != null && args.context.volumes[0].hypervisor.length > 0 && args.context.volumes[0].hypervisor != 'None') {
                                                    data = $.extend(data, {
                                                        hypervisor: args.context.volumes[0].hypervisor
                                                    });
                                                }

                                                $(['Running', 'Stopped']).each(function (_ignore, value) {
                                                    $.ajax({
                                                        url: createURL('listVirtualMachines'),
                                                        data: $.extend(data, {
                                                            state: value
                                                        }),
                                                        async: false,
                                                        success: function (json) {
                                                            var instanceObjs = json.listvirtualmachinesresponse.virtualmachine;
                                                            if (instanceObjs !== undefined) {
                                                                instanceObjs.sort(function (a, b) {
                                                                    return a.name.localeCompare(b.name);
                                                                });
                                                                $(instanceObjs).each(function () {
                                                                    items.push({
                                                                        id: this.id,
                                                                        description: this.displayname ? this.displayname : this.name
                                                                    });
                                                                });
                                                            }
                                                        }
                                                    });
                                                });

                                                args.response.success({
                                                    data: items
                                                });
                                            }
                                        },
                                        deviceid: {
                                            label: 'label.device.id',
                                            validation: {
                                                required: false
                                            }
                                        }
                                    }
                                },
                                action: function (args) {
                                    var data = {
                                        id: args.context.volumes[0].id,
                                        virtualMachineId: args.data.virtualMachineId
                                    };
                                    // Check for integer to be used as deviceId, or just leave it out
                                    if (args.data.deviceid != "" && typeof parseInt(args.data.deviceid, 10) === 'number' && args.data.deviceid % 1 === 0) {
                                        $.extend(data, {
                                            deviceid: args.data.deviceid
                                        });
                                    }
                                    $.ajax({
                                        url: createURL("attachVolume"),
                                        data: data,
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var jid = json.attachvolumeresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getUpdatedItem: function (json) {
                                                        return json.queryasyncjobresultresponse.jobresult.volume;
                                                    },
                                                    getActionFilter: function () {
                                                        return volumeActionfilter;
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
                            detachDisk: {
                                label: 'label.action.detach.disk',
                                messages: {
                                    confirm: function (args) {
                                        return 'message.detach.disk';
                                    },
                                    notification: function (args) {
                                        return 'label.action.detach.disk';
                                    }
                                },
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("detachVolume&id=" + args.context.volumes[0].id),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var jid = json.detachvolumeresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getUpdatedItem: function (json) {
                                                        return {
                                                            virtualmachineid: null,
                                                            vmdisplayname: null
                                                        };
                                                    },
                                                    getActionFilter: function () {
                                                        return volumeActionfilter;
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

                            downloadVolume: {
                                label: 'label.action.download.volume',
                                messages: {
                                    confirm: function (args) {
                                        return 'message.download.volume.confirm';
                                    },
                                    notification: function (args) {
                                        return 'label.action.download.volume';
                                    },
                                    complete: function (args) {
                                        var url = args.url;
                                        var htmlMsg = _l('message.download.volume');
                                        var htmlMsg2 = htmlMsg.replace(/#/, url).replace(/00000/, url);
                                        //$infoContainer.find("#info").html(htmlMsg2);
                                        return htmlMsg2;
                                    }
                                },
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("extractVolume&id=" + args.context.volumes[0].id + "&zoneid=" + args.context.volumes[0].zoneid + "&mode=HTTP_DOWNLOAD"),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var jid = json.extractvolumeresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getUpdatedItem: function (json) {
                                                        return json.queryasyncjobresultresponse.jobresult.volume;
                                                    },
                                                    getActionFilter: function () {
                                                        return volumeActionfilter;
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
                                    preFilter: cloudStack.preFilter.createTemplate,
                                    desc: '',
                                    preFilter: function (args) {
                                        if (args.context.volumes[0].hypervisor == "XenServer") {
                                            if (isAdmin()) {
                                                args.$form.find('.form-item[rel=xenserverToolsVersion61plus]').css('display', 'inline-block');
                                            }
                                        }
                                    },
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
                                        xenserverToolsVersion61plus: {
                                            label: 'label.xenserver.tools.version.61.plus',
                                            isBoolean: true,
                                            isChecked: function (args) {
                                                var b = false;
                                                var vmObj;
                                                $.ajax({
                                                    url: createURL("listVirtualMachines"),
                                                    data: {
                                                        id: args.context.volumes[0].virtualmachineid
                                                    },
                                                    async: false,
                                                    success: function (json) {
                                                        vmObj = json.listvirtualmachinesresponse.virtualmachine[0];
                                                    }
                                                });
                                                if (vmObj == undefined) { //e.g. VM has failed over
                                                    if (isAdmin()) {
                                                        $.ajax({
                                                            url: createURL('listConfigurations'),
                                                            data: {
                                                                name: 'xenserver.pvdriver.version'
                                                            },
                                                            async: false,
                                                            success: function (json) {
                                                                if (json.listconfigurationsresponse.configuration != null && json.listconfigurationsresponse.configuration[0].value == 'xenserver61') {
                                                                    b = true;
                                                                }
                                                            }
                                                        });
                                                    }
                                                } else {
                                                    if ('details' in vmObj && 'hypervisortoolsversion' in vmObj.details) {
                                                        if (vmObj.details.hypervisortoolsversion == 'xenserver61')
                                                            b = true;
                                                        else
                                                            b = false;
                                                    }
                                                }
                                                return b;
                                            },
                                            isHidden: true
                                        },
                                        osTypeId: {
                                            label: 'label.os.type',
                                            select: function (args) {
                                                $.ajax({
                                                    url: createURL("listOsTypes"),
                                                    dataType: "json",
                                                    async: true,
                                                    success: function (json) {
                                                        var ostypes = json.listostypesresponse.ostype;
                                                        var items = [];
                                                        $(ostypes).each(function () {
                                                            items.push({
                                                                id: this.id,
                                                                description: this.description
                                                            });
                                                        });
                                                        args.response.success({
                                                            data: items
                                                        });
                                                    }
                                                });
                                            }
                                        },
                                        isPublic: {
                                            label: 'label.public',
                                            isBoolean: true
                                        },
                                        isPasswordEnabled: {
                                            label: 'label.password.enabled',
                                            isBoolean: true
                                        },
                                        isFeatured: {
                                            label: 'label.featured',
                                            isBoolean: true
                                        },
                                        isdynamicallyscalable: {
                                            label: 'label.dynamically.scalable',
                                            isBoolean: true
                                        }
                                    }
                                },
                                action: function (args) {
                                    var data = {
                                        volumeId: args.context.volumes[0].id,
                                        name: args.data.name,
                                        displayText: args.data.displayText,
                                        osTypeId: args.data.osTypeId,
                                        isPublic: (args.data.isPublic == "on"),
                                        passwordEnabled: (args.data.isPasswordEnabled == "on"),
                                        isdynamicallyscalable: (args.data.isdynamicallyscalable == "on")

                                    };

                                    if (args.$form.find('.form-item[rel=isFeatured]').css("display") != "none") {
                                        $.extend(data, {
                                            isfeatured: (args.data.isFeatured == "on")
                                        });
                                    }

                                    //XenServer only (starts here)
                                    if (args.$form.find('.form-item[rel=xenserverToolsVersion61plus]').length > 0) {
                                        if (args.$form.find('.form-item[rel=xenserverToolsVersion61plus]').css("display") != "none") {
                                            $.extend(data, {
                                                'details[0].hypervisortoolsversion': (args.data.xenserverToolsVersion61plus == "on") ? "xenserver61" : "xenserver56"
                                            });
                                        }
                                    }
                                    //XenServer only (ends here)

                                    $.ajax({
                                        url: createURL('createTemplate'),
                                        data: data,
                                        success: function (json) {
                                            var jid = json.createtemplateresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getUpdatedItem: function (json) {
                                                        return {}; //no properties in this volume needs to be updated
                                                    },
                                                    getActionFilter: function () {
                                                        return volumeActionfilter;
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

                            migrateToAnotherStorage: {
                                label: 'label.migrate.volume.to.primary.storage',
                                messages: {
                                    notification: function (args) {
                                        return 'label.volume.migrated';
                                    }
                                },

                                action: {
                                    custom: cloudStack.uiCustom.migrate({
                                        listView: {
                                            label: 'label.migrate.volume.to.primary.storage',
                                            listView: {
                                                id: 'availableStorage',
                                                fields: {
                                                    availableStorageName: {
                                                        label: 'label.storage.pool'
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
                                                        url: createURL("listStoragePools&zoneid=" + args.context.volumes[0].zoneid),
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
                                                                        availableStorageName: this.name
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
                                                url: createURL("migrateVolume&storageid=" + args.context.selectedHost[0].id + "&volumeid=" + args.context.volumes[0].id),
                                                dataType: "json",
                                                async: true,
                                                success: function (json) {
                                                    var jid = json.migratevolumeresponse.jobid;
                                                    args.response.success({
                                                        _custom: {
                                                            jobId: jid,
                                                            getUpdatedItem: function (json) {
                                                                return json.queryasyncjobresultresponse.jobresult.volume;
                                                            },
                                                            getActionFilter: function () {
                                                                return volumeActionfilter;
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

                            remove: {
                                label: 'label.action.delete.volume',
                                messages: {
                                    confirm: function (args) {
                                        return 'message.action.delete.volume';
                                    },
                                    notification: function (args) {
                                        return 'label.action.delete.volume';
                                    }
                                },
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("deleteVolume&id=" + args.context.volumes[0].id),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            args.response.success();
                                        }
                                    });
                                },
                                notification: {
                                    poll: function (args) {
                                        args.complete();
                                    }
                                }
                            },

                            resize: {
                                label: 'label.action.resize.volume',
                                messages: {
                                    notification: function (args) {
                                        return 'label.action.resize.volume';
                                    }
                                },
                                createForm: {
                                    title: 'label.action.resize.volume',
                                    preFilter: function (args) {
                                        if (args.context.volumes != null && args.context.volumes[0].type == 'ROOT') {
                                            args.$form.find('.form-item[rel=newdiskoffering]').hide();
                                        }
                                    },
                                    fields: {
                                        newdiskoffering: {
                                            label: 'label.resize.new.offering.id',
                                            select: function (args) {
                                                if (args.context.volumes != null && args.context.volumes[0].type == 'ROOT') {
                                                    args.response.success({
                                                        data: []
                                                    });
                                                    return;
                                                }

                                                $.ajax({
                                                    url: createURL("listDiskOfferings"),
                                                    dataType: "json",
                                                    success: function (json) {
                                                        diskofferingObjs = json.listdiskofferingsresponse.diskoffering;
                                                        var items = [];
                                                        var current_offering = args.context.volumes[0].diskofferingid;
                                                        $(diskofferingObjs).each(function () {
                                                            items.push({
                                                                id: this.id,
                                                                description: this.displaytext
                                                            });
                                                        });
                                                        //Hack to get current offering as default in selection
                                                        var index = items.findIndex(function (args) {
                                                            return args.id === current_offering;
                                                        });
                                                        items.splice(0, 0, items.splice(index, 1)[0]);
                                                        args.response.success({
                                                            data: items
                                                        });
                                                    }
                                                });

                                                args.$select.change(function () {
                                                    var diskOfferingId = $(this).val();
                                                    $(diskofferingObjs).each(function () {
                                                        if (this.id == diskOfferingId) {
                                                            selectedDiskOfferingObj = this;
                                                            return false; //break the $.each() loop
                                                        }
                                                    });
                                                    if (selectedDiskOfferingObj == null)
                                                        return;

                                                    var $form = $(this).closest('form');
                                                    var $shrinkok = $form.find('.form-item[rel=shrinkok]');
                                                    //unit of args.context.volumes[0].size is "byte"
                                                    //unit of selectedDiskOfferingObj.disksize is "gigabyte" ("GB"), so transfer it into "byte" by multiply (1024 * 1024 * 1024)
                                                    if (args.context.volumes[0].size > selectedDiskOfferingObj.disksize * (1024 * 1024 * 1024)) { //if original disk size  > new disk size
                                                        $shrinkok.css('display', 'inline-block');
                                                    } else {
                                                        $shrinkok.hide();
                                                    }

                                                    var $newsize = $form.find('.form-item[rel=newsize]');
                                                    if (selectedDiskOfferingObj.iscustomized == true) {
                                                        $newsize.css('display', 'inline-block');
                                                    } else {
                                                        $newsize.hide();
                                                    }

                                                    var $minIops = $form.find('.form-item[rel=minIops]');
                                                    var $maxIops = $form.find('.form-item[rel=maxIops]');
                                                    if (selectedDiskOfferingObj.iscustomizediops == true) {
                                                        $minIops.css('display', 'inline-block');
                                                        $maxIops.css('display', 'inline-block');
                                                    } else {
                                                        $minIops.hide();
                                                        $maxIops.hide();
                                                    }
                                                });
                                            }
                                        },
                                        newsize: {
                                            label: 'label.resize.new.size',
                                            validation: {
                                                required: true,
                                                number: true
                                            },
                                            defaultValue: function (args) {
                                                var _size = parseInt(args.volumes[0].size / (1024 * 1024 * 1024));
                                                return _size < 1 ? 1 : _size;
                                            }
                                        },
                                        shrinkok: {
                                            label: 'label.resize.shrink.ok',
                                            isBoolean: true,
                                            isChecked: false
                                        },
                                        minIops: {
                                            label: 'label.disk.iops.min',
                                            validation: {
                                                required: false,
                                                number: true
                                            },
                                            isHidden: true
                                        },
                                        maxIops: {
                                            label: 'label.disk.iops.max',
                                            validation: {
                                                required: false,
                                                number: true
                                            },
                                            isHidden: true
                                        }
                                    }
                                },
                                action: function (args) {
                                    var array1 = [];

                                    if (args.$form.find('.form-item[rel=shrinkok]').css("display") != "none") {
                                        array1.push("&shrinkok=" + (args.data.shrinkok == "on"));
                                    }

                                    var newDiskOffering = args.data.newdiskoffering;
                                    var newSize;
                                    if (selectedDiskOfferingObj == null || selectedDiskOfferingObj.iscustomized == true) {
                                        newSize = args.data.newsize;
                                    }
                                    if (newDiskOffering != null && newDiskOffering.length > 0) {
                                        array1.push("&diskofferingid=" + todb(newDiskOffering));
                                    }
                                    if (newSize != null && newSize.length > 0) {
                                        array1.push("&size=" + todb(newSize));
                                    }

                                    var minIops;
                                    var maxIops;

                                    if (selectedDiskOfferingObj != null && selectedDiskOfferingObj.iscustomizediops == true) {
                                        minIops = args.data.minIops;
                                        maxIops = args.data.maxIops;
                                    }

                                    if (minIops != null && minIops.length > 0) {
                                        array1.push("&miniops=" + todb(minIops));
                                    }

                                    if (maxIops != null && maxIops.length > 0) {
                                        array1.push("&maxiops=" + todb(maxIops));
                                    }

                                    $.ajax({
                                        url: createURL("resizeVolume&id=" + args.context.volumes[0].id + array1.join("")),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var jid = json.resizevolumeresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getUpdatedItem: function (json) {
                                                        return json.queryasyncjobresultresponse.jobresult.volume;
                                                    },
                                                    getActionFilter: function () {
                                                        return volumeActionfilter;
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
                                    var hiddenFields;
                                    if (isAdmin() || isDomainAdmin()) {
                                        hiddenFields = [];
                                    } else {
                                        hiddenFields = ['storage', 'hypervisor'];
                                    }
                                    if (typeof args.context.volumes[0].diskIopsTotalRate == 'undefined') {
                                        hiddenFields.push('diskIopsTotalRate');
                                    }
                                    if (typeof args.context.volumes[0].diskIopsReadRate == 'undefined') {
                                        hiddenFields.push('diskIopsReadRate');
                                    }
                                    if (typeof args.context.volumes[0].diskIopsWriteRate == 'undefined') {
                                        hiddenFields.push('diskIopsWriteRate');
                                    }

                                    return hiddenFields;
                                },

                                fields: [{
                                    name: {
                                        label: 'label.name',
                                        isEditable: true
                                    }
                                }, {
                                    diskcontroller: {
                                        label: 'label.disk.controller'
                                    },
                                    deviceid: {
                                        label: 'label.device.id'
                                    },
                                    format: {
                                        label: 'label.format'
                                    },
                                    storage: {
                                        label: 'label.storage'
                                    },
                                    state: {
                                        label: 'label.state',
                                        pollAgainIfValueIsIn: {
                                            'UploadNotStarted': 1
                                        },
                                        pollAgainFn: function (context) {
                                            var toClearInterval = false;
                                            $.ajax({
                                                url: createURL("listVolumes&id=" + context.volumes[0].id),
                                                dataType: "json",
                                                async: false,
                                                success: function (json) {
                                                    var jsonObj = json.listvolumesresponse.volume[0];
                                                    if (jsonObj.state != context.volumes[0].state) {
                                                        toClearInterval = true; //to clear interval
                                                    }
                                                }
                                            });
                                            return toClearInterval;
                                        }
                                    },
                                    path: {
                                        label: 'label.path',
                                        isCopyPaste: true
                                    },
                                    diskofferingname: {
                                        label: 'label.disk.offering'
                                    },
                                    type: {
                                        label: 'label.type'
                                    },
                                    size: {
                                        label: 'label.size',
                                        converter: function (args) {
                                            if (args == null || args == 0)
                                                return "";
                                            else
                                                return cloudStack.converters.convertBytes(args);
                                        }
                                    },
                                    diskIopsTotalRate: {
                                        label: 'label.disk.iops.total',
                                        converter: function (args) {
                                            if (args == null || args == 0)
                                                return "";
                                            else
                                                return args;
                                        }
                                    },
                                    diskIopsReadRate: {
                                        label: 'label.disk.iops.read.rate',
                                        converter: function (args) {
                                            if (args == null || args == 0)
                                                return "";
                                            else
                                                return args;
                                        }
                                    },
                                    diskIopsWriteRate: {
                                        label: 'label.disk.iops.write.rate',
                                        converter: function (args) {
                                            if (args == null || args == 0)
                                                return "";
                                            else
                                                return args;
                                        }
                                    },
                                    virtualmachineid: {
                                        label: 'label.vm.id',
                                        converter: function (args) {
                                            if (args == null)
                                                return _l('state.detached');
                                            else
                                                return args;
                                        }
                                    },
                                    //vmname: { label: 'label.vm.name' },
                                    vmdisplayname: {
                                        label: 'label.vm.display.name'
                                    },
                                    vmstate: {
                                        label: 'label.vm.state'
                                    },
                                    domain: {
                                        label: 'label.domain'
                                    },
                                    account: {
                                        label: 'label.account'
                                    },
                                    storagetype: {
                                        label: 'label.storage.type'
                                    },
                                    provisioningtype: {
                                        label: 'label.disk.provisioningtype'
                                    },
                                    hypervisor: {
                                        label: 'label.hypervisor'
                                    },
                                    id: {
                                        label: 'label.id',
                                        isCopyPaste: true
                                    },
                                    created: {
                                        label: 'label.created',
                                        converter: cloudStack.converters.toLocalDate
                                    },
                                    zonename: {
                                        label: 'label.zone'
                                    }
                                }],

                                tags: cloudStack.api.tags({
                                    resourceType: 'Volume',
                                    contextId: 'volumes'
                                }),


                                dataProvider: function (args) {
                                    $.ajax({
                                        url: createURL("listVolumes&id=" + args.context.volumes[0].id),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var jsonObj = json.listvolumesresponse.volume[0];

                                            $(window).trigger('cloudStack.module.sharedFunctions.addExtraProperties', {
                                                obj: jsonObj,
                                                objType: "Volume"
                                            });

                                            args.response.success({
                                                actionFilter: volumeActionfilter,
                                                data: jsonObj
                                            });
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            },

            /**
             * Snapshots
             */
            snapshots: {
                type: 'select',
                title: 'label.snapshots',
                listView: {
                    id: 'snapshots',
                    label: 'label.snapshots',
                    fields: {
                        volumename: {
                            label: 'label.volume'
                        },
                        name: {
                            label: 'label.name'
                        },
                        intervaltype: {
                            label: 'label.interval.type'
                        },
                        created: {
                            label: 'label.created',
                            converter: cloudStack.converters.toLocalDate
                        },
                        state: {
                            label: 'label.state',
                            indicator: {
                                'BackedUp': 'on',
                                'Destroyed': 'off'
                            }
                        }
                    },

                    advSearchFields: {
                        name: {
                            label: 'label.name'
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

                    dataProvider: function (args) {
                        var data = {};
                        var instanceVolumeIds = [];
                        listViewDataProvider(args, data);

                        if (args.context != null) {
                            if ("volumes" in args.context) {
                                $.extend(data, {
                                    volumeid: args.context.volumes[0].id
                                });
                            } else if (args.context.instances) {
                                $.ajax({
                                    url: createURL('listVolumes'),
                                    data: {
                                        virtualmachineid: args.context.instances[0].id,
                                        listAll: true
                                    },
                                    async: false,
                                    success: function (json) {
                                        instanceVolumeIds = $.map(json.listvolumesresponse.volume, function (volume) {
                                            return volume.id;
                                        })
                                    }
                                });
                                data.volumeid = instanceVolumeIds.join(',');
                            }
                        }

                        $.ajax({
                            url: createURL('listSnapshots'),
                            data: data,
                            success: function (json) {
                                var items = json.listsnapshotsresponse.snapshot;
                                args.response.success({
                                    actionFilter: snapshotActionfilter,
                                    data: items
                                });
                            },
                            error: function (XMLHttpResponse) {
                                cloudStack.dialog.notice({
                                    message: parseXMLHttpResponse(XMLHttpResponse)
                                });
                                args.response.error();
                            }
                        });
                    },

                    detailView: {
                        name: 'Snapshot detail',
                        actions: {
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
                                    desc: '',
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
                                                $.ajax({
                                                    url: createURL("listOsTypes"),
                                                    dataType: "json",
                                                    async: true,
                                                    success: function (json) {
                                                        var ostypes = json.listostypesresponse.ostype;
                                                        var items = [];
                                                        $(ostypes).each(function () {
                                                            items.push({
                                                                id: this.id,
                                                                description: this.description
                                                            });
                                                        });
                                                        args.response.success({
                                                            data: items
                                                        });
                                                    }
                                                });
                                            }
                                        },
                                        isPublic: {
                                            label: 'label.public',
                                            isBoolean: true
                                        },
                                        isPasswordEnabled: {
                                            label: 'label.password.enabled',
                                            isBoolean: true
                                        },
                                        isdynamicallyscalable: {
                                            label: 'label.dynamically.scalable',
                                            isBoolean: true
                                        }
                                    }
                                },
                                action: function (args) {
                                    var data = {
                                        snapshotid: args.context.snapshots[0].id,
                                        name: args.data.name,
                                        displayText: args.data.displayText,
                                        osTypeId: args.data.osTypeId,
                                        isPublic: (args.data.isPublic == "on"),
                                        passwordEnabled: (args.data.isPasswordEnabled == "on"),
                                        isdynamicallyscalable: (args.data.isdynamicallyscalable == "on")
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
                                                        return {}; //nothing in this snapshot needs to be updated
                                                    },
                                                    getActionFilter: function () {
                                                        return snapshotActionfilter;
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

                            createVolume: {
                                label: 'label.action.create.volume',
                                messages: {
                                    confirm: function (args) {
                                        return 'message.confirm.create.volume';
                                    },
                                    notification: function (args) {
                                        return 'label.action.create.volume';
                                    }
                                },
                                createForm: {
                                    title: 'label.action.create.volume',
                                    desc: '',
                                    preFilter: function (args) {
                                        if (g_regionsecondaryenabled == true) {
                                            args.$form.find('.form-item[rel=zoneid]').css('display', 'inline-block');
                                        } else {
                                            args.$form.find('.form-item[rel=zoneid]').hide();
                                        }
                                    },
                                    fields: {
                                        name: {
                                            label: 'label.name',
                                            validation: {
                                                required: true
                                            }
                                        },
                                        zoneid: {
                                            label: 'label.availability.zone',
                                            isHidden: true,
                                            select: function (args) {
                                                $.ajax({
                                                    url: createURL("listZones&available=true"),
                                                    dataType: "json",
                                                    async: true,
                                                    success: function (json) {
                                                        var zoneObjs = json.listzonesresponse.zone;
                                                        var items = [{
                                                            id: '',
                                                            description: ''
                                                        }];
                                                        if (zoneObjs != null) {
                                                            for (i = 0; i < zoneObjs.length; i++) {
                                                                items.push({
                                                                    id: zoneObjs[i].id,
                                                                    description: zoneObjs[i].name
                                                                });
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
                                        snapshotid: args.context.snapshots[0].id,
                                        name: args.data.name
                                    };

                                    if (args.$form.find('.form-item[rel=zoneid]').css("display") != "none" && args.data.zoneid != '') {
                                        $.extend(data, {
                                            zoneId: args.data.zoneid
                                        });
                                    }

                                    $.ajax({
                                        url: createURL('createVolume'),
                                        data: data,
                                        success: function (json) {
                                            var jid = json.createvolumeresponse.jobid;
                                            args.response.success({
                                                _custom: {
                                                    jobId: jid,
                                                    getUpdatedItem: function (json) {
                                                        return {}; //nothing in this snapshot needs to be updated
                                                    },
                                                    getActionFilter: function () {
                                                        return snapshotActionfilter;
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

                            revertSnapshot: {
                                label: 'label.action.revert.snapshot',
                                messages: {
                                    confirm: function (args) {
                                        return 'message.action.revert.snapshot';
                                    },
                                    notification: function (args) {
                                        return 'label.action.revert.snapshot';
                                    }
                                },
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("revertSnapshot&id=" + args.context.snapshots[0].id),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var jid = json.revertsnapshotresponse.jobid;
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

                            remove: {
                                label: 'label.action.delete.snapshot',
                                messages: {
                                    confirm: function (args) {
                                        return 'message.action.delete.snapshot';
                                    },
                                    notification: function (args) {
                                        return 'label.action.delete.snapshot';
                                    }
                                },
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("deleteSnapshot&id=" + args.context.snapshots[0].id),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var jid = json.deletesnapshotresponse.jobid;
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
                                    name: {
                                        label: 'label.name'
                                    }
                                }, {
                                    id: {
                                        label: 'label.id'
                                    },
                                    volumename: {
                                        label: 'label.volume.name'
                                    },
                                    state: {
                                        label: 'label.state'
                                    },
                                    intervaltype: {
                                        label: 'label.interval.type'
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
                                    }
                                }],

                                tags: cloudStack.api.tags({
                                    resourceType: 'Snapshot',
                                    contextId: 'snapshots'
                                }),

                                dataProvider: function (args) {
                                    $.ajax({
                                        url: createURL("listSnapshots&id=" + args.context.snapshots[0].id),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var jsonObj = json.listsnapshotsresponse.snapshot[0];
                                            args.response.success({
                                                actionFilter: snapshotActionfilter,
                                                data: jsonObj
                                            });
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            },

            /**
             * VM Snapshots
             */
            vmsnapshots: {
                type: 'select',
                title: 'label.vmsnapshot',
                listView: {
                    id: 'vmsnapshots',
                    isMaximized: true,
                    fields: {
                        displayname: {
                            label: 'label.name'
                        },
                        state: {
                            label: 'label.state',
                            indicator: {
                                'Ready': 'on',
                                'Error': 'off'
                            }
                        },
                        type: {
                            label: 'label.vmsnapshot.type'
                        },
                        current: {
                            label: 'label.vmsnapshot.current',
                            converter: cloudStack.converters.toBooleanText
                        },
                        parentName: {
                            label: 'label.vmsnapshot.parentname'
                        },
                        created: {
                            label: 'label.date',
                            converter: cloudStack.converters.toLocalDate
                        }
                    },
                    advSearchFields: {
                        name: {
                            label: 'label.name'
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
                                return !(isAdmin() || isDomainAdmin());
                            }
                        },
                        account: {
                            label: 'label.account',
                            isHidden: function (args) {
                                return !(isAdmin() || isDomainAdmin());
                            }
                        },
                        tagKey: {
                            label: 'label.tag.key'
                        },
                        tagValue: {
                            label: 'label.tag.value'
                        }
                    },
                    dataProvider: function (args) {
                        var data = {};
                        listViewDataProvider(args, data);

                        if (args.context != null) {
                            if ("instances" in args.context) {
                                $.extend(data, {
                                    virtualMachineId: args.context.instances[0].id
                                });
                            }
                        }
                        $.ajax({
                            url: createURL('listVMSnapshot&listAll=true'),
                            data: data,
                            dataType: "json",
                            async: true,
                            success: function (json) {
                                var jsonObj;
                                jsonObj = json.listvmsnapshotresponse.vmSnapshot;
                                args.response.success({
                                    actionFilter: vmSnapshotActionfilter,
                                    data: jsonObj
                                });
                            }
                        });
                    },
                    //dataProvider end
                    detailView: {
                        tabs: {
                            details: {
                                title: 'label.details',
                                fields: {
                                    id: {
                                        label: 'label.id'
                                    },
                                    name: {
                                        label: 'label.name'
                                    },
                                    displayname: {
                                        label: 'label.display.name'
                                    },
                                    type: {
                                        label: 'label.vmsnapshot.type'
                                    },
                                    description: {
                                        label: 'label.description'
                                    },
                                    state: {
                                        label: 'label.state',
                                        indicator: {
                                            'Ready': 'on',
                                            'Error': 'off'
                                        }
                                    },
                                    current: {
                                        label: 'label.vmsnapshot.current',
                                        converter: cloudStack.converters.toBooleanText
                                    },
                                    parentName: {
                                        label: 'label.vmsnapshot.parentname'
                                    },
                                    domain: {
                                        label: 'label.domain'
                                    },
                                    account: {
                                        label: 'label.account'
                                    },
                                    virtualmachineid: {
                                        label: 'label.vm.id'
                                    },
                                    created: {
                                        label: 'label.date',
                                        converter: cloudStack.converters.toLocalDate
                                    }
                                },
                                dataProvider: function (args) {
                                    $.ajax({
                                        url: createURL("listVMSnapshot&listAll=true&vmsnapshotid=" + args.context.vmsnapshots[0].id),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var jsonObj;
                                            jsonObj = json.listvmsnapshotresponse.vmSnapshot[0];
                                            args.response.success({
                                                actionFilter: vmSnapshotActionfilter,
                                                data: jsonObj
                                            });
                                        }
                                    });
                                },
                                tags: cloudStack.api.tags({
                                    resourceType: 'VMSnapshot',
                                    contextId: 'vmsnapshots'
                                })
                            }
                        },
                        actions: {
                            //delete a snapshot
                            remove: {
                                label: 'label.action.vmsnapshot.delete',
                                messages: {
                                    confirm: function (args) {
                                        return 'message.action.vmsnapshot.delete';
                                    },
                                    notification: function (args) {
                                        return 'label.action.vmsnapshot.delete';
                                    }
                                },
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("deleteVMSnapshot&vmsnapshotid=" + args.context.vmsnapshots[0].id),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var jid = json.deletevmsnapshotresponse.jobid;
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
                            revertToVMSnapshot: {
                                label: 'label.action.vmsnapshot.revert',
                                messages: {
                                    confirm: function (args) {
                                        return 'label.action.vmsnapshot.revert';
                                    },
                                    notification: function (args) {
                                        return 'message.action.vmsnapshot.revert';
                                    }
                                },
                                action: function (args) {
                                    $.ajax({
                                        url: createURL("revertToVMSnapshot&vmsnapshotid=" + args.context.vmsnapshots[0].id),
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var jid = json.reverttovmsnapshotresponse.jobid;
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
                            takeSnapshot: {
                                label: 'Create Snapshot From VM Snapshot',
                                messages: {
                                    confirm: function (args) {
                                        return 'Please confirm that you want to create a volume snapshot from the vm snapshot.';
                                    },
                                    notification: function (args) {
                                        return 'Volume snapshot is created from vm snapshot';
                                    }
                                },
                                createForm: {
                                    title: 'label.action.take.snapshot',
                                    desc: 'message.action.take.snapshot',
                                    fields: {
                                        name: {
                                            label: 'label.name',
                                        },
                                        volume: {
                                            label: 'label.volume',
                                            validation: {
                                                required: true
                                            },
                                            select: function (args) {
                                                $.ajax({
                                                    url: createURL("listVolumes&virtualMachineId=" + args.context.vmsnapshots[0].virtualmachineid),
                                                    dataType: "json",
                                                    async: true,
                                                    success: function (json) {
                                                        var volumes = json.listvolumesresponse.volume;
                                                        var items = [];
                                                        $(volumes).each(function () {
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
                                    var data = {
                                        volumeid: args.data.volume,
                                        vmsnapshotid: args.context.vmsnapshots[0].id
                                    };
                                    if (args.data.name != null && args.data.name.length > 0) {
                                        $.extend(data, {
                                            name: args.data.name
                                        });
                                    }
                                    $.ajax({
                                        url: createURL("createSnapshotFromVMSnapshot"),
                                        data: data,
                                        dataType: "json",
                                        async: true,
                                        success: function (json) {
                                            var jid = json.createsnapshotfromvmsnapshotresponse.jobid;
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
                    }
                    //detailview end
                }
            }
        }
    };

    var volumeActionfilter = cloudStack.actionFilter.volumeActionfilter = function (args) {
        var jsonObj = args.context.item;
        var allowedActions = [];


        if (jsonObj.state == 'Destroyed' || jsonObj.state == 'Migrating' || jsonObj.state == 'Uploading') {
            return [];
        }
        if (jsonObj.state == 'UploadError') {
            return ["remove"];
        }

        if (jsonObj.state == "Ready") {
            if (jsonObj.vmstate == 'Stopped') {
                if (g_kvmsnapshotenabled == true) { //"kvm.snapshot.enabled" flag should be taken to account only when snapshot is being created for Running vm (CLOUDSTACK-4428)
                    allowedActions.push("takeSnapshot");
                }
            }
        }

        if (jsonObj.state == "Ready" || jsonObj.state == "Allocated") {
            allowedActions.push("resize");
        }

        if (jsonObj.state != "Allocated") {
            if ((jsonObj.vmstate == "Stopped" || jsonObj.virtualmachineid == null) && jsonObj.state == "Ready") {
                allowedActions.push("downloadVolume");
            }
        }

        if (jsonObj.type == "ROOT" || jsonObj.type == "DATADISK") {
            if (jsonObj.state == "Ready" && isAdmin() && jsonObj.virtualmachineid != null) {
                allowedActions.push("migrateVolume");
            }
        }

        if (jsonObj.state != "Creating") {
            if (jsonObj.type == "ROOT") {
                if (jsonObj.vmstate == "Stopped" || jsonObj.vmstate == "Destroyed") {
                    allowedActions.push("createTemplate");
                    allowedActions.push("detachDisk");
                }
            } else { //jsonObj.type == "DATADISK"
                if (jsonObj.virtualmachineid != null) {
                    if (jsonObj.vmstate == "Running" || jsonObj.vmstate == "Stopped" || jsonObj.vmstate == "Destroyed") {
                        allowedActions.push("detachDisk");
                    }
                } else { // Disk not attached
                    allowedActions.push("remove");
                    if (jsonObj.state == "Ready" && isAdmin() && jsonObj.storagetype == "shared") {
                        allowedActions.push("migrateToAnotherStorage");
                    }
                    allowedActions.push("attachDisk");
                    allowedActions.push("editDisk");
                }
            }
        }

        return allowedActions;
    };

    var snapshotActionfilter = cloudStack.actionFilter.snapshotActionfilter = function (args) {
        var jsonObj = args.context.item;

        if (jsonObj.state == 'Destroyed') {
            return [];
        }

        var allowedActions = [];
        if (jsonObj.state == "BackedUp") {
            allowedActions.push("createTemplate");
            allowedActions.push("createVolume");

            if (jsonObj.revertable) {
                allowedActions.push("revertSnapshot");
            }
        }
        allowedActions.push("remove");

        return allowedActions;
    };

    var vmSnapshotActionfilter = cloudStack.actionFilter.vmSnapshotActionfilter = function (args) {
        var jsonObj = args.context.item;

        if (jsonObj.state == 'Error') {
            return ["remove"];
        }

        var allowedActions = [];
        if (jsonObj.state == "Ready") {
            allowedActions.push("remove");
            allowedActions.push("revertToVMSnapshot");
            allowedActions.push("takeSnapshot");
        }

        return allowedActions;
    };

})(cloudStack);
