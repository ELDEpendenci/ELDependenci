package com.ericlam.mc.eld.registrations;

/**
 * 註冊部件
 */
public interface ComponentsRegistry {

    /**
     * 註冊指令
     * @param registry 指令註冊
     */
    void registerCommand(CommandRegistry registry);

    /**
     * 註冊事件監聽器
     * @param registry 事件監聽器註冊
     */
    void registerListeners(ListenerRegistry registry);



}
