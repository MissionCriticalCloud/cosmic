package org.apache.cloudstack.api.command.test;

import com.cloud.utils.Pair;
import org.apache.cloudstack.api.command.admin.usage.GetUsageRecordsCmd;
import org.apache.cloudstack.usage.Usage;
import org.apache.cloudstack.usage.UsageService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

public class UsageCmdTest extends TestCase {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private GetUsageRecordsCmd getUsageRecordsCmd;

    @Override
    @Before
    public void setUp() {

        getUsageRecordsCmd = new GetUsageRecordsCmd() {

        };
    }

    @Test
    public void testExecuteSuccess() {
        final UsageService usageService = Mockito.mock(UsageService.class);
        getUsageRecordsCmd._usageService = usageService;
        getUsageRecordsCmd.execute();
    }

    @Test
    public void testExecuteEmptyResult() {

        final UsageService usageService = Mockito.mock(UsageService.class);

        final Pair<List<? extends Usage>, Integer> usageRecords = new Pair<>(new ArrayList<>(), new Integer(0));

        Mockito.when(usageService.getUsageRecords(getUsageRecordsCmd)).thenReturn(usageRecords);

        getUsageRecordsCmd._usageService = usageService;
        getUsageRecordsCmd.execute();
    }

    @Test
    public void testCrud() {
        getUsageRecordsCmd.setDomainId(1L);
        assertTrue(getUsageRecordsCmd.getDomainId().equals(1L));

        getUsageRecordsCmd.setAccountName("someAccount");
        assertTrue(getUsageRecordsCmd.getAccountName().equals("someAccount"));

        final Date d = new Date();
        getUsageRecordsCmd.setStartDate(d);
        getUsageRecordsCmd.setEndDate(d);
        assertTrue(getUsageRecordsCmd.getStartDate().equals(d));
        assertTrue(getUsageRecordsCmd.getEndDate().equals(d));

        getUsageRecordsCmd.setUsageId("someId");
        assertTrue(getUsageRecordsCmd.getUsageId().equals("someId"));
    }
}
