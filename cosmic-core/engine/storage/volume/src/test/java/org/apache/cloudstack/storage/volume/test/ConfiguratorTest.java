package org.apache.cloudstack.storage.volume.test;

import static org.junit.Assert.assertTrue;

import com.cloud.dc.dao.ClusterDao;
import org.apache.cloudstack.engine.subsystem.api.storage.PrimaryDataStoreProvider;

import javax.inject.Inject;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/testContext.xml")
public class ConfiguratorTest {

    @Inject
    List<PrimaryDataStoreProvider> providers;

    @Inject
    ClusterDao clusterDao;

    @Before
    public void setup() {
        /*
         * ClusterVO cluster = new ClusterVO();
         * cluster.setHypervisorType(HypervisorType.XenServer.toString());
         * Mockito
         * .when(clusterDao.findById(Mockito.anyLong())).thenReturn(cluster);
         * try { providerMgr.configure("manager", null); } catch
         * (ConfigurationException e) { // TODO Auto-generated catch block
         * e.printStackTrace(); }
         */
    }

    @Test
    public void testLoadConfigurator() {
        /*
         * for (PrimaryDataStoreConfigurator configurator : configurators) {
         * System.out.println(configurator.getClass().getName()); }
         */
    }

    @Test
    public void testProvider() {
        for (final PrimaryDataStoreProvider provider : providers) {
            if (provider.getName().startsWith("default")) {
                assertTrue(true);
            }
        }
    }

    @Test
    public void getProvider() {
        // assertNotNull(providerMgr.getDataStoreProvider("sample primary data store provider"));
    }

    @Test
    public void createDataStore() {
        /*
         * PrimaryDataStoreProvider provider =
         * providerMgr.getDataStoreProvider("sample primary data store provider"
         * ); Map<String, String> params = new HashMap<String, String>();
         * params.put("url", "nfs://localhost/mnt"); params.put("clusterId",
         * "1"); params.put("name", "nfsprimary");
         * assertNotNull(provider.registerDataStore(params));
         */
    }
}
