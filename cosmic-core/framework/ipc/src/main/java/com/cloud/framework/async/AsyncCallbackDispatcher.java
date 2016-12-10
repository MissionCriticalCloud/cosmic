package com.cloud.framework.async;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;

public class AsyncCallbackDispatcher<T, R> implements AsyncCompletionCallback {
    private static final Map<Class, Enhancer> enMap = new HashMap<>();
    private final T _targetObject;
    private Method _callbackMethod;
    private Object _contextObject;
    private Object _resultObject;
    private final AsyncCallbackDriver _driver = new InplaceAsyncCallbackDriver();

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
                en.setCallback((MethodInterceptor) (arg0, arg1, arg2, arg3) -> null);
                enMap.put(clz, en);
            }
        }

        final T t = (T) en.create();
        final Factory factory = (Factory) t;
        factory.setCallback(0, (MethodInterceptor) (arg0, arg1, arg2, arg3) -> {
            if (arg1.getParameterTypes().length == 0 && arg1.getName().equals("finalize")) {
                return null;
            } else {
                _callbackMethod = arg1;
                _callbackMethod.setAccessible(true);
                return null;
            }
        });
        return t;
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
