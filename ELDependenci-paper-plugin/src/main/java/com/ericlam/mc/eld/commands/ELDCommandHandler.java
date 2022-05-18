package com.ericlam.mc.eld.commands;

import com.ericlam.mc.eld.HierarchyNode;
import com.ericlam.mc.eld.annotations.Commander;
import com.ericlam.mc.eld.annotations.RemainArgs;
import com.ericlam.mc.eld.components.CommandNode;
import com.ericlam.mc.eld.exceptions.ArgumentParseException;
import com.google.inject.Injector;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

public final class ELDCommandHandler implements TabCompleter, CommandExecutor {
    private final Set<HierarchyNode<CommandNode>> commandNodes;
    private final CommandProcessor<CommandSender, CommandNode> processor;

    private ELDCommandHandler(Set<HierarchyNode<CommandNode>> commandNodes, CommandProcessor<CommandSender, CommandNode> processor) {
        this.commandNodes = commandNodes;
        this.processor = processor;
    }


    @Override
    public boolean onCommand(@Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] strings) {
        for (HierarchyNode<CommandNode> node : commandNodes) {
            if (processor.labelMatch(node.current.getAnnotation(Commander.class), label)) {
                processor.invokeCommand(commandSender, node, new LinkedList<>(List.of(strings)));
            }
        }
        return true;
    }



    @Override
    public List<String> onTabComplete(@Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] strings) {
        for (HierarchyNode<CommandNode> node : commandNodes) {
            if (processor.labelMatch(node.current.getAnnotation(Commander.class), label)) {
                var result = processor.invokeTabComplete(commandSender, node, new ArrayList<>(List.of(strings)));
                String lastAug = strings[strings.length - 1];
                if (result != null && !lastAug.equals("")) {
                    result.removeIf(tabItem -> !tabItem.startsWith(lastAug));
                }
                return result;

            }
        }
        return null;
    }

    public static void registers(JavaPlugin plugin, Set<HierarchyNode<CommandNode>> commands, CommandProcessor<CommandSender, CommandNode> processor) {
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


}
