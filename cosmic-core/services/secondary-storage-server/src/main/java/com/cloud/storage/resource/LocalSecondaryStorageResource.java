package com.cloud.storage.resource;

import com.cloud.agent.api.storage.ListTemplateAnswer;
import com.cloud.agent.api.storage.ListTemplateCommand;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.CheckHealthAnswer;
import com.cloud.legacymodel.communication.answer.ReadyAnswer;
import com.cloud.legacymodel.communication.command.CheckHealthCommand;
import com.cloud.legacymodel.communication.command.Command;
import com.cloud.legacymodel.communication.command.ComputeChecksumCommand;
import com.cloud.legacymodel.communication.command.PingCommand;
import com.cloud.legacymodel.communication.command.PingStorageCommand;
import com.cloud.legacymodel.communication.command.ReadyCommand;
import com.cloud.legacymodel.communication.command.SecStorageSetupCommand;
import com.cloud.legacymodel.communication.command.StartupCommand;
import com.cloud.legacymodel.communication.command.StartupStorageCommand;
import com.cloud.legacymodel.storage.TemplateProp;
import com.cloud.legacymodel.to.NfsTO;
import com.cloud.model.enumeration.HostType;
import com.cloud.model.enumeration.StoragePoolType;
import com.cloud.model.enumeration.StorageResourceType;
import com.cloud.resource.ServerResourceBase;
import com.cloud.storage.StorageLayer;
import com.cloud.storage.command.DownloadCommand;
import com.cloud.storage.command.DownloadProgressCommand;
import com.cloud.storage.template.DownloadManager;
import com.cloud.storage.template.DownloadManagerImpl;
import com.cloud.utils.component.ComponentContext;

import javax.naming.ConfigurationException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalSecondaryStorageResource extends ServerResourceBase implements SecondaryStorageResource {
    private static final Logger s_logger = LoggerFactory.getLogger(LocalSecondaryStorageResource.class);
    int _timeout;

    String _instance;
    String _parent;

    String _dc;
    String _pod;
    String _guid;

    StorageLayer _storage;

    DownloadManager _dlMgr;

    @Override
    public String getRootDir(final String url) {
        return getRootDir();
    }

    public String getRootDir() {
        return _parent;
    }

    @Override
    public HostType getType() {
        return HostType.LocalSecondaryStorage;
    }

    @Override
    public StartupCommand[] initialize() {

        final StartupStorageCommand cmd =
                new StartupStorageCommand(_parent, StoragePoolType.Filesystem, 1024l * 1024l * 1024l * 1024l, _dlMgr.gatherTemplateInfo(_parent));
        cmd.setResourceType(StorageResourceType.LOCAL_SECONDARY_STORAGE);
        cmd.setIqn("local://");
        fillNetworkInformation(cmd);
        cmd.setDataCenter(_dc);
        cmd.setPod(_pod);
        cmd.setGuid(_guid);
        cmd.setName(_guid);
        cmd.setVersion(LocalSecondaryStorageResource.class.getPackage().getImplementationVersion());

        return new StartupCommand[]{cmd};
    }

    @Override
    public PingCommand getCurrentStatus(final long id) {
        return new PingStorageCommand(HostType.Storage, id, new HashMap<>());
    }

    @Override
    public Answer executeRequest(final Command cmd) {
        if (cmd instanceof DownloadProgressCommand) {
            return _dlMgr.handleDownloadCommand(this, (DownloadProgressCommand) cmd);
        } else if (cmd instanceof DownloadCommand) {
            return _dlMgr.handleDownloadCommand(this, (DownloadCommand) cmd);
        } else if (cmd instanceof CheckHealthCommand) {
            return new CheckHealthAnswer((CheckHealthCommand) cmd, true);
        } else if (cmd instanceof SecStorageSetupCommand) {
            return new Answer(cmd, true, "success");
        } else if (cmd instanceof ReadyCommand) {
            return new ReadyAnswer((ReadyCommand) cmd);
        } else if (cmd instanceof ListTemplateCommand) {
            return execute((ListTemplateCommand) cmd);
        } else if (cmd instanceof ComputeChecksumCommand) {
            return execute((ComputeChecksumCommand) cmd);
        } else {
            return Answer.createUnsupportedCommandAnswer(cmd);
        }
    }

    private Answer execute(final ListTemplateCommand cmd) {
        final String root = getRootDir();
        final Map<String, TemplateProp> templateInfos = _dlMgr.gatherTemplateInfo(root);
        return new ListTemplateAnswer(((NfsTO) cmd.getDataStore()).getUrl(), templateInfos);
    }

    private Answer execute(final ComputeChecksumCommand cmd) {
        return new Answer(cmd, false, null);
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

        _instance = (String) params.get("instance");

        _parent = (String) params.get("mount.path");
        if (_parent == null) {
            throw new ConfigurationException("No directory specified.");
        }

        _storage = (StorageLayer) params.get(StorageLayer.InstanceConfigKey);
        if (_storage == null) {
            String value = (String) params.get(StorageLayer.ClassConfigKey);
            if (value == null) {
                value = "com.cloud.storage.JavaStorageLayer";
            }

            try {
                final Class<StorageLayer> clazz = (Class<StorageLayer>) Class.forName(value);
                _storage = ComponentContext.inject(clazz);
            } catch (final ClassNotFoundException e) {
                throw new ConfigurationException("Unable to find class " + value);
            }
        }

        if (!_storage.mkdirs(_parent)) {
            s_logger.warn("Unable to create the directory " + _parent);
            throw new ConfigurationException("Unable to create the directory " + _parent);
        }

        s_logger.info("Mount point established at " + _parent);

        params.put("template.parent", _parent);
        params.put(StorageLayer.InstanceConfigKey, _storage);

        _dlMgr = new DownloadManagerImpl();
        _dlMgr.configure("DownloadManager", params);

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

    @Override
    protected String getDefaultScriptsDir() {
        return "scripts/storage/secondary";
    }

    @Override
    public void disconnected() {
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
