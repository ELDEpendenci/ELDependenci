package com.ericlam.mc.eld.event;

import net.md_5.bungee.api.plugin.Event;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * Bungee 的插件事件
 */
public abstract class PluginEvent extends Event {

    private final Plugin plugin;

    public PluginEvent(Plugin plugin) {
        this.plugin = plugin;
    }

    public Plugin getPlugin() {
        return plugin;
    }
}
