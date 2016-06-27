package com.cloud.vpc.dao;

import com.cloud.network.vpc.Vpc;
import com.cloud.network.vpc.Vpc.State;
import com.cloud.network.vpc.VpcVO;
import com.cloud.network.vpc.dao.VpcDao;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DB()
public class MockVpcDaoImpl extends GenericDaoBase<VpcVO, Long> implements VpcDao {
    private static final Logger s_logger = LoggerFactory.getLogger(MockNetworkOfferingDaoImpl.class);

    /* (non-Javadoc)
     * @see com.cloud.network.vpc.Dao.VpcDao#getVpcCountByOfferingId(long)
     */
    @Override
    public int getVpcCountByOfferingId(final long offId) {
        return 100;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.vpc.Dao.VpcDao#getActiveVpcById(long)
     */
    @Override
    public Vpc getActiveVpcById(final long vpcId) {
        final Vpc vpc = findById(vpcId);
        if (vpc != null && vpc.getState() == Vpc.State.Enabled) {
            return vpc;
        }

        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.vpc.Dao.VpcDao#listByAccountId(long)
     */
    @Override
    public List<? extends Vpc> listByAccountId(final long accountId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.vpc.Dao.VpcDao#listInactiveVpcs()
     */
    @Override
    public List<VpcVO> listInactiveVpcs() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.vpc.Dao.VpcDao#countByAccountId(long)
     */
    @Override
    public long countByAccountId(final long accountId) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public VpcVO persist(final VpcVO vpc, final Map<String, List<String>> serviceProviderMap) {
        return null;
    }

    @Override
    public void persistVpcServiceProviders(final long vpcId, final Map<String, List<String>> serviceProviderMap) {
        return;
    }

    @Override
    public VpcVO findById(final Long id) {
        VpcVO vo = null;
        if (id.longValue() == 1) {
            vo = new VpcVO(1, "new vpc", "new vpc", 1, 1, 1, "0.0.0.0/0", "vpc domain", false, false, false);
        } else if (id.longValue() == 2) {
            vo = new VpcVO(1, "new vpc", "new vpc", 1, 1, 1, "0.0.0.0/0", "vpc domain", false, false, false);
            vo.setState(State.Inactive);
        }

        vo = setId(vo, id);

        return vo;
    }

    private VpcVO setId(final VpcVO vo, final long id) {
        final VpcVO voToReturn = vo;
        final Class<?> c = voToReturn.getClass();
        try {
            final Field f = c.getDeclaredField("id");
            f.setAccessible(true);
            f.setLong(voToReturn, id);
        } catch (final NoSuchFieldException ex) {
            s_logger.warn(ex.toString());
            return null;
        } catch (final IllegalAccessException ex) {
            s_logger.warn(ex.toString());
            return null;
        }

        return voToReturn;
    }

    @Override
    public boolean update(final Long id, final VpcVO vo) {
        return true;
    }

    @Override
    public boolean remove(final Long id) {
        return true;
    }
}
