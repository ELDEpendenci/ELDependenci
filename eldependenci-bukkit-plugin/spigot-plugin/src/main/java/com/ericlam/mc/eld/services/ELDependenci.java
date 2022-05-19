package com.ericlam.mc.eld.services;

import com.ericlam.mc.eld.BukkitPlugin;
import com.ericlam.mc.eld.HierarchyNode;
import com.ericlam.mc.eld.annotations.Commander;
import com.ericlam.mc.eld.commands.*;
import com.ericlam.mc.eld.components.CommandNode;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Set;

public class ELDependenci extends BukkitPlugin {

    private BukkitAudiences audiences;

    @Override
    public void onEnable() {
        super.onEnable();
        audiences = BukkitAudiences.create(this);
    }

    @Override
    public CommandProcessor<CommandSender, CommandNode> getCommandProcessor() {
        return new ELDCommandProcessor<>(injector, argumentManager, eldMessageConfig) {
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
                        audiences.sender(commandSender).sendMessage(component);
                    }

                    @Override
                    public boolean isPlayer() {
                        return commandSender instanceof Player;
                    }
                };
            }
        };
    }

    @Override
    public CommandRegister getCommandRegister() {
        return new CommandRegister() {
            @Override
            public void registerCommand(JavaPlugin plugin, Set<HierarchyNode<CommandNode>> commands, CommandProcessor<CommandSender, CommandNode> processor) {
                var executor = new ELDCommandHandler(commands, processor);
                for (HierarchyNode<CommandNode> command : commands) {
                    var cmd = command.current.getAnnotation(Commander.class);
                    PluginCommand pluginCommand = plugin.getCommand(cmd.name());
                    if (pluginCommand == null){
                        getLogger().warning("指令 " + cmd.name() + " 尚未在 plugin.yml 註冊，已略過。");
                        continue;
                    }
                    pluginCommand.setAliases(List.of(cmd.alias()));
                    pluginCommand.setDescription(cmd.description());
                    pluginCommand.setExecutor(executor);
                    pluginCommand.setTabCompleter(executor);
                }
            }
        };
    }
}
