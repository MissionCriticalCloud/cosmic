//

//

package com.cloud.network.resource.wrapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CheckHealthCommand;
import com.cloud.network.nicira.ControlClusterStatus;
import com.cloud.network.nicira.NiciraNvpApi;
import com.cloud.network.nicira.NiciraNvpApiException;
import com.cloud.network.resource.NiciraNvpResource;

import org.junit.Before;
import org.junit.Test;

public class NiciraCheckHealthCommandWrapperTest {

    private final NiciraNvpResource niciraResource = mock(NiciraNvpResource.class);
    private final NiciraNvpApi niciraApi = mock(NiciraNvpApi.class);

    @Before
    public void setup() {
        when(niciraResource.getNiciraNvpApi()).thenReturn(niciraApi);
    }

    @Test
    public void tetsExecuteWhenClusterIsNotStable() throws Exception {
        when(niciraApi.getControlClusterStatus()).thenReturn(new ControlClusterStatus());

        final NiciraCheckHealthCommandWrapper commandWrapper = new NiciraCheckHealthCommandWrapper();
        final Answer answer = commandWrapper.execute(new CheckHealthCommand(), niciraResource);

        assertThat(answer.getResult(), equalTo(false));
    }

    @Test
    public void tetsExecuteWhenApiThrowsException() throws Exception {
        when(niciraApi.getControlClusterStatus()).thenThrow(NiciraNvpApiException.class);

        final NiciraCheckHealthCommandWrapper commandWrapper = new NiciraCheckHealthCommandWrapper();
        final Answer answer = commandWrapper.execute(new CheckHealthCommand(), niciraResource);

        assertThat(answer.getResult(), equalTo(false));
    }

    @Test
    public void tetsExecuteWhenClusterIsStable() throws Exception {
        final ControlClusterStatus statusValue = mock(ControlClusterStatus.class);
        when(statusValue.getClusterStatus()).thenReturn("stable");
        when(niciraApi.getControlClusterStatus()).thenReturn(statusValue);

        final NiciraCheckHealthCommandWrapper commandWrapper = new NiciraCheckHealthCommandWrapper();
        final Answer answer = commandWrapper.execute(new CheckHealthCommand(), niciraResource);

        assertThat(answer.getResult(), equalTo(true));
    }
}
