package com.cloud.hypervisor;

import com.cloud.agent.api.Command;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.utils.Pair;
import com.cloud.utils.component.ManagerBase;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HypervisorGuruManagerImpl extends ManagerBase implements HypervisorGuruManager {
    public static final Logger s_logger = LoggerFactory.getLogger(HypervisorGuruManagerImpl.class.getName());

    @Inject
    HostDao _hostDao;

    List<HypervisorGuru> _hvGuruList;
    Map<HypervisorType, HypervisorGuru> _hvGurus = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        for (final HypervisorGuru guru : _hvGuruList) {
            _hvGurus.put(guru.getHypervisorType(), guru);
        }
    }

    @Override
    public HypervisorGuru getGuru(final HypervisorType hypervisorType) {
        if (hypervisorType == null) {
            return null;
        }

        HypervisorGuru result = _hvGurus.get(hypervisorType);

        if (result == null) {
            for (final HypervisorGuru guru : _hvGuruList) {
                if (guru.getHypervisorType() == hypervisorType) {
                    _hvGurus.put(hypervisorType, guru);
                    result = guru;
                    break;
                }
            }
        }

        return result;
    }

    @Override
    public long getGuruProcessedCommandTargetHost(final long hostId, final Command cmd) {
        for (final HypervisorGuru guru : _hvGuruList) {
            final Pair<Boolean, Long> result = guru.getCommandHostDelegation(hostId, cmd);
            if (result.first()) {
                return result.second();
            }
        }
        return hostId;
    }

    public List<HypervisorGuru> getHvGuruList() {
        return _hvGuruList;
    }

    @Inject
    public void setHvGuruList(final List<HypervisorGuru> hvGuruList) {
        this._hvGuruList = hvGuruList;
    }
}
