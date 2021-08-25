package com.ericlam.mc.eld.services;

import com.ericlam.mc.eld.GenericPoolService;
import com.ericlam.mc.eld.components.GroupLangConfiguration;

import javax.annotation.Nullable;

public final class ELDLangPoolService extends GenericPoolService<GroupLangConfiguration> implements LanguagePoolService {

    @Override
    public <C extends GroupLangConfiguration> ScheduleService.BukkitPromise<C> getLangAsync(Class<C> config, String id) {
        return this.getConfigAsync(config, id);
    }

    @Nullable
    @Override
    public <C extends GroupLangConfiguration> C getLang(Class<C> config, String id) {
        return this.getConfig(config, id);
    }

    @Override
    public <C extends GroupLangConfiguration> boolean isLangCached(Class<C> config, String id) {
        return this.isConfigCached(config, id);
    }
}
