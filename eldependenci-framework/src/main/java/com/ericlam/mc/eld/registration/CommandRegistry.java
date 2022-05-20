package com.ericlam.mc.eld.registration;

import com.ericlam.mc.eld.common.CommonCommandNode;

import java.util.function.Consumer;

/**
 * 指令註冊
 * @param <T> 該平台所屬的指令類型
 */
public interface CommandRegistry<T extends CommonCommandNode<?>> {

    /**
     * 註冊含分支指令的指令
     *
     * @param node  指令類
     * @param child 分支指令註冊
     */
    void command(Class<T> node, Consumer<CommandRegistry<T>> child);

    /**
     * 註冊指令
     *
     * @param node 指令類
     */
    void command(Class<T> node);

}