package com.cloud.snapshot;

import com.cloud.storage.Snapshot;
import com.cloud.storage.SnapshotVO;
import com.cloud.storage.dao.SnapshotDaoImpl;
import com.cloud.utils.component.ComponentContext;

import javax.inject.Inject;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/SnapshotDaoTestContext.xml")
public class SnapshotDaoTest extends TestCase {
    @Inject
    SnapshotDaoImpl dao;

    @Before
    public void setup() throws Exception {
        ComponentContext.initComponentsLifeCycle();
    }

    @Test
    public void testListBy() {
        final List<SnapshotVO> snapshots = dao.listByInstanceId(3, Snapshot.State.BackedUp);
        for (final SnapshotVO snapshot : snapshots) {
            Assert.assertTrue(snapshot.getState() == Snapshot.State.BackedUp);
        }
    }
}
