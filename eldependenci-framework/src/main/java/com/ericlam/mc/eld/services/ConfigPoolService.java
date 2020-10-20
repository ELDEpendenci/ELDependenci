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
    <C extends GroupConfiguration> ScheduleService.BukkitPromise<Map<String, C>> getPoolAsync(Class<C> config);

    /**
     * 從快取獲取文件池，若快取並無此文件池，則為 null
     * @param config 文件
     * @param <C> 映射物件類
     * @return 文件池
     */
    @Nullable
    <C extends GroupConfiguration> Map<String, C> getPool(Class<C> config);

    /**
     * 檢查該文件池已有快取
     * @param config 文件
     * @param <C> 映射物件類
     * @return 是否已有快取
     */
    <C extends GroupConfiguration> boolean isPoolCached(Class<C> config);

    /**
     * 重載文件池
     * @param config 文件
     * @param <C> 映射物件類
     * @return 異步重載
     */
    <C extends GroupConfiguration> CompletableFuture<Void> reloadPool(Class<C> config);

}
