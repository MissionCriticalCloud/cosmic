//

//

package com.cloud.agent.api.routing;

import static org.junit.Assert.assertEquals;

import com.cloud.agent.api.to.NetworkACLTO;

import java.util.List;

import com.google.common.collect.Lists;
import org.junit.Test;

public class SetNetworkACLCommandTest {

    @Test
    public void testNetworkAclRuleOrdering() {

        //given
        final List<NetworkACLTO> aclList = Lists.newArrayList();

        aclList.add(new NetworkACLTO(3, null, null, null, null, false, false, null, null, null, null, false, 3));
        aclList.add(new NetworkACLTO(1, null, null, null, null, false, false, null, null, null, null, false, 1));
        aclList.add(new NetworkACLTO(2, null, null, null, null, false, false, null, null, null, null, false, 2));

        final SetNetworkACLCommand cmd = new SetNetworkACLCommand(aclList, null);

        //when
        cmd.orderNetworkAclRulesByRuleNumber(aclList);

        //then
        for (int i = 0; i < aclList.size(); i++) {
            assertEquals(aclList.get(i).getNumber(), i + 1);
        }
    }
}
