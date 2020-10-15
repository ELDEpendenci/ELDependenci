package com.ericlam.mc.eld.managers;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import java.util.function.Consumer;

/**
 * 物品交互器
 */
public interface ItemInteractManager {

    /**
     * 設置左右鍵事件
     * @param key 標識id
     * @param eventConsumer 事件處理
     */
    void addInteractEvent(String key, Consumer<PlayerInteractEvent> eventConsumer);

    /**
     * 設置進食事件
     * @param key 標識id
     * @param eventConsumer 事件處理
     */
    void addConsumeEvent(String key, Consumer<PlayerItemConsumeEvent> eventConsumer);


}
