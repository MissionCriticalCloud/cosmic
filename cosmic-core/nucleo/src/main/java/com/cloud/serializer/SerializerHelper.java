//

//

package com.cloud.serializer;

import com.cloud.utils.DateUtil;
import com.cloud.utils.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Note: toPairList and appendPairList only support simple POJO objects currently
 */
public class SerializerHelper {
    public static final Logger s_logger = LoggerFactory.getLogger(SerializerHelper.class.getName());
    public static final String token = "/";

    public static String toSerializedStringOld(final Object result) {
        if (result != null) {
            final Class<?> clz = result.getClass();
            final Gson gson = GsonHelper.getGson();
            return clz.getName() + token + gson.toJson(result);
        }
        return null;
    }

    public static Object fromSerializedString(final String result) {
        try {
            if (result != null && !result.isEmpty()) {

                final String[] serializedParts = result.split(token);

                if (serializedParts.length < 2) {
                    return null;
                }
                final String clzName = serializedParts[0];
                String nameField = null;
                String content = null;
                if (serializedParts.length == 2) {
                    content = serializedParts[1];
                } else {
                    nameField = serializedParts[1];
                    final int index = result.indexOf(token + nameField + token);
                    content = result.substring(index + nameField.length() + 2);
                }

                final Class<?> clz;
                try {
                    clz = Class.forName(clzName);
                } catch (final ClassNotFoundException e) {
                    return null;
                }

                final Gson gson = GsonHelper.getGson();
                final Object obj = gson.fromJson(content, clz);
                return obj;
            }
            return null;
        } catch (final RuntimeException e) {
            s_logger.error("Caught runtime exception when doing GSON deserialization on: " + result);
            throw e;
        }
    }

    public static List<Pair<String, Object>> toPairList(final Object o, final String name) {
        final List<Pair<String, Object>> l = new ArrayList<>();
        return appendPairList(l, o, name);
    }

    public static List<Pair<String, Object>> appendPairList(final List<Pair<String, Object>> l, final Object o, final String name) {
        if (o != null) {
            final Class<?> clz = o.getClass();

            if (clz.isPrimitive() || clz.getSuperclass() == Number.class || clz == String.class || clz == Date.class) {
                l.add(new Pair<>(name, o.toString()));
                return l;
            }

            for (final Field f : clz.getDeclaredFields()) {
                if ((f.getModifiers() & Modifier.STATIC) != 0) {
                    continue;
                }

                final Param param = f.getAnnotation(Param.class);
                if (param == null) {
                    continue;
                }

                String propName = f.getName();
                if (!param.propName().isEmpty()) {
                    propName = param.propName();
                }

                String paramName = param.name();
                if (paramName.isEmpty()) {
                    paramName = propName;
                }

                final Method method = getGetMethod(o, propName);
                if (method != null) {
                    try {
                        final Object fieldValue = method.invoke(o);
                        if (fieldValue != null) {
                            if (f.getType() == Date.class) {
                                l.add(new Pair<>(paramName, DateUtil.getOutputString((Date) fieldValue)));
                            } else {
                                l.add(new Pair<>(paramName, fieldValue.toString()));
                            }
                        }
                        //else
                        //    l.add(new Pair<String, Object>(paramName, ""));
                    } catch (final IllegalArgumentException e) {
                        s_logger.error("Illegal argument exception when calling POJO " + o.getClass().getName() + " get method for property: " + propName);
                    } catch (final IllegalAccessException e) {
                        s_logger.error("Illegal access exception when calling POJO " + o.getClass().getName() + " get method for property: " + propName);
                    } catch (final InvocationTargetException e) {
                        s_logger.error("Invocation target exception when calling POJO " + o.getClass().getName() + " get method for property: " + propName);
                    }
                }
            }
        }
        return l;
    }

    private static Method getGetMethod(final Object o, final String propName) {
        Method method = null;
        String methodName = getGetMethodName("get", propName);
        try {
            method = o.getClass().getMethod(methodName);
        } catch (final SecurityException e1) {
            s_logger.error("Security exception in getting POJO " + o.getClass().getName() + " get method for property: " + propName);
        } catch (final NoSuchMethodException e1) {
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("POJO " + o.getClass().getName() + " does not have " + methodName + "() method for property: " + propName +
                        ", will check is-prefixed method to see if it is boolean property");
            }
        }

        if (method != null) {
            return method;
        }

        methodName = getGetMethodName("is", propName);
        try {
            method = o.getClass().getMethod(methodName);
        } catch (final SecurityException e1) {
            s_logger.error("Security exception in getting POJO " + o.getClass().getName() + " get method for property: " + propName);
        } catch (final NoSuchMethodException e1) {
            s_logger.warn("POJO " + o.getClass().getName() + " does not have " + methodName + "() method for property: " + propName);
        }
        return method;
    }

    private static String getGetMethodName(final String prefix, final String fieldName) {
        final StringBuffer sb = new StringBuffer(prefix);

        if (fieldName.length() >= prefix.length() && fieldName.substring(0, prefix.length()).equals(prefix)) {
            return fieldName;
        } else {
            sb.append(fieldName.substring(0, 1).toUpperCase());
            sb.append(fieldName.substring(1));
        }

        return sb.toString();
    }
}
