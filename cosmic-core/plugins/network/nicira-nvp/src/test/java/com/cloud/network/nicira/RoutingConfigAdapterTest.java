//

//

package com.cloud.network.nicira;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import org.junit.Test;

public class RoutingConfigAdapterTest {
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(RoutingConfig.class, new RoutingConfigAdapter())
            .create();

    @Test(expected = JsonParseException.class)
    public void testRoutingConfigAdapterNoType() {
        gson.fromJson("{}", RoutingConfig.class);
    }

    @Test(expected = JsonParseException.class)
    public void testRoutingConfigAdapterWrongType() {
        gson.fromJson("{type : \"WrongType\"}", RoutingConfig.class);
    }

    @Test()
    public void testRoutingConfigAdapter() throws Exception {
        final String jsonString = "{type : \"SingleDefaultRouteImplicitRoutingConfig\"}";

        final SingleDefaultRouteImplicitRoutingConfig object = gson.fromJson(jsonString, SingleDefaultRouteImplicitRoutingConfig.class);

        assertThat(object, notNullValue());
        assertThat(object, instanceOf(SingleDefaultRouteImplicitRoutingConfig.class));
    }
}
