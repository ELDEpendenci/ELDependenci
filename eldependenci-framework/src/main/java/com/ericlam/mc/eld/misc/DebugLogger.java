package com.ericlam.mc.eld.misc;

import java.util.logging.Logger;

public interface DebugLogger {

    void debug(String message, Object... args);

    void debug(Throwable throwable, String message, Object... args);

    void debug(Throwable throwable);

    void info(String message, Object... args);

    void info(Throwable throwable, String message, Object... args);

    void info(Throwable throwable);

    void warn(String message, Object... args);

    void warn(Throwable throwable, String message, Object... args);

    void warn(Throwable throwable);

    Logger getRealLogger();

}
