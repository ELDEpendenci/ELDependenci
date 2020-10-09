package com.ericlam.mc.eld.components;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface CommandNode {

    void execute(CommandSender sender);

    default List<String> tabComplete(CommandSender sender, List<String> args) {
        return null;
    }

}
