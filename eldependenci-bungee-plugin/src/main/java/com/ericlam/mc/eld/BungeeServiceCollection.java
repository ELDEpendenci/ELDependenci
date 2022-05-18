package com.ericlam.mc.eld;

import com.ericlam.mc.eld.common.CommonRegistry;
import com.ericlam.mc.eld.components.CommandNode;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.Map;

public class BungeeServiceCollection extends ELDServiceCollection<CommandNode, Listener, Plugin> {

    public BungeeServiceCollection(ELDCommonModule module, MCPlugin plugin, Map<Class<?>, Object> customInstallation, ConfigHandler handler) {
        super(module, plugin, customInstallation, handler);
    }

    @Override
    public Map.Entry<Class<? extends CommonRegistry<CommandNode, Listener>>, Class<? extends LifeCycle<Plugin>>> getComponents(MCPlugin plugin) {
        if (!plugin.getClass().isAnnotationPresent(ELDBungee.class)) {
            ELDServiceCollection.DISABLED.add(plugin);
            throw new IllegalStateException("插件 " + plugin.getName() + " 缺少 @ELDBungee 標註");
        }
        var eld = plugin.getClass().getAnnotation(ELDBungee.class);
        return Map.entry(eld.registry(), eld.lifeCycle());
    }
}

