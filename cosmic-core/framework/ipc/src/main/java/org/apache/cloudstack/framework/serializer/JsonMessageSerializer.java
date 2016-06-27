package org.apache.cloudstack.framework.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonMessageSerializer implements MessageSerializer {

    // this will be injected from external to allow installation of
    // type adapters needed by upper layer applications
    private Gson _gson;

    private OnwireClassRegistry _clzRegistry;

    public JsonMessageSerializer() {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setVersion(1.5);
        _gson = gsonBuilder.create();
    }

    public Gson getGson() {
        return _gson;
    }

    public void setGson(final Gson gson) {
        _gson = gson;
    }

    public OnwireClassRegistry getOnwireClassRegistry() {
        return _clzRegistry;
    }

    public void setOnwireClassRegistry(final OnwireClassRegistry clzRegistry) {
        _clzRegistry = clzRegistry;
    }

    @Override
    public <T> String serializeTo(final Class<?> clz, final T object) {
        assert (clz != null);
        assert (object != null);

        final StringBuffer sbuf = new StringBuffer();

        final OnwireName onwire = clz.getAnnotation(OnwireName.class);
        if (onwire == null) {
            throw new RuntimeException("Class " + clz.getCanonicalName() + " is not declared to be onwire");
        }

        sbuf.append(onwire.name()).append("|");
        sbuf.append(_gson.toJson(object));

        return sbuf.toString();
    }

    @Override
    public <T> T serializeFrom(final String message) {
        assert (message != null);
        final int contentStartPos = message.indexOf('|');
        if (contentStartPos < 0) {
            throw new RuntimeException("Invalid on-wire message format");
        }

        final String onwireName = message.substring(0, contentStartPos);
        final Class<?> clz = _clzRegistry.getOnwireClass(onwireName);
        if (clz == null) {
            throw new RuntimeException("Onwire class is not registered. name: " + onwireName);
        }

        return (T) _gson.fromJson(message.substring(contentStartPos + 1), clz);
    }
}
