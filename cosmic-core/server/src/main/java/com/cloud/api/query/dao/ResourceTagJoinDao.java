package com.cloud.api.query.dao;

import com.cloud.api.query.vo.ResourceTagJoinVO;
import com.cloud.server.ResourceTag;
import com.cloud.server.ResourceTag.ResourceObjectType;
import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.api.response.ResourceTagResponse;

import java.util.List;

public interface ResourceTagJoinDao extends GenericDao<ResourceTagJoinVO, Long> {

    ResourceTagResponse newResourceTagResponse(ResourceTagJoinVO uvo, boolean keyValueOnly);

    ResourceTagJoinVO newResourceTagView(ResourceTag vr);

    List<ResourceTagJoinVO> searchByIds(Long... ids);

    List<ResourceTagJoinVO> listBy(String resourceUUID, ResourceObjectType resourceType);

    ResourceTagJoinVO searchById(Long id);
}
