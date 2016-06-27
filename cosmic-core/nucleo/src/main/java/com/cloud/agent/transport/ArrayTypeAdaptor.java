//

//

package com.cloud.agent.transport;

import com.cloud.utils.exception.CloudRuntimeException;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ArrayTypeAdaptor<T> implements JsonDeserializer<T[]>, JsonSerializer<T[]> {

    protected Gson _gson = null;

    public ArrayTypeAdaptor() {
    }

    public void initGson(final Gson gson) {
        _gson = gson;
    }

    @Override
    public JsonElement serialize(final T[] src, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonArray array = new JsonArray();
        for (final T cmd : src) {
            final JsonObject obj = new JsonObject();
            obj.add(cmd.getClass().getName(), _gson.toJsonTree(cmd));
            array.add(obj);
        }

        return array;
    }

    @Override
    public T[] deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final JsonArray array = json.getAsJsonArray();
        final Iterator<JsonElement> it = array.iterator();
        final ArrayList<T> cmds = new ArrayList<>();
        while (it.hasNext()) {
            final JsonObject element = (JsonObject) it.next();
            final Map.Entry<String, JsonElement> entry = element.entrySet().iterator().next();

            final String name = entry.getKey();
            final Class<?> clazz;
            try {
                clazz = Class.forName(name);
            } catch (final ClassNotFoundException e) {
                throw new CloudRuntimeException("can't find " + name);
            }
            final T cmd = (T) _gson.fromJson(entry.getValue(), clazz);
            cmds.add(cmd);
        }
        final Class<?> type = ((Class<?>) typeOfT).getComponentType();
        final T[] ts = (T[]) Array.newInstance(type, cmds.size());
        return cmds.toArray(ts);
    }
}
