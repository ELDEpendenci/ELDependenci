package com.ericlam.mc.eld.guice;

import com.ericlam.mc.eld.annotations.InjectLogger;
import com.ericlam.mc.eld.misc.DebugLogger;
import com.ericlam.mc.eld.services.LoggingService;
import com.ericlam.mc.eld.services.ReflectionService;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import java.lang.reflect.Field;

public class ELDDebugLoggerListener implements TypeListener {

    private final ReflectionService reflectionService;
    private final LoggingService loggingService;

    public ELDDebugLoggerListener(ReflectionService reflectionService, LoggingService loggingService) {
        this.reflectionService = reflectionService;
        this.loggingService = loggingService;
    }

    @Override
    public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
        for (Field field : reflectionService.getDeclaredFieldsUpTo(type.getRawType(), null)) {
            if (!field.isAnnotationPresent(InjectLogger.class) || field.getType() != DebugLogger.class) continue;
            var name = field.getAnnotation(InjectLogger.class).name();
            var loggerName = name.isBlank() ? type.getRawType().getName() : name;
            var logger = loggingService.getLogger(loggerName);
            encounter.register(new InstanceInjector<>(field, logger));
        }
    }

}
