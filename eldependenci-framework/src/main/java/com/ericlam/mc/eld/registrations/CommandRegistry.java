package com.ericlam.mc.eld.registrations;

import com.ericlam.mc.eld.components.CommandNode;

import java.util.function.Consumer;

/**
 * 指令註冊
 */
public interface CommandRegistry {

    /**
     * 註冊含分支指令的指令
     * @param node 指令類
     * @param child 分支指令註冊
     */
    void command(Class<? extends CommandNode> node, Consumer<CommandRegistry> child);

    /**
     * 註冊指令
     * @param node 指令類
     */
    void command(Class<? extends CommandNode> node);

}
