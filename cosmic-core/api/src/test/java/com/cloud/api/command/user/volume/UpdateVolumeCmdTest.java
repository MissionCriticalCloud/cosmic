package com.cloud.api.command.user.volume;

import com.cloud.api.ResponseGenerator;
import com.cloud.api.ResponseObject;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.VolumeResponse;
import com.cloud.legacymodel.storage.Volume;
import com.cloud.model.enumeration.DiskControllerType;
import com.cloud.storage.VolumeApiService;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class UpdateVolumeCmdTest {

    private VolumeApiService volumeService;
    private ResponseGenerator responseGenerator;
    private UpdateVolumeCmd updateVolumeCmd;

    @Before
    public void setUp() {
        volumeService = Mockito.mock(VolumeApiService.class);
        responseGenerator = Mockito.mock(ResponseGenerator.class);
        updateVolumeCmd = new UpdateVolumeCmd() {
            @Override
            public Long getId() {
                return 1L;
            }
        };
    }

    @Test
    public void testUpdateSuccess() {
        final Volume volume = Mockito.mock(Volume.class);

        updateVolumeCmd._volumeService = volumeService;
        updateVolumeCmd._responseGenerator = responseGenerator;

        Mockito.when(volume.getAccountId()).thenReturn(1L);
        Mockito.when(responseGenerator.findVolumeById(Mockito.anyLong())).thenReturn(volume);
        Mockito.when(volumeService.updateVolume(Mockito.eq(1L), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyLong(), Mockito.anyBoolean(), Mockito.anyString(), Mockito.eq(1L), Mockito.anyString(), Mockito.any(DiskControllerType.class))).thenReturn(volume);

        final VolumeResponse response = new VolumeResponse();
        Mockito.when(responseGenerator.createVolumeResponse(ResponseObject.ResponseView.Restricted, volume)).thenReturn(response);

        updateVolumeCmd.execute();
    }

    @Test(expected = ServerApiException.class)
    public void testUpdateFailure() {
        final Volume volume = Mockito.mock(Volume.class);

        updateVolumeCmd._volumeService = volumeService;
        updateVolumeCmd._responseGenerator = responseGenerator;

        Mockito.when(volume.getAccountId()).thenReturn(1L);
        Mockito.when(responseGenerator.findVolumeById(Mockito.anyLong())).thenReturn(volume);
        Mockito.when(volumeService.updateVolume(Mockito.eq(1L), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyLong(), Mockito.anyBoolean(), Mockito.anyString(), Mockito.eq(1L), Mockito.anyString(), Mockito.any(DiskControllerType.class))).thenReturn(null);

        updateVolumeCmd.execute();
    }
}