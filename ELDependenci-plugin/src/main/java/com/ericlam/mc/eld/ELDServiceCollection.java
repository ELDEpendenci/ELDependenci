package com.ericlam.mc.eld;

import com.ericlam.mc.eld.annotations.ELDPlugin;
import com.ericlam.mc.eld.components.*;
import com.ericlam.mc.eld.configurations.ELDConfigManager;
import com.ericlam.mc.eld.registrations.ComponentsRegistry;
import com.ericlam.mc.eld.registrations.ELDCommandRegistry;
import com.ericlam.mc.eld.registrations.ELDListenerRegistry;
import com.google.inject.Module;
import org.bukkit.event.Listener;

import javax.inject.Provider;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ELDServiceCollection implements ServiceCollection, AddonManager {

    final Map<Class<?>, Object> customInstallation;

    final Set<HierarchyNode> commands;

    final Set<Class<? extends Listener>> listeners;

    final Set<Class<? extends ELDListener>> eldListeners;

    final ELDLifeCycle lifeCycleHook;

    final ELDConfigManager configManager;

    private final ELDModule module;

    public ELDServiceCollection(ELDModule module, ELDBukkit plugin, Map<Class<?>, Object> customInstallation) {
        this.module = module;
        this.customInstallation = customInstallation;
        if (!plugin.getClass().isAnnotationPresent(ELDPlugin.class)) {
            throw new IllegalStateException("插件 " + plugin.getName() + " 缺少 @ELDPlugin 標註");
        }
        var eld = plugin.getClass().getAnnotation(ELDPlugin.class);
        var registry = this.toInstanceRegistry(eld.registry());
        this.lifeCycleHook = this.toInstanceLifeCycle(eld.lifeCycle());

        //register command
        var cmdregistry = new ELDCommandRegistry();
        registry.registerCommand(cmdregistry);
        this.commands = cmdregistry.getNodes();

        //register listeners
        var listenerRegistry = new ELDListenerRegistry();
        registry.registerListeners(listenerRegistry);

        this.listeners = listenerRegistry.getBukkitListenerClass();
        this.eldListeners = listenerRegistry.getEldListenerClass();

        this.configManager = new ELDConfigManager(module, plugin);
    }

    @Override
    public ServiceCollection addSingleton(Class<?> singleton) {
        if (Modifier.isAbstract(singleton.getModifiers())) throw new IllegalStateException("Singleton class cannot be an abstract class");
        module.bindSingleton(singleton);
        return this;
    }

    @Override
    public <T, L extends T> ServiceCollection bindService(Class<T> service, Class<L> implementation) {
        if (!Modifier.isAbstract(service.getModifiers())) throw new IllegalStateException("Service class must be an abstract class");
        module.bindService(service, implementation);
        return this;
    }

    @Override
    public <T, P extends Provider<T>> ServiceCollection bindServiceProvider(Class<T> service, Class<P> provider) {
        module.addServiceProvider(service, provider);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getInstallation(Class<T> cls) {
        return Optional.ofNullable(customInstallation.get(cls)).map(r -> (T)r).orElseThrow(() -> new IllegalStateException("unknown installation: "+cls));
    }

    @Override
    public <T extends Overridable, L extends T> ServiceCollection overrideService(Class<T> service, Class<L> implementation) {
        if (!Modifier.isAbstract(service.getModifiers())) throw new IllegalStateException("Service class must be an abstract class");
        module.overrideService(service, implementation);
        return this;
    }

    @Override
    public <T, L extends T> ServiceCollection addService(Class<T> service, Class<L> implementation) {
        if (!Modifier.isAbstract(service.getModifiers())) throw new IllegalStateException("Service class must be an abstract class");
        module.addService(service, implementation);
        return this;
    }

    @Override
    public <T> ServiceCollection addServices(Class<T> service, Map<String, Class<? extends T>> implementations) {
        if (!Modifier.isAbstract(service.getModifiers())) throw new IllegalStateException("Service class must be an abstract class");
        module.addServices(service, implementations);
        return this;
    }

    @Override
    public <T extends Configuration> ServiceCollection addConfiguration(Class<T> config) {
        configManager.loadConfig(config);
        return this;
    }

    @Override
    public <T extends GroupConfiguration> ServiceCollection addGroupConfiguration(Class<T> config) {
        this.configManager.loadConfigPool(config);
        return this;
    }

    @Override
    public <T extends GroupLangConfiguration> ServiceCollection addMultipleLanguages(Class<T> lang) {
        this.configManager.loadLanguagePool(lang);
        return this;
    }

    private ComponentsRegistry toInstanceRegistry(Class<? extends ComponentsRegistry> registry) {
        try {
            var con = registry.getConstructor();
            con.setAccessible(true);
            return con.newInstance();
        } catch (Exception e) {
            if (e instanceof NoSuchMethodException) {
                throw new IllegalStateException("ComponentRegistry must have no-args constructor");
            }
            throw new RuntimeException(e);
        }
    }

    private ELDLifeCycle toInstanceLifeCycle(Class<? extends ELDLifeCycle> lifeCycle) {
        try {
            var con = lifeCycle.getConstructor();
            con.setAccessible(true);
            return con.newInstance();
        } catch (Exception e) {
            if (e instanceof NoSuchMethodException) {
                throw new IllegalStateException("LifeCycle must have no-args constructor");
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public void installModule(Module module) {
        this.module.addModule(module);
    }

    @Override
    public <T> void customInstallation(Class<T> regCls, T ins) {
        customInstallation.put(regCls, ins);
    }
}
