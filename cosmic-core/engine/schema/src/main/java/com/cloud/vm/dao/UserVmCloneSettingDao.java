package com.cloud.vm.dao;

import com.cloud.utils.db.GenericDao;
import com.cloud.vm.UserVmCloneSettingVO;

import java.util.List;

public interface UserVmCloneSettingDao extends GenericDao<UserVmCloneSettingVO, Long> {

    /*
     * Returns a User VM clone type record by vm id.
     */
    UserVmCloneSettingVO findByVmId(long id);

    /*
     * Returns a list of VMs by clone type.
     * cloneType can be full/linked.
     */
    List<UserVmCloneSettingVO> listByCloneType(String cloneType);
}
