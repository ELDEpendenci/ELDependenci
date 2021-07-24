package com.ericlam.mc.eld.services.factory;

import com.ericlam.mc.eld.bukkit.ItemInteractListener;
import com.ericlam.mc.eld.services.ItemStackService;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class ELDItemStackService implements ItemStackService {

    @Override
    public ItemFactory build(Material material) {
        return new ELDItemFactory(material);
    }

    @Override
    public ItemFactory edit(ItemStack stack) {
        return new ELDItemFactory(stack);
    }

    private static class ELDItemFactory implements ItemFactory{

        private final ItemStack itemStack;

        private ELDItemFactory(ItemStack itemStack) {
            this.itemStack = itemStack;
        }

        private ELDItemFactory(Material material){
            this.itemStack = new ItemStack(material);
        }

        @Override
        public ItemFactory material(Material material) {
            this.itemStack.setType(material);
            return this;
        }

        @Override
        public ItemFactory amount(int amount) {
            this.itemStack.setAmount(amount);
            return this;
        }

        @Override
        public ItemFactory durability(int damage) {
            if (this.itemStack.getItemMeta() instanceof Damageable){
                var meta = (Damageable)this.itemStack.getItemMeta();
                meta.setDamage(damage);
                this.itemStack.setItemMeta((ItemMeta) meta);
            }
            return this;
        }

        @Override
        public ItemFactory display(String display) {
            display = ChatColor.translateAlternateColorCodes('&', display);
            var meta = this.itemStack.getItemMeta();
            meta.setDisplayName(display);
            this.itemStack.setItemMeta(meta);
            return this;
        }

        @Override
        public ItemFactory lore(String... lore) {
            var meta = itemStack.getItemMeta();
            var list = Optional.ofNullable(meta.getLore()).orElseGet(ArrayList::new);
            list.addAll(Arrays.stream(lore).map(s -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList()));
            meta.setLore(list);
            itemStack.setItemMeta(meta);
            return this;
        }

        @Override
        public ItemFactory lore(List<String> lore) {
            var meta = itemStack.getItemMeta();
            meta.setLore(lore.stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList()));
            itemStack.setItemMeta(meta);
            return this;
        }

        @Override
        public ItemFactory lore(Consumer<List<String>> loreEditor) {
            var meta = itemStack.getItemMeta();
            var lore = Optional.ofNullable(meta.getLore()).orElseGet(ArrayList::new);
            loreEditor.accept(lore);
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
            return this;
        }

        @Override
        public ItemFactory enchant(Enchantment enchantment, int level) {
            itemStack.addEnchantment(enchantment, level);
            return this;
        }

        @Override
        public ItemFactory enchant(Map<Enchantment, Integer> enchants) {
            itemStack.addEnchantments(enchants);
            return this;
        }

        @Override
        public ItemFactory enchant(Consumer<Map<Enchantment, Integer>> enchantEditor) {
            var enchants = new LinkedHashMap<>(itemStack.getEnchantments());
            enchantEditor.accept(enchants);
            itemStack.addEnchantments(enchants);
            return this;
        }

        @Override
        public ItemFactory unbreakable(boolean unbreakable) {
            var meta = itemStack.getItemMeta();
            meta.setUnbreakable(unbreakable);
            itemStack.setItemMeta(meta);
            return this;
        }

        @Override
        public ItemFactory modelData(int modelData) {
            var meta = itemStack.getItemMeta();
            meta.setCustomModelData(modelData);
            itemStack.setItemMeta(meta);
            return this;
        }

        @Override
        public ItemFactory itemFlags(ItemFlag... itemFlags) {
            itemStack.addItemFlags(itemFlags);
            return this;
        }

        @Override
        public ItemFactory editItemMeta(Consumer<ItemMeta> meta) {
            var itemMeta = itemStack.getItemMeta();
            meta.accept(itemMeta);
            itemStack.setItemMeta(itemMeta);
            return this;
        }

        @Override
        public ItemFactory editPersisData(Consumer<PersistentDataContainer> dataEditor) {
            var itemMeta = itemStack.getItemMeta();
            dataEditor.accept(itemMeta.getPersistentDataContainer());
            itemStack.setItemMeta(itemMeta);
            return this;
        }

        @Override
        public ItemFactory onInteractEvent(String keyExecute) {
            return this.editPersisData(data -> data.set(ItemInteractListener.INTERACT_EVENT_KEY, PersistentDataType.STRING, keyExecute));
        }

        @Override
        public ItemFactory onInteractEventTemp(Consumer<PlayerInteractEvent> handler) {
            return this.editPersisData(data -> data.set(ItemInteractListener.INTERACT_EVENT_KEY, PersistentDataType.STRING, ItemInteractListener.setInteractKeyTemp(handler)));
        }

        @Override
        public ItemFactory onConsumeEventTemp(Consumer<PlayerItemConsumeEvent> handler) {
            return this.editPersisData(data -> data.set(ItemInteractListener.CONSUME_EVENT_KEY, PersistentDataType.STRING, ItemInteractListener.setConsumeKeyTemp(handler)));
        }

        @Override
        public ItemFactory onConsumeEvent(String keyExecute) {
            return this.editPersisData(data -> data.set(ItemInteractListener.CONSUME_EVENT_KEY, PersistentDataType.STRING, keyExecute));
        }

        @Override
        public ItemFactory destroyInteractEvent() {
            return this.editPersisData(data -> data.remove(ItemInteractListener.INTERACT_EVENT_KEY));
        }

        @Override
        public ItemFactory destroyClickerEvent() {
            return this.editPersisData(data -> data.remove(ItemInteractListener.CONSUME_EVENT_KEY));
        }

        @Override
        public ItemStack getItem() {
            return itemStack;
        }
    }
}
