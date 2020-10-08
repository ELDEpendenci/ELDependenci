package com.ericlam.mc.eld.components;

import org.bukkit.command.CommandSender;

public interface CommandNode<T extends CommandSender> {

    void execute(T sender);

}
