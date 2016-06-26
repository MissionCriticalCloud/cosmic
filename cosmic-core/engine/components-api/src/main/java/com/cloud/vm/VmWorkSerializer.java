package com.cloud.vm;

import org.apache.cloudstack.framework.jobs.impl.JobSerializerHelper;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class VmWorkSerializer {
    protected static Gson s_gson;

    static {
        final GsonBuilder gBuilder = new GsonBuilder();
        gBuilder.setVersion(1.3);
        gBuilder.registerTypeAdapter(Map.class, new StringMapTypeAdapter());
        s_gson = gBuilder.create();
    }

    public static String serialize(final VmWork work) {
        // TODO: there are way many generics, too tedious to get serialization work under GSON
        // use java binary serialization instead
        //
        return JobSerializerHelper.toObjectSerializedString(work);
        // return s_gson.toJson(work);
    }

    public static <T extends VmWork> T deserialize(final Class<?> clazz, final String workInJsonText) {
        // TODO: there are way many generics, too tedious to get serialization work under GSON
        // use java binary serialization instead
        //
        return (T) JobSerializerHelper.fromObjectSerializedString(workInJsonText);
        // return (T)s_gson.fromJson(workInJsonText, clazz);
    }

    static class StringMapTypeAdapter implements JsonDeserializer<Map> {

        @Override
        public Map deserialize(final JsonElement src, final Type srcType, final JsonDeserializationContext context) throws JsonParseException {

            final Map<String, String> obj = new HashMap<>();
            final JsonObject json = src.getAsJsonObject();

            for (final Entry<String, JsonElement> entry : json.entrySet()) {
                obj.put(entry.getKey(), entry.getValue().getAsString());
            }

            return obj;
        }
    }
}
