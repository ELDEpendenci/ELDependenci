package com.ericlam.mc.eld.services;

import com.ericlam.mc.eld.misc.ChainCallable;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * 計時器
 */
public interface ScheduleService {

    /**
     * 注入計時器, 可讓計時器進行依賴注入
     * @param runnable 計時器
     * @return 計時器工廠
     */
    ScheduleFactory injectTask(BukkitRunnable runnable);

    /**
     * 從異步呼叫數值
     * @param <E> 回傳數值
     * @param plugin 插件
     * @param callable 呼叫類
     * @return bukkit promise
     */
    <E> BukkitPromise<E> callAsync(Plugin plugin, Callable<E> callable);

    /***
     * 從異步運行
     * @param plugin 插件
     * @param runnable 異步運行
     * @return bukkit promise
     */
    BukkitPromise<Void> runAsync(Plugin plugin, Runnable runnable);

    /**
     * 等待多個異步的數值呼叫並傳回
     * @param plugin 插件
     * @param promises 異步呼叫
     * @return bukkit promise
     */
    BukkitPromise<Object[]> callAllAsync(Plugin plugin, List<BukkitPromise<Object>> promises);

    /**
     * 等待多個異步完成
     * @param plugin 插件
     * @param promises 異步運行
     * @return bukkit promise
     */
    BukkitPromise<Void> runAllAsync(Plugin plugin, List<BukkitPromise<Void>> promises);


    /**
     * 計時器工廠
     */
    interface ScheduleFactory {

        /**
         * 是否異步
         * @param async 異步
         * @return this
         */
        ScheduleFactory asynchronous(boolean async);

        /**
         * 間隔, 設定後即為 timer
         * @param ticks ticks
         * @return this
         */
        ScheduleFactory interval(long ticks);

        /**
         * 延遲
         * @param ticks ticks
         * @return this
         */
        ScheduleFactory timeout(long ticks);

        /**
         * 運行
         * @param plugin 插件
         * @return this
         */
        BukkitTask run(Plugin plugin);

    }

    /**
     * 鏈式的計時及數值提取器 (bukkit promise)
     * @param <E> 回傳數值
     */
    interface BukkitPromise<E> {

        /**
         * 同步運行並傳遞數值
         * @param function 運行
         * @param <R> 新的回傳數值
         * @return this
         */
        <R> BukkitPromise<R> thenApplySync(ChainCallable<E, R> function);

        /**
         * 同步運行
         * @param function 運行
         * @return 沒有傳遞數值的 bukkit promise
         */
        BukkitPromise<Void> thenRunSync(Consumer<E> function);

        /**
         * 異步運行並傳遞數值
         * @param function 運行
         * @param <R> 新的回傳數值
         * @return this
         */
        <R> BukkitPromise<R> thenApplyAsync(ChainCallable<E, R> function);

        /**
         * 異步運行
         * @param function 運行
         * @return 沒有傳遞數值的 bukkit promise
         */
        BukkitPromise<Void> thenRunAsync(Consumer<E> function);

        /**
         * 啟動
         */
        void join();


        /**
         * 啟動並手動處理錯誤(如有)
         * @param handler 錯誤處理
         */
        void joinWithCatch(Consumer<Throwable> handler);
    }

}
