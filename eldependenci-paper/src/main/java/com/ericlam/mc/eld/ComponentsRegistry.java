package com.ericlam.mc.eld;

import com.ericlam.mc.eld.components.CommandNode;
import com.ericlam.mc.eld.registration.CommandRegistry;
import com.ericlam.mc.eld.registration.ListenerRegistry;
import org.bukkit.event.Listener;

/**
 * 註冊部件
 */
public interface ComponentsRegistry {

    /**
     * 註冊指令
     *
     * @param registry 指令註冊
     */
    void registerCommand(CommandRegistry<CommandNode> registry);

    /**
     * 註冊事件監聽器
     *
     * @param registry 事件監聽器註冊
     */
    void registerListeners(ListenerRegistry<Listener> registry);


}
