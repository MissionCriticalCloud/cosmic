package com.cloud.alert;

import com.cloud.utils.component.ManagerBase;

import javax.naming.ConfigurationException;
import java.util.Map;

public class MockAlertManagerImpl extends ManagerBase implements AlertManager {

    /* (non-Javadoc)
     * @see com.cloud.utils.component.Manager#getName()
     */
    @Override
    public String getName() {
        return "MockAlertManagerImpl";
    }

    /* (non-Javadoc)
     * @see com.cloud.utils.component.Manager#configure(java.lang.String, java.util.Map)
     */
    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        return true;
    }

    /* (non-Javadoc)
     * @see com.cloud.utils.component.Manager#start()
     */
    @Override
    public boolean start() {
        return true;
    }

    /* (non-Javadoc)
     * @see com.cloud.utils.component.Manager#stop()
     */
    @Override
    public boolean stop() {
        return true;
    }

    /* (non-Javadoc)
     * @see com.cloud.alert.AlertManager#clearAlert(short, long, long)
     */
    @Override
    public void clearAlert(final AlertType alertType, final long dataCenterId, final long podId) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.cloud.alert.AlertManager#recalculateCapacity()
     */
    @Override
    public void recalculateCapacity() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.cloud.alert.AlertManager#sendAlert(short, long, java.lang.Long, java.lang.String, java.lang.String)
     */
    @Override
    public void sendAlert(final AlertType alertType, final long dataCenterId, final Long podId, final String subject, final String body) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean generateAlert(final AlertType alertType, final long dataCenterId, final Long podId, final String msg) {
        // TODO Auto-generated method stub
        return false;
    }
}
