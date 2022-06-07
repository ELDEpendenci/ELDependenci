package com.ericlam.mc.eld.registration;

import java.util.Collection;

/**
 * 事件註冊器
 * @param <Listener> 該平台所屬事件註冊類
 */
public interface ListenerRegistry<Listener> {

    /**
     * 註冊事件監聽器
     *
     * @param listener 事件監聽器類
     */
    void listeners(Collection<Class<? extends Listener>> listener);

}
