package org.apache.cloudstack.framework.ws.jackson;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.cxf.jaxrs.impl.tl.ThreadLocalUriInfo;

/**
 * Serializer for a list of ids.
 */
public class UrisSerializer extends JsonSerializer<List<? extends Object>> {
    Url _annotation;

    public UrisSerializer(final Url annotation) {
        _annotation = annotation;
    }

    protected UrisSerializer() {
    }

    @Override
    public void serialize(final List<? extends Object> lst, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonProcessingException {
        final Iterator<? extends Object> it = lst.iterator();
        jgen.writeStartObject();
        while (it.hasNext()) {
            final Object id = it.next();
            jgen.writeStartObject();
            jgen.writeFieldName("id");
            jgen.writeObject(id);
            jgen.writeFieldName("uri");
            jgen.writeString(buildUri(_annotation.clazz(), _annotation.method(), id));
            jgen.writeEndObject();
        }
        jgen.writeEndObject();
    }

    protected String buildUri(final Class<?> clazz, final String method, final Object id) {
        final ThreadLocalUriInfo uriInfo = new ThreadLocalUriInfo();
        final UriBuilder ub = uriInfo.getAbsolutePathBuilder().path(clazz, method);
        ub.build(id);
        return ub.toString();
    }
}
