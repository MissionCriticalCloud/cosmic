package com.cloud.storage.resource;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CheckHealthAnswer;
import com.cloud.agent.api.CheckHealthCommand;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.GetStorageStatsAnswer;
import com.cloud.agent.api.GetStorageStatsCommand;
import com.cloud.agent.api.PingCommand;
import com.cloud.agent.api.PingStorageCommand;
import com.cloud.agent.api.ReadyAnswer;
import com.cloud.agent.api.ReadyCommand;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupStorageCommand;
import com.cloud.agent.api.storage.DownloadAnswer;
import com.cloud.host.Host;
import com.cloud.host.Host.Type;
import com.cloud.resource.ServerResource;
import com.cloud.resource.ServerResourceBase;
import com.cloud.storage.Storage;
import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.storage.template.TemplateConstants;
import com.cloud.storage.template.TemplateProp;
import org.apache.cloudstack.storage.command.DownloadCommand;
import org.apache.cloudstack.storage.command.DownloadProgressCommand;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummySecondaryStorageResource extends ServerResourceBase implements ServerResource {
    private static final Logger s_logger = LoggerFactory.getLogger(DummySecondaryStorageResource.class);

    String _dc;
    String _pod;
    String _guid;
    String _dummyPath;
    @Inject
    VMTemplateDao _tmpltDao;
    private boolean _useServiceVm;

    public DummySecondaryStorageResource() {
        setUseServiceVm(true);
    }

    public void setUseServiceVm(final boolean useServiceVm) {
        this._useServiceVm = useServiceVm;
    }

    public DummySecondaryStorageResource(final boolean useServiceVM) {
        setUseServiceVm(useServiceVM);
    }

    @Override
    public Type getType() {
        return Host.Type.SecondaryStorage;
    }

    @Override
    public StartupCommand[] initialize() {
        final StartupStorageCommand cmd =
                new StartupStorageCommand("dummy", StoragePoolType.NetworkFilesystem, 1024 * 1024 * 1024 * 100L, new HashMap<>());

        cmd.setResourceType(Storage.StorageResourceType.SECONDARY_STORAGE);
        cmd.setIqn(null);
        cmd.setNfsShare(_guid);

        fillNetworkInformation(cmd);
        cmd.setDataCenter(_dc);
        cmd.setPod(_pod);
        cmd.setGuid(_guid);

        cmd.setName(_guid);
        cmd.setVersion(DummySecondaryStorageResource.class.getPackage().getImplementationVersion());
        /* gather TemplateInfo in second storage */
        cmd.setTemplateInfo(getDefaultSystemVmTemplateInfo());
        cmd.getHostDetails().put("mount.parent", "dummy");
        cmd.getHostDetails().put("mount.path", "dummy");
        cmd.getHostDetails().put("orig.url", _guid);

        final String[] tok = _dummyPath.split(":");
        cmd.setPrivateIpAddress(tok[0]);
        return new StartupCommand[]{cmd};
    }

    @Override
    public PingCommand getCurrentStatus(final long id) {
        return new PingStorageCommand(Host.Type.Storage, id, new HashMap<>());
    }

    @Override
    public Answer executeRequest(final Command cmd) {
        if (cmd instanceof DownloadProgressCommand) {
            return new DownloadAnswer(null, 100, cmd, com.cloud.storage.VMTemplateStorageResourceAssoc.Status.DOWNLOADED, "dummyFS", "/dummy");
        } else if (cmd instanceof DownloadCommand) {
            return new DownloadAnswer(null, 100, cmd, com.cloud.storage.VMTemplateStorageResourceAssoc.Status.DOWNLOADED, "dummyFS", "/dummy");
        } else if (cmd instanceof GetStorageStatsCommand) {
            return execute((GetStorageStatsCommand) cmd);
        } else if (cmd instanceof CheckHealthCommand) {
            return new CheckHealthAnswer((CheckHealthCommand) cmd, true);
        } else if (cmd instanceof ReadyCommand) {
            return new ReadyAnswer((ReadyCommand) cmd);
        } else {
            return Answer.createUnsupportedCommandAnswer(cmd);
        }
    }

    protected GetStorageStatsAnswer execute(final GetStorageStatsCommand cmd) {
        final long size = 1024 * 1024 * 1024 * 100L;
        return new GetStorageStatsAnswer(cmd, 0, size);
    }

    public Map<String, TemplateProp> getDefaultSystemVmTemplateInfo() {
        final List<VMTemplateVO> tmplts = _tmpltDao.listAllSystemVMTemplates();
        final Map<String, TemplateProp> tmpltInfo = new HashMap<>();
        if (tmplts != null) {
            for (final VMTemplateVO tmplt : tmplts) {
                final TemplateProp routingInfo =
                        new TemplateProp(tmplt.getUniqueName(), TemplateConstants.DEFAULT_SYSTEM_VM_TEMPLATE_PATH + tmplt.getId() + File.separator, false, false);
                tmpltInfo.put(tmplt.getUniqueName(), routingInfo);
            }
        }
        return tmpltInfo;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);

        _guid = (String) params.get("guid");
        if (_guid == null) {
            throw new ConfigurationException("Unable to find the guid");
        }

        _dc = (String) params.get("zone");
        if (_dc == null) {
            throw new ConfigurationException("Unable to find the zone");
        }
        _pod = (String) params.get("pod");

        _dummyPath = (String) params.get("mount.path");
        if (_dummyPath == null) {
            throw new ConfigurationException("Unable to find mount.path");
        }

        return true;
    }

    @Override
    protected String getDefaultScriptsDir() {
        return "dummy";
    }

    public boolean useServiceVm() {
        return _useServiceVm;
    }

    @Override
    public void setName(final String name) {
        // TODO Auto-generated method stub

    }

    @Override
    public Map<String, Object> getConfigParams() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setConfigParams(final Map<String, Object> params) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getRunLevel() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setRunLevel(final int level) {
        // TODO Auto-generated method stub

    }
}
