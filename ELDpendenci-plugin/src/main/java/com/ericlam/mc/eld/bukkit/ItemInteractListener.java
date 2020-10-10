package com.ericlam.mc.eld.bukkit;

import com.ericlam.mc.eld.ELDependenci;
import com.ericlam.mc.eld.managers.ItemInteractManager;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ItemInteractListener implements ItemInteractManager, Listener {

    public static NamespacedKey CLICK_EVENT_KEY = new NamespacedKey(ELDependenci.getPlugin(ELDependenci.class), "event.click.key");
    public static NamespacedKey INTERACT_EVENT_KEY = new NamespacedKey(ELDependenci.getPlugin(ELDependenci.class), "event.interact.key");

    private static final Map<String, Consumer<PlayerInteractEvent>> eventInteractMap = new ConcurrentHashMap<>();
    private static final Map<String, Consumer<InventoryClickEvent>> eventClickerMap = new ConcurrentHashMap<>();


    public static String setInteractKeyTemp(Consumer<PlayerInteractEvent> consumer){
        var key = "temp:".concat(UUID.randomUUID().toString());
        eventInteractMap.putIfAbsent(key, consumer);
        return key;
    }

    public static String setClickerKeyTemp(Consumer<InventoryClickEvent> consumer){
        var key = "temp:".concat(UUID.randomUUID().toString());
        eventClickerMap.putIfAbsent(key, consumer);
        return key;
    }

    private final Plugin plugin;

    public ItemInteractListener(Plugin plugin) {
        this.plugin = plugin;
    }


    @Override
    public void addInteractEvent(String key, Consumer<PlayerInteractEvent> eventConsumer) {
        if (eventInteractMap.putIfAbsent(key, eventConsumer) != null){
            plugin.getLogger().warning("interact key "+key+" 已存在, 因此無法儲存。");
        }
    }

    @Override
    public void addClickEvent(String key, Consumer<InventoryClickEvent> eventConsumer) {
        if (eventClickerMap.putIfAbsent(key, eventConsumer) != null){
            plugin.getLogger().warning("clicker key "+key+" 已存在, 因此無法儲存。");
        }
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e){
        var item = e.getItem();
        if (item == null) return;
        if (item.getItemMeta() == null) return;
        var data = item.getItemMeta().getPersistentDataContainer();
        var key = data.get(INTERACT_EVENT_KEY, PersistentDataType.STRING);
        if (key == null) return;
        var consumer = eventInteractMap.get(key);
        if (consumer == null){
            if (!key.startsWith("temp")){
                plugin.getLogger().warning("左右鍵物品執行事件的 execute key "+key+" 並不存在，請確保插件師已經註冊!");
                e.getPlayer().sendMessage("左右鍵物品執行事件的 execute key "+key+" 並不存在，請確保插件師已經註冊!");
            }
            return;
        }
        consumer.accept(e);
    }

    @EventHandler
    public void onPlayerClick(InventoryClickEvent e){
        var item = e.getCurrentItem();
        if (item == null || e.getSlotType() == InventoryType.SlotType.OUTSIDE) return;
        if (item.getItemMeta() == null) return;
        var data = item.getItemMeta().getPersistentDataContainer();
        var key = data.get(CLICK_EVENT_KEY, PersistentDataType.STRING);
        if (key == null) return;
        var consumer = eventClickerMap.get(key);
        if (consumer == null){
            if (!key.startsWith("temp")){
                plugin.getLogger().warning("左右鍵物品執行事件的 execute key "+key+" 並不存在，請確保插件師已經註冊!");
                e.getWhoClicked().sendMessage("左右鍵物品執行事件的 execute key "+key+" 並不存在，請確保插件師已經註冊!");
            }
            return;
        }
        consumer.accept(e);
    }
}
