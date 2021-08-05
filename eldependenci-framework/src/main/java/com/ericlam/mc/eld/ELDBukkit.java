package com.ericlam.mc.eld;

import org.bukkit.plugin.java.JavaPlugin;

public abstract class ELDBukkit extends JavaPlugin {


    /**
     * 綁定服務，單例與文件的地方
     *
     * @param collection 註冊器
     */
    protected abstract void bindServices(ServiceCollection collection);

    @Override
    public final void onEnable() {
        getLogger().info("Enabling plugin "+this.getName());
        getLogger().info("This plugin is hooking the lifecycle of ELDependenci framework.");
    }

    @Override
    public final void onDisable() {
        getLogger().info("Disabling plugin "+this.getName());
    }


}
