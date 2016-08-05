(function ($, cloudStack) {
    $(window).bind('cloudStack.ready', function () {
        var $header = $('#header .controls');
        var $projectSwitcher = $('<div>').addClass('project-switcher');
        var $projectSelect = $('<select>').append(
            $('<option>').attr('value', '-1').html(_l('Default view'))
        );
        var $label = $('<label>').html('Project:');

        // Get project list
        cloudStack.projects.dataProvider({
            context: cloudStack.context,
            response: {
                success: function (args) {
                    var projects = args.data;
                    var arrayOfProjs = [];

                    $(projects).map(function (index, project) {
                        var proj = {id: _s(project.id), html: _s(project.displaytext ? project.displaytext : project.name)};
                        arrayOfProjs.push(proj);
                    });

                    arrayOfProjs.sort(function (a, b) {
                        return a.html.localeCompare(b.html);
                    });

                    $(arrayOfProjs).map(function (index, project) {
                        var $option = $('<option>').val(_s(project.id));

                        $option.html(_s(project.html));
                        $option.appendTo($projectSelect);
                    });
                },
                error: function () {
                }
            }
        });

        $projectSwitcher.append($label, $projectSelect);
        $projectSwitcher.insertBefore($header.find('.region-switcher'));

        // Change project event
        $projectSelect.change(function () {
            var projectID = $projectSelect.val();

            if (projectID != -1) {
                cloudStack.context.projects = [{
                    id: projectID
                }];

                cloudStack.uiCustom.projects({
                    alreadySelected: true
                });
            } else {
                cloudStack.context.projects = null;
                $('#cloudStack3-container').removeClass('project-view');
                $('#navigation li.dashboard').click();
            }
        });
    });
}(jQuery, cloudStack));
