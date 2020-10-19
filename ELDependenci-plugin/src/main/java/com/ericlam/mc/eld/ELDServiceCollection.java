package com.ericlam.mc.eld;

import com.ericlam.mc.eld.annotations.ELDPlugin;
import com.ericlam.mc.eld.components.Configuration;
import com.ericlam.mc.eld.components.ELDListener;
import com.ericlam.mc.eld.components.GroupConfiguration;
import com.ericlam.mc.eld.components.Overridable;
import com.ericlam.mc.eld.configurations.ELDConfigManager;
import com.ericlam.mc.eld.registrations.ComponentsRegistry;
import com.ericlam.mc.eld.registrations.ELDCommandRegistry;
import com.ericlam.mc.eld.registrations.ELDListenerRegistry;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.Set;

public final class ELDServiceCollection implements ServiceCollection {

    final Set<HierarchyNode> commands;

    final Set<Class<? extends Listener>> listeners;

    final Set<Class<? extends ELDListener>> eldListeners;

    final ELDLifeCycle lifeCycleHook;

    final ELDConfigManager configManager;

    private final ELDModule module;

    public ELDServiceCollection(ELDModule module, ELDBukkitPlugin plugin) {
        this.module = module;
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
        if (singleton.isInterface()) throw new IllegalStateException("單例不能為 Interface");
        module.bindSingleton(singleton);
        return this;
    }

    @Override
    public <T, L extends T> ServiceCollection bindService(Class<T> service, Class<L> implementation) {
        if (!service.isInterface()) throw new IllegalStateException("服務類必須為 Interface");
        module.bindService(service, implementation);
        return this;
    }

    @Override
    public <T extends Overridable, L extends T> ServiceCollection overrideService(Class<T> service, Class<L> implementation) {
        if (!service.isInterface()) throw new IllegalStateException("服務類必須為 Interface");
        module.overrideService(service, implementation);
        return this;
    }

    @Override
    public <T, L extends T> ServiceCollection addService(Class<T> service, Class<L> implementation) {
        if (!service.isInterface()) throw new IllegalStateException("服務類必須為 Interface");
        module.addService(service, implementation);
        return this;
    }

    @Override
    public <T> ServiceCollection addServices(Class<T> service, Map<String, Class<? extends T>> implementations) {
        if (!service.isInterface()) throw new IllegalStateException("服務類必須為 Interface");
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

    private ComponentsRegistry toInstanceRegistry(Class<? extends ComponentsRegistry> registry) {
        try {
            var con = registry.getConstructor();
            con.setAccessible(true);
            return con.newInstance();
        } catch (Exception e) {
            if (e instanceof NoSuchMethodException) {
                throw new IllegalStateException("ComponentRegistry 必須擁有無參數構造器(no-args constructor)。");
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
                throw new IllegalStateException("LifeCycle 必須擁有無參數構造器(no-args constructor)。");
            }
            throw new RuntimeException(e);
        }
    }

}
