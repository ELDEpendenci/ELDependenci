package com.ericlam.mc.eld.bukkit;

import org.bukkit.command.CommandSender;

/**
 * 父指令節點, 使用此作爲父節點則無需實作 execute 方法
 */
public interface ParentCommandNode extends CommandNode {

	@Override
	default void execute(CommandSender commandSender) {
		throw new IllegalStateException("This is a parent node, cannot execute.");
	}

}
