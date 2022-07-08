package com.ericlam.mc.eld;

import com.ericlam.mc.eld.annotations.Commander;
import com.ericlam.mc.eld.bukkit.CommandNode;
import com.ericlam.mc.eld.commands.CommandProcessor;
import com.ericlam.mc.eld.commands.ELDArgumentManager;
import com.ericlam.mc.eld.commands.BukkitCommandHandler;
import com.ericlam.mc.eld.exceptions.ArgumentParseException;
import com.ericlam.mc.eld.implement.ELDMessageConfig;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
    protected void registerParser(ELDArgumentManager<CommandSender> argumentManager, ELDMessageConfig eldMessageConfig) {
        super.registerParser(argumentManager, eldMessageConfig);
        argumentManager.registerParser(OfflinePlayer.class, (args, sender, parser) -> {
            var uuid = Bukkit.getPlayerUniqueId(args.next());
            if (uuid == null) {
                throw new ArgumentParseException(eldMessageConfig.getConvertError("player-not-exist"));
            }
            return Bukkit.getOfflinePlayer(uuid);
        });
    }

    @Override
    public void registerCommand(JavaPlugin plugin, Set<HierarchyNode<? extends CommandNode>> commands, CommandProcessor<CommandSender, CommandNode> processor) {
        var executor = new BukkitCommandHandler(commands, processor);
        commands.forEach(hir -> {
            var cmd = hir.current.getAnnotation(Commander.class);
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
            plugin.getServer().getCommandMap().register(plugin.getName().toLowerCase(), pluginCommand);
            // printDebug(pluginCommand, plugin.getLogger());
        });
    }

    private void printDebug(PluginCommand command, Logger logger){
        try {
            logger.info("activeAliases: " + command.getAliases());
            Field aliases = Command.class.getDeclaredField("aliases");
            aliases.setAccessible(true);
            logger.info("aliases: " + aliases.get(command));
            SimpleCommandMap commandMap = (SimpleCommandMap) Bukkit.getServer().getCommandMap();
            logger.info("commands: "+commandMap.getCommands().stream()
                    .filter(cmd -> cmd.getName().equalsIgnoreCase(command.getName()))
                    .map(cmd -> String.format("name: %s\ndescription: %s\naliases: %s", cmd.getName(), cmd.getDescription(), cmd.getAliases()))
                    .collect(Collectors.joining("\n")));

            logger.info("knownCommands: "+
                    commandMap.getKnownCommands().entrySet().stream()
                            .filter(entry -> entry.getKey().equalsIgnoreCase(command.getName()))
                            .map(entry -> String.format("%s => name: %s\ndescription: %s\naliases: %s", entry.getKey(), entry.getValue().getName(), entry.getValue().getDescription(), entry.getValue().getAliases()))
                            .collect(Collectors.joining("\n"))
            );
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
