package com.ericlam.mc.eld.services;

import com.ericlam.mc.eld.components.GroupConfiguration;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 文件池服務
 */
public interface ConfigPoolService {

    /**
     * 異步獲取文件池。若快取已有，則直接從快取獲取。
     * @param config 文件
     * @param <C> 映射物件類
     * @return 文件池的 bukkit promise
     */
    @Deprecated
    <C extends GroupConfiguration> ScheduleService.BukkitPromise<Map<String, C>> getPoolAsync(Class<C> config);

    /**
     * 獲取文件池中的一份文件，若無則從異步加載
     * @param config 文件
     * @param id 文件id
     * @param <C> 映射物件類
     * @return 文件的 bukkit promise
     */
    <C extends GroupConfiguration> ScheduleService.BukkitPromise<C> getConfigAsync(Class<C> config, String id);

    /**
     * 從快取獲取文件池，若快取並無此文件池，則為 null
     * @param config 文件
     * @param <C> 映射物件類
     * @return 文件池
     */
    @Deprecated
    @Nullable
    <C extends GroupConfiguration> Map<String, C> getPool(Class<C> config);

    /**
     * 獲取文件池中的一份文件，若無則返回 null
     * @param config 文件
     * @param id 文件id
     * @param <C> 映射物件類
     * @return 文件
     */
    @Nullable
    <C extends GroupConfiguration> C getConfig(Class<C> config, String id);

    /**
     * 檢查該文件池已有快取
     * @param config 文件
     * @param <C> 映射物件類
     * @return 是否已有快取
     */
    @Deprecated
    <C extends GroupConfiguration> boolean isPoolCached(Class<C> config);

    /**
     * 檢查該文件已有快取
     * @param config 文件
     * @param id 文件id
     * @param <C> 映射物件類
     * @return 是否已有快取
     */
    <C extends GroupConfiguration> boolean isConfigCached(Class<C> config, String id);

    /**
     * 重載文件池
     * @param config 文件
     * @param <C> 映射物件類
     * @return 異步重載
     */
    <C extends GroupConfiguration> CompletableFuture<Void> reloadPool(Class<C> config);

}
