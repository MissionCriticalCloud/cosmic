package org.apache.cloudstack.api.command.test;

import org.apache.cloudstack.api.ResponseGenerator;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.command.admin.region.AddRegionCmd;
import org.apache.cloudstack.api.response.RegionResponse;
import org.apache.cloudstack.region.Region;
import org.apache.cloudstack.region.RegionService;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class RegionCmdTest extends TestCase {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private AddRegionCmd addRegionCmd;
    private ResponseGenerator responseGenerator;

    @Override
    @Before
    public void setUp() {

        addRegionCmd = new AddRegionCmd() {

            @Override
            public Integer getId() {
                return 2;
            }

            @Override
            public String getRegionName() {
                return "APAC";
            }
        };
    }

    @Test
    public void testCreateSuccess() {

        final RegionService regionService = Mockito.mock(RegionService.class);

        final Region region = Mockito.mock(Region.class);
        Mockito.when(regionService.addRegion(Matchers.anyInt(), Matchers.anyString(), Matchers.anyString())).thenReturn(region);

        addRegionCmd._regionService = regionService;
        responseGenerator = Mockito.mock(ResponseGenerator.class);

        final RegionResponse regionResponse = Mockito.mock(RegionResponse.class);

        Mockito.when(responseGenerator.createRegionResponse(region)).thenReturn(regionResponse);

        addRegionCmd._responseGenerator = responseGenerator;
        addRegionCmd.execute();
    }

    @Test
    public void testCreateFailure() {

        final RegionService regionService = Mockito.mock(RegionService.class);

        final Region region = Mockito.mock(Region.class);
        Mockito.when(regionService.addRegion(Matchers.anyInt(), Matchers.anyString(), Matchers.anyString())).thenReturn(null);

        addRegionCmd._regionService = regionService;

        try {
            addRegionCmd.execute();
        } catch (final ServerApiException exception) {
            Assert.assertEquals("Failed to add Region", exception.getDescription());
        }
    }
}
