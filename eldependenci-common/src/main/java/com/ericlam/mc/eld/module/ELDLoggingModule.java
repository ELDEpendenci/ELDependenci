package com.ericlam.mc.eld.module;

import com.ericlam.mc.eld.services.LoggingService;
import com.google.inject.AbstractModule;

public final class ELDLoggingModule extends AbstractModule {

    private final LoggingService loggingService;

    public ELDLoggingModule(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    @Override
    protected void configure() {
        bind(LoggingService.class).toInstance(loggingService);
    }
}
