package com.ericlam.mc.eld.bungee;

import net.md_5.bungee.api.CommandSender;

/**
 * 父指令節點, 使用此作爲父節點則無需實作 execute 方法
 */
public interface ParentCommandNode extends CommandNode {

	@Override
	default void execute(CommandSender commandSender) {
		throw new IllegalStateException("Parent Command Node cannot be executed.");
	}

}
