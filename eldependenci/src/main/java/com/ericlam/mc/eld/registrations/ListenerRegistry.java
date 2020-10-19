package com.ericlam.mc.eld.registrations;

import com.ericlam.mc.eld.components.ELDListener;
import org.bukkit.event.Listener;

import java.util.Collection;

/**
 * 事件監聽器註冊
 */
public interface ListenerRegistry {

    /**
     * 註冊 bukkit 原版事件監聽器
     * @param listener 事件監聽器類
     * @param <T> bukkit 監聽器
     */
    <T extends Listener> void listeners(Collection<Class<T>> listener);


    /**
     * 註冊 ELD 事件監聽器
     * @param listener 事件監聽器類
     * @param <T> ELD 監聽器
     */
    <T extends ELDListener> void ELDListeners(Collection<Class<T>> listener);

}
