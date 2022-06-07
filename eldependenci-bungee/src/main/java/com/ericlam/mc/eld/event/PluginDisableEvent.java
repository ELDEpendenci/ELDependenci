package com.ericlam.mc.eld.event;

import net.md_5.bungee.api.plugin.Plugin;

/**
 * Bungee 的插件關閉事件, 由於本身沒有因此自行實作
 */
public final class PluginDisableEvent extends PluginEvent{

    public PluginDisableEvent(Plugin plugin) {
        super(plugin);
    }

}
