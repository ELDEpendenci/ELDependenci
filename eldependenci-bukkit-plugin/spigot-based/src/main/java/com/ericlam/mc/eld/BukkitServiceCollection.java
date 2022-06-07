package com.ericlam.mc.eld;

import com.ericlam.mc.eld.bukkit.CommandNode;
import com.ericlam.mc.eld.common.CommonRegistry;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class BukkitServiceCollection extends ELDServiceCollection<CommandNode, Listener, JavaPlugin> {


    public BukkitServiceCollection(ELDCommonModule module, MCPlugin plugin, Map<Class<?>, Object> customInstallation, ConfigHandler handler) {
        super(module, plugin, customInstallation, handler);
    }

    @Override
    public Map.Entry<Class<? extends CommonRegistry<CommandNode, Listener>>, Class<? extends LifeCycle<JavaPlugin>>> getComponents(MCPlugin plugin) {
        if (!plugin.getClass().isAnnotationPresent(ELDBukkit.class)) {
            ELDServiceCollection.DISABLED.add(plugin);
            throw new IllegalStateException("插件 " + plugin.getName() + " 缺少 @ELDBukkit 標註");
        }
        var eld = plugin.getClass().getAnnotation(ELDBukkit.class);

        return Map.entry(eld.registry(), eld.lifeCycle());
    }
}
