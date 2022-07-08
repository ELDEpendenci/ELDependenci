package com.ericlam.mc.eld;

import com.ericlam.mc.eld.annotations.Commander;
import com.ericlam.mc.eld.bukkit.CommandNode;
import com.ericlam.mc.eld.commands.BukkitCommandHandler;
import com.ericlam.mc.eld.commands.CommandProcessor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.*;

public class ELDependenci extends BukkitPlugin {

    private static ELDependenciAPI api;

    public static ELDependenciAPI getApi() {
        return Optional.ofNullable(api).orElseThrow(() -> new IllegalStateException("ELDependencies has not yet loaded，make sure your plugin.yml has added eld-plugin as depend"));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        api = elDependenciCore;
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void registerCommand(JavaPlugin plugin, Set<HierarchyNode<? extends CommandNode>> commands, CommandProcessor<CommandSender, CommandNode> processor) {
        var executor = new BukkitCommandHandler(commands, processor);
        for (HierarchyNode<? extends CommandNode> command : commands) {
            var cmd = command.current.getAnnotation(Commander.class);
            var pluginCommand = Optional.ofNullable(plugin.getCommand(cmd.name())).orElseGet(() -> {
                try {
                    var constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
                    constructor.setAccessible(true);
                    return constructor.newInstance(cmd.name(), plugin);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            });
            if (pluginCommand == null) {
                plugin.getLogger().warning("Command " + cmd.name() + " did not register in plugin.yml and force register failed.");
                return;
            }
            pluginCommand.setAliases(new ArrayList<>(Arrays.asList(cmd.alias())));
            pluginCommand.setDescription(cmd.description());
            pluginCommand.setExecutor(executor);
            pluginCommand.setTabCompleter(executor);
            try {
                Field activeAlias = Command.class.getDeclaredField("activeAliases");
                activeAlias.setAccessible(true);
                activeAlias.set(pluginCommand, new ArrayList<>(Arrays.asList(cmd.alias())));
                plugin.getLogger().info("activeAliases: " + pluginCommand.getAliases());
            }catch (Exception e){
                e.printStackTrace();
                plugin.getLogger().warning("failed to register aliases: "+e.getMessage());
            }

            try {
                Field cmdMapField = SimplePluginManager.class.getDeclaredField("commandMap");
                cmdMapField.setAccessible(true);
                var cmdMap = (SimpleCommandMap) cmdMapField.get(plugin.getServer().getPluginManager());
                cmdMap.register(plugin.getName().toLowerCase(), pluginCommand);
            }catch (Exception e) {
                e.printStackTrace();
                plugin.getLogger().warning("failed to register command "+cmd.name()+": " + e.getMessage());
            }
        }
    }
}
