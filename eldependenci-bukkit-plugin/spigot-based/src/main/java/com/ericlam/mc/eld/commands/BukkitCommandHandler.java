package com.ericlam.mc.eld.commands;

import com.ericlam.mc.eld.HierarchyNode;
import com.ericlam.mc.eld.annotations.Commander;
import com.ericlam.mc.eld.components.BukkitCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public final class BukkitCommandHandler implements TabCompleter, CommandExecutor {
    private final Set<HierarchyNode<? extends BukkitCommand>> commandNodes;
    private final CommandProcessor<CommandSender, BukkitCommand> processor;

    public BukkitCommandHandler(Set<HierarchyNode<? extends BukkitCommand>> commandNodes, CommandProcessor<CommandSender, BukkitCommand> processor) {
        this.commandNodes = commandNodes;
        this.processor = processor;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] strings) {
        for (HierarchyNode<? extends BukkitCommand> node : commandNodes) {
            if (processor.labelMatch(node.current.getAnnotation(Commander.class), label)) {
                processor.invokeCommand(commandSender, node, new LinkedList<>(List.of(strings)));
            }
        }
        return true;
    }


    @Override
    public List<String> onTabComplete(@Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] strings) {
        for (HierarchyNode<? extends BukkitCommand> node : commandNodes) {
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


}
