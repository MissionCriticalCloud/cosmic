package org.apache.cloudstack.test.utils;

import com.cloud.utils.component.ComponentContext;
import com.cloud.utils.component.ComponentInstantiationPostProcessor;
import com.cloud.utils.component.ComponentMethodInterceptor;
import com.cloud.utils.db.TransactionContextBuilder;
import com.cloud.utils.exception.CloudRuntimeException;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

public class SpringUtils {

    /**
     * This method allows you to use @ComponentScan for your unit testing but
     * it limits the scope of the classes found to the class specified in
     * the @ComponentScan annotation.
     * <p>
     * Without using this method, the default behavior of @ComponentScan is
     * to actually scan in the package of the class specified rather than
     * only the class. This can cause extra classes to be loaded which causes
     * the classes these extra classes depend on to be loaded. The end effect
     * is often most of the project gets loaded.
     * <p>
     * In order to use this method properly, you must do the following: <li>
     * - Specify @ComponentScan with basePackageClasses, includeFilters, and
     * useDefaultFilters=true.  See the following example.
     * <p>
     * <pre>
     *     @ComponentScan(basePackageClasses={AffinityGroupServiceImpl.class, EventUtils.class},
     *     includeFilters={@Filter(value=TestConfiguration.Library.class, type=FilterType.CUSTOM)},
     *     useDefaultFilters=false)
     * </pre>
     * <p>
     * - Create a Library class and use that to call this method.  See the
     * following example.  The Library class you define here is the Library
     * class being added in the filter above.
     * <p>
     * <pre>
     * public static class Library implements TypeFilter {
     *      @Override
     *      public boolean match(MetadataReader mdr, MetadataReaderFactory arg1) throws IOException {
     *          ComponentScan cs = TestConfiguration.class.getAnnotation(ComponentScan.class);
     *          return SpringUtils.includedInBasePackageClasses(mdr.getClassMetadata().getClassName(), cs);
     *      }
     * }
     * </pre>
     *
     * @param clazzName name of the class that should be included in the Spring components
     * @param cs        ComponentScan annotation that was declared on the configuration
     * @return
     */
    public static boolean includedInBasePackageClasses(final String clazzName, final ComponentScan cs) {
        final Class<?> clazzToCheck;
        try {
            clazzToCheck = Class.forName(clazzName);
        } catch (final ClassNotFoundException e) {
            throw new CloudRuntimeException("Unable to find " + clazzName);
        }
        final Class<?>[] clazzes = cs.basePackageClasses();
        for (final Class<?> clazz : clazzes) {
            if (clazzToCheck.isAssignableFrom(clazz)) {
                return true;
            }
        }
        return false;
    }

    public static class CloudStackTestConfiguration {

        @Bean
        public ComponentContext componentContext() {
            return new ComponentContext();
        }

        @Bean
        public TransactionContextBuilder transactionContextBuilder() {
            return new TransactionContextBuilder();
        }

        @Bean
        public ComponentInstantiationPostProcessor instantiatePostProcessor() {
            final ComponentInstantiationPostProcessor processor = new ComponentInstantiationPostProcessor();

            final List<ComponentMethodInterceptor> interceptors = new ArrayList<>();
            interceptors.add(new TransactionContextBuilder());
            processor.setInterceptors(interceptors);

            return processor;
        }
    }
}
