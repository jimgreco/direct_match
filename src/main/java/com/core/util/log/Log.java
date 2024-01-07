package com.core.util.log;

/**
 * User: jgreco
 */
public interface Log {
    boolean isDebugEnabled();
    void setDebug(boolean debug);

    void debug(Logger log);
    void info(Logger log);
    void warn(Logger log);
    void error(Logger log);

    Logger log();
}
