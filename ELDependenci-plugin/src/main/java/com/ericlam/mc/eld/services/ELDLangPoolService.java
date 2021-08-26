package com.ericlam.mc.eld.services;

import com.ericlam.mc.eld.GenericPoolService;
import com.ericlam.mc.eld.components.GroupLangConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class ELDLangPoolService extends GenericPoolService<GroupLangConfiguration> implements LanguagePoolService {

    private final Logger LOGGER = LoggerFactory.getLogger(ELDLangPoolService.class);

    private final Map<Class<? extends GroupLangConfiguration>, GroupLangConfiguration> defaultLanguageMap = new ConcurrentHashMap<>();

    public void addDefaultLanguages(Map<Class<? extends GroupLangConfiguration>, GroupLangConfiguration> defaultLanguageMap){
        this.defaultLanguageMap.putAll(defaultLanguageMap);
    }

    @Override
    public <C extends GroupLangConfiguration> ScheduleService.BukkitPromise<C> getLangAsync(Class<C> config, String id) {
        return this.getConfigAsync(config, id).thenApplyAsync(lang -> lang.orElseGet(() -> {
            LOGGER.warn("unknown language: {}, rollback to default language.", id);
            return this.getDefaultLang(config);
        }));
    }

    @Nullable
    @Override
    public <C extends GroupLangConfiguration> C getLang(Class<C> config, String id) {
        return this.getConfig(config, id);
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public <C extends GroupLangConfiguration> C getDefaultLang(Class<C> config) {
        return (C)defaultLanguageMap.get(config);
    }

    @Override
    public <C extends GroupLangConfiguration> boolean isLangCached(Class<C> config, String id) {
        return this.isConfigCached(config, id);
    }
}
