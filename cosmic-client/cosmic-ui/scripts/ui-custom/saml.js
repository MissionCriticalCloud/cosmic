(function ($, cloudStack) {
    $(window).bind('cloudStack.ready', function () {
        var showSamlDomainSwitcher = false;
        if (g_idpList) {
            showSamlDomainSwitcher = true;
        }
        if (!showSamlDomainSwitcher) {
            return;
        }

        var $label = $('<label>').html('Domain:');
        var $header = $('#header .controls');
        var $domainSwitcher = $('<div>').addClass('domain-switcher');
        var $domainSelect = $('<select>');
        $domainSwitcher.append($label, $domainSelect);

        var switchAccount = function (userId, domainId) {
            var toReload = true;
            $.ajax({
                url: createURL('listAndSwitchSamlAccount'),
                type: 'POST',
                async: false,
                data: {
                    userid: userId,
                    domainid: domainId
                },
                success: function (data, textStatus) {
                    document.location.reload(true);
                },
                error: function (data) {
                    cloudStack.dialog.notice({
                        message: parseXMLHttpResponse(data)
                    });
                    if (data.status !== 200) {
                        toReload = false;
                    }
                },
                complete: function () {
                    if (toReload) {
                        document.location.reload(true);
                    }
                    toReload = true;
                }
            });
        };

        $domainSelect.change(function () {
            var selectedOption = $domainSelect.val();
            var userId = selectedOption.split('/')[0];
            var domainId = selectedOption.split('/')[1];
            switchAccount(userId, domainId);
        });

        $.ajax({
            url: createURL('listAndSwitchSamlAccount'),
            success: function (json) {
                var accounts = json.listandswitchsamlaccountresponse.samluseraccount;
                if (accounts.length < 2) {
                    return;
                }
                ;
                $domainSelect.empty();
                for (var i = 0; i < accounts.length; i++) {
                    var option = $('<option>');
                    option.data("userId", accounts[i].userId);
                    option.data("domainId", accounts[i].domainId);
                    option.val(accounts[i].userId + '/' + accounts[i].domainId);
                    option.html(accounts[i].accountName + "/" + accounts[i].domainName);
                    option.appendTo($domainSelect);
                }
                var currentAccountDomain = g_userid + '/' + g_domainid;
                $domainSelect.find('option[value="' + currentAccountDomain + '"]').attr("selected", "selected");
                $domainSwitcher.insertAfter($header.find('.region-switcher'));
            },
            error: function (data) {
                // if call fails, the logged in user in not a SAML authenticated user
            }
        });
    });
}(jQuery, cloudStack));
