package com.ericlam.mc.eld.guice;

import com.ericlam.mc.eld.services.LoggingService;
import com.ericlam.mc.eld.services.ReflectionService;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

public final class ELDLoggingModule extends AbstractModule {

    private final LoggingService loggingService;
    private final ReflectionService reflectionService;

    public ELDLoggingModule(LoggingService loggingService, ReflectionService reflectionService) {
        this.loggingService = loggingService;
        this.reflectionService = reflectionService;
    }

    @Override
    protected void configure() {
        bind(LoggingService.class).toInstance(loggingService);
        bindListener(Matchers.any(), new ELDDebugLoggerListener(reflectionService, loggingService));
    }
}
