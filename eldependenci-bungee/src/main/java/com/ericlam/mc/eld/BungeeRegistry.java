package com.ericlam.mc.eld;

import com.ericlam.mc.eld.common.CommonRegistry;
import com.ericlam.mc.eld.components.CommandNode;
import net.md_5.bungee.api.plugin.Listener;

/**
 * Bungee 專用 的 組件註冊器
 */
public interface BungeeRegistry extends CommonRegistry<CommandNode, Listener> {
}
