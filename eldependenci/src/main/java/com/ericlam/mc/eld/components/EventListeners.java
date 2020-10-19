package com.ericlam.mc.eld.components;

import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 事件監聽器
 */
public interface EventListeners {

    /**
     * 監聽事件
     * @param eventClass 事件
     * @param <E> bukkit 事件
     * @return 事件訂閱器
     */
    <E extends Event> EventSubscriber<E> listen(Class<E> eventClass);

    /**
     * 事件訂閱器
     * @param <E> bukkit 事件
     */
    interface EventSubscriber<E extends Event> {

        /**
         * 執行多少次後取消執行
         * @param times 次數
         * @return this
         */
        EventSubscriber<E> expireAfter(int times);

        /**
         * 設定事件優先度
         * @param priority 優先度
         * @return this
         */
        EventSubscriber<E> priority(EventPriority priority);

        /**
         * 過濾事件
         * @param filter 過濾
         * @return this
         */
        EventSubscriber<E> filter(Function<E, Boolean> filter);

        /**
         * 建立分支點
         * @return this
         */
        EventFiltered<E> fork();

        /**
         * 處理事件
         * @param handler 處理
         */
        void handle(Consumer<E> handler);

    }

    /**
     * 事件分支點
     * @param <E> bukkit 事件
     */
    interface EventFiltered<E extends Event> {

        /**
         * true 時執行
         * @param handler 執行
         * @return this
         */
        EventFiltered<E> ifTrue(Consumer<E> handler);

        /**
         * false 時執行
         * @param handler 執行
         */
        void ifFalse(Consumer<E> handler);
    }

}


