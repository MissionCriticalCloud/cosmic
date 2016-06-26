package org.apache.cloudstack.framework.jobs.impl;

import com.cloud.utils.DateUtil;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.TransactionCallback;
import com.cloud.utils.db.TransactionCallbackNoReturn;
import com.cloud.utils.db.TransactionStatus;
import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.framework.jobs.dao.SyncQueueDao;
import org.apache.cloudstack.framework.jobs.dao.SyncQueueItemDao;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyncQueueManagerImpl extends ManagerBase implements SyncQueueManager {
    public static final Logger s_logger = LoggerFactory.getLogger(SyncQueueManagerImpl.class.getName());

    @Inject
    private SyncQueueDao _syncQueueDao;
    @Inject
    private SyncQueueItemDao _syncQueueItemDao;

    @Override
    @DB
    public SyncQueueVO queue(final String syncObjType, final long syncObjId, final String itemType, final long itemId, final long queueSizeLimit) {
        try {
            return Transaction.execute(new TransactionCallback<SyncQueueVO>() {
                @Override
                public SyncQueueVO doInTransaction(final TransactionStatus status) {
                    _syncQueueDao.ensureQueue(syncObjType, syncObjId);
                    final SyncQueueVO queueVO = _syncQueueDao.find(syncObjType, syncObjId);
                    if (queueVO == null) {
                        throw new CloudRuntimeException("Unable to queue item into DB, DB is full?");
                    }

                    queueVO.setQueueSizeLimit(queueSizeLimit);
                    _syncQueueDao.update(queueVO.getId(), queueVO);

                    final Date dt = DateUtil.currentGMTTime();
                    final SyncQueueItemVO item = new SyncQueueItemVO();
                    item.setQueueId(queueVO.getId());
                    item.setContentType(itemType);
                    item.setContentId(itemId);
                    item.setCreated(dt);

                    _syncQueueItemDao.persist(item);
                    return queueVO;
                }
            });
        } catch (final Exception e) {
            s_logger.error("Unexpected exception: ", e);
        }
        return null;
    }

    @Override
    @DB
    public SyncQueueItemVO dequeueFromOne(final long queueId, final Long msid) {
        try {
            return Transaction.execute(new TransactionCallback<SyncQueueItemVO>() {
                @Override
                public SyncQueueItemVO doInTransaction(final TransactionStatus status) {
                    final SyncQueueVO queueVO = _syncQueueDao.findById(queueId);
                    if (queueVO == null) {
                        s_logger.error("Sync queue(id: " + queueId + ") does not exist");
                        return null;
                    }

                    if (queueReadyToProcess(queueVO)) {
                        final SyncQueueItemVO itemVO = _syncQueueItemDao.getNextQueueItem(queueVO.getId());
                        if (itemVO != null) {
                            Long processNumber = queueVO.getLastProcessNumber();
                            if (processNumber == null) {
                                processNumber = new Long(1);
                            } else {
                                processNumber = processNumber + 1;
                            }
                            final Date dt = DateUtil.currentGMTTime();
                            queueVO.setLastProcessNumber(processNumber);
                            queueVO.setLastUpdated(dt);
                            queueVO.setQueueSize(queueVO.getQueueSize() + 1);
                            _syncQueueDao.update(queueVO.getId(), queueVO);

                            itemVO.setLastProcessMsid(msid);
                            itemVO.setLastProcessNumber(processNumber);
                            itemVO.setLastProcessTime(dt);
                            _syncQueueItemDao.update(itemVO.getId(), itemVO);

                            return itemVO;
                        } else {
                            if (s_logger.isDebugEnabled()) {
                                s_logger.debug("Sync queue (" + queueId + ") is currently empty");
                            }
                        }
                    } else {
                        if (s_logger.isDebugEnabled()) {
                            s_logger.debug("There is a pending process in sync queue(id: " + queueId + ")");
                        }
                    }

                    return null;
                }
            });
        } catch (final Exception e) {
            s_logger.error("Unexpected exception: ", e);
        }

        return null;
    }

    @Override
    @DB
    public List<SyncQueueItemVO> dequeueFromAny(final Long msid, final int maxItems) {

        final List<SyncQueueItemVO> resultList = new ArrayList<>();

        try {
            Transaction.execute(new TransactionCallbackNoReturn() {
                @Override
                public void doInTransactionWithoutResult(final TransactionStatus status) {
                    final List<SyncQueueItemVO> l = _syncQueueItemDao.getNextQueueItems(maxItems);
                    if (l != null && l.size() > 0) {
                        for (final SyncQueueItemVO item : l) {
                            final SyncQueueVO queueVO = _syncQueueDao.findById(item.getQueueId());
                            final SyncQueueItemVO itemVO = _syncQueueItemDao.findById(item.getId());
                            if (queueReadyToProcess(queueVO) && itemVO != null && itemVO.getLastProcessNumber() == null) {
                                Long processNumber = queueVO.getLastProcessNumber();
                                if (processNumber == null) {
                                    processNumber = new Long(1);
                                } else {
                                    processNumber = processNumber + 1;
                                }

                                final Date dt = DateUtil.currentGMTTime();
                                queueVO.setLastProcessNumber(processNumber);
                                queueVO.setLastUpdated(dt);
                                queueVO.setQueueSize(queueVO.getQueueSize() + 1);
                                _syncQueueDao.update(queueVO.getId(), queueVO);

                                itemVO.setLastProcessMsid(msid);
                                itemVO.setLastProcessNumber(processNumber);
                                itemVO.setLastProcessTime(dt);
                                _syncQueueItemDao.update(item.getId(), itemVO);

                                resultList.add(itemVO);
                            }
                        }
                    }
                }
            });

            return resultList;
        } catch (final Exception e) {
            s_logger.error("Unexpected exception: ", e);
        }

        return null;
    }

    @Override
    @DB
    public void purgeItem(final long queueItemId) {
        try {
            Transaction.execute(new TransactionCallbackNoReturn() {
                @Override
                public void doInTransactionWithoutResult(final TransactionStatus status) {
                    final SyncQueueItemVO itemVO = _syncQueueItemDao.findById(queueItemId);
                    if (itemVO != null) {
                        final SyncQueueVO queueVO = _syncQueueDao.findById(itemVO.getQueueId());

                        _syncQueueItemDao.expunge(itemVO.getId());

                        // if item is active, reset queue information
                        if (itemVO.getLastProcessMsid() != null) {
                            queueVO.setLastUpdated(DateUtil.currentGMTTime());
                            // decrement the count
                            assert (queueVO.getQueueSize() > 0) : "Count reduce happens when it's already <= 0!";
                            queueVO.setQueueSize(queueVO.getQueueSize() - 1);
                            _syncQueueDao.update(queueVO.getId(), queueVO);
                        }
                    }
                }
            });
        } catch (final Exception e) {
            s_logger.error("Unexpected exception: ", e);
        }
    }

    @Override
    @DB
    public void returnItem(final long queueItemId) {
        s_logger.info("Returning queue item " + queueItemId + " back to queue for second try in case of DB deadlock");
        try {
            Transaction.execute(new TransactionCallbackNoReturn() {
                @Override
                public void doInTransactionWithoutResult(final TransactionStatus status) {
                    final SyncQueueItemVO itemVO = _syncQueueItemDao.findById(queueItemId);
                    if (itemVO != null) {
                        final SyncQueueVO queueVO = _syncQueueDao.findById(itemVO.getQueueId());

                        itemVO.setLastProcessMsid(null);
                        itemVO.setLastProcessNumber(null);
                        itemVO.setLastProcessTime(null);
                        _syncQueueItemDao.update(queueItemId, itemVO);

                        queueVO.setQueueSize(queueVO.getQueueSize() - 1);
                        queueVO.setLastUpdated(DateUtil.currentGMTTime());
                        _syncQueueDao.update(queueVO.getId(), queueVO);
                    }
                }
            });
        } catch (final Exception e) {
            s_logger.error("Unexpected exception: ", e);
        }
    }

    @Override
    public List<SyncQueueItemVO> getActiveQueueItems(final Long msid, final boolean exclusive) {
        return _syncQueueItemDao.getActiveQueueItems(msid, exclusive);
    }

    @Override
    public List<SyncQueueItemVO> getBlockedQueueItems(final long thresholdMs, final boolean exclusive) {
        return _syncQueueItemDao.getBlockedQueueItems(thresholdMs, exclusive);
    }

    @Override
    public void purgeAsyncJobQueueItemId(final long asyncJobId) {
        final Long itemId = _syncQueueItemDao.getQueueItemIdByContentIdAndType(asyncJobId, SyncQueueItem.AsyncJobContentType);
        if (itemId != null) {
            purgeItem(itemId);
        }
    }

    @Override
    public void cleanupActiveQueueItems(final Long msid, final boolean exclusive) {
        final List<SyncQueueItemVO> l = getActiveQueueItems(msid, false);
        for (final SyncQueueItemVO item : l) {
            if (s_logger.isInfoEnabled()) {
                s_logger.info("Discard left-over queue item: " + item.toString());
            }
            purgeItem(item.getId());
        }
    }

    private boolean queueReadyToProcess(final SyncQueueVO queueVO) {
        final int nActiveItems = _syncQueueItemDao.getActiveQueueItemCount(queueVO.getId());
        if (nActiveItems < queueVO.getQueueSizeLimit()) {
            return true;
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Queue (queue id, sync type, sync id) - (" + queueVO.getId()
                    + "," + queueVO.getSyncObjType() + ", " + queueVO.getSyncObjId()
                    + ") is reaching concurrency limit " + queueVO.getQueueSizeLimit());
        }
        return false;
    }
}
