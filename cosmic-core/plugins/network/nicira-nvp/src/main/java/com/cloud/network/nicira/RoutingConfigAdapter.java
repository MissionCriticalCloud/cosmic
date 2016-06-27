//

//

package com.cloud.network.nicira;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class RoutingConfigAdapter implements JsonDeserializer<RoutingConfig> {

    private static final String ROUTING_TABLE_ROUTING_CONFIG = "RoutingTableRoutingConfig";
    private static final String SINGLE_DEFAULT_ROUTE_IMPLICIT_ROUTING_CONFIG = "SingleDefaultRouteImplicitRoutingConfig";

    @Override
    public RoutingConfig deserialize(final JsonElement jsonElement, final Type type, final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = jsonElement.getAsJsonObject();

        if (!jsonObject.has("type")) {
            throw new JsonParseException("Deserializing as a RoutingConfig, but no type present in the json object");
        }

        final String routingConfigType = jsonObject.get("type").getAsString();
        if (SINGLE_DEFAULT_ROUTE_IMPLICIT_ROUTING_CONFIG.equals(routingConfigType)) {
            return context.deserialize(jsonElement, SingleDefaultRouteImplicitRoutingConfig.class);
        } else if (ROUTING_TABLE_ROUTING_CONFIG.equals(routingConfigType)) {
            return context.deserialize(jsonElement, RoutingTableRoutingConfig.class);
        }

        throw new JsonParseException("Failed to deserialize type \"" + routingConfigType + "\"");
    }
}
