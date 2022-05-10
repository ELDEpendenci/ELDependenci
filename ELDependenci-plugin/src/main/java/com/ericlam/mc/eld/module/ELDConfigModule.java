package com.ericlam.mc.eld.module;

import com.ericlam.mc.eld.ELDTypeListener;
import com.ericlam.mc.eld.configurations.ELDConfigManager;
import com.ericlam.mc.eld.services.ConfigPoolService;
import com.ericlam.mc.eld.services.ELDConfigPoolService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;

public final class ELDConfigModule extends AbstractModule {

    private final ELDConfigPoolService groupConfigService;

    public ELDConfigModule(ELDConfigPoolService groupConfigService) {
        this.groupConfigService = groupConfigService;
    }

    @Override
    protected void configure() {
        bind(ObjectMapper.class).annotatedWith(Names.named("eld-yaml")).toInstance(ELDConfigManager.YAML_MAPPER);
        bind(ObjectMapper.class).annotatedWith(Names.named("eld-json")).toInstance(ELDConfigManager.JSON_MAPPER);
        bind(ConfigPoolService.class).toInstance(groupConfigService);
        bindListener(Matchers.any(), new ELDTypeListener(groupConfigService));
    }
}
