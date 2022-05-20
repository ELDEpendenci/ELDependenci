package com.ericlam.mc.eld.bukkit;

import com.ericlam.mc.eld.BukkitPlugin;
import com.ericlam.mc.eld.ELDependenci;
import com.ericlam.mc.eld.managers.ItemInteractManager;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class ItemInteractListener implements ItemInteractManager, Listener {

    private final Map<String, Consumer<PlayerInteractEvent>> eventInteractMap = new ConcurrentHashMap<>();
    private final Map<String, Consumer<PlayerItemConsumeEvent>> eventClickerMap = new ConcurrentHashMap<>();

    public final NamespacedKey CONSUME_EVENT_KEY;
    public final NamespacedKey INTERACT_EVENT_KEY;


    public String setInteractKeyTemp(Consumer<PlayerInteractEvent> consumer) {
        var key = "temp:".concat(UUID.randomUUID().toString());
        eventInteractMap.putIfAbsent(key, consumer);
        return key;
    }

    public String setConsumeKeyTemp(Consumer<PlayerItemConsumeEvent> consumer) {
        var key = "temp:".concat(UUID.randomUUID().toString());
        eventClickerMap.putIfAbsent(key, consumer);
        return key;
    }

    private final Plugin plugin;

    public ItemInteractListener(Plugin plugin) {
        this.plugin = plugin;
        CONSUME_EVENT_KEY = new NamespacedKey(plugin, "event.consume.key");
        INTERACT_EVENT_KEY = new NamespacedKey(plugin, "event.interact.key");
    }


    @Override
    public void addInteractEvent(String key, Consumer<PlayerInteractEvent> eventConsumer) {
        if (eventInteractMap.putIfAbsent(key, eventConsumer) != null) {
            plugin.getLogger().warning("interact key " + key + " exist, so can't save");
        }
    }

    public void addConsumeEvent(String key, Consumer<PlayerItemConsumeEvent> eventConsumer) {
        if (eventClickerMap.putIfAbsent(key, eventConsumer) != null) {
            plugin.getLogger().warning("consume key " + key + " exist, so can't save");
        }
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        var item = e.getItem();
        if (item == null) return;
        if (item.getItemMeta() == null) return;
        var data = item.getItemMeta().getPersistentDataContainer();
        var key = data.get(INTERACT_EVENT_KEY, PersistentDataType.STRING);
        if (key == null) return;
        var consumer = eventInteractMap.get(key);
        if (consumer == null) {
            if (!key.startsWith("temp")) {
                plugin.getLogger().warning("interact event's execute key " + key + " not exist, make sure developer has registered!");
                e.getPlayer().sendMessage("interact event's execute key " + key + " not exist, make sure developer has registered!");
            }
            return;
        }
        consumer.accept(e);
    }

    @EventHandler
    public void onPlayerClick(PlayerItemConsumeEvent e) {
        var item = e.getItem();
        if (item.getItemMeta() == null) return;
        var data = item.getItemMeta().getPersistentDataContainer();
        var key = data.get(CONSUME_EVENT_KEY, PersistentDataType.STRING);
        if (key == null) return;
        var consumer = eventClickerMap.get(key);
        if (consumer == null) {
            if (!key.startsWith("temp")) {
                plugin.getLogger().warning("consume event's execute key " + key + " not exist, make sure developer has registered!");
                e.getPlayer().sendMessage("consume event's execute key " + key + " not exist, make sure developer has registered!");
            }
            return;
        }
        consumer.accept(e);
    }
}
