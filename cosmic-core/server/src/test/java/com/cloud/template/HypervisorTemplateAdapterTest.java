//

package com.cloud.template;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cloud.dc.dao.DataCenterDao;
import com.cloud.event.EventTypes;
import com.cloud.event.UsageEventUtils;
import com.cloud.event.UsageEventVO;
import com.cloud.event.dao.UsageEventDao;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.TemplateProfile;
import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.dao.VMTemplateZoneDao;
import com.cloud.user.AccountVO;
import com.cloud.user.ResourceLimitService;
import com.cloud.user.dao.AccountDao;
import com.cloud.utils.component.ComponentContext;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.TemplateDataFactory;
import org.apache.cloudstack.engine.subsystem.api.storage.TemplateInfo;
import org.apache.cloudstack.engine.subsystem.api.storage.TemplateService;
import org.apache.cloudstack.engine.subsystem.api.storage.TemplateService.TemplateApiResult;
import org.apache.cloudstack.framework.async.AsyncCallFuture;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.framework.events.Event;
import org.apache.cloudstack.framework.events.EventBus;
import org.apache.cloudstack.framework.events.EventBusException;
import org.apache.cloudstack.framework.messagebus.MessageBus;
import org.apache.cloudstack.storage.datastore.db.TemplateDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.TemplateDataStoreVO;
import org.apache.cloudstack.storage.image.datastore.ImageStoreEntity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ComponentContext.class)
public class HypervisorTemplateAdapterTest {
    //UsageEventUtils reflection abuse helpers
    private final Map<String, Object> oldFields = new HashMap<>();
    private final List<UsageEventVO> usageEvents = new ArrayList<>();
    @Mock
    EventBus _bus;
    List<Event> events = new ArrayList<>();
    @Mock
    TemplateManager _templateMgr;
    @Mock
    TemplateService _templateService;
    @Mock
    TemplateDataFactory _dataFactory;
    @Mock
    VMTemplateZoneDao _templateZoneDao;
    @Mock
    TemplateDataStoreDao _templateStoreDao;
    @Mock
    UsageEventDao _usageEventDao;
    @Mock
    ResourceLimitService _resourceManager;
    @Mock
    MessageBus _messageBus;
    @Mock
    AccountDao _accountDao;
    @Mock
    DataCenterDao _dcDao;
    @Mock
    ConfigurationDao _configDao;
    @InjectMocks
    HypervisorTemplateAdapter _adapter;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testEmitDeleteEventUuid() throws InterruptedException, ExecutionException, EventBusException {
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

        final
        AsyncCallFuture<TemplateApiResult> future = mock(AsyncCallFuture.class);
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

        //Test actually begins here.
        setupUsageUtils();

        _adapter.delete(profile);
        Assert.assertNotNull(usageEvents);
        Assert.assertNotNull(events);
        Assert.assertEquals(1, events.size());

        final Event event = events.get(0);
        Assert.assertNotNull(event);
        Assert.assertNotNull(event.getResourceType());
        Assert.assertEquals(VirtualMachineTemplate.class.getName(), event.getResourceType());
        Assert.assertNotNull(event.getResourceUUID());
        Assert.assertEquals("Test UUID", event.getResourceUUID());
        Assert.assertEquals(EventTypes.EVENT_TEMPLATE_DELETE, event.getEventType());

        cleanupUsageUtils();
    }

    public UsageEventUtils setupUsageUtils() throws EventBusException {
        Mockito.when(_configDao.getValue(eq("publish.usage.events"))).thenReturn("true");
        Mockito.when(_usageEventDao.persist(Mockito.any(UsageEventVO.class))).then(new Answer<Void>() {
            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable {
                final UsageEventVO vo = (UsageEventVO) invocation.getArguments()[0];
                usageEvents.add(vo);
                return null;
            }
        });

        Mockito.when(_usageEventDao.listAll()).thenReturn(usageEvents);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable {
                final Event event = (Event) invocation.getArguments()[0];
                events.add(event);
                return null;
            }
        }).when(_bus).publish(any(Event.class));

        PowerMockito.mockStatic(ComponentContext.class);
        when(ComponentContext.getComponent(eq(EventBus.class))).thenReturn(_bus);

        final UsageEventUtils utils = new UsageEventUtils();

        final Map<String, String> usageUtilsFields = new HashMap<String, String>();
        usageUtilsFields.put("usageEventDao", "_usageEventDao");
        usageUtilsFields.put("accountDao", "_accountDao");
        usageUtilsFields.put("dcDao", "_dcDao");
        usageUtilsFields.put("configDao", "_configDao");

        for (final String fieldName : usageUtilsFields.keySet()) {
            try {
                final Field f = UsageEventUtils.class.getDeclaredField(fieldName);
                f.setAccessible(true);
                //Remember the old fields for cleanup later (see cleanupUsageUtils)
                final Field staticField = UsageEventUtils.class.getDeclaredField("s_" + fieldName);
                staticField.setAccessible(true);
                oldFields.put(f.getName(), staticField.get(null));
                f.set(utils,
                        this.getClass()
                            .getDeclaredField(
                                    usageUtilsFields.get(fieldName))
                            .get(this));
            } catch (IllegalArgumentException | IllegalAccessException
                    | NoSuchFieldException | SecurityException e) {
                e.printStackTrace();
            }
        }
        try {
            final Method method = UsageEventUtils.class.getDeclaredMethod("init");
            method.setAccessible(true);
            method.invoke(utils);
        } catch (SecurityException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException e) {
            e.printStackTrace();
        }

        return utils;
    }

    public void cleanupUsageUtils() {
        final UsageEventUtils utils = new UsageEventUtils();

        for (final String fieldName : oldFields.keySet()) {
            try {
                final Field f = UsageEventUtils.class.getDeclaredField(fieldName);
                f.setAccessible(true);
                f.set(utils, oldFields.get(fieldName));
            } catch (IllegalArgumentException | IllegalAccessException
                    | NoSuchFieldException | SecurityException e) {
                e.printStackTrace();
            }
        }
        try {
            final Method method = UsageEventUtils.class.getDeclaredMethod("init");
            method.setAccessible(true);
            method.invoke(utils);
        } catch (SecurityException | NoSuchMethodException
                | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
