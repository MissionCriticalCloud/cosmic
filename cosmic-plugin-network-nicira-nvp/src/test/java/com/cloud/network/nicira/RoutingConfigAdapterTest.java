//
// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
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
