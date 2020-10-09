package com.ericlam.mc.eld;

import com.ericlam.mc.eld.commands.ELDArgumentManager;
import com.ericlam.mc.eld.components.CommandNode;
import com.ericlam.mc.eld.components.Configuration;
import com.ericlam.mc.eld.managers.ArgumentManager;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;
import io.netty.util.internal.ConcurrentSet;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class ELDModule implements Module {

    private final Set<Class<?>> singleton = new ConcurrentSet<>();
    private final Map<Class<?>, Class> services = new ConcurrentHashMap<>();

    final Set<Class<? extends CommandNode>> commands = new HashSet<>();
    private final Map<Class, Configuration> configs = new ConcurrentHashMap<>();

    public final Plugin plugin;

    public ELDModule(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void configure(Binder binder) {
        //services internal bind

        singleton.forEach(cls -> binder.bind(cls).in(Scopes.SINGLETON));
        services.forEach((service, impl) -> binder.bind(service).to(impl).in(Scopes.SINGLETON));
        configs.forEach((cls, config) -> binder.bind(cls).toInstance(config));
    }

    public void bindSingleton(Class<?> singleton) {
        this.singleton.add(singleton);
    }

    public <T, R extends T> void bindService(Class<T> service, Class<R> implement) {
        if (this.services.put(service, implement) != null) {
            plugin.getLogger().warning("Service " + service.getName() + " 先前已有註冊，現已被覆蓋。");
        }
    }

    public <T extends CommandNode> void bindCommand(Class<T> cmd) {
        this.commands.add(cmd);
    }


    public void bindConfig(Class<? extends Configuration> cls, Configuration c) {
        this.configs.put(cls, c);
    }
}
