package com.cloud.vm.dao;

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
@ComponentScan(basePackageClasses = {UserVmCloneSettingDaoImpl.class}, includeFilters = {@Filter(value = UserVmCloneSettingDaoTestConfiguration.Library.class,
        type = FilterType.CUSTOM)}, useDefaultFilters = false)
public class UserVmCloneSettingDaoTestConfiguration {

    public static class Library implements TypeFilter {

        @Override
        public boolean match(final MetadataReader mdr, final MetadataReaderFactory arg1) throws IOException {
            mdr.getClassMetadata().getClassName();
            final ComponentScan cs = UserVmCloneSettingDaoTestConfiguration.class.getAnnotation(ComponentScan.class);
            return SpringUtils.includedInBasePackageClasses(mdr.getClassMetadata().getClassName(), cs);
        }
    }
}
