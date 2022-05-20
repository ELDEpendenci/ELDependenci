package com.ericlam.mc.eld.common;

import java.util.List;

/**
 * 泛平台指令類，用於不同平台作擴展
 * @param <CommandSender> 平台所屬的指令發送者
 */
public interface CommonCommandNode<CommandSender> {

    /**
     * 指令執行
     *
     * @param sender 指令發送者
     */
    void execute(CommandSender sender);

    /**
     * 自動完成
     *
     * @param sender 指令發送者
     * @param args   參數
     * @return 自動完成列表
     */
    default List<String> tabComplete(CommandSender sender, List<String> args) {
        return null;
    }

}
