package com.ericlam.mc.eld.exceptions;

import net.md_5.bungee.api.ChatColor;

/**
 * 用於參數解析失敗時拋出
 */
public class ArgumentParseException extends Exception {

    public ArgumentParseException(String message) {
        super(ChatColor.translateAlternateColorCodes('&', message));
    }
}
