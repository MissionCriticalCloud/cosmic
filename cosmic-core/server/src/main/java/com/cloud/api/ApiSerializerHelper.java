package com.cloud.api;

import org.apache.cloudstack.api.ResponseObject;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiSerializerHelper {
    public static final Logger s_logger = LoggerFactory.getLogger(ApiSerializerHelper.class.getName());
    private static final String token = "/";

    public static String toSerializedString(final Object result) {
        if (result != null) {
            final Class<?> clz = result.getClass();
            final Gson gson = ApiGsonHelper.getBuilder().create();

            if (result instanceof ResponseObject) {
                return clz.getName() + token + ((ResponseObject) result).getObjectName() + token + gson.toJson(result);
            } else {
                return clz.getName() + token + gson.toJson(result);
            }
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

                final Gson gson = ApiGsonHelper.getBuilder().create();
                final Object obj = gson.fromJson(content, clz);
                if (nameField != null) {
                    ((ResponseObject) obj).setObjectName(nameField);
                }
                return obj;
            }
            return null;
        } catch (final RuntimeException e) {
            s_logger.error("Caught runtime exception when doing GSON deserialization on: " + result);
            throw e;
        }
    }
}
