package com.cloud.api.query.dao;

import com.cloud.api.query.vo.ImageStoreJoinVO;
import com.cloud.storage.ImageStore;
import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.api.response.ImageStoreResponse;

import java.util.List;

public interface ImageStoreJoinDao extends GenericDao<ImageStoreJoinVO, Long> {

    ImageStoreResponse newImageStoreResponse(ImageStoreJoinVO os);

    ImageStoreResponse setImageStoreResponse(ImageStoreResponse response, ImageStoreJoinVO os);

    List<ImageStoreJoinVO> newImageStoreView(ImageStore os);

    List<ImageStoreJoinVO> searchByIds(Long... spIds);
}
