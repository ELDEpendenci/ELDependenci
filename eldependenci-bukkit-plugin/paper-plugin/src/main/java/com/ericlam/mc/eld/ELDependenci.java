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
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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
}
