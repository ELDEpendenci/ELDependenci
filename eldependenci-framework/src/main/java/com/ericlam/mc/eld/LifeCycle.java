package com.ericlam.mc.eld;

/**
 * 生命週期
 */
public interface LifeCycle<Plugin> {

    /**
     * 啟用插件時
     * @param plugin spigot 插件
     */
    void onEnable(Plugin plugin);

    /**
     * 禁用插件時
     * @param plugin spigot 插件
     */
    void onDisable(Plugin plugin);


}
