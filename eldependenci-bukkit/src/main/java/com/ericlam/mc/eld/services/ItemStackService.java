package com.ericlam.mc.eld.services;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 物品編輯器
 */
public interface ItemStackService {

    /**
     * 建立物品
     * @param material 物品類別
     * @return 物品工廠
     */
    ItemFactory build(Material material);

    /**
     * 編輯物品
     * @param stack 物品
     * @return 物品工廠
     */
    ItemFactory edit(ItemStack stack);

    /**
     * 物品工廠
     */
    interface ItemFactory {

        /**
         * 修改類別
         * @param material 類別
         * @return this
         */
        ItemFactory material(Material material);

        /**
         * 修改數量
         * @param amount 數量
         * @return this
         */
        ItemFactory amount(int amount);

        /**
         * 修改耐久度
         * @param damage 耐久度
         * @return this
         */
        ItemFactory durability(int damage);

        /**
         * 顯示名稱
         * @param display 顯示名稱
         * @return this
         */
        ItemFactory display(String display);

        /**
         * 新增敘述
         * @param lore 敘述(行)
         * @return this
         */
        ItemFactory lore(String... lore);

        /**
         * 設置敘述
         * @param lore 敘述
         * @return this
         */
        ItemFactory lore(List<String> lore);

        /**
         * 修改敘述
         * @param loreEditor 修改敘述
         * @return this
         */
        ItemFactory lore(Consumer<List<String>> loreEditor);

        /**
         * 新增附魔
         * @param enchantment 附魔
         * @param level 等級
         * @return this
         */
        ItemFactory enchant(Enchantment enchantment, int level);

        /**
         * 設置附魔
         * @param enchants 附魔表
         * @return this
         */
        ItemFactory enchant(Map<Enchantment, Integer> enchants);

        /**
         * 修改附魔
         * @param enchantEditor 附魔編輯
         * @return this
         */
        ItemFactory enchant(Consumer<Map<Enchantment, Integer>> enchantEditor);

        /**
         * 設置頭顱皮膚, 物品必須為 PLAYER_HEAD
         * @deprecated 不建議使用，因為每次使用都會請求 URL, 建議使用 {@link #head(String)}
         * @param player 玩家
         * @return this
         */
        @Deprecated
        ItemFactory head(OfflinePlayer player);

        /**
         * 設置頭顱皮膚, 物品必須為 PLAYER_HEAD
         * @param skullKey 皮膚ID
         * @return this
         */
        ItemFactory head(String skullKey);

        /**
         * 設置不可破壞
         * @param unbreakable 不可破壞
         * @return this
         */
        ItemFactory unbreakable(boolean unbreakable);

        /**
         * 設置 model data
         * @param modelData model data
         * @return this
         */
        ItemFactory modelData(int modelData);

        /**
         * 設置 item flags
         * @param itemFlags item flags
         * @return this
         */
        ItemFactory itemFlags(ItemFlag... itemFlags);

        /**
         * 編輯物品
         * @param meta 物品meta
         * @return this
         */
        ItemFactory editItemMeta(Consumer<ItemMeta> meta);

        /**
         * 設置物品數據儲存器
         * @param dataEditor 數據儲存編輯
         * @return this
         */
        ItemFactory editPersisData(Consumer<PersistentDataContainer> dataEditor);

        /**
         * 執行左右鍵事件
         * @param keyExecute 標識id
         * @return this
         */
        ItemFactory onInteractEvent(String keyExecute);

        /**
         * 執行臨時的左右鍵事件 (伺服器重啟後消失)
         * @param handler 事件處理
         * @return this
         */
        ItemFactory onInteractEventTemp(Consumer<PlayerInteractEvent> handler);

        /**
         * 執行臨時的進食事件 (伺服器重啟後消失)
         * @param handler 事件處理
         * @return this
         */
        ItemFactory onConsumeEventTemp(Consumer<PlayerItemConsumeEvent> handler);

        /**
         * 執行進食事件
         * @param keyExecute 標識id
         * @return this
         */
        ItemFactory onConsumeEvent(String keyExecute);

        /**
         * 解除左右鍵事件
         * @return this
         */
        ItemFactory destroyInteractEvent();

        /**
         * 解除進食事件
         * @return this
         */
        ItemFactory destroyClickerEvent();

        /**
         * 獲取物品
         * @return 物品
         */
        ItemStack getItem();

    }
}
