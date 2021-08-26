package com.ericlam.mc.eld.configurations;

import com.ericlam.mc.eld.annotations.DefaultLanguage;
import com.ericlam.mc.eld.annotations.GroupResource;
import com.ericlam.mc.eld.components.GroupLangConfiguration;
import com.ericlam.mc.eld.components.LangConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FilenameUtils;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

import static com.ericlam.mc.eld.configurations.ELDConfigManager.ConfigUtils.setField;

public final class ELDLangConfigManager {

    private final ConfigInitializer initializer;
    private final Plugin plugin;


    private final Map<Class<? extends GroupLangConfiguration>, Map<String, GroupLangConfiguration>> langPoolMap = new ConcurrentHashMap<>();
    private final Map<Class<? extends GroupLangConfiguration>, GroupLangConfiguration> defaultLanguageMap = new ConcurrentHashMap<>();

    public ELDLangConfigManager(Plugin plugin, ConfigInitializer initializer) {
        this.initializer = initializer;
        this.plugin = plugin;
    }


    public <T extends GroupLangConfiguration> void loadLanguagePool(Class<T> config) {
        langPoolMap.putIfAbsent(config, new ConcurrentHashMap<>());
        preloadLanguagePool(config).whenComplete((p, ex) -> {
            if (ex != null) ex.printStackTrace();
            else langPoolMap.put(config, p);
        });
    }

    public <T extends GroupLangConfiguration> CompletableFuture<Map<String, GroupLangConfiguration>> preloadLanguagePool(Class<T> config) {
        if (!config.isAnnotationPresent(GroupResource.class))
            throw new IllegalStateException("language pool " + config.getSimpleName() + " is lack of @GroupResource annotation");
        if (!config.isAnnotationPresent(DefaultLanguage.class))
            throw new IllegalStateException("language pool " + config.getSimpleName() + " is lack of @DefaultLanguage annotation");
        var resource = config.getAnnotation(GroupResource.class);
        var defaultLang = config.getAnnotation(DefaultLanguage.class).value();
        return CompletableFuture.supplyAsync(() -> {
            Map<String, GroupLangConfiguration> groupMap = new LinkedHashMap<>();
            File[] child = initializer.loadGroupConfigs(resource, config.getSimpleName());
            if (child == null) return groupMap;
            try {

                // load default language first

                File folder = new File(plugin.getDataFolder(), resource.folder());
                var defaultFile = new File(folder, defaultLang + ".yml");
                if (!defaultFile.exists())
                    throw new IllegalStateException("Default Language file cannot be non-exist !");
                T defaultIns = loadLanguage(config, defaultLang, defaultFile);
                groupMap.put(defaultLang, defaultIns);
                this.defaultLanguageMap.put(config, defaultIns);

                // then load other languages

                for (File data : child) {
                    var id = FilenameUtils.getBaseName(data.getName());
                    if (id.equals(defaultLang)) continue;
                    var ins = loadLanguage(config, id, data);
                    groupMap.put(defaultLang, ins);
                }

            } catch (Exception e) {
                plugin.getLogger().warning("Error while loading config pool " + resource.folder());
                e.printStackTrace();
            }
            return groupMap;
        }).thenApply(p -> {
            plugin.getLogger().info("All language resources in folder " + resource.folder() + " has been loaded.");
            return p;
        });
    }

    private <T extends GroupLangConfiguration> T loadLanguage(Class<T> config, String id, File file) throws Exception {
        var defaultIns = initializer.initConfiguration(config, file);
        YamlConfiguration defaultConfiguration = YamlConfiguration.loadConfiguration(file);
        var defaultController = LangConfiguration.class.getDeclaredField("lang");
        var defaultLocale = GroupLangConfiguration.class.getDeclaredField("locale");
        setField(defaultController, new ELDConfigManager.MessageGetterImpl(defaultIns, defaultConfiguration, file, plugin), defaultIns);
        setField(defaultLocale, id, defaultIns);
        return defaultIns;
    }

    public <T extends GroupLangConfiguration> CompletableFuture<Optional<GroupLangConfiguration>> loadOneLangConfig(Class<T> config, String key) {
        if (!config.isAnnotationPresent(GroupResource.class))
            throw new IllegalStateException("config pool " + config.getSimpleName() + " is lack of @GroupResource annotation");
        var resource = config.getAnnotation(GroupResource.class);
        return CompletableFuture.supplyAsync(() -> {
            try {
                File folder = new File(plugin.getDataFolder(), resource.folder());
                var data = new File(folder, key + ".yml");
                if (!data.exists()) return Optional.empty();
                var ins = loadLanguage(config, key, data);
                return Optional.of(ins);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }


    public Map<Class<? extends GroupLangConfiguration>, GroupLangConfiguration> getDefaultLanguageMap() {
        return defaultLanguageMap;
    }

    public Map<Class<? extends GroupLangConfiguration>, Map<String, GroupLangConfiguration>> getLangPoolMap() {
        return langPoolMap;
    }


}
