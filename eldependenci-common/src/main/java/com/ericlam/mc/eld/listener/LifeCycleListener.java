package com.ericlam.mc.eld.listener;

public interface LifeCycleListener<Plugin> {

    void onPluginEnable(Plugin plugin);

    void onPluginDisable(Plugin plugin);

}
