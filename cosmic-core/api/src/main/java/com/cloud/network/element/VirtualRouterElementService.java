package com.cloud.network.element;

import com.cloud.api.command.admin.router.ConfigureVirtualRouterElementCmd;
import com.cloud.api.command.admin.router.ListVirtualRouterElementsCmd;
import com.cloud.network.VirtualRouterProvider;
import com.cloud.network.VirtualRouterProvider.Type;
import com.cloud.utils.component.PluggableService;

import java.util.List;

public interface VirtualRouterElementService extends PluggableService {
    VirtualRouterProvider configure(ConfigureVirtualRouterElementCmd cmd);

    VirtualRouterProvider addElement(Long nspId, Type providerType);

    VirtualRouterProvider getCreatedElement(long id);

    List<? extends VirtualRouterProvider> searchForVirtualRouterElement(ListVirtualRouterElementsCmd cmd);
}
