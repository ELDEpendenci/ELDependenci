package com.ericlam.mc.eld.services.logging;

import com.ericlam.mc.eld.implement.ELDConfig;
import com.ericlam.mc.eld.misc.DebugLogger;
import com.ericlam.mc.eld.services.LoggingService;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class ELDLoggingService implements LoggingService {

    private final Map<Class<?>, DebugLogger> loggerMap = new ConcurrentHashMap<>();
    private final Map<String, DebugLogger> loggerNameMap = new ConcurrentHashMap<>();

    private final ELDConfig config;
    private final Logger parent;

    public ELDLoggingService(ELDConfig config, Logger parent) {
        this.config = config;
        this.parent = parent;
    }

    @Override
    public DebugLogger getLogger(Class<?> cls) {
        return Optional.ofNullable(loggerMap.get(cls)).orElseGet(() -> {
            var logger = new ELDLogger(cls, config, parent);
            loggerMap.put(cls, logger);
            return logger;
        });
    }

    @Override
    public DebugLogger getLogger(String name) {
        return Optional.ofNullable(loggerNameMap.get(name)).orElseGet(() -> {
            var logger = new ELDLogger(name, config, parent);
            loggerNameMap.put(name, logger);
            return logger;
        });
    }
}
