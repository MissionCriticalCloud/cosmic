package com.cloud.vm;

import com.cloud.serializer.GsonHelper;
import com.cloud.utils.Pair;
import org.apache.cloudstack.framework.jobs.impl.JobSerializerHelper;
import org.apache.cloudstack.jobs.JobInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VmWorkJobHandlerProxy can not be used as standalone due to run-time
 * reflection usage in its implementation, run-time reflection conflicts with Spring proxy mode.
 * It means that we can not instantiate VmWorkJobHandlerProxy beans directly in Spring and expect
 * it can handle VmWork directly from there.
 */
public class VmWorkJobHandlerProxy implements VmWorkJobHandler {

    private static final Logger s_logger = LoggerFactory.getLogger(VmWorkJobHandlerProxy.class);

    private final Object _target;
    private final Map<Class<?>, Method> _handlerMethodMap = new HashMap<>();

    private final Gson _gsonLogger;

    public VmWorkJobHandlerProxy(final Object target) {
        _gsonLogger = GsonHelper.getGsonLogger();

        buildLookupMap(target.getClass());
        _target = target;
    }

    private void buildLookupMap(final Class<?> hostClass) {
        Class<?> clz = hostClass;
        while (clz != null && clz != Object.class) {
            final Method[] hostHandlerMethods = clz.getDeclaredMethods();

            for (final Method method : hostHandlerMethods) {
                if (isVmWorkJobHandlerMethod(method)) {
                    final Class<?> paramType = method.getParameterTypes()[0];
                    assert (_handlerMethodMap.get(paramType) == null);

                    method.setAccessible(true);
                    _handlerMethodMap.put(paramType, method);
                }
            }

            clz = clz.getSuperclass();
        }
    }

    private boolean isVmWorkJobHandlerMethod(final Method method) {
        if (method.getParameterTypes().length != 1) {
            return false;
        }

        final Class<?> returnType = method.getReturnType();
        if (!Pair.class.isAssignableFrom(returnType)) {
            return false;
        }

        final Class<?> paramType = method.getParameterTypes()[0];
        if (!VmWork.class.isAssignableFrom(paramType)) {
            return false;
        }

        return true;
    }

    @Override
    public Pair<JobInfo.Status, String> handleVmWorkJob(final VmWork work) throws Exception {

        final Method method = getHandlerMethod(work.getClass());
        if (method != null) {

            try {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Execute VM work job: " + work.getClass().getName() + _gsonLogger.toJson(work));
                }

                final Object obj = method.invoke(_target, work);

                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Done executing VM work job: " + work.getClass().getName() + _gsonLogger.toJson(work));
                }

                assert (obj instanceof Pair);
                return (Pair<JobInfo.Status, String>) obj;
            } catch (final InvocationTargetException e) {
                s_logger.error("Invocation exception, caused by: " + e.getCause());

                // legacy CloudStack code relies on checked exception for error handling
                // we need to re-throw the real exception here
                if (e.getCause() != null && e.getCause() instanceof Exception) {
                    s_logger.info("Rethrow exception " + e.getCause());
                    throw (Exception) e.getCause();
                }

                throw e;
            }
        } else {
            s_logger.error("Unable to find handler for VM work job: " + work.getClass().getName() + _gsonLogger.toJson(work));

            final RuntimeException ex = new RuntimeException("Unable to find handler for VM work job: " + work.getClass().getName());
            return new Pair<>(JobInfo.Status.FAILED, JobSerializerHelper.toObjectSerializedString(ex));
        }
    }

    private Method getHandlerMethod(final Class<?> paramType) {
        return _handlerMethodMap.get(paramType);
    }
}
