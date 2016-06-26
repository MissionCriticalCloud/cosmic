package org.apache.cloudstack.framework.rpc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class RpcCallbackDispatcher<T> {
    private Method _callbackMethod;
    private final T _targetObject;

    private RpcCallbackDispatcher(final T target) {
        _targetObject = target;
    }

    public static <P> RpcCallbackDispatcher<P> create(final P target) {
        return new RpcCallbackDispatcher<>(target);
    }

    public T getTarget() {
        return (T) Enhancer.create(_targetObject.getClass(), new MethodInterceptor() {
            @Override
            public Object intercept(final Object arg0, final Method arg1, final Object[] arg2, final MethodProxy arg3) throws Throwable {
                _callbackMethod = arg1;
                return null;
            }
        });
    }

    public RpcCallbackDispatcher<T> setCallback(final Object useless) {
        return this;
    }

    public boolean dispatch(final RpcClientCall clientCall) {
        assert (clientCall != null);

        if (_callbackMethod == null) {
            return false;
        }

        try {
            _callbackMethod.invoke(_targetObject, clientCall, clientCall.getContext());
        } catch (final IllegalArgumentException e) {
            throw new RpcException("IllegalArgumentException when invoking RPC callback for command: " + clientCall.getCommand());
        } catch (final IllegalAccessException e) {
            throw new RpcException("IllegalAccessException when invoking RPC callback for command: " + clientCall.getCommand());
        } catch (final InvocationTargetException e) {
            throw new RpcException("InvocationTargetException when invoking RPC callback for command: " + clientCall.getCommand());
        }

        return true;
    }
}
