package org.apache.cloudstack.framework.ws.jackson;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.cxf.jaxrs.impl.tl.ThreadLocalUriInfo;

public class UriSerializer extends JsonSerializer<String> {

    Url _annotation;

    public UriSerializer(final Url annotation) {
        _annotation = annotation;
    }

    protected UriSerializer() {
    }

    @Override
    public void serialize(final String id, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        jgen.writeStringField("id", id);
        jgen.writeFieldName("uri");
        jgen.writeString(buildUri(_annotation.clazz(), _annotation.method(), id));
        jgen.writeEndObject();
    }

    protected String buildUri(final Class<?> clazz, final String method, final String id) {
        final ThreadLocalUriInfo uriInfo = new ThreadLocalUriInfo();
        final UriBuilder ub = uriInfo.getAbsolutePathBuilder().path(clazz, method);
        ub.build(id);
        return ub.toString();
    }
}
