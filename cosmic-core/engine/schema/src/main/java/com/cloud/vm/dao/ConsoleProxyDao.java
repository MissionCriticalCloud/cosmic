package com.cloud.vm.dao;

import com.cloud.info.ConsoleProxyLoadInfo;
import com.cloud.utils.Pair;
import com.cloud.utils.db.GenericDao;
import com.cloud.vm.ConsoleProxyVO;
import com.cloud.vm.VirtualMachine.State;

import java.util.Date;
import java.util.List;

public interface ConsoleProxyDao extends GenericDao<ConsoleProxyVO, Long> {

    public void update(long id, int activeSession, Date updateTime, byte[] sessionDetails);

    public List<ConsoleProxyVO> getProxyListInStates(long dataCenterId, State... states);

    public List<ConsoleProxyVO> getProxyListInStates(State... states);

    public List<ConsoleProxyVO> listByHostId(long hostId);

    public List<ConsoleProxyVO> listByLastHostId(long hostId);

    public List<ConsoleProxyVO> listUpByHostId(long hostId);

    public List<ConsoleProxyLoadInfo> getDatacenterProxyLoadMatrix();

    public List<ConsoleProxyLoadInfo> getDatacenterVMLoadMatrix();

    public List<ConsoleProxyLoadInfo> getDatacenterSessionLoadMatrix();

    public List<Pair<Long, Integer>> getDatacenterStoragePoolHostInfo(long dcId, boolean countAllPoolTypes);

    public List<Pair<Long, Integer>> getProxyLoadMatrix();

    public int getProxyStaticLoad(long proxyVmId);

    public int getProxyActiveLoad(long proxyVmId);

    public List<Long> getRunningProxyListByMsid(long msid);
}
