package com.ericlam.mc.eld.services;

import com.ericlam.mc.eld.components.GroupLangConfiguration;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * 語言文件池
 */
public interface LanguagePoolService {

    /**
     * 獲取文件池中的一份語言文件，若無則從異步加載
     *
     * @param config 語言文件
     * @param id     語言文件 locale
     * @param <C>    映射語言物件類
     * @return 語言文件的 bukkit promise
     */
    <C extends GroupLangConfiguration> ScheduleService.BukkitPromise<C> getLangAsync(Class<C> config, String id);

    /**
     * 獲取文件池中的一份語言文件，若無則返回 null
     *
     * @param config 語言文件
     * @param id     語言文件 locale
     * @param <C>    映射語言物件類
     * @return 語言文件
     */
    @Nullable
    <C extends GroupLangConfiguration> C getLang(Class<C> config, String id);

    /**
     * 檢查該語言文件已有快取
     *
     * @param config 語言文件
     * @param id     語言文件 locale
     * @param <C>    映射語言物件類
     * @return 是否已有快取
     */
    <C extends GroupLangConfiguration> boolean isLangCached(Class<C> config, String id);

    /**
     * 重載文件池
     *
     * @param config 文件
     * @param <C>    映射物件類
     * @return 異步重載
     */
    <C extends GroupLangConfiguration> CompletableFuture<Void> reloadPool(Class<C> config);

}