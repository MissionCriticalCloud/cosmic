package com.cloud.agent.resource.virtualnetwork;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.cloud.agent.api.routing.LoadBalancerConfigCommand;
import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.api.routing.SetPortForwardingRulesVpcCommand;
import com.cloud.agent.api.to.LoadBalancerTO;
import com.cloud.agent.api.to.NicTO;
import com.cloud.agent.api.to.PortForwardingRuleTO;
import com.cloud.agent.resource.virtualnetwork.facade.AbstractConfigItemFacade;
import com.cloud.agent.resource.virtualnetwork.model.ForwardingRule;
import com.cloud.agent.resource.virtualnetwork.model.ForwardingRules;
import com.cloud.agent.resource.virtualnetwork.model.LoadBalancerRule;
import com.cloud.agent.resource.virtualnetwork.model.LoadBalancerRules;
import com.cloud.network.lb.LoadBalancingRule.LbDestination;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;

public class ConfigHelperTest {

    private final static Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    private final String ROUTERNAME = "r-4-VM";

    @Test
    public void testGenerateCommandCfgLoadBalancer() {

        final LoadBalancerConfigCommand command = generateLoadBalancerConfigCommand();

        final AbstractConfigItemFacade configItemFacade = AbstractConfigItemFacade.getInstance(command.getClass());

        final List<ConfigItem> config = configItemFacade.generateConfig(command);
        assertTrue(config.size() > 0);

        final ConfigItem fileConfig = config.get(0);
        assertNotNull(fileConfig);
        assertTrue(fileConfig instanceof FileConfigItem);

        final String fileContents = ((FileConfigItem) fileConfig).getFileContents();
        assertNotNull(fileContents);

        final LoadBalancerRules jsonClass = gson.fromJson(fileContents, LoadBalancerRules.class);
        assertNotNull(jsonClass);
        assertEquals(jsonClass.getType(), "loadbalancer");

        final List<LoadBalancerRule> rules = jsonClass.getRules();
        assertNotNull(rules);
        assertTrue(rules.size() == 1);
        assertEquals(rules.get(0).getRouterIp(), "10.1.10.2");

        final ConfigItem scriptConfig = config.get(1);
        assertNotNull(scriptConfig);
        assertTrue(scriptConfig instanceof ScriptConfigItem);
    }

    protected LoadBalancerConfigCommand generateLoadBalancerConfigCommand() {
        final List<LoadBalancerTO> lbs = new ArrayList<>();
        final List<LbDestination> dests = new ArrayList<>();
        dests.add(new LbDestination(80, 8080, "10.1.10.2", false));
        dests.add(new LbDestination(80, 8080, "10.1.10.2", true));
        lbs.add(new LoadBalancerTO(UUID.randomUUID().toString(), "64.10.1.10", 80, "tcp", "algo", false, false, false, dests, 60000, 60000));

        final LoadBalancerTO[] arrayLbs = new LoadBalancerTO[lbs.size()];
        lbs.toArray(arrayLbs);

        final NicTO nic = new NicTO();
        final LoadBalancerConfigCommand cmd = new LoadBalancerConfigCommand(arrayLbs, "64.10.2.10", "10.1.10.2", "192.168.1.2", nic, null, "1000", false);
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_IP, "10.1.10.2");
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_NAME, ROUTERNAME);

        return cmd;
    }

    @Test
    public void testSetPortForwardingRulesVpc() {

        final SetPortForwardingRulesVpcCommand command = generateSetPortForwardingRulesVpcCommand();

        final AbstractConfigItemFacade configItemFacade = AbstractConfigItemFacade.getInstance(command.getClass());

        final List<ConfigItem> config = configItemFacade.generateConfig(command);
        assertTrue(config.size() > 0);

        final ConfigItem fileConfig = config.get(0);
        assertNotNull(fileConfig);
        assertTrue(fileConfig instanceof FileConfigItem);

        final String fileContents = ((FileConfigItem) fileConfig).getFileContents();
        assertNotNull(fileContents);

        final ForwardingRules jsonClass = gson.fromJson(fileContents, ForwardingRules.class);
        assertNotNull(jsonClass);
        assertEquals(jsonClass.getType(), "forwardrules");

        final ForwardingRule[] rules = jsonClass.getRules();
        assertNotNull(rules);
        assertTrue(rules.length == 2);
        assertEquals(rules[0].getSourceIpAddress(), "64.1.1.10");

        final ConfigItem scriptConfig = config.get(1);
        assertNotNull(scriptConfig);
        assertTrue(scriptConfig instanceof ScriptConfigItem);
    }

    protected SetPortForwardingRulesVpcCommand generateSetPortForwardingRulesVpcCommand() {
        final List<PortForwardingRuleTO> pfRules = new ArrayList<>();
        pfRules.add(new PortForwardingRuleTO(1, "64.1.1.10", 22, 80, "10.10.1.10", 22, 80, "TCP", false, false));
        pfRules.add(new PortForwardingRuleTO(2, "64.1.1.11", 8080, 8080, "10.10.1.11", 8080, 8080, "UDP", true, false));

        final SetPortForwardingRulesVpcCommand cmd = new SetPortForwardingRulesVpcCommand(pfRules);
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_NAME, ROUTERNAME);
        assertEquals(cmd.getAnswersCount(), 2);

        return cmd;
    }
}
