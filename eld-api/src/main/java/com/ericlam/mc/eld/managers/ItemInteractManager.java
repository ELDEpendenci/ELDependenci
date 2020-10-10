package com.ericlam.mc.eld.managers;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.function.Consumer;

public interface ItemInteractManager {

    void addInteractEvent(String key, Consumer<PlayerInteractEvent> eventConsumer);

    void addClickEvent(String key, Consumer<InventoryClickEvent> eventConsumer);


}
