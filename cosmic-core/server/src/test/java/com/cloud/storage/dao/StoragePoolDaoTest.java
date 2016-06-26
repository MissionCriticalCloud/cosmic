package com.cloud.storage.dao;

import com.cloud.storage.StoragePoolStatus;
import org.apache.cloudstack.storage.datastore.db.PrimaryDataStoreDaoImpl;

import javax.inject.Inject;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/StoragePoolDaoTestContext.xml")
public class StoragePoolDaoTest extends TestCase {
    @Inject
    PrimaryDataStoreDaoImpl dao;

    @Test
    public void testCountByStatus() {
        final long count = dao.countPoolsByStatus(StoragePoolStatus.Up);
        System.out.println("Found " + count + " storage pools");
    }
}
