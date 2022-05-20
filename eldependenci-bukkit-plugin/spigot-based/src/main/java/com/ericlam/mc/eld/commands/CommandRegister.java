package com.ericlam.mc.eld.commands;

import com.ericlam.mc.eld.HierarchyNode;
import com.ericlam.mc.eld.components.BukkitCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public interface CommandRegister {

    void registerCommand(JavaPlugin plugin, Set<HierarchyNode<BukkitCommand>> commands, CommandProcessor<CommandSender, BukkitCommand> processor);

}
