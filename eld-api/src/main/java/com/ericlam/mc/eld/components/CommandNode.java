package com.ericlam.mc.eld.components;

import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * 用於定義指令
 */
public interface CommandNode {

    /**
     * 指令執行
     * @param sender 指令發送者
     */
    void execute(CommandSender sender);

    /**
     * 自動完成
     * @param sender 指令發送者
     * @param args 參數
     * @return 自動完成列表
     */
    default List<String> tabComplete(CommandSender sender, List<String> args) {
        return null;
    }

}
