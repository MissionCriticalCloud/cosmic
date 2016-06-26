//

//

package com.cloud.network.nicira;

import static org.junit.Assert.assertTrue;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;

public class NatRuleTest {

    @Test
    public void testNatRuleEncoding() {
        final Gson gson =
                new GsonBuilder().registerTypeAdapter(NatRule.class, new NatRuleAdapter())
                                 .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                                 .create();

        final DestinationNatRule rn1 = new DestinationNatRule();
        rn1.setToDestinationIpAddress("10.10.10.10");
        rn1.setToDestinationPort(80);
        final Match mr1 = new Match();
        mr1.setSourceIpAddresses("11.11.11.11/24");
        mr1.setEthertype("IPv4");
        mr1.setProtocol(6);
        rn1.setMatch(mr1);

        final String jsonString = gson.toJson(rn1);
        final NatRule dnr = gson.fromJson(jsonString, NatRule.class);

        assertTrue(dnr instanceof DestinationNatRule);
        assertTrue(rn1.equals(dnr));
    }
}
