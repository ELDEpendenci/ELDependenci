package com.ericlam.mc.eld;

import com.ericlam.mc.eld.annotations.Commander;
import com.ericlam.mc.eld.bukkit.CommandNode;
import com.ericlam.mc.eld.commands.BukkitCommandHandler;
import com.ericlam.mc.eld.commands.CommandProcessor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
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
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void registerCommand(JavaPlugin javaPlugin, Set<HierarchyNode<? extends CommandNode>> commands, CommandProcessor<CommandSender, CommandNode> processor) {
        var executor = new BukkitCommandHandler(commands, processor);
        for (HierarchyNode<? extends CommandNode> command : commands) {
            var cmd = command.current.getAnnotation(Commander.class);
            PluginCommand pluginCommand = javaPlugin.getCommand(cmd.name());
            if (pluginCommand == null) {
                getLogger().warning("指令 " + cmd.name() + " 尚未在 plugin.yml 註冊，已略過。");
                continue;
            }
            pluginCommand.setAliases(List.of(cmd.alias()));
            pluginCommand.setDescription(cmd.description());
            pluginCommand.setExecutor(executor);
            pluginCommand.setTabCompleter(executor);
        }
    }
}
