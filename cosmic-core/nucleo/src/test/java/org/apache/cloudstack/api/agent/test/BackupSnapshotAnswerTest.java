//

//

package org.apache.cloudstack.api.agent.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.cloud.agent.api.BackupSnapshotAnswer;
import com.cloud.agent.api.BackupSnapshotCommand;
import com.cloud.storage.StoragePool;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class BackupSnapshotAnswerTest {
    private BackupSnapshotCommand bsc;
    private BackupSnapshotAnswer bsa;

    @Before
    public void setUp() {

        final StoragePool pool = Mockito.mock(StoragePool.class);

        bsc =
                new BackupSnapshotCommand("secondaryStoragePoolURL", 101L, 102L, 103L, 104L, 105L, "volumePath", pool, "snapshotUuid", "snapshotName", "prevSnapshotUuid",
                        "prevBackupUuid", false, "vmName", 5);
        bsa = new BackupSnapshotAnswer(bsc, true, "results", "bussname", false);
    }

    @Test
    public void testExecuteInSequence() {
        final boolean b = bsa.executeInSequence();
        assertFalse(b);
    }

    @Test
    public void testIsFull() {
        final boolean b = bsa.isFull();
        assertFalse(b);
    }

    @Test
    public void testGetBackupSnapshotName() {
        final String name = bsa.getBackupSnapshotName();
        assertTrue(name.equals("bussname"));
    }

    @Test
    public void testGetResult() {
        final boolean b = bsa.getResult();
        assertTrue(b);
    }

    @Test
    public void testDetails() {
        final String details = bsa.getDetails();
        assertTrue(details.equals("results"));
    }
}
