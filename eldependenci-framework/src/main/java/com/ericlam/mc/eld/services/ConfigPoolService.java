package com.ericlam.mc.eld.services;

import com.ericlam.mc.eld.components.GroupConfiguration;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 文件池服務
 */
public interface ConfigPoolService {


    /**
     * 獲取文件池中的一份文件，若無則從異步加載
     * @param config 文件
     * @param id 文件id
     * @param <C> 映射物件類
     * @return bukkit promise 中的 Optional 文件
     */
    <C extends GroupConfiguration> ScheduleService.BukkitPromise<Optional<C>> getConfigAsync(Class<C> config, String id);


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
