package com.ericlam.mc.eld.commands;

import com.ericlam.mc.eld.annotations.Commander;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class BungeecordCommand extends Command implements TabExecutor {

    private final BungeeCommandHandler handler;

    public BungeecordCommand(Commander commander, BungeeCommandHandler handler) {
        super(commander.name(), commander.permission(), commander.alias());
        this.handler = handler;
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        this.handler.executeCommand(commandSender, getName(), strings);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender commandSender, String[] strings) {
        return this.handler.executeTabComplete(commandSender, getName(), strings);
    }
}
