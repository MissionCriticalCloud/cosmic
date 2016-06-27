(function ($, cloudStack) {
    cloudStack.uiCustom.granularSettings = function (args) {
        var dataProvider = args.dataProvider;
        var actions = args.actions;

        return function (args) {
            var context = args.context;

            var listView = {
                id: 'settings',
                fields: {
                    name: {
                        label: 'label.name'
                    },
                    description: {
                        label: 'label.description'
                    },
                    value: {
                        label: 'label.value',
                        editable: true
                    }
                },
                actions: {
                    edit: {
                        label: 'label.change.value',
                        action: actions.edit
                    }
                },
                dataProvider: dataProvider
            };

            var $listView = $('<div>').listView({
                context: context,
                listView: listView
            });

            return $listView;
        }
    };
}(jQuery, cloudStack));
