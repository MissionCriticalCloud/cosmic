package org.apache.cloudstack.api.command.test;

import com.cloud.configuration.ConfigurationService;
import com.cloud.exception.InvalidParameterValueException;
import org.apache.cloudstack.api.ResponseGenerator;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.command.admin.config.UpdateCfgCmd;
import org.apache.cloudstack.api.response.ConfigurationResponse;
import org.apache.cloudstack.config.Configuration;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class UpdateCfgCmdTest extends TestCase {

    private UpdateCfgCmd updateCfgCmd;
    private ConfigurationService configService;
    private ResponseGenerator responseGenerator;

    @Override
    @Before
    public void setUp() {
        responseGenerator = Mockito.mock(ResponseGenerator.class);
        configService = Mockito.mock(ConfigurationService.class);
        updateCfgCmd = new UpdateCfgCmd();
    }

    @Test
    public void testExecuteForEmptyResult() {
        updateCfgCmd._configService = configService;

        try {
            updateCfgCmd.execute();
        } catch (final ServerApiException exception) {
            Assert.assertEquals("Failed to update config", exception.getDescription());
        }
    }

    @Test
    public void testExecuteForNullResult() {

        updateCfgCmd._configService = configService;

        try {
            Mockito.when(configService.updateConfiguration(updateCfgCmd)).thenReturn(null);
        } catch (final InvalidParameterValueException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            updateCfgCmd.execute();
        } catch (final ServerApiException exception) {
            Assert.assertEquals("Failed to update config", exception.getDescription());
        }
    }

    @Test
    public void testCreateSuccess() {

        final Configuration cfg = Mockito.mock(Configuration.class);
        updateCfgCmd._configService = configService;
        updateCfgCmd._responseGenerator = responseGenerator;

        try {
            Mockito.when(configService.updateConfiguration(updateCfgCmd)).thenReturn(cfg);
        } catch (final Exception e) {
            Assert.fail("Received exception when success expected " + e.getMessage());
        }

        final ConfigurationResponse response = new ConfigurationResponse();
        response.setName("Test case");
        Mockito.when(responseGenerator.createConfigurationResponse(cfg)).thenReturn(response);

        updateCfgCmd.execute();
        Mockito.verify(responseGenerator).createConfigurationResponse(cfg);
        final ConfigurationResponse actualResponse = (ConfigurationResponse) updateCfgCmd.getResponseObject();
        Assert.assertEquals(response, actualResponse);
        Assert.assertEquals("updateconfigurationresponse", response.getResponseName());
    }
}
