package com.ericlam.mc.eld;

import com.ericlam.mc.eld.commands.CommandProcessor;
import com.ericlam.mc.eld.commands.CommandRegister;
import com.ericlam.mc.eld.components.CommandNode;
import com.google.inject.Injector;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.logging.Level;

public class PluginLifeCycleListener implements Listener {

    private boolean disabled = false;

    private final Map<JavaPlugin, BukkitServiceCollection> collectionMap;
    private final Injector injector;

    private final CommandProcessor<CommandSender, CommandNode> commandProcessor;
    private final CommandRegister commandRegister;

    public PluginLifeCycleListener(
            Map<JavaPlugin, BukkitServiceCollection> collectionMap,
            Injector injector,
            CommandProcessor<CommandSender, CommandNode> commandProcessor,
            CommandRegister register
    ) {
        this.collectionMap = collectionMap;
        this.injector = injector;
        this.commandProcessor = commandProcessor;
        this.commandRegister = register;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @EventHandler
    public void onPluginEnable(final PluginEnableEvent e) {
        if (!(e.getPlugin() instanceof ELDBukkitPlugin plugin)) return;
        if (ELDServiceCollection.DISABLED.contains(plugin)) {
            plugin.getLogger().log(Level.SEVERE, "此插件由於註冊不完整，已被禁用。");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        var services = collectionMap.get(plugin);
        if (services == null) return; // not eld plugin

        if (disabled) {
            plugin.getLogger().log(Level.SEVERE, "由於 ELDependenci 無法啟動，此插件已被禁用。");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        var configManager = services.configManager;

        configManager.setInjector(injector);

        injector.injectMembers(services.lifeCycleHook);

        services.lifeCycleHook.onEnable(plugin);

        //register command
        if (!services.commands.isEmpty()) {
            plugin.getLogger().info("Registering all of the commands of plugin " + plugin.getName());
            commandRegister.registerCommand(plugin, services.commands, commandProcessor);
        }

        //register listener
        if (!services.listeners.isEmpty()) {
            plugin.getLogger().info("Registering all of the listeners of plugin " + plugin.getName());
            services.listeners.forEach(listenerCls -> {
                var listener = injector.getInstance(listenerCls);
                plugin.getServer().getPluginManager().registerEvents(listener, plugin);
            });
        }
    }

    @EventHandler
    public void onPluginDisable(final PluginDisableEvent e) {
        if (!(e.getPlugin() instanceof ELDBukkitPlugin plugin)) return;
        var services = collectionMap.get(plugin);
        if (services == null) return; // not eld plugin
        if (disabled || ELDServiceCollection.DISABLED.contains(plugin)) return;
        services.lifeCycleHook.onDisable(plugin);

    }
}
