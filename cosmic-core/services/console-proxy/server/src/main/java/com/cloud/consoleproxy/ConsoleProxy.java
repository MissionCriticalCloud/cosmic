package com.cloud.consoleproxy;

import com.cloud.consoleproxy.util.Logger;
import com.cloud.utils.PropertiesUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * ConsoleProxy, singleton class that manages overall activities in console proxy process. To make legacy code work, we still
 */
public class ConsoleProxy {
    public static final int KEYBOARD_RAW = 0;
    public static final int KEYBOARD_COOKED = 1;
    public static final int VIEWER_LINGER_SECONDS = 180;
    private static final Logger s_logger = Logger.getLogger(ConsoleProxy.class);
    public static Object context;

    // this has become more ugly, to store keystore info passed from management server (we now use management server managed keystore to support
    // dynamically changing to customer supplied certificate)
    public static byte[] ksBits;
    public static String ksPassword;

    public static Method authMethod;
    public static Method reportMethod;
    public static Method ensureRouteMethod;

    static Hashtable<String, ConsoleProxyClient> connectionMap = new Hashtable<>();
    static int httpListenPort = 80;
    static int httpCmdListenPort = 8001;
    static int reconnectMaxRetry = 5;
    static int readTimeoutSeconds = 90;
    static int keyboardType = KEYBOARD_RAW;
    static String factoryClzName;
    static boolean standaloneStart = false;

    static String encryptorPassword = genDefaultEncryptorPassword();

    private static String genDefaultEncryptorPassword() {
        try {
            final SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

            final byte[] randomBytes = new byte[16];
            random.nextBytes(randomBytes);
            return Base64.encodeBase64String(randomBytes);
        } catch (final NoSuchAlgorithmException e) {
            s_logger.error("Unexpected exception ", e);
            assert (false);
        }

        return "Dummy";
    }

    public static void ensureRoute(final String address) {
        if (ensureRouteMethod != null) {
            try {
                ensureRouteMethod.invoke(ConsoleProxy.context, address);
            } catch (final IllegalAccessException e) {
                s_logger.error("Unable to invoke ensureRoute due to " + e.getMessage());
            } catch (final InvocationTargetException e) {
                s_logger.error("Unable to invoke ensureRoute due to " + e.getMessage());
            }
        } else {
            s_logger.warn("Unable to find ensureRoute method, console proxy agent is not up to date");
        }
    }

    public static void startWithContext(final Properties conf, final Object context, final byte[] ksBits, final String ksPassword) {
        s_logger.info("Start console proxy with context");
        if (conf != null) {
            for (final Object key : conf.keySet()) {
                s_logger.info("Context property " + (String) key + ": " + conf.getProperty((String) key));
            }
        }

        configLog4j();
        Logger.setFactory(new ConsoleProxyLoggerFactory());

        // Using reflection to setup private/secure communication channel towards management server
        ConsoleProxy.context = context;
        ConsoleProxy.ksBits = ksBits;
        ConsoleProxy.ksPassword = ksPassword;
        try {
            final Class<?> contextClazz = Class.forName("com.cloud.agent.resource.consoleproxy.ConsoleProxyResource");
            authMethod = contextClazz.getDeclaredMethod("authenticateConsoleAccess", String.class, String.class, String.class, String.class, String.class, Boolean.class);
            reportMethod = contextClazz.getDeclaredMethod("reportLoadInfo", String.class);
            ensureRouteMethod = contextClazz.getDeclaredMethod("ensureRoute", String.class);
        } catch (final SecurityException e) {
            s_logger.error("Unable to setup private channel due to SecurityException", e);
        } catch (final NoSuchMethodException e) {
            s_logger.error("Unable to setup private channel due to NoSuchMethodException", e);
        } catch (final IllegalArgumentException e) {
            s_logger.error("Unable to setup private channel due to IllegalArgumentException", e);
        } catch (final ClassNotFoundException e) {
            s_logger.error("Unable to setup private channel due to ClassNotFoundException", e);
        }

        // merge properties from conf file
        InputStream confs = ConsoleProxy.class.getResourceAsStream("/conf/consoleproxy.properties");
        final Properties props = new Properties();
        if (confs == null) {
            final File file = PropertiesUtil.findConfigFile("consoleproxy.properties");
            if (file == null) {
                s_logger.info("Can't load consoleproxy.properties from classpath, will use default configuration");
            } else {
                try {
                    confs = new FileInputStream(file);
                } catch (final FileNotFoundException e) {
                    s_logger.info("Ignoring file not found exception and using defaults");
                }
            }
        }
        if (confs != null) {
            try {
                props.load(confs);

                for (final Object key : props.keySet()) {
                    // give properties passed via context high priority, treat properties from consoleproxy.properties
                    // as default values
                    if (conf.get(key) == null) {
                        conf.put(key, props.get(key));
                    }
                }
            } catch (final Exception e) {
                s_logger.error(e.toString(), e);
            }
        }
        try {
            confs.close();
        } catch (final IOException e) {
            s_logger.error("Failed to close consolepropxy.properties : " + e.toString(), e);
        }

        start(conf);
    }

    private static void configLog4j() {
        URL configUrl = System.class.getResource("/conf/log4j-cloud.xml");
        if (configUrl == null) {
            configUrl = ClassLoader.getSystemResource("log4j-cloud.xml");
        }

        if (configUrl == null) {
            configUrl = ClassLoader.getSystemResource("conf/log4j-cloud.xml");
        }

        if (configUrl != null) {
            try {
                System.out.println("Configure log4j using " + configUrl.toURI().toString());
            } catch (final URISyntaxException e1) {
                e1.printStackTrace();
            }

            try {
                final File file = new File(configUrl.toURI());

                System.out.println("Log4j configuration from : " + file.getAbsolutePath());
                DOMConfigurator.configureAndWatch(file.getAbsolutePath(), 10000);
            } catch (final URISyntaxException e) {
                System.out.println("Unable to convert log4j configuration Url to URI");
            }
            // DOMConfigurator.configure(configUrl);
        } else {
            System.out.println("Configure log4j with default properties");
        }
    }

    public static void start(final Properties conf) {
        System.setProperty("java.awt.headless", "true");

        configProxy(conf);

        final ConsoleProxyServerFactory factory = getHttpServerFactory();
        if (factory == null) {
            s_logger.error("Unable to load console proxy server factory");
            System.exit(1);
        }

        if (httpListenPort != 0) {
            startupHttpMain();
        } else {
            s_logger.error("A valid HTTP server port is required to be specified, please check your consoleproxy.httpListenPort settings");
            System.exit(1);
        }

        if (httpCmdListenPort > 0) {
            startupHttpCmdPort();
        } else {
            s_logger.info("HTTP command port is disabled");
        }

        final ConsoleProxyGCThread cthread = new ConsoleProxyGCThread(connectionMap);
        cthread.setName("Console Proxy GC Thread");
        cthread.start();
    }

    private static void configProxy(final Properties conf) {
        s_logger.info("Configure console proxy...");
        for (final Object key : conf.keySet()) {
            s_logger.info("Property " + (String) key + ": " + conf.getProperty((String) key));
        }

        String s = conf.getProperty("consoleproxy.httpListenPort");
        if (s != null) {
            httpListenPort = Integer.parseInt(s);
            s_logger.info("Setting httpListenPort=" + s);
        }

        s = conf.getProperty("premium");
        if (s != null && s.equalsIgnoreCase("true")) {
            s_logger.info("Premium setting will override settings from consoleproxy.properties, listen at port 443");
            httpListenPort = 443;
            factoryClzName = "com.cloud.consoleproxy.ConsoleProxySecureServerFactoryImpl";
        } else {
            factoryClzName = ConsoleProxyBaseServerFactoryImpl.class.getName();
        }

        s = conf.getProperty("consoleproxy.httpCmdListenPort");
        if (s != null) {
            httpCmdListenPort = Integer.parseInt(s);
            s_logger.info("Setting httpCmdListenPort=" + s);
        }

        s = conf.getProperty("consoleproxy.reconnectMaxRetry");
        if (s != null) {
            reconnectMaxRetry = Integer.parseInt(s);
            s_logger.info("Setting reconnectMaxRetry=" + reconnectMaxRetry);
        }

        s = conf.getProperty("consoleproxy.readTimeoutSeconds");
        if (s != null) {
            readTimeoutSeconds = Integer.parseInt(s);
            s_logger.info("Setting readTimeoutSeconds=" + readTimeoutSeconds);
        }
    }

    public static ConsoleProxyServerFactory getHttpServerFactory() {
        try {
            final Class<?> clz = Class.forName(factoryClzName);
            try {
                final ConsoleProxyServerFactory factory = (ConsoleProxyServerFactory) clz.newInstance();
                factory.init(ConsoleProxy.ksBits, ConsoleProxy.ksPassword);
                return factory;
            } catch (final InstantiationException e) {
                s_logger.error(e.getMessage(), e);
                return null;
            } catch (final IllegalAccessException e) {
                s_logger.error(e.getMessage(), e);
                return null;
            }
        } catch (final ClassNotFoundException e) {
            s_logger.warn("Unable to find http server factory class: " + factoryClzName);
            return new ConsoleProxyBaseServerFactoryImpl();
        }
    }

    private static void startupHttpMain() {
        try {
            final ConsoleProxyServerFactory factory = getHttpServerFactory();
            if (factory == null) {
                s_logger.error("Unable to load HTTP server factory");
                System.exit(1);
            }

            final HttpServer server = factory.createHttpServerInstance(httpListenPort);
            server.createContext("/getscreen", new ConsoleProxyThumbnailHandler());
            server.createContext("/resource/", new ConsoleProxyResourceHandler());
            server.createContext("/ajax", new ConsoleProxyAjaxHandler());
            server.createContext("/ajaximg", new ConsoleProxyAjaxImageHandler());
            server.setExecutor(new ThreadExecutor()); // creates a default executor
            server.start();
        } catch (final Exception e) {
            s_logger.error(e.getMessage(), e);
            System.exit(1);
        }
    }

    private static void startupHttpCmdPort() {
        try {
            s_logger.info("Listening for HTTP CMDs on port " + httpCmdListenPort);
            final HttpServer cmdServer = HttpServer.create(new InetSocketAddress(httpCmdListenPort), 2);
            cmdServer.createContext("/cmd", new ConsoleProxyCmdHandler());
            cmdServer.setExecutor(new ThreadExecutor()); // creates a default executor
            cmdServer.start();
        } catch (final Exception e) {
            s_logger.error(e.getMessage(), e);
            System.exit(1);
        }
    }

    public static void main(final String[] argv) {
        standaloneStart = true;
        configLog4j();
        Logger.setFactory(new ConsoleProxyLoggerFactory());

        final InputStream confs = ConsoleProxy.class.getResourceAsStream("/conf/consoleproxy.properties");
        final Properties conf = new Properties();
        if (confs == null) {
            s_logger.info("Can't load consoleproxy.properties from classpath, will use default configuration");
        } else {
            try {
                conf.load(confs);
            } catch (final Exception e) {
                s_logger.error(e.toString(), e);
            } finally {
                try {
                    confs.close();
                } catch (final IOException ioex) {
                    s_logger.error(ioex.toString(), ioex);
                }
            }
        }
        start(conf);
    }

    public static ConsoleProxyClient getVncViewer(final ConsoleProxyClientParam param) throws Exception {
        ConsoleProxyClient viewer = null;

        boolean reportLoadChange = false;
        final String clientKey = param.getClientMapKey();
        synchronized (connectionMap) {
            viewer = connectionMap.get(clientKey);
            if (viewer == null) {
                viewer = getClient(param);
                viewer.initClient(param);
                connectionMap.put(clientKey, viewer);
                s_logger.info("Added viewer object " + viewer);

                reportLoadChange = true;
            } else if (!viewer.isFrontEndAlive()) {
                s_logger.info("The rfb thread died, reinitializing the viewer " + viewer);
                viewer.initClient(param);
            } else if (!param.getClientHostPassword().equals(viewer.getClientHostPassword())) {
                s_logger.warn("Bad sid detected(VNC port may be reused). sid in session: " + viewer.getClientHostPassword() + ", sid in request: " +
                        param.getClientHostPassword());
                viewer.initClient(param);
            }
        }

        if (reportLoadChange) {
            final ConsoleProxyClientStatsCollector statsCollector = getStatsCollector();
            final String loadInfo = statsCollector.getStatsReport();
            reportLoadInfo(loadInfo);
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Report load change : " + loadInfo);
            }
        }

        return viewer;
    }

    private static ConsoleProxyClient getClient(final ConsoleProxyClientParam param) {
        return new ConsoleProxyVncClient();
    }

    public static ConsoleProxyClientStatsCollector getStatsCollector() {
        synchronized (connectionMap) {
            return new ConsoleProxyClientStatsCollector(connectionMap);
        }
    }

    public static void reportLoadInfo(final String gsonLoadInfo) {
        if (reportMethod != null) {
            try {
                reportMethod.invoke(ConsoleProxy.context, gsonLoadInfo);
            } catch (final IllegalAccessException e) {
                s_logger.error("Unable to invoke reportLoadInfo due to " + e.getMessage());
            } catch (final InvocationTargetException e) {
                s_logger.error("Unable to invoke reportLoadInfo due to " + e.getMessage());
            }
        } else {
            s_logger.warn("Private channel towards management server is not setup. Switch to offline mode and ignore load report");
        }
    }

    public static ConsoleProxyClient getAjaxVncViewer(final ConsoleProxyClientParam param, final String ajaxSession) throws Exception {

        boolean reportLoadChange = false;
        final String clientKey = param.getClientMapKey();
        synchronized (connectionMap) {
            ConsoleProxyClient viewer = connectionMap.get(clientKey);
            if (viewer == null) {
                authenticationExternally(param);
                viewer = getClient(param);
                viewer.initClient(param);

                connectionMap.put(clientKey, viewer);
                s_logger.info("Added viewer object " + viewer);
                reportLoadChange = true;
            } else {
                // protected against malicous attack by modifying URL content
                if (ajaxSession != null) {
                    final long ajaxSessionIdFromUrl = Long.parseLong(ajaxSession);
                    if (ajaxSessionIdFromUrl != viewer.getAjaxSessionId()) {
                        throw new AuthenticationException("Cannot use the existing viewer " + viewer + ": modified AJAX session id");
                    }
                }

                if (param.getClientHostPassword() == null || param.getClientHostPassword().isEmpty() ||
                        !param.getClientHostPassword().equals(viewer.getClientHostPassword())) {
                    throw new AuthenticationException("Cannot use the existing viewer " + viewer + ": bad sid");
                }

                if (!viewer.isFrontEndAlive()) {

                    authenticationExternally(param);
                    viewer.initClient(param);
                    reportLoadChange = true;
                }
            }

            if (reportLoadChange) {
                final ConsoleProxyClientStatsCollector statsCollector = getStatsCollector();
                final String loadInfo = statsCollector.getStatsReport();
                reportLoadInfo(loadInfo);
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Report load change : " + loadInfo);
                }
            }
            return viewer;
        }
    }

    public static void authenticationExternally(final ConsoleProxyClientParam param) throws AuthenticationException {
        final ConsoleProxyAuthenticationResult authResult = authenticateConsoleAccess(param, false);

        if (authResult == null || !authResult.isSuccess()) {
            s_logger.warn("External authenticator failed authencation request for vm " + param.getClientTag() + " with sid " + param.getClientHostPassword());

            throw new AuthenticationException("External authenticator failed request for vm " + param.getClientTag() + " with sid " + param.getClientHostPassword());
        }
    }

    public static ConsoleProxyAuthenticationResult authenticateConsoleAccess(final ConsoleProxyClientParam param, final boolean reauthentication) {

        ConsoleProxyAuthenticationResult authResult = new ConsoleProxyAuthenticationResult();
        authResult.setSuccess(true);
        authResult.setReauthentication(reauthentication);
        authResult.setHost(param.getClientHostAddress());
        authResult.setPort(param.getClientHostPort());

        if (standaloneStart) {
            return authResult;
        }

        if (authMethod != null) {
            final Object result;
            try {
                result =
                        authMethod.invoke(ConsoleProxy.context, param.getClientHostAddress(), String.valueOf(param.getClientHostPort()), param.getClientTag(),
                                param.getClientHostPassword(), param.getTicket(), new Boolean(reauthentication));
            } catch (final IllegalAccessException e) {
                s_logger.error("Unable to invoke authenticateConsoleAccess due to IllegalAccessException" + " for vm: " + param.getClientTag(), e);
                authResult.setSuccess(false);
                return authResult;
            } catch (final InvocationTargetException e) {
                s_logger.error("Unable to invoke authenticateConsoleAccess due to InvocationTargetException " + " for vm: " + param.getClientTag(), e);
                authResult.setSuccess(false);
                return authResult;
            }

            if (result != null && result instanceof String) {
                authResult = new Gson().fromJson((String) result, ConsoleProxyAuthenticationResult.class);
            } else {
                s_logger.error("Invalid authentication return object " + result + " for vm: " + param.getClientTag() + ", decline the access");
                authResult.setSuccess(false);
            }
        } else {
            s_logger.warn("Private channel towards management server is not setup. Switch to offline mode and allow access to vm: " + param.getClientTag());
        }

        return authResult;
    }

    public static void removeViewer(final ConsoleProxyClient viewer) {
        synchronized (connectionMap) {
            for (final Map.Entry<String, ConsoleProxyClient> entry : connectionMap.entrySet()) {
                if (entry.getValue() == viewer) {
                    connectionMap.remove(entry.getKey());
                    return;
                }
            }
        }
    }

    public static ConsoleProxyAuthenticationResult reAuthenticationExternally(final ConsoleProxyClientParam param) {
        return authenticateConsoleAccess(param, true);
    }

    public static String getEncryptorPassword() {
        return encryptorPassword;
    }

    public static void setEncryptorPassword(final String password) {
        encryptorPassword = password;
    }

    static class ThreadExecutor implements Executor {
        @Override
        public void execute(final Runnable r) {
            new Thread(r).start();
        }
    }
}
