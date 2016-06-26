//

//

package com.cloud.agent.transport;

import com.cloud.utils.exception.CloudRuntimeException;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class InterfaceTypeAdaptor<T> implements JsonDeserializer<T>, JsonSerializer<T> {

    protected Gson _gson = null;

    public InterfaceTypeAdaptor() {
    }

    public void initGson(final Gson gson) {
        _gson = gson;
    }

    @Override
    public JsonElement serialize(final T src, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonObject obj = new JsonObject();
        obj.add(src.getClass().getName(), _gson.toJsonTree(src));
        return obj;
    }

    @Override
    public T deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject element = (JsonObject) json;
        final Map.Entry<String, JsonElement> entry = element.entrySet().iterator().next();
        final String name = entry.getKey();
        final Class<?> clazz;
        try {
            clazz = Class.forName(name);
        } catch (final ClassNotFoundException e) {
            throw new CloudRuntimeException("can't find " + name);
        }
        return (T) _gson.fromJson(entry.getValue(), clazz);
    }
}
