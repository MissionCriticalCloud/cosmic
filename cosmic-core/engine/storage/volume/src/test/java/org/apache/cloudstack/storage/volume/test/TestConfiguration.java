package org.apache.cloudstack.storage.volume.test;

import com.cloud.dc.dao.ClusterDao;
import com.cloud.dc.dao.ClusterDaoImpl;
import org.apache.cloudstack.storage.image.motion.ImageMotionService;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfiguration {
    @Bean
    public ImageMotionService imageMotion() {
        return Mockito.mock(ImageMotionService.class);
    }

    @Bean
    public ClusterDao clusterDao() {
        return Mockito.mock(ClusterDaoImpl.class);
    }
}
