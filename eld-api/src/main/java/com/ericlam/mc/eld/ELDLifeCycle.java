package com.ericlam.mc.eld;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * 生命週期
 */
public interface ELDLifeCycle {

    /**
     * 啟用插件時
     * @param plugin spigot 插件
     */
    void onEnable(JavaPlugin plugin);

    /**
     * 禁用插件時
     * @param plugin spigot 插件
     */
    void onDisable(JavaPlugin plugin);


}
