package com.ericlam.mc.eld;

import com.ericlam.mc.eld.annotations.ELDPlugin;
import com.ericlam.mc.eld.components.Configuration;
import com.ericlam.mc.eld.configurations.ConfigStorage;
import com.ericlam.mc.eld.configurations.ELDConfigManager;
import com.ericlam.mc.eld.registrations.ComponentsRegistry;
import com.ericlam.mc.eld.registrations.ELDCommandRegistry;
import io.netty.util.internal.ConcurrentSet;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ELDServiceCollection implements ServiceCollection {

    final Set<HierarchyNode> commands;

    final ELDConfigManager configManager;

    private final ELDModule module;

    public ELDServiceCollection(ELDModule module, JavaPlugin plugin) {
        this.module = module;
        if (!plugin.getClass().isAnnotationPresent(ELDPlugin.class)) {
            throw new IllegalStateException("插件 " + plugin.getName() + " 缺少 @ELDPlugin 標註");
        }
        var eld = plugin.getClass().getAnnotation(ELDPlugin.class);
        var registry = toRealInstance(eld.registry());
        var cmdregistry = new ELDCommandRegistry(module);
        registry.registerCommand(cmdregistry);
        this.commands = cmdregistry.getNodes();
        this.configManager = new ELDConfigManager(module, plugin);
    }

    @Override
    public ServiceCollection addSingleton(Class<?> singleton) {
        module.bindSingleton(singleton);
        return this;
    }

    @Override
    public <T, L extends T> ServiceCollection addService(Class<T> service, Class<L> implement) {
        module.bindService(service, implement);
        return this;
    }

    @Override
    public <T extends Configuration> ServiceCollection addConfiguration(Class<T> config) {
        configManager.loadConfig(config);
        return this;
    }

    private ComponentsRegistry toRealInstance(Class<? extends ComponentsRegistry> registry){
        try{
            var con = registry.getConstructor();
            con.setAccessible(true);
            return con.newInstance();
        }catch (Exception e){
            if (e instanceof NoSuchMethodException){
                throw new IllegalStateException("ComponentRegistry 必須擁有無參數構造器(no-args constructor)。");
            }
            throw new RuntimeException(e);
        }
    }

}
