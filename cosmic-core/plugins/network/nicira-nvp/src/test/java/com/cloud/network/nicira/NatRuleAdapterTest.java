//

//

package com.cloud.network.nicira;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import org.junit.Test;

public class NatRuleAdapterTest {
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(NatRule.class, new NatRuleAdapter())
            .create();

    @Test(expected = JsonParseException.class)
    public void testNatRuleAdapterNoType() {
        gson.fromJson("{}", NatRule.class);
    }

    @Test(expected = JsonParseException.class)
    public void testNatRuleAdapterWrongType() {
        gson.fromJson("{type : \"WrongType\"}", NatRule.class);
    }

    @Test()
    public void testNatRuleAdapterWithSourceNatRule() {
        final SourceNatRule sourceNatRule = (SourceNatRule) gson.fromJson("{type : \"SourceNatRule\"}", NatRule.class);

        assertThat(sourceNatRule, instanceOf(SourceNatRule.class));
    }

    @Test()
    public void testNatRuleAdapterWithDestinationNatRule() {
        final DestinationNatRule destinationNatRule = (DestinationNatRule) gson.fromJson("{type : \"DestinationNatRule\"}", NatRule.class);

        assertThat(destinationNatRule, instanceOf(DestinationNatRule.class));
    }
}
