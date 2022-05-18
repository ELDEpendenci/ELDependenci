package com.ericlam.mc.eld.listener;

public abstract class PluginEventListeners<Plugin> {

    protected final LifeCycleListener<Plugin> lifeCycleListener;

    public PluginEventListeners(LifeCycleListener<Plugin> lifeCycleListener) {
        this.lifeCycleListener = lifeCycleListener;
    }

}
