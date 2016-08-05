(function ($, cloudStack) {
    $(window).bind('cloudStack.ready', function () {
        var $header = $('#header .controls');
        var $zoneFilter = $('<div>').addClass('zone-filter');
        var $zoneTypeSelect = $('<select>').append(
            $('<option>').attr('value', '').html(_l('All zones')),
            $('<option>').attr('value', 'Basic').html(_l('Basic')),
            $('<option>').attr('value', 'Advanced').html(_l('Advanced'))
        );
        var $label = $('<label>').html('Zone type:');

        $zoneFilter.append($label, $zoneTypeSelect);
        $zoneFilter.insertAfter($header.find('.project-switcher'));
        $zoneTypeSelect.change(function () {
            cloudStack.context.zoneType = $zoneTypeSelect.val();

            // Go to default/start page (dashboard)
            $('#breadcrumbs .home').click();
        });
    });
}(jQuery, cloudStack));
