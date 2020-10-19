package com.ericlam.mc.eld.components;

/**
 * 用於定義ELD事件監聽器
 */
public interface ELDListener {

    /**
     * 定義監聽節點
     * @param listeners 事件監聽器
     */
    void defineNodes(EventListeners listeners);

}
