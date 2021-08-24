package com.ericlam.mc.eld.components;

import org.bukkit.command.CommandSender;

/**
 * 用於父指令繼承
 */
public abstract class ParentCommandNode implements CommandNode {

    /**
     * 永遠不會被執行
     * @param sender 指令發送者
     */
    @Override
    public final void execute(CommandSender sender) {
    }

}
