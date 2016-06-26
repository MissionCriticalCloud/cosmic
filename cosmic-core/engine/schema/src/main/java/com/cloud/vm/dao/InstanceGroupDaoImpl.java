package com.cloud.vm.dao;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.vm.InstanceGroupVO;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class InstanceGroupDaoImpl extends GenericDaoBase<InstanceGroupVO, Long> implements InstanceGroupDao {
    protected final SearchBuilder<InstanceGroupVO> AccountSearch;
    private final SearchBuilder<InstanceGroupVO> AccountIdNameSearch;

    protected InstanceGroupDaoImpl() {
        AccountSearch = createSearchBuilder();
        AccountSearch.and("account", AccountSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        AccountSearch.done();

        AccountIdNameSearch = createSearchBuilder();
        AccountIdNameSearch.and("accountId", AccountIdNameSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        AccountIdNameSearch.and("groupName", AccountIdNameSearch.entity().getName(), SearchCriteria.Op.EQ);
        AccountIdNameSearch.done();
    }

    @Override
    public List<InstanceGroupVO> listByAccountId(final long id) {
        final SearchCriteria<InstanceGroupVO> sc = AccountSearch.create();
        sc.setParameters("account", id);
        return listBy(sc);
    }

    @Override
    public boolean isNameInUse(final Long accountId, final String name) {
        final SearchCriteria<InstanceGroupVO> sc = createSearchCriteria();
        sc.addAnd("name", SearchCriteria.Op.EQ, name);
        if (accountId != null) {
            sc.addAnd("accountId", SearchCriteria.Op.EQ, accountId);
        }
        final List<InstanceGroupVO> vmGroups = listBy(sc);
        return ((vmGroups != null) && !vmGroups.isEmpty());
    }

    @Override
    public InstanceGroupVO findByAccountAndName(final Long accountId, final String name) {
        final SearchCriteria<InstanceGroupVO> sc = AccountIdNameSearch.create();
        sc.setParameters("accountId", accountId);
        sc.setParameters("groupName", name);
        return findOneBy(sc);
    }

    @Override
    public void updateVmGroup(final long id, final String name) {
        final InstanceGroupVO vo = createForUpdate();
        vo.setName(name);
        update(id, vo);
    }
}
