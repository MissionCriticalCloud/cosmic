//

package com.cloud.template;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cloud.engine.subsystem.api.storage.DataStore;
import com.cloud.engine.subsystem.api.storage.TemplateDataFactory;
import com.cloud.engine.subsystem.api.storage.TemplateInfo;
import com.cloud.engine.subsystem.api.storage.TemplateService;
import com.cloud.engine.subsystem.api.storage.TemplateService.TemplateApiResult;
import com.cloud.framework.async.AsyncCallFuture;
import com.cloud.framework.events.Event;
import com.cloud.framework.messagebus.MessageBus;
import com.cloud.legacymodel.storage.VMTemplateStorageResourceAssoc.Status;
import com.cloud.model.enumeration.ImageFormat;
import com.cloud.storage.TemplateProfile;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.dao.VMTemplateZoneDao;
import com.cloud.storage.datastore.db.TemplateDataStoreDao;
import com.cloud.storage.datastore.db.TemplateDataStoreVO;
import com.cloud.storage.image.datastore.ImageStoreEntity;
import com.cloud.user.AccountVO;
import com.cloud.user.dao.AccountDao;
import com.cloud.utils.component.ComponentContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ComponentContext.class)
public class HypervisorTemplateAdapterTest {
    List<Event> events = new ArrayList<>();
    @Mock
    TemplateManager _templateMgr;
    @Mock
    TemplateService _templateService;
    @Mock
    TemplateDataFactory _dataFactory;
    @Mock
    TemplateDataStoreDao _templateStoreDao;
    @Mock
    AccountDao _accountDao;
    @Mock
    VMTemplateZoneDao _templateZoneDao;
    @Mock
    MessageBus _messageBus;
    @InjectMocks
    HypervisorTemplateAdapter _adapter;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testEmitDeleteEventUuid() throws InterruptedException, ExecutionException {
        //All the mocks required for this test to work.
        final ImageStoreEntity store = mock(ImageStoreEntity.class);
        when(store.getId()).thenReturn(1l);
        when(store.getDataCenterId()).thenReturn(1l);
        when(store.getName()).thenReturn("Test Store");

        final TemplateDataStoreVO dataStoreVO = mock(TemplateDataStoreVO.class);
        when(dataStoreVO.getDownloadState()).thenReturn(Status.DOWNLOADED);

        final TemplateInfo info = mock(TemplateInfo.class);
        when(info.getDataStore()).thenReturn(store);

        final VMTemplateVO template = mock(VMTemplateVO.class);
        when(template.getId()).thenReturn(1l);
        when(template.getName()).thenReturn("Test Template");
        when(template.getFormat()).thenReturn(ImageFormat.QCOW2);
        when(template.getAccountId()).thenReturn(1l);
        when(template.getUuid()).thenReturn("Test UUID"); //TODO possibly return this from method for comparison, if things work how i want

        final TemplateProfile profile = mock(TemplateProfile.class);
        when(profile.getTemplate()).thenReturn(template);
        when(profile.getZoneId()).thenReturn(1l);

        final TemplateApiResult result = mock(TemplateApiResult.class);
        when(result.isSuccess()).thenReturn(true);
        when(result.isFailed()).thenReturn(false);

        final AsyncCallFuture<TemplateApiResult> future = mock(AsyncCallFuture.class);
        when(future.get()).thenReturn(result);

        final AccountVO acct = mock(AccountVO.class);
        when(acct.getId()).thenReturn(1l);
        when(acct.getDomainId()).thenReturn(1l);

        when(_templateMgr.getImageStoreByTemplate(anyLong(), anyLong())).thenReturn(Collections.singletonList((DataStore) store));
        when(_templateStoreDao.listByTemplateStore(anyLong(), anyLong())).thenReturn(Collections.singletonList(dataStoreVO));
        when(_dataFactory.getTemplate(anyLong(), any(DataStore.class))).thenReturn(info);
        when(_dataFactory.listTemplateOnCache(anyLong())).thenReturn(Collections.singletonList(info));
        when(_templateService.deleteTemplateAsync(any(TemplateInfo.class))).thenReturn(future);
        when(_accountDao.findById(anyLong())).thenReturn(acct);
        when(_accountDao.findByIdIncludingRemoved(anyLong())).thenReturn(acct);

        _adapter.delete(profile);
        Assert.assertNotNull(events);
        Assert.assertEquals(0, events.size());
    }
}
