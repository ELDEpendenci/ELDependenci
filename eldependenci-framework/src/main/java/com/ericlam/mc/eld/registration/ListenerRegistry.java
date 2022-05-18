package com.ericlam.mc.eld.registration;

import java.util.Collection;

/**
 * 事件監聽器註冊
 */
public interface ListenerRegistry<Listener> {

    /**
     * 註冊事件監聽器
     *
     * @param listener 事件監聽器類
     */
    void listeners(Collection<Class<? extends Listener>> listener);

}
