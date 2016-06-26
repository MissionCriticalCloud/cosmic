//

//

package org.apache.cloudstack.api.agent.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.cloud.agent.api.BackupSnapshotCommand;
import com.cloud.agent.api.to.SwiftTO;
import com.cloud.hypervisor.Hypervisor;
import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.storage.StoragePool;
import com.cloud.storage.StoragePoolStatus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

public class BackupSnapshotCommandTest {
    public StoragePool pool = new StoragePool() {
        @Override
        public long getId() {
            return 1L;
        }

        @Override
        public String getName() {
            return "name";
        }

        @Override
        public StoragePoolType getPoolType() {
            return StoragePoolType.Filesystem;
        }

        @Override
        public Date getCreated() {
            Date date = null;
            try {
                date = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").parse("01/01/1970 12:12:12");
            } catch (final ParseException e) {
                e.printStackTrace();
            }
            return date;
        }

        @Override
        public Date getUpdateTime() {
            return new Date();
        }

        @Override
        public long getDataCenterId() {
            return 0L;
        }

        @Override
        public long getCapacityBytes() {
            return 0L;
        }

        @Override
        public long getUsedBytes() {
            return 0L;
        }

        @Override
        public Long getCapacityIops() {
            return 0L;
        }

        @Override
        public Long getClusterId() {
            return 0L;
        }

        @Override
        public String getHostAddress() {
            return "hostAddress";
        }

        @Override
        public String getPath() {
            return "path";
        }

        @Override
        public String getUserInfo() {
            return "userInfo";
        }

        @Override
        public boolean isShared() {
            return false;
        }

        @Override
        public boolean isLocal() {
            return false;
        }

        @Override
        public StoragePoolStatus getStatus() {
            return StoragePoolStatus.Up;
        }

        @Override
        public int getPort() {
            return 25;
        }

        @Override
        public Long getPodId() {
            return 0L;
        }

        @Override
        public String getStorageProviderName() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isInMaintenance() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public Hypervisor.HypervisorType getHypervisor() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getUuid() {
            return "bed9f83e-cac3-11e1-ac8a-0050568b007e";
        }
    };

    BackupSnapshotCommand bsc = new BackupSnapshotCommand("http://secondary.Storage.Url", 101L, 102L, 103L, 104L, 105L, "vPath", pool,
            "420fa39c-4ef1-a83c-fd93-46dc1ff515ae", "sName", "9012793e-0657-11e2-bebc-0050568b0057", "7167e0b2-f5b0-11e1-8414-0050568b0057", false, "vmName", 5);

    BackupSnapshotCommand bsc1 = new BackupSnapshotCommand("http://secondary.Storage.Url", 101L, 102L, 103L, 104L, 105L, "vPath", pool,
            "420fa39c-4ef1-a83c-fd93-46dc1ff515ae", "sName", "9012793e-0657-11e2-bebc-0050568b0057", "7167e0b2-f5b0-11e1-8414-0050568b0057", false, "vmName", 5);

    @Test
    public void testGetSecondaryStorageUrl() {
        final String url = bsc.getSecondaryStorageUrl();
        assertTrue(url.equals("http://secondary.Storage.Url"));
    }

    @Test
    public void testGetDataCenterId() {
        final Long dcId = bsc.getDataCenterId();
        final Long expected = 101L;
        assertEquals(expected, dcId);
    }

    @Test
    public void testGetAccountId() {
        final Long aId = bsc.getAccountId();
        final Long expected = 102L;
        assertEquals(expected, aId);
    }

    @Test
    public void testGetVolumeId() {
        final Long vId = bsc.getVolumeId();
        final Long expected = 103L;
        assertEquals(expected, vId);
    }

    @Test
    public void testGetSnapshotId() {
        final Long ssId = bsc.getSnapshotId();
        final Long expected = 104L;
        assertEquals(expected, ssId);
    }

    @Test
    public void testGetCreated() {
        try {
            final Date date = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").parse("01/01/1970 12:12:12");
            final Date d = pool.getCreated();
            assertTrue(d.compareTo(date) == 0);
        } catch (final ParseException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetSwift() {
        final SwiftTO s1 = new SwiftTO();
        bsc.setSwift(s1);
        final SwiftTO s2 = bsc.getSwift();
        assertEquals(s1, s2);
    }

    @Test
    public void testGetSnapshotName() {
        final String ssName = bsc.getSnapshotName();
        assertTrue(ssName.equals("sName"));
    }

    @Test
    public void testGetSnapshotUuid() {
        final String uuid = bsc.getSnapshotUuid();
        assertTrue(uuid.equals("420fa39c-4ef1-a83c-fd93-46dc1ff515ae"));
    }

    @Test
    public void testGetPrevSnapshotUuid() {
        final String uuid = bsc.getPrevSnapshotUuid();
        assertTrue(uuid.equals("9012793e-0657-11e2-bebc-0050568b0057"));
    }

    @Test
    public void testGetPrevBackupUuid() {
        final String uuid = bsc.getPrevBackupUuid();
        assertTrue(uuid.equals("7167e0b2-f5b0-11e1-8414-0050568b0057"));
    }

    @Test
    public void testGetVolumePath() {
        String path = bsc.getVolumePath();
        assertTrue(path.equals("vPath"));

        bsc.setVolumePath("vPath1");
        path = bsc.getVolumePath();
        assertTrue(path.equals("vPath1"));

        bsc1.setVolumePath("vPath2");
        path = bsc1.getVolumePath();
        assertTrue(path.equals("vPath2"));
    }

    @Test
    public void testExecuteInSequence() {
        boolean b = bsc.executeInSequence();
        assertFalse(b);

        b = bsc1.executeInSequence();
        assertFalse(b);
    }
}
