package com.ericlam.mc.eld;

import com.ericlam.mc.eld.common.CommonRegistry;
import com.ericlam.mc.eld.components.BukkitCommand;
import org.bukkit.event.Listener;

/**
 * Bukkit 專用的組件註冊器
 */
public interface BukkitRegistry extends CommonRegistry<BukkitCommand, Listener> {
}
