package com.ericlam.mc.eld.bukkit;

import com.ericlam.mc.eld.annotations.Resource;
import com.ericlam.mc.eld.components.Configuration;
import com.ericlam.mc.eld.services.ELDConfigPoolService;

@Resource(locate = "config.yml")
public class ELDConfig extends Configuration {

    public boolean sharePluginInstance = false;
    public boolean defaultSingleton = true;
    public ELDConfigPoolService.WalkerWay fileWalker = ELDConfigPoolService.WalkerWay.TREE;

    public boolean debugLogging = false;

}
