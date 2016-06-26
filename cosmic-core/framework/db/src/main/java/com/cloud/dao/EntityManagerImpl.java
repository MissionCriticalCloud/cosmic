package com.cloud.dao;

import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.db.GenericDao;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import javax.naming.ConfigurationException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.Cache;

public class EntityManagerImpl extends ManagerBase implements EntityManager {
    String _name;
    Cache _cache;

    @Override
    public <T, K extends Serializable> T findById(final Class<T> entityType, final K id) {
        final GenericDao<? extends T, K> dao = (GenericDao<? extends T, K>) GenericDaoBase.getDao(entityType);
        return dao.findById(id);
    }

    @Override
    public <T> T findByUuid(final Class<T> entityType, final String uuid) {
        // Finds and returns a unique VO using uuid, null if entity not found in db
        final GenericDao<? extends T, String> dao = (GenericDao<? extends T, String>) GenericDaoBase.getDao(entityType);
        return dao.findByUuid(uuid);
    }

    @Override
    public <T> T findByUuidIncludingRemoved(final Class<T> entityType, final String uuid) {
        // Finds and returns a unique VO using uuid, null if entity not found in db
        final GenericDao<? extends T, String> dao = (GenericDao<? extends T, String>) GenericDaoBase.getDao(entityType);
        return dao.findByUuidIncludingRemoved(uuid);
    }

    @Override
    public <T> List<? extends T> list(final Class<T> entityType) {
        final GenericDao<? extends T, ? extends Serializable> dao = GenericDaoBase.getDao(entityType);
        return dao.listAll();
    }

    @Override
    public <T, K extends Serializable> void remove(final Class<T> entityType, final K id) {
        final GenericDao<T, K> dao = (GenericDao<T, K>) GenericDaoBase.getDao(entityType);
        dao.remove(id);
    }

    @Override
    public <T, K extends Serializable> T findByIdIncludingRemoved(final Class<T> entityType, final K id) {
        final GenericDao<? extends T, K> dao = (GenericDao<? extends T, K>) GenericDaoBase.getDao(entityType);
        return dao.findByIdIncludingRemoved(id);
    }

    public <T> T persist(final T t) {
        final GenericDao<T, ? extends Serializable> dao = (GenericDao<T, ? extends Serializable>) GenericDaoBase.getDao((Class<T>) t.getClass());
        return dao.persist(t);
    }

    public <T> SearchBuilder<T> createSearchBuilder(final Class<T> entityType) {
        final GenericDao<T, ? extends Serializable> dao = (GenericDao<T, ? extends Serializable>) GenericDaoBase.getDao(entityType);
        return dao.createSearchBuilder();
    }

    public <T, K> GenericSearchBuilder<T, K> createGenericSearchBuilder(final Class<T> entityType, final Class<K> resultType) {
        final GenericDao<T, ? extends Serializable> dao = (GenericDao<T, ? extends Serializable>) GenericDaoBase.getDao(entityType);
        return dao.createSearchBuilder((Class<K>) resultType.getClass());
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        _name = name;

        return true;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }

    public <T, K> List<K> search(final Class<T> entityType, final SearchCriteria<K> sc) {
        final GenericDao<T, ? extends Serializable> dao = (GenericDao<T, ? extends Serializable>) GenericDaoBase.getDao(entityType);
        return dao.customSearch(sc, null);
    }
}
