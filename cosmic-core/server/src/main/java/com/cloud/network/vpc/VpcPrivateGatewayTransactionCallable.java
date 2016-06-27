package com.cloud.network.vpc;

import com.cloud.network.vpc.dao.PrivateIpDao;
import com.cloud.network.vpc.dao.VpcGatewayDao;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.TransactionCallbackNoReturn;
import com.cloud.utils.db.TransactionStatus;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VpcPrivateGatewayTransactionCallable implements Callable<Boolean> {

    private static final Logger s_logger = LoggerFactory.getLogger(VpcPrivateGatewayTransactionCallable.class);

    @Inject
    private VpcGatewayDao _vpcGatewayDao;
    @Inject
    private PrivateIpDao _privateIpDao;

    private PrivateGateway gateway;
    private boolean deleteNetwork = true;

    @Override
    public Boolean call() throws Exception {
        final long networkId = gateway.getNetworkId();

        Transaction.execute(new TransactionCallbackNoReturn() {
            @Override
            public void doInTransactionWithoutResult(final TransactionStatus status) {

                final List<PrivateIpVO> privateIps = _privateIpDao.listByNetworkId(networkId);
                if (privateIps.size() > 1 || !privateIps.get(0).getIpAddress().equalsIgnoreCase(gateway.getIp4Address())) {
                    s_logger.debug("Not removing network id=" + gateway.getNetworkId() + " as it has private ip addresses for other gateways");
                    deleteNetwork = false;
                }

                final PrivateIpVO ip = _privateIpDao.findByIpAndVpcId(gateway.getVpcId(), gateway.getIp4Address());
                if (ip != null) {
                    _privateIpDao.remove(ip.getId());
                    s_logger.debug("Deleted private ip " + ip);
                }

                _vpcGatewayDao.remove(gateway.getId());
                s_logger.debug("Deleted private gateway " + gateway);
            }
        });

        return deleteNetwork;
    }

    public void setGateway(final PrivateGateway gateway) {
        this.gateway = gateway;
    }
}
