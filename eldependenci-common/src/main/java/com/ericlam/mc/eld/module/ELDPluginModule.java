package com.ericlam.mc.eld.module;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ELDPluginModule<Plugin> extends AbstractModule {

    private final Map<String, Plugin> pluginInjectors = new ConcurrentHashMap<>();

    @Override
    protected void configure() {
        var pluginBinding = MapBinder.newMapBinder(binder(), new TypeLiteral<String>(){}, new TypeLiteral<Plugin>(){});
        pluginInjectors.forEach((pluginName, plugin) -> pluginBinding.addBinding(pluginName).toInstance(plugin));
    }

    public void mapPluginInstance(Plugin instance) {
        this.pluginInjectors.put(getName(instance), instance);
    }


    public abstract String getName(Plugin instance);

}
