//

//

package com.cloud.utils.component;

import com.cloud.utils.Pair;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;

public class ComponentInstantiationPostProcessor implements InstantiationAwareBeanPostProcessor {
    private static final Logger s_logger = LoggerFactory.getLogger(ComponentInstantiationPostProcessor.class);
    private final Callback[] _callbacks;
    private final CallbackFilter _callbackFilter;
    private List<ComponentMethodInterceptor> _interceptors = new ArrayList<>();

    public ComponentInstantiationPostProcessor() {
        _callbacks = new Callback[2];
        _callbacks[0] = NoOp.INSTANCE;
        _callbacks[1] = new InterceptorDispatcher();

        _callbackFilter = new InterceptorFilter();
    }

    public List<ComponentMethodInterceptor> getInterceptors() {
        return _interceptors;
    }

    public void setInterceptors(final List<ComponentMethodInterceptor> interceptors) {
        _interceptors = interceptors;
    }

    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessBeforeInstantiation(final Class<?> beanClass, final String beanName) throws BeansException {
        if (_interceptors != null && _interceptors.size() > 0) {
            if (ComponentMethodInterceptable.class.isAssignableFrom(beanClass)) {
                final Enhancer enhancer = new Enhancer();
                enhancer.setSuperclass(beanClass);
                enhancer.setCallbacks(getCallbacks());
                enhancer.setCallbackFilter(getCallbackFilter());
                enhancer.setNamingPolicy(ComponentNamingPolicy.INSTANCE);

                final Object bean = enhancer.create();
                return bean;
            }
        }
        return null;
    }

    private Callback[] getCallbacks() {
        return _callbacks;
    }

    private CallbackFilter getCallbackFilter() {
        return _callbackFilter;
    }

    @Override
    public boolean postProcessAfterInstantiation(final Object bean, final String beanName) throws BeansException {
        return true;
    }

    @Override
    public PropertyValues postProcessPropertyValues(final PropertyValues pvs, final PropertyDescriptor[] pds, final Object bean, final String beanName) throws BeansException {
        return pvs;
    }

    protected class InterceptorDispatcher implements MethodInterceptor {
        @Override
        public Object intercept(final Object target, final Method method, final Object[] args, final MethodProxy methodProxy) throws Throwable {
            final ArrayList<Pair<ComponentMethodInterceptor, Object>> interceptors = new ArrayList<>();

            for (final ComponentMethodInterceptor interceptor : getInterceptors()) {
                if (interceptor.needToIntercept(method)) {
                    final Object objReturnedInInterceptStart = interceptor.interceptStart(method, target);
                    interceptors.add(new Pair<>(interceptor, objReturnedInInterceptStart));
                }
            }
            boolean success = false;
            try {
                final Object obj = methodProxy.invokeSuper(target, args);
                success = true;
                return obj;
            } finally {
                for (final Pair<ComponentMethodInterceptor, Object> interceptor : interceptors) {
                    if (success) {
                        interceptor.first().interceptComplete(method, target, interceptor.second());
                    } else {
                        interceptor.first().interceptException(method, target, interceptor.second());
                    }
                }
            }
        }
    }

    protected class InterceptorFilter implements CallbackFilter {
        @Override
        public int accept(final Method method) {
            for (final ComponentMethodInterceptor interceptor : getInterceptors()) {

                if (interceptor.needToIntercept(method)) {
                    return 1;
                }
            }
            return 0;
        }
    }
}
