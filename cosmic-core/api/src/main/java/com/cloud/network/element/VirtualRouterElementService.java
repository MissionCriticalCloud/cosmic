package com.cloud.network.element;

import com.cloud.network.VirtualRouterProvider;
import com.cloud.network.VirtualRouterProvider.Type;
import com.cloud.utils.component.PluggableService;
import org.apache.cloudstack.api.command.admin.router.ConfigureVirtualRouterElementCmd;
import org.apache.cloudstack.api.command.admin.router.ListVirtualRouterElementsCmd;

import java.util.List;

public interface VirtualRouterElementService extends PluggableService {
    VirtualRouterProvider configure(ConfigureVirtualRouterElementCmd cmd);

    VirtualRouterProvider addElement(Long nspId, Type providerType);

    VirtualRouterProvider getCreatedElement(long id);

    List<? extends VirtualRouterProvider> searchForVirtualRouterElement(ListVirtualRouterElementsCmd cmd);
}
