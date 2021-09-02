package com.ericlam.mc.eld;

import com.ericlam.mc.eld.services.ConfigPoolService;
import com.ericlam.mc.eld.services.ELDConfigPoolService;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

public final class ELDConfigModule extends AbstractModule {

    private final ELDConfigPoolService groupConfigService;

    public ELDConfigModule(ELDConfigPoolService groupConfigService) {
        this.groupConfigService = groupConfigService;
    }

    @Override
    protected void configure() {
        bind(ConfigPoolService.class).toInstance(groupConfigService);
        bindListener(Matchers.any(), new ELDTypeListener(groupConfigService));
    }
}