package com.cloud.snapshot;

import com.cloud.cluster.agentlb.dao.HostTransferMapDaoImpl;
import com.cloud.dc.dao.ClusterDaoImpl;
import com.cloud.dc.dao.HostPodDaoImpl;
import com.cloud.host.dao.HostDaoImpl;
import com.cloud.host.dao.HostDetailsDaoImpl;
import com.cloud.host.dao.HostTagsDaoImpl;
import com.cloud.storage.dao.SnapshotDaoImpl;
import com.cloud.storage.dao.VolumeDaoImpl;
import com.cloud.tags.dao.ResourceTagsDaoImpl;
import com.cloud.vm.dao.NicDaoImpl;
import com.cloud.vm.dao.VMInstanceDaoImpl;
import org.apache.cloudstack.test.utils.SpringUtils;

import java.io.IOException;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

@Configuration
@ComponentScan(basePackageClasses = {SnapshotDaoImpl.class, ResourceTagsDaoImpl.class, VMInstanceDaoImpl.class, VolumeDaoImpl.class, NicDaoImpl.class, HostDaoImpl.class,
        HostDetailsDaoImpl.class, HostTagsDaoImpl.class, HostTransferMapDaoImpl.class, ClusterDaoImpl.class, HostPodDaoImpl.class},
        includeFilters = {@Filter(value = SnapshotDaoTestConfiguration.Library.class, type = FilterType.CUSTOM)},
        useDefaultFilters = false)
public class SnapshotDaoTestConfiguration {

    public static class Library implements TypeFilter {

        @Override
        public boolean match(final MetadataReader mdr, final MetadataReaderFactory arg1) throws IOException {
            mdr.getClassMetadata().getClassName();
            final ComponentScan cs = SnapshotDaoTestConfiguration.class.getAnnotation(ComponentScan.class);
            return SpringUtils.includedInBasePackageClasses(mdr.getClassMetadata().getClassName(), cs);
        }
    }
}
