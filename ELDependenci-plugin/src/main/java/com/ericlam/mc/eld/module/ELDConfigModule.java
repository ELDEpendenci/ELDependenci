package com.ericlam.mc.eld.module;

import com.ericlam.mc.eld.ELDTypeListener;
import com.ericlam.mc.eld.ReflectionService;
import com.ericlam.mc.eld.configurations.ELDConfigManager;
import com.ericlam.mc.eld.services.ConfigPoolService;
import com.ericlam.mc.eld.services.ELDConfigPoolService;
import com.ericlam.mc.eld.services.ELDReflectionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;

public final class ELDConfigModule extends AbstractModule {

    private final ELDConfigPoolService groupConfigService;
    private final ELDReflectionService reflectionService;

    public ELDConfigModule(ELDConfigPoolService groupConfigService, ELDReflectionService reflectionService) {
        this.groupConfigService = groupConfigService;
        this.reflectionService = reflectionService;
    }

    @Override
    protected void configure() {
        bind(ObjectMapper.class).annotatedWith(Names.named("eld-yaml")).toInstance(ELDConfigManager.YAML_MAPPER);
        bind(ObjectMapper.class).annotatedWith(Names.named("eld-json")).toInstance(ELDConfigManager.JSON_MAPPER);
        bind(ConfigPoolService.class).toInstance(groupConfigService);
        bind(ReflectionService.class).toInstance(reflectionService);
        bindListener(Matchers.any(), new ELDTypeListener(groupConfigService, reflectionService));
    }
}
