package com.ericlam.mc.eld.commands;

import com.ericlam.mc.eld.HierarchyNode;
import com.ericlam.mc.eld.annotations.Commander;
import com.ericlam.mc.eld.components.BungeeCommand;
import net.md_5.bungee.api.CommandSender;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class BungeeCommandHandler {

    private final Set<HierarchyNode<? extends BungeeCommand>> commandNodes;
    private final CommandProcessor<CommandSender, BungeeCommand> processor;

    public BungeeCommandHandler(Set<HierarchyNode<? extends BungeeCommand>> nodes, CommandProcessor<CommandSender, BungeeCommand> processor) {
        this.commandNodes = nodes;
        this.processor = processor;
    }


    public void executeCommand(CommandSender commandSender, String command, String[] strings) {
        for (HierarchyNode<? extends BungeeCommand> node : commandNodes) {
            if (processor.labelMatch(node.current.getAnnotation(Commander.class), command)) {
                processor.invokeCommand(commandSender, node, new LinkedList<>(List.of(strings)));
            }
        }
    }

    public List<String> executeTabComplete(CommandSender sender, String command, String[] strings) {
        for (HierarchyNode<? extends BungeeCommand> node : commandNodes) {
            if (processor.labelMatch(node.current.getAnnotation(Commander.class), command)) {
                var result = processor.invokeTabComplete(sender, node, new LinkedList<>(List.of(strings)));
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
