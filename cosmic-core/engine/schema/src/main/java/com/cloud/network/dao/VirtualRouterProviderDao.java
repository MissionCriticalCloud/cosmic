package com.cloud.network.dao;

import com.cloud.network.VirtualRouterProvider.Type;
import com.cloud.network.element.VirtualRouterProviderVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface VirtualRouterProviderDao extends GenericDao<VirtualRouterProviderVO, Long> {
    public VirtualRouterProviderVO findByNspIdAndType(long nspId, Type type);

    public List<VirtualRouterProviderVO> listByEnabledAndType(boolean enabled, Type type);

    public VirtualRouterProviderVO findByIdAndEnabledAndType(long id, boolean enabled, Type type);

    public List<VirtualRouterProviderVO> listByType(Type type);
}
