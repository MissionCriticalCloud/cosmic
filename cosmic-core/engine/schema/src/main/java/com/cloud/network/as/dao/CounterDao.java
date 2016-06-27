package com.cloud.network.as.dao;

import com.cloud.network.as.CounterVO;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface CounterDao extends GenericDao<CounterVO, Long> {
    public List<CounterVO> listCounters(Long id, String name, String source, String keyword, Filter filter);
}
