//

//

package com.cloud.serializer;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.SecStorageFirewallCfgCommand.PortConfig;
import com.cloud.agent.api.to.DataStoreTO;
import com.cloud.agent.api.to.DataTO;
import com.cloud.agent.transport.ArrayTypeAdaptor;
import com.cloud.agent.transport.InterfaceTypeAdaptor;
import com.cloud.agent.transport.LoggingExclusionStrategy;
import com.cloud.agent.transport.Request.NwGroupsCommandTypeAdaptor;
import com.cloud.agent.transport.Request.PortConfigListTypeAdaptor;
import com.cloud.utils.Pair;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.log4j.Logger;

public class GsonHelper {
    protected static final Gson s_gson;
    protected static final Gson s_gogger;
    private static final Logger s_logger = Logger.getLogger(GsonHelper.class);

    static {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        s_gson = setDefaultGsonConfig(gsonBuilder);
        final GsonBuilder loggerBuilder = new GsonBuilder();
        loggerBuilder.disableHtmlEscaping();
        loggerBuilder.setExclusionStrategies(new LoggingExclusionStrategy(s_logger));
        s_gogger = setDefaultGsonConfig(loggerBuilder);
        s_logger.info("Default Builder inited.");
    }

    static Gson setDefaultGsonConfig(final GsonBuilder builder) {
        builder.setVersion(1.5);
        final InterfaceTypeAdaptor<DataStoreTO> dsAdaptor = new InterfaceTypeAdaptor<>();
        builder.registerTypeAdapter(DataStoreTO.class, dsAdaptor);
        final InterfaceTypeAdaptor<DataTO> dtAdaptor = new InterfaceTypeAdaptor<>();
        builder.registerTypeAdapter(DataTO.class, dtAdaptor);
        final ArrayTypeAdaptor<Command> cmdAdaptor = new ArrayTypeAdaptor<>();
        builder.registerTypeAdapter(Command[].class, cmdAdaptor);
        final ArrayTypeAdaptor<Answer> ansAdaptor = new ArrayTypeAdaptor<>();
        builder.registerTypeAdapter(Answer[].class, ansAdaptor);
        builder.registerTypeAdapter(new TypeToken<List<PortConfig>>() {
        }.getType(), new PortConfigListTypeAdaptor());
        builder.registerTypeAdapter(new TypeToken<Pair<Long, Long>>() {
        }.getType(), new NwGroupsCommandTypeAdaptor());
        final Gson gson = builder.create();
        dsAdaptor.initGson(gson);
        dtAdaptor.initGson(gson);
        cmdAdaptor.initGson(gson);
        ansAdaptor.initGson(gson);
        return gson;
    }

    public final static Gson getGson() {
        return s_gson;
    }

    public final static Gson getGsonLogger() {
        return s_gogger;
    }

    public final static Logger getLogger() {
        return s_logger;
    }
}
