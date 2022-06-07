package com.ericlam.mc.eld;

import com.ericlam.mc.eld.listener.LifeCycleListener;
import com.ericlam.mc.eld.listener.PluginEventListeners;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitLifeCycleListener extends PluginEventListeners<JavaPlugin> implements Listener {

    public BukkitLifeCycleListener(LifeCycleListener<JavaPlugin> lifeCycleListener) {
        super(lifeCycleListener);
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent e){
        if (!(e.getPlugin() instanceof ELDBukkitPlugin plugin)) return;
        lifeCycleListener.onPluginEnable(plugin);
    }


    @EventHandler
    public void onPluginDisable(PluginDisableEvent e){
        if (!(e.getPlugin() instanceof ELDBukkitPlugin plugin)) return;
        lifeCycleListener.onPluginDisable(plugin);
    }

}
