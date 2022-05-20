package com.ericlam.mc.eld;

import com.ericlam.mc.eld.event.PluginDisableEvent;
import com.ericlam.mc.eld.event.PluginEnableEvent;
import com.ericlam.mc.eld.listener.LifeCycleListener;
import com.ericlam.mc.eld.listener.PluginEventListeners;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class BungeeLifeCycleListener extends PluginEventListeners<Plugin> implements Listener {

    public BungeeLifeCycleListener(LifeCycleListener<Plugin> lifeCycleListener) {
        super(lifeCycleListener);
    }


    @EventHandler
    public void onPluginEnable(PluginEnableEvent e){
        if (!(e.getPlugin() instanceof ELDBungeePlugin plugin)) return;
        lifeCycleListener.onPluginEnable(plugin);
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent e){
        if (!(e.getPlugin() instanceof ELDBungeePlugin plugin)) return;
        lifeCycleListener.onPluginDisable(plugin);
    }

}
