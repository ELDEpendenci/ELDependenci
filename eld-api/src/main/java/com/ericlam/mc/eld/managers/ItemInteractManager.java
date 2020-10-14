package com.ericlam.mc.eld.managers;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import java.util.function.Consumer;

public interface ItemInteractManager {

    void addInteractEvent(String key, Consumer<PlayerInteractEvent> eventConsumer);

    void addConsumeEvent(String key, Consumer<PlayerItemConsumeEvent> eventConsumer);


}
