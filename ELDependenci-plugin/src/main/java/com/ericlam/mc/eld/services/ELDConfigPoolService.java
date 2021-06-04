package com.ericlam.mc.eld.services;

import com.ericlam.mc.eld.ELDependenci;
import com.ericlam.mc.eld.components.GroupConfiguration;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public final class ELDConfigPoolService implements ConfigPoolService {

    @Inject
    private ScheduleService scheduleService;
    private final Plugin plugin = ELDependenci.getPlugin(ELDependenci.class);

    private final Map<Class<? extends GroupConfiguration>, Map<String, GroupConfiguration>> configPoolMap = new ConcurrentHashMap<>();
    private final Map<Class<? extends GroupConfiguration>, Supplier<CompletableFuture<Map<String, GroupConfiguration>>>> initMap = new ConcurrentHashMap<>();

    public void addInitializer(Class<? extends GroupConfiguration> cls, Supplier<CompletableFuture<Map<String, GroupConfiguration>>> init) {
        initMap.put(cls, init);
    }

    public void dumpAll(Map<Class<? extends GroupConfiguration>, Map<String, GroupConfiguration>> configPoolMap) {
        this.configPoolMap.putAll(configPoolMap);
    }

    @Override
    public <C extends GroupConfiguration> ScheduleService.BukkitPromise<Map<String, C>> getPoolAsync(Class<C> config) {
        return scheduleService.callAsync(plugin, () -> {
            if (isPoolCached(config)) {
                return getPool(config);
            } else {
                if (!initMap.containsKey(config)) throw new IllegalStateException("config pool " + config + " is not registered.");
                return (Map<String, C>) initMap.get(config).get().thenApply(s -> {
                    configPoolMap.put(config, s);
                    return s;
                }).get();
            }
        });
    }

    @Nullable
    @Override
    public <C extends GroupConfiguration> Map<String, C> getPool(Class<C> config) {
        return (Map<String, C>) configPoolMap.get(config);
    }

    @Override
    public <C extends GroupConfiguration> boolean isPoolCached(Class<C> config) {
        return configPoolMap.containsKey(config);
    }

    @Override
    public <C extends GroupConfiguration> CompletableFuture<Void> reloadPool(Class<C> config) {
        if (!initMap.containsKey(config)) throw new IllegalStateException("config pool " + config + " is not registered.");
        return initMap.get(config).get().thenApply(c -> {
            configPoolMap.put(config, c);
            return null;
        });
    }
}
