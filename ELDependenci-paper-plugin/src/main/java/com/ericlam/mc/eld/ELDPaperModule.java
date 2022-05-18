package com.ericlam.mc.eld;

import com.ericlam.mc.eld.services.ItemStackService;
import com.ericlam.mc.eld.services.ScheduleService;
import com.ericlam.mc.eld.services.factory.ELDItemStackService;
import com.ericlam.mc.eld.services.scheduler.ELDSchedulerService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.MapBinder;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ELDPaperModule extends AbstractModule {

    private final Map<String, Plugin> pluginInjectors = new ConcurrentHashMap<>();

    @Override
    protected void configure() {

        bind(ItemStackService.class).to(ELDItemStackService.class).in(Scopes.SINGLETON);
        bind(ScheduleService.class).to(ELDSchedulerService.class).in(Scopes.SINGLETON);

        var pluginBinding = MapBinder.newMapBinder(binder(), String.class, Plugin.class);
        pluginInjectors.forEach((pluginName, plugin) -> pluginBinding.addBinding(pluginName).toInstance(plugin));
    }

    void mapPluginInstance(Plugin instance) {
        this.pluginInjectors.put(instance.getName(), instance);
    }

}
