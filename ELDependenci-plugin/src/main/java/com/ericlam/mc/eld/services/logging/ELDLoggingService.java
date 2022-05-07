package com.ericlam.mc.eld.services.logging;

import com.ericlam.mc.eld.bukkit.ELDConfig;
import com.ericlam.mc.eld.misc.DebugLogger;
import com.ericlam.mc.eld.services.LoggingService;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ELDLoggingService implements LoggingService {

    private final Map<Class<?>, DebugLogger> loggerMap = new ConcurrentHashMap<>();
    private final Map<String, DebugLogger> loggerNameMap = new ConcurrentHashMap<>();

    private final ELDConfig config;

    public ELDLoggingService(ELDConfig config) {
        this.config = config;
    }

    @Override
    public DebugLogger getLogger(Class<?> cls) {
        return Optional.ofNullable(loggerMap.get(cls)).orElseGet(() -> {
            var logger = new ELDLogger(cls, config);
            loggerMap.put(cls, logger);
            return logger;
        });
    }

    @Override
    public DebugLogger getLogger(String name) {
        return Optional.ofNullable(loggerNameMap.get(name)).orElseGet(() -> {
            var logger = new ELDLogger(name, config);
            loggerNameMap.put(name, logger);
            return logger;
        });
    }
}
