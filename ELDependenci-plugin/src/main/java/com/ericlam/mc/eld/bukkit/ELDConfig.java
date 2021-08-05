package com.ericlam.mc.eld.bukkit;

import com.ericlam.mc.eld.annotations.Resource;
import com.ericlam.mc.eld.components.Configuration;

@Resource(locate = "config.yml")
public class ELDConfig extends Configuration {

    public boolean sharePluginInstance = false;
    public boolean defaultSingleton = true;

}
