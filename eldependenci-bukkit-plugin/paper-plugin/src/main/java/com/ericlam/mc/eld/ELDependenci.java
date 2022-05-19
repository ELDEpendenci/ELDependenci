package com.ericlam.mc.eld;

import com.ericlam.mc.eld.annotations.Commander;
import com.ericlam.mc.eld.commands.*;
import com.ericlam.mc.eld.components.CommandNode;
import com.ericlam.mc.eld.exceptions.ArgumentParseException;
import com.ericlam.mc.eld.misc.ArgParser;
import com.ericlam.mc.eld.services.ArgParserService;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ELDependenci extends BukkitPlugin {
    @Override
    public CommandProcessor<CommandSender, CommandNode> getCommandProcessor() {
        return new ELDCommandProcessor<CommandSender, CommandNode>(injector, argumentManager, eldMessageConfig) {
            @Override
            public CommonCommandSender toSender(CommandSender commandSender) {
                return new CommonCommandSender() {
                    @Override
                    public boolean hasPermission(String permission) {
                        return commandSender.hasPermission(permission);
                    }

                    @Override
                    public void sendMessage(String message) {
                        commandSender.sendMessage(message);
                    }

                    @Override
                    public void sendMessage(String[] messages) {
                        commandSender.sendMessage(messages);
                    }

                    @Override
                    public void sendMessage(TextComponent component) {
                        commandSender.sendMessage(component);
                    }

                    @Override
                    public boolean isPlayer() {
                        return commandSender instanceof Player;
                    }
                };
            }
        };
    }

    // paper支援強制註冊
    @Override
    public CommandRegister getCommandRegister() {
        return new CommandRegister() {
            @Override
            public void registerCommand(JavaPlugin plugin, Set<HierarchyNode<CommandNode>> commands, CommandProcessor<CommandSender, CommandNode> processor) {
                var executor = new ELDCommandHandler(commands, processor);
                commands.forEach(hir -> {
                    var cmd = hir.current.getAnnotation(Commander.class);
                    var pluginCommand = Optional.ofNullable(plugin.getCommand(cmd.name())).orElseGet(() -> {
                        try {
                            var constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
                            constructor.setAccessible(true);
                            var pluginCmd = constructor.newInstance(cmd.name(), plugin);
                            plugin.getServer().getCommandMap().register(plugin.getDescription().getName(), pluginCmd);
                            return pluginCmd;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    });
                    if (pluginCommand == null) {
                        plugin.getLogger().warning("Command " + cmd.name() + " did not register in plugin.yml and force register failed.");
                        return;
                    }
                    pluginCommand.setAliases(List.of(cmd.alias()));
                    pluginCommand.setDescription(cmd.description());
                    pluginCommand.setExecutor(executor);
                    pluginCommand.setTabCompleter(executor);
                });
            }
        };
    }


    @Override
    public void onEnable() {
        super.onEnable();
        argumentManager.registerParser(OfflinePlayer.class, (args, sender, parser) -> {
            var uuid = Bukkit.getPlayerUniqueId(args.next());
            if (uuid == null) {
                throw new ArgumentParseException(eldMessageConfig.getConvertError("player-not-exist"));
            }
            return Bukkit.getOfflinePlayer(uuid);
        });
    }
}
