package com.ericlam.mc.eld.services.logging;

import com.ericlam.mc.eld.misc.DebugLogger;
import com.ericlam.mc.eld.services.LoggingService;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ELDLoggingService implements LoggingService {

    private final Map<Class<?>, DebugLogger> loggerMap = new ConcurrentHashMap<>();
    private final Map<String, DebugLogger> loggerNameMap = new ConcurrentHashMap<>();

    @Override
    public DebugLogger getLogger(Class<?> cls) {
        return Optional.ofNullable(loggerMap.get(cls)).orElseGet(() -> {
            var logger = new ELDLogger(cls);
            loggerMap.put(cls, logger);
            return logger;
        });
    }

    @Override
    public DebugLogger getLogger(String name) {
        return Optional.ofNullable(loggerNameMap.get(name)).orElseGet(() -> {
            var logger = new ELDLogger(name);
            loggerNameMap.put(name, logger);
            return logger;
        });
    }
}
