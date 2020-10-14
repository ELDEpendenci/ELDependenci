package com.ericlam.mc.eld.services;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface ItemStackService {

    ItemFactory build(Material material);

    ItemFactory edit(ItemStack stack);

    interface ItemFactory {

        ItemFactory material(Material material);

        ItemFactory amount(int amount);

        ItemFactory durability(int damage);

        ItemFactory display(String display);

        ItemFactory lore(String... lore);

        ItemFactory lore(List<String> lore);

        ItemFactory lore(Consumer<List<String>> loreEditor);

        ItemFactory enchant(Enchantment enchantment, int level);

        ItemFactory enchant(Map<Enchantment, Integer> enchants);

        ItemFactory enchant(Consumer<Map<Enchantment, Integer>> enchantEditor);

        ItemFactory unbreakable(boolean unbreakable);

        ItemFactory modelData(int modelData);

        ItemFactory itemFlags(ItemFlag... itemFlags);

        ItemFactory editItemMeta(Consumer<ItemMeta> meta);

        ItemFactory editPersisData(Consumer<PersistentDataContainer> dataEditor);

        ItemFactory onInteractEvent(String keyExecute);

        ItemFactory onInteractEventTemp(Consumer<PlayerInteractEvent> handler);

        ItemFactory onConsumeEventTemp(Consumer<PlayerItemConsumeEvent> handler);

        ItemFactory onConsumeEvent(String keyExecute);

        ItemFactory destroyInteractEvent();

        ItemFactory destroyClickerEvent();

        ItemStack getItem();

    }
}
