package com.cloud.event;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.event.dao.EventDao;
import com.cloud.legacymodel.acl.ControlledEntity;
import com.cloud.legacymodel.user.Account;
import com.cloud.server.ManagementServerImpl;
import com.cloud.user.AccountManager;

import java.util.Date;
import java.util.List;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventControlsUnitTest extends TestCase {
    private static final Logger s_logger = LoggerFactory.getLogger(EventControlsUnitTest.class);

    @Spy
    ManagementServerImpl _mgmtServer = new ManagementServerImpl();
    @Mock
    AccountManager _accountMgr;
    @Mock
    EventDao _eventDao;
    List<EventVO> _events = null;

    @Override
    @Before
    protected void setUp() {
        MockitoAnnotations.initMocks(this);
        _mgmtServer._eventDao = _eventDao;
        _mgmtServer._accountMgr = _accountMgr;
        doNothing().when(_accountMgr).checkAccess(any(Account.class), any(AccessType.class), any(Boolean.class), any(ControlledEntity.class));
        when(_eventDao.listToArchiveOrDeleteEvents(anyList(), anyString(), any(Date.class), any(Date.class), anyList())).thenReturn(_events);
    }

    @Override
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testInjected() throws Exception {
        s_logger.info("Starting test to archive and delete events");
        archiveEvents();
        deleteEvents();
        s_logger.info("archive/delete events: TEST PASSED");
    }

    protected void archiveEvents() {
        // archive alerts
        doNothing().when(_eventDao).archiveEvents(_events);
    }

    protected void deleteEvents() {
        // delete alerts
    }
}
