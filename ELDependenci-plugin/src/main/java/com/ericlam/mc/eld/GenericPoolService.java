package com.ericlam.mc.eld;

import com.ericlam.mc.eld.components.Configuration;
import com.ericlam.mc.eld.services.ScheduleService;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

@SuppressWarnings("unchecked")
public abstract class GenericPoolService<R extends Configuration> {

    @Inject
    protected ScheduleService scheduleService;

    protected final Plugin plugin = ELDependenci.getPlugin(ELDependenci.class);

    protected final Map<Class<? extends R>, Map<String, R>> groupPoolMap = new ConcurrentHashMap<>();
    protected final Map<Class<? extends R>, BiFunction<Class<? extends R>, String, CompletableFuture<? extends Optional<R>>>> groupConfigLoader = new ConcurrentHashMap<>();

    public void addGroupConfigLoader(Class<? extends R> type, BiFunction<Class<? extends R>, String, CompletableFuture<? extends Optional<R>>> init) {
        this.groupConfigLoader.put(type, init);
    }
    public void dumpAll(Map<Class<? extends R>, Map<String, R>> groupPoolMap) {
        this.groupPoolMap.putAll(groupPoolMap);
    }

    public <C extends R> ScheduleService.BukkitPromise<Optional<C>> getConfigAsync(Class<C> config, String id) {
        return scheduleService.callAsync(plugin, () -> {
            var cached = Optional.ofNullable(groupPoolMap.get(config)).map(c -> c.get(id));
            if (cached.isPresent()) return Optional.of((C)cached.get());
            var future = Optional.ofNullable(groupConfigLoader.get(config)).map(f -> f.apply(config, id)).orElseThrow(() -> new IllegalStateException("no loader for config: "+config));
            var ins = (Optional<C>)future.get();
            if (ins.isEmpty()) return ins;
            groupPoolMap.putIfAbsent(config, new ConcurrentHashMap<>());
            groupPoolMap.get(config).put(id, ins.get());
            return ins;
        });
    }

    @Nullable
    public <C extends R> C getConfig(Class<C> config, String id) {
        return (C) Optional.ofNullable(groupPoolMap.get(config)).map(c -> c.get(id)).orElse(null);
    }


    public <C extends R> boolean isConfigCached(Class<C> config, String id) {
        return Optional.ofNullable(groupPoolMap.get(config)).map(c -> c.containsKey(id)).orElse(false);
    }

    public <C extends R> CompletableFuture<Void> reloadPool(Class<C> config) {
        if (!groupPoolMap.containsKey(config)) throw new IllegalStateException("language pool " + config + " is not registered.");
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        groupPoolMap.get(config).keySet().forEach(id -> {
            var future = Optional.ofNullable(groupConfigLoader.get(config)).map(f -> f.apply(config, id)).orElseThrow(() -> new IllegalStateException("no loader for config: "+config));;
            futures.add(future.thenAccept(insOpt -> insOpt.ifPresent(ins -> groupPoolMap.get(config).put(id, ins))));
        });
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }
}
