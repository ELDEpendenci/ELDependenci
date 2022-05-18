package com.ericlam.mc.eld;

import com.ericlam.mc.eld.module.ELDPluginModule;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeePluginModule extends ELDPluginModule<Plugin> {

    @Override
    protected void configure() {
        super.configure();

    }

    @Override
    public String getName(Plugin instance) {
        return instance.getDescription().getName();
    }

}
