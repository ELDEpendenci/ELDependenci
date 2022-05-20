package com.ericlam.mc.eld;

import com.ericlam.mc.eld.module.ELDPluginModule;
import com.ericlam.mc.eld.services.ItemStackService;
import com.ericlam.mc.eld.services.ScheduleService;
import com.ericlam.mc.eld.services.factory.ELDItemStackService;
import com.ericlam.mc.eld.services.scheduler.ELDSchedulerService;
import com.google.inject.Scopes;
import org.bukkit.plugin.java.JavaPlugin;

public final class BukkitModule extends ELDPluginModule<JavaPlugin> {

    @Override
    protected void configure() {
        bind(ItemStackService.class).to(ELDItemStackService.class).in(Scopes.SINGLETON);
        bind(ScheduleService.class).to(ELDSchedulerService.class).in(Scopes.SINGLETON);
        super.configure();
    }

    @Override
    public String getName(JavaPlugin instance) {
        return instance.getName();
    }

    @Override
    public Class<JavaPlugin> getPluginClass() {
        return JavaPlugin.class;
    }


}
