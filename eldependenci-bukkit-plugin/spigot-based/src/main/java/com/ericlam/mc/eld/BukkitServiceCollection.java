package com.ericlam.mc.eld;

import com.ericlam.mc.eld.components.CommandNode;
import com.ericlam.mc.eld.registrations.ELDCommandRegistry;
import com.ericlam.mc.eld.registrations.ELDListenerRegistry;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.Set;

public class BukkitServiceCollection extends ELDServiceCollection {

    final Set<HierarchyNode<CommandNode>> commands;

    final Set<Class<? extends Listener>> listeners;

    final BukkitLifeCycle lifeCycleHook;


    public BukkitServiceCollection(ELDCommonModule module, MCPlugin plugin, Map<Class<?>, Object> customInstallation, ConfigHandler handler) {
        super(module, plugin, customInstallation, handler);
        if (!plugin.getClass().isAnnotationPresent(ELDBukkit.class)) {
            ELDServiceCollection.DISABLED.add(plugin);
            throw new IllegalStateException("插件 " + plugin.getName() + " 缺少 @ELDPlugin 標註");
        }
        var eld = plugin.getClass().getAnnotation(ELDBukkit.class);
        var registry = this.toInstance(eld.registry());
        this.lifeCycleHook = this.toInstance(eld.lifeCycle());

        //register command
        var cmdregistry = new ELDCommandRegistry<CommandNode>();
        registry.registerCommand(cmdregistry);
        this.commands = cmdregistry.getNodes();

        //register listeners
        var listenerRegistry = new ELDListenerRegistry<Listener>();
        registry.registerListeners(listenerRegistry);

        this.listeners = listenerRegistry.getListenersCls();
    }


}
