package org.apache.cloudstack.framework.async;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncCallbackDispatcher<T, R> implements AsyncCompletionCallback {
    private static final Logger s_logger = LoggerFactory.getLogger(AsyncCallbackDispatcher.class);
    private static final Map<Class, Enhancer> enMap = new HashMap<>();
    private final T _targetObject;
    private Method _callbackMethod;
    private Object _contextObject;
    private Object _resultObject;
    private AsyncCallbackDriver _driver = new InplaceAsyncCallbackDriver();

    private AsyncCallbackDispatcher(final T target) {
        assert (target != null);
        _targetObject = target;
    }

    public static <P, R> AsyncCallbackDispatcher<P, R> create(final P target) {
        return new AsyncCallbackDispatcher<>(target);
    }

    public static boolean dispatch(final Object target, final AsyncCallbackDispatcher callback) {
        assert (callback != null);
        assert (target != null);

        try {
            callback.getCallbackMethod().invoke(target, callback, callback.getContext());
        } catch (final IllegalArgumentException e) {
            throw new RuntimeException("IllegalArgumentException when invoking RPC callback for command: " + callback.getCallbackMethod().getName());
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("IllegalAccessException when invoking RPC callback for command: " + callback.getCallbackMethod().getName());
        } catch (final InvocationTargetException e) {
            throw new RuntimeException("InvocationTargetException when invoking RPC callback for command: " + callback.getCallbackMethod().getName(), e);
        }

        return true;
    }

    public Method getCallbackMethod() {
        return _callbackMethod;
    }

    public <P> P getContext() {
        return (P) _contextObject;
    }

    public AsyncCallbackDispatcher<T, R> setContext(final Object context) {
        _contextObject = context;
        return this;
    }

    public AsyncCallbackDispatcher<T, R> attachDriver(final AsyncCallbackDriver driver) {
        assert (driver != null);
        _driver = driver;

        return this;
    }

    public T getTarget() {
        Class<?> clz = _targetObject.getClass();
        final String clzName = clz.getName();
        if (clzName.contains("EnhancerByCloudStack")) {
            clz = clz.getSuperclass();
        }

        Enhancer en = null;
        synchronized (enMap) {
            en = enMap.get(clz);
            if (en == null) {
                en = new Enhancer();

                en.setSuperclass(clz);
                en.setCallback(new MethodInterceptor() {
                    @Override
                    public Object intercept(final Object arg0, final Method arg1, final Object[] arg2, final MethodProxy arg3) throws Throwable {
                        return null;
                    }
                });
                enMap.put(clz, en);
            }
        }

        try {
            final T t = (T) en.create();
            final Factory factory = (Factory) t;
            factory.setCallback(0, new MethodInterceptor() {
                @Override
                public Object intercept(final Object arg0, final Method arg1, final Object[] arg2, final MethodProxy arg3) throws Throwable {
                    if (arg1.getParameterTypes().length == 0 && arg1.getName().equals("finalize")) {
                        return null;
                    } else {
                        _callbackMethod = arg1;
                        _callbackMethod.setAccessible(true);
                        return null;
                    }
                }
            });
            return t;
        } catch (final Throwable e) {
            s_logger.error("Unexpected exception", e);
        }

        return null;
    }

    public AsyncCallbackDispatcher<T, R> setCallback(final Object useless) {
        return this;
    }

    @Override
    public void complete(final Object resultObject) {
        _resultObject = resultObject;
        _driver.performCompletionCallback(this);
    }

    public R getResult() {
        return (R) _resultObject;
    }

    // for internal use
    Object getTargetObject() {
        return _targetObject;
    }
}
