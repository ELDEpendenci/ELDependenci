package com.ericlam.mc.eld.services;

import com.ericlam.mc.eld.annotations.DefaultLanguage;
import com.ericlam.mc.eld.annotations.GroupResource;
import com.ericlam.mc.eld.components.GroupConfiguration;
import com.ericlam.mc.eld.components.GroupLangConfiguration;
import com.ericlam.mc.eld.configurations.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public final class ELDConfigPoolService implements ConfigPoolService {

    private final ObjectMapper mapper = ELDConfigManager.OBJECT_MAPPER;
    private final Map<Class<?>, Plugin> pluginMapper = new ConcurrentHashMap<>();

    private final Map<Class<? extends GroupConfiguration>, GroupConfig<? extends GroupConfiguration>> groupConfigMap = new ConcurrentHashMap<>();
    private final Map<Class<? extends GroupLangConfiguration>, GroupLang<? extends GroupLangConfiguration>> groupLangMap = new ConcurrentHashMap<>();

    public void addTypeMapper(Class<?> type, Plugin plugin) {
        this.pluginMapper.put(type, plugin);
    }

    @Override
    public <T extends GroupConfiguration> GroupConfig<T> getGroupConfig(Class<T> type) {
        if (groupConfigMap.containsKey(type)) return (GroupConfig<T>) groupConfigMap.get(type);
        if (!type.isAnnotationPresent(GroupResource.class))
            throw new IllegalStateException("config pool " + type.getSimpleName() + " is lack of @GroupResource annotation");
        var resource = type.getAnnotation(GroupResource.class);
        var plugin = Optional.ofNullable(pluginMapper.get(type)).orElseThrow(() -> new IllegalStateException("cannot find suitable plugin for config pool type: " + type.getSimpleName()));
        File folder = new File(plugin.getDataFolder(), resource.folder());
        preloadYaml(resource, folder, plugin, type.getSimpleName());
        GroupConfig<T> groupConfig = new SimpleGroupConfig<>(mapper, folder, type);
        this.groupConfigMap.put(type, groupConfig);
        return groupConfig;
    }

    @Override
    public <T extends GroupLangConfiguration> GroupLang<T> getGroupLang(Class<T> type) {
        if (groupLangMap.containsKey(type)) return (GroupLang<T>) groupLangMap.get(type);
        if (!type.isAnnotationPresent(GroupResource.class))
            throw new IllegalStateException("config pool " + type.getSimpleName() + " is lack of @GroupResource annotation");
        if (!type.isAnnotationPresent(DefaultLanguage.class))
            throw new IllegalStateException("language pool " + type.getSimpleName() + " is lack of @DefaultLanguage annotation");
        var resource = type.getAnnotation(GroupResource.class);
        var defaultLang = type.getAnnotation(DefaultLanguage.class).value();
        var plugin = Optional.ofNullable(pluginMapper.get(type)).orElseThrow(() -> new IllegalStateException("cannot find suitable plugin for config pool type: " + type.getSimpleName()));
        File folder = new File(plugin.getDataFolder(), resource.folder());
        preloadYaml(resource, folder, plugin, type.getSimpleName());
        GroupLang<T> groupLang = new SimpleGroupLang<>(folder, type, plugin, defaultLang);
        this.groupLangMap.put(type, groupLang);
        return groupLang;
    }

    private void preloadYaml(GroupResource resource, File folder, Plugin plugin, String simpleName){
        if (!folder.exists() && folder.mkdirs()) plugin.getLogger().info("Folder "+resource.folder()+" created.");
        if (!folder.isDirectory())
            throw new IllegalStateException("config pool " + simpleName + " 's path ' " + resource.folder() + " is not a directory!");
        for (String preload : resource.preloads()) {
            String yml = preload.concat(".yml");
            File preLoadFile = new File(folder, yml);
            if (!preLoadFile.exists()) plugin.saveResource(resource.folder().concat("/").concat(yml), true);
        }
    }
}
