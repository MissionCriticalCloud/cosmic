package com.cloud.api.query.dao;

import com.cloud.api.ApiConstants.VMDetails;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.query.vo.UserVmJoinVO;
import com.cloud.api.response.UserVmResponse;
import com.cloud.user.Account;
import com.cloud.uservm.UserVm;
import com.cloud.utils.db.GenericDao;

import java.util.EnumSet;
import java.util.List;

public interface UserVmJoinDao extends GenericDao<UserVmJoinVO, Long> {

    UserVmResponse newUserVmResponse(ResponseView view, String objectName, UserVmJoinVO userVm, EnumSet<VMDetails> details, Account caller);

    UserVmResponse setUserVmResponse(ResponseView view, UserVmResponse userVmData, UserVmJoinVO uvo);

    List<UserVmJoinVO> newUserVmView(UserVm... userVms);

    List<UserVmJoinVO> searchByIds(Long... ids);

    List<UserVmJoinVO> listActiveByIsoId(Long isoId);
}
