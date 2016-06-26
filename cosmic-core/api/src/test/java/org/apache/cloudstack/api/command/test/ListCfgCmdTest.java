package org.apache.cloudstack.api.command.test;

import com.cloud.server.ManagementService;
import com.cloud.utils.Pair;
import org.apache.cloudstack.api.ResponseGenerator;
import org.apache.cloudstack.api.command.admin.config.ListCfgsByCmd;
import org.apache.cloudstack.api.response.ConfigurationResponse;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.config.Configuration;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ListCfgCmdTest extends TestCase {

    private ListCfgsByCmd listCfgsByCmd;
    private ManagementService mgr;
    private ResponseGenerator responseGenerator;

    @Override
    @Before
    public void setUp() {
        responseGenerator = Mockito.mock(ResponseGenerator.class);
        mgr = Mockito.mock(ManagementService.class);
        listCfgsByCmd = new ListCfgsByCmd();
    }

    @Test
    public void testCreateSuccess() {

        final Configuration cfg = Mockito.mock(Configuration.class);
        listCfgsByCmd._mgr = mgr;
        listCfgsByCmd._responseGenerator = responseGenerator;

        final List<Configuration> configList = new ArrayList<>();
        configList.add(cfg);

        final Pair<List<? extends Configuration>, Integer> result = new Pair<>(configList, 1);

        try {
            Mockito.when(mgr.searchForConfigurations(listCfgsByCmd)).thenReturn(result);
        } catch (final Exception e) {
            Assert.fail("Received exception when success expected " + e.getMessage());
        }
        final ConfigurationResponse cfgResponse = new ConfigurationResponse();
        cfgResponse.setName("Test case");
        Mockito.when(responseGenerator.createConfigurationResponse(cfg)).thenReturn(cfgResponse);

        listCfgsByCmd.execute();
        Mockito.verify(responseGenerator).createConfigurationResponse(cfg);

        final ListResponse<ConfigurationResponse> actualResponse = (ListResponse<ConfigurationResponse>) listCfgsByCmd.getResponseObject();
        Assert.assertEquals(cfgResponse, actualResponse.getResponses().get(0));
    }
}
