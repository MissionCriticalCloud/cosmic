//

//

package com.cloud.network.resource;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.resource.ServerResource;

public class CreateLoadBalancerApplianceAnswer extends Answer {
    String deviceName;
    String providerName;
    ServerResource serverResource;
    String username;
    String password;
    String publicInterface;
    String privateInterface;

    public CreateLoadBalancerApplianceAnswer(final Command cmd, final boolean success, final String details, final String deviceName, final String providerName, final
    ServerResource serverResource,
                                             final String publicInterface, final String privateInterface, final String username, final String password) {
        this.deviceName = deviceName;
        this.providerName = providerName;
        this.serverResource = serverResource;
        this.result = success;
        this.details = details;
        this.username = username;
        this.password = password;
        this.publicInterface = publicInterface;
        this.privateInterface = privateInterface;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getProviderName() {
        return providerName;
    }

    public ServerResource getServerResource() {
        return serverResource;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getPublicInterface() {
        return publicInterface;
    }

    public String getPrivateInterface() {
        return privateInterface;
    }
}
