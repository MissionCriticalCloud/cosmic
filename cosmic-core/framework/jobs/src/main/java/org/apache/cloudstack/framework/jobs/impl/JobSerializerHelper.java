package org.apache.cloudstack.framework.jobs.impl;

import com.cloud.utils.exception.CloudRuntimeException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Note: toPairList and appendPairList only support simple POJO objects currently
 */
public class JobSerializerHelper {
    public static final String token = "/";
    private static final Logger s_logger = LoggerFactory.getLogger(JobSerializerHelper.class);
    private static final Gson s_gson;

    static {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setVersion(1.5);
        s_logger.debug("Job GSON Builder initialized.");
        gsonBuilder.registerTypeAdapter(Class.class, new ClassTypeAdapter());
        gsonBuilder.registerTypeAdapter(Throwable.class, new ThrowableTypeAdapter());
        s_gson = gsonBuilder.create();
    }

    public static String toSerializedString(final Object result) {
        if (result != null) {
            final Class<?> clz = result.getClass();
            return clz.getName() + token + s_gson.toJson(result);
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

                final Object obj = s_gson.fromJson(content, clz);
                return obj;
            }
            return null;
        } catch (final RuntimeException e) {
            throw new CloudRuntimeException("Unable to deserialize: " + result, e);
        }
    }

    public static String toObjectSerializedString(final Serializable object) {
        assert (object != null);

        final ByteArrayOutputStream bs = new ByteArrayOutputStream();
        try {
            final ObjectOutputStream os = new ObjectOutputStream(bs);
            os.writeObject(object);
            os.close();
            bs.close();

            return Base64.encodeBase64URLSafeString(bs.toByteArray());
        } catch (final IOException e) {
            throw new CloudRuntimeException("Unable to serialize: " + object, e);
        }
    }

    public static Object fromObjectSerializedString(final String base64EncodedString) {
        if (base64EncodedString == null) {
            return null;
        }

        final byte[] content = Base64.decodeBase64(base64EncodedString);
        final ByteArrayInputStream bs = new ByteArrayInputStream(content);
        try {
            final ObjectInputStream is = new ObjectInputStream(bs);
            final Object obj = is.readObject();
            is.close();
            bs.close();
            return obj;
        } catch (final IOException e) {
            throw new CloudRuntimeException("Unable to serialize: " + base64EncodedString, e);
        } catch (final ClassNotFoundException e) {
            throw new CloudRuntimeException("Unable to serialize: " + base64EncodedString, e);
        }
    }

    public static class ClassTypeAdapter implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {
        @Override
        public JsonElement serialize(final Class<?> clazz, final Type typeOfResponseObj, final JsonSerializationContext ctx) {
            return new JsonPrimitive(clazz.getName());
        }

        @Override
        public Class<?> deserialize(final JsonElement arg0, final Type arg1, final JsonDeserializationContext arg2) throws JsonParseException {
            final String str = arg0.getAsString();
            try {
                return Class.forName(str);
            } catch (final ClassNotFoundException e) {
                throw new CloudRuntimeException("Unable to find class " + str);
            }
        }
    }

    public static class ThrowableTypeAdapter implements JsonSerializer<Throwable>, JsonDeserializer<Throwable> {

        @Override
        public Throwable deserialize(final JsonElement json, final Type type, final JsonDeserializationContext ctx) throws JsonParseException {
            final JsonObject obj = (JsonObject) json;

            final String className = obj.get("class").getAsString();
            try {
                final Class<Throwable> clazz = (Class<Throwable>) Class.forName(className);
                final Throwable cause = s_gson.fromJson(obj.get("cause"), Throwable.class);
                final String msg = obj.get("msg").getAsString();
                final Constructor<Throwable> constructor = clazz.getConstructor(String.class, Throwable.class);
                final Throwable th = constructor.newInstance(msg, cause);
                return th;
            } catch (final ClassNotFoundException e) {
                throw new JsonParseException("Unable to find " + className);
            } catch (final NoSuchMethodException e) {
                throw new JsonParseException("Unable to find constructor for " + className);
            } catch (final SecurityException e) {
                throw new JsonParseException("Unable to get over security " + className);
            } catch (final InstantiationException e) {
                throw new JsonParseException("Unable to instantiate " + className);
            } catch (final IllegalAccessException e) {
                throw new JsonParseException("Illegal access to " + className, e);
            } catch (final IllegalArgumentException e) {
                throw new JsonParseException("Illegal argument to " + className, e);
            } catch (final InvocationTargetException e) {
                throw new JsonParseException("Cannot invoke " + className, e);
            }
        }

        @Override
        public JsonElement serialize(final Throwable th, final Type type, final JsonSerializationContext ctx) {
            final JsonObject json = new JsonObject();

            json.add("class", new JsonPrimitive(th.getClass().getName()));
            json.add("cause", s_gson.toJsonTree(th.getCause()));
            json.add("msg", new JsonPrimitive(th.getMessage()));
            //            json.add("stack", s_gson.toJsonTree(th.getStackTrace()));

            return json;
        }
    }
}
