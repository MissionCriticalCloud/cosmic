(function(cloudStack, $) {
    cloudStack.uiCustom.migrate = function(args) {
        var listView = args.listView;
        var action = args.action;

        return function(args) {
            var context = args.context;

            var hostList = function(args) {
                var $listView;

                var hosts = $.extend(true, {}, args.listView, {
                    context: context,
                    uiCustom: true
                });

                hosts.listView.actions = {
                    select: {
                        label: _l(args.listView.label),
                        type: 'radio',
                        action: {
                            uiCustom: function(args) {
                                var $item = args.$item;
                                var $input = $item.find('td.actions input:visible');

                                if ($input.attr('type') == 'checkbox') {
                                    if ($input.is(':checked'))
                                        $item.addClass('multi-edit-selected');
                                    else
                                        $item.removeClass('multi-edit-selected');
                                } else {
                                    $item.siblings().removeClass('multi-edit-selected');
                                    $item.addClass('multi-edit-selected');
                                }
                            }
                        }
                    }
                };

                $listView = $('<div>').listView(hosts);

                // Change action label
                $listView.find('th.actions').html(_l('label.select'));

                return $listView;
            };

            var $dataList = hostList({
                listView: listView
            }).dialog({
                dialogClass: 'multi-edit-add-list panel migrate-vm-available-host-list',
                width: 825,
                draggable: false,
                title: _l(listView.label),
                buttons: [{
                    text: _l('label.ok'),
                    'class': 'ok migrateok',
                    click: function() {
                        var complete = args.complete;
                        var selectedHostObj = $dataList.find('tr.multi-edit-selected').data('json-obj');
                        if(selectedHostObj != undefined) {
                            $dataList.fadeOut(function() {
                                action({
                                    context: $.extend(true, {}, context, {
                                        selectedHost: [
                                            selectedHostObj
                                        ]
                                    }),
                                    response: {
                                        success: function(args) {
                                            complete({
                                                _custom: args._custom,
                                                $item: $('<div>'),
                                            });
                                        },
                                        error: function(args) {
                                            cloudStack.dialog.notice({
                                                message: args
                                            });
                                        }
                                    }
                                });
                            });

                            $('div.overlay').fadeOut(function() {
                                $('div.overlay').remove();
                            });
                        }
                        else {
                            cloudStack.dialog.notice({
                                message: _l('message.migrate.instance.select.host')
                            });
                        }
                    }
                }, {
                    text: _l('label.cancel'),
                    'class': 'cancel migratecancel',
                    click: function() {
                        $dataList.fadeOut(function() {
                            $dataList.remove();
                        });
                        $('div.overlay').fadeOut(function() {
                            $('div.overlay').remove();
                            $(':ui-dialog').dialog('destroy');
                        });
                    }
                }]
            }).parent('.ui-dialog').overlay();
        };
    };
}(cloudStack, jQuery));
