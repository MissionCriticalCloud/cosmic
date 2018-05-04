package com.cloud.common.transport;

import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.Command;
import com.cloud.legacymodel.to.DataStoreTO;
import com.cloud.legacymodel.to.DataTO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GsonHelper {
    protected static final Gson s_gson;
    protected static final Gson s_gogger;
    private static final Logger s_logger = LoggerFactory.getLogger(GsonHelper.class);

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
        final InterfaceTypeAdaptor<DataStoreTO> dsAdaptor = new InterfaceTypeAdaptor<>();
        builder.registerTypeAdapter(DataStoreTO.class, dsAdaptor);

        final InterfaceTypeAdaptor<DataTO> dtAdaptor = new InterfaceTypeAdaptor<>();
        builder.registerTypeAdapter(DataTO.class, dtAdaptor);

        final ArrayTypeAdaptor<Command> cmdAdaptor = new ArrayTypeAdaptor<>();
        builder.registerTypeAdapter(new TypeToken<Command[]>() {
        }.getType(), cmdAdaptor);

        final ArrayTypeAdaptor<Answer> ansAdaptor = new ArrayTypeAdaptor<>();
        builder.registerTypeAdapter(new TypeToken<Answer[]>() {
        }.getType(), ansAdaptor);

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
