package com.ericlam.mc.eld.services;

import com.ericlam.mc.eld.misc.ChainCallable;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

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
     * @param callable 呼叫類
     * @param plugin 插件
     * @param <E> 回傳數值
     * @return 鏈式計時器
     */
    <E> ChainScheduleService<E> callAsync(Callable<E> callable, Plugin plugin);


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
     * 鏈式計時器
     * @param <E> 回傳數值
     */
    interface ChainScheduleService<E> {

        /**
         * 同步運行
         * @param function 運行
         * @param <R> 新的回傳數值
         * @return this
         */
        <R> ChainScheduleService<R> thenRunSync(ChainCallable<E, R> function);

        /**
         * 異步運行
         * @param function 運行
         * @param <R> 新的回傳數值
         * @return this
         */
        <R> ChainScheduleService<R> thenRunAsync(ChainCallable<E, R> function);

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
