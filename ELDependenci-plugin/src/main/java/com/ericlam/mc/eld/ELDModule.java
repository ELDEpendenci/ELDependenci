package com.ericlam.mc.eld;

import com.ericlam.mc.eld.components.Configuration;
import com.ericlam.mc.eld.components.Overridable;
import com.ericlam.mc.eld.services.*;
import com.ericlam.mc.eld.services.factory.ELDItemStackService;
import com.ericlam.mc.eld.services.factory.ELDMessageService;
import com.ericlam.mc.eld.services.scheduler.ELDSchedulerService;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import io.netty.util.internal.ConcurrentSet;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class ELDModule implements Module {

    private final Set<Class<?>> singleton = new ConcurrentSet<>();
    private final Map<Class<?>, Class> services = new ConcurrentHashMap<>();
    private final Map<Class<?>, Map<String, Class>> servicesMulti = new ConcurrentHashMap<>();
    private final Map<Class<?>, Set<Class>> servicesSet = new ConcurrentHashMap<>();
    private final Map<Class, Object> instances = new ConcurrentHashMap<>();

    private final Map<Class, Configuration> configs = new ConcurrentHashMap<>();

    public final Plugin plugin;

    public ELDModule(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void configure(Binder binder) {
        //services internal bind
        binder.bind(InstanceInjector.class).in(Scopes.SINGLETON);
        binder.bind(ScheduleService.class).to(ELDSchedulerService.class).in(Scopes.SINGLETON);
        binder.bind(ItemStackService.class).to(ELDItemStackService.class).in(Scopes.SINGLETON);
        binder.bind(MessageService.class).to(ELDMessageService.class).in(Scopes.SINGLETON);
        binder.bind(ConfigPoolService.class).to(ELDConfigPoolService.class).in(Scopes.SINGLETON);


        singleton.forEach(cls -> binder.bind(cls).in(Scopes.SINGLETON));
        services.forEach((service, impl) -> binder.bind(service).to(impl).in(Scopes.SINGLETON));
        configs.forEach((cls, config) -> binder.bind(cls).toInstance(config));
        instances.forEach((cls, ins) -> binder.bind(cls).toInstance(ins));
        servicesMulti.forEach((service, map) -> {
            var binding = MapBinder.newMapBinder(binder, String.class, service);
            map.forEach((key, impl) -> binding.addBinding(key).to(impl).in(Scopes.SINGLETON));
        });
        servicesSet.forEach((services, cls) -> {
            var binding = Multibinder.newSetBinder(binder, services);
            cls.forEach(c -> binding.addBinding().to(c).in(Scopes.SINGLETON));
        });
    }

    void bindSingleton(Class<?> singleton) {
        this.singleton.add(singleton);
    }

    <T, R extends T> void bindService(Class<T> service, Class<R> implement) {
        if (services.containsKey(service) || servicesMulti.containsKey(service) || servicesSet.containsKey(service)) {
            plugin.getLogger().warning("Service " + service.getName() + " 先前已有註冊，無法再被註冊。");
            return;
        }
        this.services.putIfAbsent(service, implement);
    }

    <T> void addServices(Class<T> service, Map<String, Class<? extends T>> implementations) {
        if (services.containsKey(service) || servicesSet.containsKey(service)) {
            plugin.getLogger().warning("Service " + service.getName() + " 先前已被用其他方式註冊，無法再被註冊。");
            return;
        }
        this.servicesMulti.putIfAbsent(service, new LinkedHashMap<>());
        var map = new HashMap<String, Class>(implementations);
        this.servicesMulti.get(service).putAll(map);
    }

    <T, L extends T> void addService(Class<T> service, Class<L> implement){
        if (services.containsKey(service) || servicesMulti.containsKey(service)) {
            plugin.getLogger().warning("Service " + service.getName() + " 先前已被用其他方式註冊，無法再被註冊。");
            return;
        }
        this.servicesSet.putIfAbsent(service, new LinkedHashSet<>());
        this.servicesSet.get(service).add(implement);
    }

    <T extends Overridable, L extends T> void overrideService(Class<T> service, Class<L> implement){
        this.services.put(service, implement);
    }

    <T> void bindInstance(Class<T> cls, T instance) {
        this.instances.putIfAbsent(cls, instance);
    }


    public void bindConfig(Class<? extends Configuration> cls, Configuration c) {
        this.configs.put(cls, c);
    }
}
