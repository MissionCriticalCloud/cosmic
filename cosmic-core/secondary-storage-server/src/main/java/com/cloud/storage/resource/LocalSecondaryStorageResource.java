package com.cloud.storage.resource;

import com.cloud.common.resource.ServerResourceBase;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.CheckHealthAnswer;
import com.cloud.legacymodel.communication.answer.ListTemplateAnswer;
import com.cloud.legacymodel.communication.answer.ReadyAnswer;
import com.cloud.legacymodel.communication.command.CheckHealthCommand;
import com.cloud.legacymodel.communication.command.Command;
import com.cloud.legacymodel.communication.command.ComputeChecksumCommand;
import com.cloud.legacymodel.communication.command.DownloadCommand;
import com.cloud.legacymodel.communication.command.DownloadProgressCommand;
import com.cloud.legacymodel.communication.command.ListTemplateCommand;
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
import com.cloud.storage.template.DownloadManager;
import com.cloud.storage.template.DownloadManagerImpl;
import com.cloud.utils.component.ComponentContext;
import com.cloud.utils.storage.StorageLayer;

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
        return this._parent;
    }

    @Override
    public HostType getType() {
        return HostType.LocalSecondaryStorage;
    }

    @Override
    public StartupCommand[] initialize() {

        final StartupStorageCommand cmd =
                new StartupStorageCommand(this._parent, StoragePoolType.Filesystem, 1024l * 1024l * 1024l * 1024l, this._dlMgr.gatherTemplateInfo(this._parent));
        cmd.setResourceType(StorageResourceType.LOCAL_SECONDARY_STORAGE);
        cmd.setIqn("local://");
        fillNetworkInformation(cmd);
        cmd.setDataCenter(this._dc);
        cmd.setPod(this._pod);
        cmd.setGuid(this._guid);
        cmd.setName(this._guid);
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
            return this._dlMgr.handleDownloadCommand(this, (DownloadProgressCommand) cmd);
        } else if (cmd instanceof DownloadCommand) {
            return this._dlMgr.handleDownloadCommand(this, (DownloadCommand) cmd);
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
        final Map<String, TemplateProp> templateInfos = this._dlMgr.gatherTemplateInfo(root);
        return new ListTemplateAnswer(((NfsTO) cmd.getDataStore()).getUrl(), templateInfos);
    }

    private Answer execute(final ComputeChecksumCommand cmd) {
        return new Answer(cmd, false, null);
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);

        this._guid = (String) params.get("guid");
        if (this._guid == null) {
            throw new ConfigurationException("Unable to find the guid");
        }

        this._dc = (String) params.get("zone");
        if (this._dc == null) {
            throw new ConfigurationException("Unable to find the zone");
        }
        this._pod = (String) params.get("pod");

        this._instance = (String) params.get("instance");

        this._parent = (String) params.get("mount.path");
        if (this._parent == null) {
            throw new ConfigurationException("No directory specified.");
        }

        this._storage = (StorageLayer) params.get(StorageLayer.InstanceConfigKey);
        if (this._storage == null) {
            String value = (String) params.get(StorageLayer.ClassConfigKey);
            if (value == null) {
                value = "com.cloud.utils.storage.JavaStorageLayer";
            }

            try {
                final Class<StorageLayer> clazz = (Class<StorageLayer>) Class.forName(value);
                this._storage = ComponentContext.inject(clazz);
            } catch (final ClassNotFoundException e) {
                throw new ConfigurationException("Unable to find class " + value);
            }
        }

        if (!this._storage.mkdirs(this._parent)) {
            s_logger.warn("Unable to create the directory " + this._parent);
            throw new ConfigurationException("Unable to create the directory " + this._parent);
        }

        s_logger.info("Mount point established at " + this._parent);

        params.put("template.parent", this._parent);
        params.put(StorageLayer.InstanceConfigKey, this._storage);

        this._dlMgr = new DownloadManagerImpl();
        this._dlMgr.configure("DownloadManager", params);

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
