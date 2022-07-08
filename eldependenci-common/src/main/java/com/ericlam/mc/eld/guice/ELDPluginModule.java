package com.ericlam.mc.eld.guice;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ELDPluginModule<Plugin> extends AbstractModule {

    private final Map<String, Plugin> pluginInjectors = new ConcurrentHashMap<>();

    @Override
    protected void configure() {
        var pluginBinding = MapBinder.newMapBinder(binder(), String.class, getPluginClass());
        pluginInjectors.forEach((pluginName, plugin) -> pluginBinding.addBinding(pluginName).toInstance(plugin));
    }

    public void mapPluginInstance(Plugin instance) {
        this.pluginInjectors.put(getName(instance), instance);
    }


    public abstract String getName(Plugin instance);

    public abstract Class<Plugin> getPluginClass();

}
