package org.apache.cloudstack.api.command.test;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;

import com.cloud.storage.ImageStore;
import com.cloud.storage.StorageService;
import org.apache.cloudstack.api.ResponseGenerator;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.command.admin.storage.AddImageStoreCmd;
import org.apache.cloudstack.api.response.ImageStoreResponse;

import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

public class AddSecondaryStorageCmdTest extends TestCase {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private AddImageStoreCmd addImageStoreCmd;

    @Override
    @Before
    public void setUp() {
        addImageStoreCmd = new AddImageStoreCmd() {
        };
    }

    @Test
    public void testExecuteForResult() throws Exception {

        final StorageService resourceService = Mockito.mock(StorageService.class);
        addImageStoreCmd._storageService = resourceService;

        final ImageStore store = Mockito.mock(ImageStore.class);

        Mockito.when(resourceService.discoverImageStore(anyString(), anyString(), anyString(), anyLong(), (Map) anyObject()))
               .thenReturn(store);

        final ResponseGenerator responseGenerator = Mockito.mock(ResponseGenerator.class);
        addImageStoreCmd._responseGenerator = responseGenerator;

        final ImageStoreResponse responseHost = new ImageStoreResponse();
        responseHost.setName("Test");

        Mockito.when(responseGenerator.createImageStoreResponse(store)).thenReturn(responseHost);

        addImageStoreCmd.execute();

        Mockito.verify(responseGenerator).createImageStoreResponse(store);

        final ImageStoreResponse actualResponse = (ImageStoreResponse) addImageStoreCmd.getResponseObject();

        Assert.assertEquals(responseHost, actualResponse);
        Assert.assertEquals("addimagestoreresponse", actualResponse.getResponseName());
    }

    @Test
    public void testExecuteForNullResult() throws Exception {

        final StorageService resourceService = Mockito.mock(StorageService.class);
        addImageStoreCmd._storageService = resourceService;

        Mockito.when(resourceService.discoverImageStore(anyString(), anyString(), anyString(), anyLong(), (Map) anyObject()))
               .thenReturn(null);

        try {
            addImageStoreCmd.execute();
        } catch (final ServerApiException exception) {
            Assert.assertEquals("Failed to add secondary storage", exception.getDescription());
        }
    }
}
