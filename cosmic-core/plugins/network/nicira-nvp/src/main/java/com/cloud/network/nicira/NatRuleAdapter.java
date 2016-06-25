//

//

package com.cloud.network.nicira;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class NatRuleAdapter implements JsonDeserializer<NatRule> {

    @Override
    public NatRule deserialize(final JsonElement jsonElement, final Type type, final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = jsonElement.getAsJsonObject();

        if (!jsonObject.has("type")) {
            throw new JsonParseException("Deserializing as a NatRule, but no type present in the json object");
        }

        final String natRuleType = jsonObject.get("type").getAsString();
        if ("SourceNatRule".equals(natRuleType)) {
            return context.deserialize(jsonElement, SourceNatRule.class);
        } else if ("DestinationNatRule".equals(natRuleType)) {
            return context.deserialize(jsonElement, DestinationNatRule.class);
        }

        throw new JsonParseException("Failed to deserialize type \"" + natRuleType + "\"");
    }
}
