package com.ericlam.mc.eld;

import com.ericlam.mc.eld.components.Configuration;
import com.ericlam.mc.eld.components.GroupLangConfiguration;
import com.ericlam.mc.eld.components.Overridable;
import com.ericlam.mc.eld.services.*;
import com.ericlam.mc.eld.services.factory.ELDItemStackService;
import com.ericlam.mc.eld.services.factory.ELDMessageService;
import com.ericlam.mc.eld.services.scheduler.ELDSchedulerService;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import io.netty.util.internal.ConcurrentSet;
import org.bukkit.plugin.Plugin;

import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
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
    private final Map<Class, Map<String, ? extends GroupLangConfiguration>> groupLangs = new ConcurrentHashMap<>();

    private final Map<String, Plugin> pluginInjectors = new ConcurrentHashMap<>();
    private final Map<Class, Class<? extends Provider>> serviceProviders = new ConcurrentHashMap<>();

    private final List<Module> modules = new ArrayList<>();

    public final Plugin plugin;

    private boolean defaultSingleton = true;

    public ELDModule(Plugin plugin) {
        this.plugin = plugin;
    }

    public void setDefaultSingleton(boolean defaultSingleton) {
        this.defaultSingleton = defaultSingleton;
    }

    private void setScope(ScopedBindingBuilder bindingBuilder) {
        if (defaultSingleton) {
            bindingBuilder.in(Scopes.SINGLETON);
        }
    }

    @Override
    public void configure(Binder binder) {
        //services internal bind
        binder.bind(InstanceInjector.class).in(Scopes.SINGLETON);
        binder.bind(ScheduleService.class).to(ELDSchedulerService.class).in(Scopes.SINGLETON);
        binder.bind(ItemStackService.class).to(ELDItemStackService.class).in(Scopes.SINGLETON);
        binder.bind(MessageService.class).to(ELDMessageService.class).in(Scopes.SINGLETON);
        binder.bind(ConfigPoolService.class).to(ELDConfigPoolService.class).in(Scopes.SINGLETON);


        modules.forEach(binder::install);
        singleton.forEach(cls -> setScope(binder.bind(cls)));
        serviceProviders.forEach((service, provider) -> setScope(binder.bind(service).toProvider(provider)));
        services.forEach((service, impl) -> setScope(binder.bind(service).to(impl)));
        configs.forEach((cls, config) -> binder.bind(cls).toInstance(config));
        instances.forEach((cls, ins) -> binder.bind(cls).toInstance(ins));
        servicesMulti.forEach((service, map) -> {
            var binding = MapBinder.newMapBinder(binder, String.class, service);
            map.forEach((key, impl) -> {
                setScope(binding.addBinding(key).to(impl));
                setScope(binder.bind(service).annotatedWith(Names.named(key)).to(impl));
            });
        });
        servicesSet.forEach((services, cls) -> {
            var binding = Multibinder.newSetBinder(binder, services);
            cls.forEach(c -> {
                setScope(binding.addBinding().to(c));
                Optional<Annotation> qualifierOpt = Arrays.stream(c.getAnnotations()).filter(a -> a.annotationType().isAnnotationPresent(Qualifier.class)).findAny();
                if (c.isAnnotationPresent(Named.class)) {
                    Named named = (Named) c.getAnnotation(Named.class);
                    setScope(binder.bind(services).annotatedWith(Names.named(named.value())).to(c));
                } else if (qualifierOpt.isPresent()) {
                    Annotation annotation = qualifierOpt.get();
                    setScope(binder.bind(services).annotatedWith(annotation).to(c));
                }
            });
        });
        groupLangs.forEach((cls, map) -> {
            var binding = MapBinder.newMapBinder(binder, String.class, cls);
            map.forEach((key, lang) -> binding.addBinding(key).toInstance(lang));
        });
        var pluginBinding = MapBinder.newMapBinder(binder, String.class, Plugin.class);
        pluginInjectors.forEach((pluginName, plugin) -> {
            pluginBinding.addBinding(pluginName).toInstance(plugin);
        });
    }

    void bindSingleton(Class<?> singleton) {
        this.singleton.add(singleton);
    }

    <T, R extends T> void bindService(Class<T> service, Class<R> implement) {
        if (services.containsKey(service) || servicesMulti.containsKey(service) || servicesSet.containsKey(service)) {
            plugin.getLogger().warning("Service " + service.getName() + " has already registered and cannot be registered again.");
            return;
        }
        this.services.putIfAbsent(service, implement);
    }

    <T> void addServices(Class<T> service, Map<String, Class<? extends T>> implementations) {
        if (services.containsKey(service) || servicesSet.containsKey(service)) {
            plugin.getLogger().warning("Service " + service.getName() + " has already registered and cannot be registered again.");
            return;
        }
        this.servicesMulti.putIfAbsent(service, new LinkedHashMap<>());
        var map = new HashMap<String, Class>(implementations);
        this.servicesMulti.get(service).putAll(map);
    }

    <T, L extends T> void addService(Class<T> service, Class<L> implement) {
        if (services.containsKey(service) || servicesMulti.containsKey(service)) {
            plugin.getLogger().warning("Service " + service.getName() + " has already registered and cannot be registered again.");
            return;
        }
        this.servicesSet.putIfAbsent(service, new LinkedHashSet<>());
        this.servicesSet.get(service).add(implement);
    }

    <T extends Overridable, L extends T> void overrideService(Class<T> service, Class<L> implement) {
        this.services.put(service, implement);
    }

    <T> void bindInstance(Class<T> cls, T instance) {
        this.instances.putIfAbsent(cls, instance);
    }

    <T extends ELDBukkit> void bindPluginInstance(Class<? extends ELDBukkit> cls, T instance) {
        this.instances.putIfAbsent(cls, instance);
    }

    void mapPluginInstance(Plugin instance) {
        this.pluginInjectors.put(instance.getName(), instance);
    }


    public void bindConfig(Class<? extends Configuration> cls, Configuration c) {
        this.configs.put(cls, c);
    }

    public synchronized <T extends GroupLangConfiguration> void bindLangGroup(Class<T> groupLangConfig, Map<String, T> stringMap) {
        this.groupLangs.put(groupLangConfig, stringMap);
    }

    void addModule(Module module) {
        this.modules.add(module);
    }

    <T, P extends Provider<T>> void addServiceProvider(Class<T> service, Class<P> provider) {
        this.serviceProviders.put(service, provider);
    }
}
