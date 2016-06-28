package com.cloud.consoleproxy.util;

// logger facility for dynamic switch between console logger used in Applet and log4j based logger
public class Logger {
    public static final int LEVEL_TRACE = 1;
    public static final int LEVEL_DEBUG = 2;
    public static final int LEVEL_INFO = 3;
    public static final int LEVEL_WARN = 4;
    public static final int LEVEL_ERROR = 5;
    private static LoggerFactory factory = null;
    private static int level = LEVEL_INFO;
    private Class<?> clazz;
    private Logger logger;

    public Logger(final Class<?> clazz) {
        this.clazz = clazz;
    }

    protected Logger() {
    }

    public static Logger getLogger(final Class<?> clazz) {
        return new Logger(clazz);
    }

    public static void setFactory(final LoggerFactory f) {
        factory = f;
    }

    public static void setLevel(final int l) {
        level = l;
    }

    public boolean isTraceEnabled() {
        if (factory != null) {
            if (logger == null) {
                logger = factory.getLogger(clazz);
            }

            return logger.isTraceEnabled();
        }
        return level <= LEVEL_TRACE;
    }

    public boolean isDebugEnabled() {
        if (factory != null) {
            if (logger == null) {
                logger = factory.getLogger(clazz);
            }

            return logger.isDebugEnabled();
        }
        return level <= LEVEL_DEBUG;
    }

    public boolean isInfoEnabled() {
        if (factory != null) {
            if (logger == null) {
                logger = factory.getLogger(clazz);
            }

            return logger.isInfoEnabled();
        }
        return level <= LEVEL_INFO;
    }

    public void trace(final Object message) {

        if (factory != null) {
            if (logger == null) {
                logger = factory.getLogger(clazz);
            }

            logger.trace(message);
        } else {
            if (level <= LEVEL_TRACE) {
                System.out.println(message);
            }
        }
    }

    public void trace(final Object message, final Throwable exception) {
        if (factory != null) {
            if (logger == null) {
                logger = factory.getLogger(clazz);
            }

            logger.trace(message, exception);
        } else {
            if (level <= LEVEL_TRACE) {
                System.out.println(message);
                if (exception != null) {
                    exception.printStackTrace(System.out);
                }
            }
        }
    }

    public void info(final Object message) {
        if (factory != null) {
            if (logger == null) {
                logger = factory.getLogger(clazz);
            }

            logger.info(message);
        } else {
            if (level <= LEVEL_INFO) {
                System.out.println(message);
            }
        }
    }

    public void info(final Object message, final Throwable exception) {
        if (factory != null) {
            if (logger == null) {
                logger = factory.getLogger(clazz);
            }

            logger.info(message, exception);
        } else {
            if (level <= LEVEL_INFO) {
                System.out.println(message);
                if (exception != null) {
                    exception.printStackTrace(System.out);
                }
            }
        }
    }

    public void debug(final Object message) {
        if (factory != null) {
            if (logger == null) {
                logger = factory.getLogger(clazz);
            }

            logger.debug(message);
        } else {
            if (level <= LEVEL_DEBUG) {
                System.out.println(message);
            }
        }
    }

    public void debug(final Object message, final Throwable exception) {
        if (factory != null) {
            if (logger == null) {
                logger = factory.getLogger(clazz);
            }

            logger.debug(message, exception);
        } else {
            if (level <= LEVEL_DEBUG) {
                System.out.println(message);
                if (exception != null) {
                    exception.printStackTrace(System.out);
                }
            }
        }
    }

    public void warn(final Object message) {
        if (factory != null) {
            if (logger == null) {
                logger = factory.getLogger(clazz);
            }

            logger.warn(message);
        } else {
            if (level <= LEVEL_WARN) {
                System.out.println(message);
            }
        }
    }

    public void warn(final Object message, final Throwable exception) {
        if (factory != null) {
            if (logger == null) {
                logger = factory.getLogger(clazz);
            }

            logger.warn(message, exception);
        } else {
            if (level <= LEVEL_WARN) {
                System.out.println(message);
                if (exception != null) {
                    exception.printStackTrace(System.out);
                }
            }
        }
    }

    public void error(final Object message) {
        if (factory != null) {
            if (logger == null) {
                logger = factory.getLogger(clazz);
            }

            logger.error(message);
        } else {
            if (level <= LEVEL_ERROR) {
                System.out.println(message);
            }
        }
    }

    public void error(final Object message, final Throwable exception) {
        if (factory != null) {
            if (logger == null) {
                logger = factory.getLogger(clazz);
            }

            logger.error(message, exception);
        } else {
            if (level <= LEVEL_ERROR) {
                System.out.println(message);
                if (exception != null) {
                    exception.printStackTrace(System.out);
                }
            }
        }
    }
}
