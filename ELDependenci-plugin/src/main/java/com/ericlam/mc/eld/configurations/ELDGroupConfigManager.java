package com.ericlam.mc.eld.configurations;

import com.ericlam.mc.eld.annotations.GroupResource;
import com.ericlam.mc.eld.components.GroupConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FilenameUtils;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

import static com.ericlam.mc.eld.configurations.ELDConfigManager.ConfigUtils.setField;

public final class ELDGroupConfigManager {

    private final Plugin plugin;
    private final ConfigInitializer initializer;

    private final Map<Class<? extends GroupConfiguration>, Map<String, GroupConfiguration>> configPoolMap = new ConcurrentHashMap<>();

    public ELDGroupConfigManager(Plugin plugin, ConfigInitializer initializer) {
        this.plugin = plugin;
        this.initializer = initializer;
    }

    public <T extends GroupConfiguration> void loadConfigPool(Class<T> config) {
        configPoolMap.putIfAbsent(config, new ConcurrentHashMap<>());
        preloadConfigPool(config).whenComplete((p, ex) -> {
            if (ex != null) ex.printStackTrace();
            else configPoolMap.put(config, p);
        });
    }

    public <T extends GroupConfiguration> CompletableFuture<Map<String, GroupConfiguration>> preloadConfigPool(Class<T> config) {
        if (!config.isAnnotationPresent(GroupResource.class))
            throw new IllegalStateException("config pool " + config.getSimpleName() + " is lack of @GroupResource annotation");
        var resource = config.getAnnotation(GroupResource.class);
        return CompletableFuture.supplyAsync(() -> {
            var pool = new HashMap<String, GroupConfiguration>();
            try {
                File[] child = initializer.loadGroupConfigs(resource, config.getSimpleName());
                if (child == null) {
                    return pool;
                }
                for (File data : child) {
                    var id = FilenameUtils.getBaseName(data.getName());
                    var ins = initializer.initConfiguration(config, data);
                    var idField = GroupConfiguration.class.getDeclaredField("id");
                    setField(idField, id, ins);
                    pool.put(id, ins);
                }
                return pool;
            } catch (Exception e) {
                plugin.getLogger().warning("Error while loading config pool " + resource.folder());
                e.printStackTrace();
            }
            return pool;
        }).thenApply(p -> {
            plugin.getLogger().info("All resources in folder " + resource.folder() + " has been loaded.");
            return p;
        });
    }

    public <T extends GroupConfiguration> CompletableFuture<Optional<GroupConfiguration>> loadOneGroupConfig(Class<T> config, String key) {
        if (!config.isAnnotationPresent(GroupResource.class))
            throw new IllegalStateException("config pool " + config.getSimpleName() + " is lack of @GroupResource annotation");
        var resource = config.getAnnotation(GroupResource.class);
        return CompletableFuture.supplyAsync(() -> {
            try {
                File folder = new File(plugin.getDataFolder(), resource.folder());
                var data = new File(folder, key + ".yml");
                if (!data.exists()) return Optional.empty();
                var ins = initializer.initConfiguration(config, data);
                var idField = GroupConfiguration.class.getDeclaredField("id");
                setField(idField, key, ins);
                return Optional.of(ins);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }


    public Map<Class<? extends GroupConfiguration>, Map<String, GroupConfiguration>> getConfigPoolMap() {
        return configPoolMap;
    }
}
