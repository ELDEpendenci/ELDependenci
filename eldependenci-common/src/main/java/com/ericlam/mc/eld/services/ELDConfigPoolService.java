package com.ericlam.mc.eld.services;

import com.ericlam.mc.eld.ConfigHandler;
import com.ericlam.mc.eld.MCPlugin;
import com.ericlam.mc.eld.annotations.DefaultLanguage;
import com.ericlam.mc.eld.annotations.GroupResource;
import com.ericlam.mc.eld.components.GroupConfiguration;
import com.ericlam.mc.eld.components.GroupLangConfiguration;
import com.ericlam.mc.eld.configurations.*;
import com.ericlam.mc.eld.configurations.filewalk.DirectStreamFileWalker;
import com.ericlam.mc.eld.configurations.filewalk.FileWalker;
import com.ericlam.mc.eld.configurations.filewalk.SimpleFileWalker;
import com.ericlam.mc.eld.configurations.filewalk.TreeFileWalker;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public final class ELDConfigPoolService implements ConfigPoolService {

    private static final Map<WalkerWay, FileWalker> walkerMap = Map.of(
            WalkerWay.SIMPLE, new SimpleFileWalker(),
            WalkerWay.TREE, new TreeFileWalker(),
            WalkerWay.STREAM, new DirectStreamFileWalker()
    );

    private static FileWalker getWalker(WalkerWay walkerWay) {
        return Optional.ofNullable(walkerMap.get(walkerWay)).orElseThrow(() -> new IllegalStateException("unknown walker way: " + walkerWay));
    }


    private final ObjectMapper mapper = ELDConfigManager.YAML_MAPPER;
    private final Map<Class<?>, MCPlugin> pluginMapper = new ConcurrentHashMap<>();

    private final Map<Class<? extends GroupConfiguration>, GroupConfig<? extends GroupConfiguration>> groupConfigMap = new ConcurrentHashMap<>();
    private final Map<Class<? extends GroupLangConfiguration>, GroupLang<? extends GroupLangConfiguration>> groupLangMap = new ConcurrentHashMap<>();


    private final WalkerWay fileWalker;
    private final ConfigHandler handler;

    public ELDConfigPoolService(WalkerWay fileWalker, ConfigHandler handler) {
        this.fileWalker = fileWalker;
        this.handler = handler;
    }

    public void addTypeMapper(Class<?> type, MCPlugin plugin) {
        this.pluginMapper.put(type, plugin);
        if (!type.isAnnotationPresent(GroupResource.class))
            throw new IllegalStateException("config pool " + type.getSimpleName() + " is lack of @GroupResource annotation");
        var resource = type.getAnnotation(GroupResource.class);
        File folder = new File(plugin.getDataFolder(), resource.folder());
        if (GroupConfiguration.class.isAssignableFrom(type)) {
            var t = (Class<? extends GroupConfiguration>) type;
            var simpleGroup = new SimpleGroupConfig<>(mapper, folder, t, getWalker(fileWalker));
            CompletableFuture.runAsync(simpleGroup::loadAll).whenComplete((v, ex) -> {
                if (ex != null) ex.printStackTrace();
                else plugin.getLogger().info("Cache Loaded for config group: " + type.getSimpleName());
            });
            this.groupConfigMap.put(t, simpleGroup);
        } else if (GroupLangConfiguration.class.isAssignableFrom(type)) {
            if (!type.isAnnotationPresent(DefaultLanguage.class))
                throw new IllegalStateException("language pool " + type.getSimpleName() + " is lack of @DefaultLanguage annotation");
            var defaultLang = type.getAnnotation(DefaultLanguage.class).value();
            var t = (Class<? extends GroupLangConfiguration>) type;
            var simpleLang = new SimpleGroupLang<>(folder, t, handler, defaultLang, plugin);
            CompletableFuture.runAsync(simpleLang::loadAll).whenComplete((v, ex) -> {
                if (ex != null) ex.printStackTrace();
                else plugin.getLogger().info("Cache Loaded for config group: " + type.getSimpleName());
            });
        }
    }

    @Override
    public <T extends GroupConfiguration> GroupConfig<T> getGroupConfig(Class<T> type) {
        if (groupConfigMap.containsKey(type)) return (GroupConfig<T>) groupConfigMap.get(type);
        if (!type.isAnnotationPresent(GroupResource.class))
            throw new IllegalStateException("config pool " + type.getSimpleName() + " is lack of @GroupResource annotation");
        var resource = type.getAnnotation(GroupResource.class);
        var plugin = Optional.ofNullable(pluginMapper.get(type)).orElseThrow(() -> new IllegalStateException("cannot find suitable plugin for config pool type: " + type.getSimpleName()));
        File folder = new File(plugin.getDataFolder(), resource.folder());
        GroupConfig<T> groupConfig = new SimpleGroupConfig<>(mapper, folder, type, getWalker(fileWalker));
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
        GroupLang<T> groupLang = new SimpleGroupLang<>(folder, type, handler, defaultLang, plugin);
        this.groupLangMap.put(type, groupLang);
        return groupLang;
    }


    public enum WalkerWay {
        SIMPLE, TREE, STREAM
    }
}
