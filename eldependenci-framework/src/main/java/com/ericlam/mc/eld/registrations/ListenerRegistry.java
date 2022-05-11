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
     */
    void listeners(Collection<Class<? extends Listener>> listener);


    /**
     * 註冊 ELD 事件監聽器
     * @param listener 事件監聽器類
     */
   void ELDListeners(Collection<Class<? extends ELDListener>> listener);

}
